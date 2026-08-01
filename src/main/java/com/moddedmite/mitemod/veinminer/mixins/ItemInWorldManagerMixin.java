package com.moddedmite.mitemod.veinminer.mixins;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.api.Point;
import com.moddedmite.mitemod.veinminer.api.VeinminerInitalToolCheck;
import com.moddedmite.mitemod.veinminer.configuration.ConfigurationSettings;
import com.moddedmite.mitemod.veinminer.core.MinerInstance;
import com.moddedmite.mitemod.veinminer.server.MinerServer;
import com.moddedmite.mitemod.veinminer.util.BlockID;
import com.moddedmite.mitemod.veinminer.util.PlayerStatus;
import net.minecraft.Block;
import net.minecraft.ItemInWorldManager;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts block harvesting to trigger vein mining. Replaces Forge's
 * BlockEvent.BreakEvent used in the original mod.
 */
@Mixin(ItemInWorldManager.class)
public abstract class ItemInWorldManagerMixin {

    @Shadow
    public World theWorld;

    @Shadow
    public ServerPlayer thisPlayerMP;

    @Inject(method = "tryHarvestBlock", at = @At("HEAD"), cancellable = true)
    private void veinminer$onTryHarvestBlock(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (this.theWorld.isRemote) {
            return;
        }
        MinerServer server = VeinMiner.instance.minerServer;
        if (server == null) {
            return;
        }

        // Re-entrancy guard: if the player already has an active vein-mining
        // instance, this call originates from MinerInstance.mineBlock and must
        // proceed as a normal harvest.
        if (server.getInstance(this.thisPlayerMP) != null) {
            return;
        }

        Point breakPoint = new Point(x, y, z);
        if (server.pointIsBlacklisted(breakPoint)) {
            return;
        }

        // Only trigger when the player is vein-mining active.
        PlayerStatus status = server.getPlayerStatus(this.thisPlayerMP.getUniqueID());
        if (status == null || status == PlayerStatus.INACTIVE) {
            return;
        }
        if (status == PlayerStatus.SNEAK_ACTIVE && !this.thisPlayerMP.isSneaking()) {
            return;
        }
        if (status == PlayerStatus.SNEAK_INACTIVE && this.thisPlayerMP.isSneaking()) {
            return;
        }

        Block block = this.theWorld.getBlock(x, y, z);
        if (block == null) {
            return;
        }
        int meta = this.theWorld.getBlockMetadata(x, y, z);
        BlockID blockID = new BlockID(block.blockID, meta);

        ConfigurationSettings settings = server.getConfigurationSettings();
        int radiusLimit = settings.getRadiusLimit();
        int blockLimit = settings.getBlockLimit();

        VeinminerInitalToolCheck startConfig = new VeinminerInitalToolCheck(this.thisPlayerMP, breakPoint, radiusLimit, blockLimit, radiusLimit, blockLimit);
        if (!startConfig.allowVeinminerStart.isAllowed()) {
            return;
        }

        MinerInstance instance = new MinerInstance(this.theWorld, this.thisPlayerMP, breakPoint, blockID, server, radiusLimit, blockLimit);
        int result = instance.mineBlock(breakPoint);
        if (result > 0) {
            // Vein mining took over the first block; cancel the original harvest.
            cir.setReturnValue(false);
        }
    }
}
