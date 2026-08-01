package com.moddedmite.mitemod.veinminer.mixins;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.api.Point;
import com.moddedmite.mitemod.veinminer.core.MinerInstance;
import com.moddedmite.mitemod.veinminer.server.MinerServer;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.ItemStack;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts entity spawning to capture drops produced during vein mining.
 * Replaces Forge's EntityJoinWorldEvent used by the original EntityDropHook.
 *
 * <p>When a block is harvested by {@code MinerInstance.mineBlock}, the vanilla
 * code path spawns an {@link EntityItem} for each drop. This mixin catches
 * those spawns, routes the items into the active {@link MinerInstance} drop
 * collection, and cancels the normal spawn so the items can be re-spawned
 * consolidated at the initial break point.</p>
 */
@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(method = "spawnEntityInWorld", at = @At("HEAD"), cancellable = true)
    private void veinminer$onSpawnEntityInWorld(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (((World) (Object) this).isRemote) {
            return;
        }
        if (!(entity instanceof EntityItem)) {
            return;
        }
        MinerServer minerServer = VeinMiner.instance.minerServer;
        if (minerServer == null) {
            return;
        }

        EntityItem entityItem = (EntityItem) entity;
        ItemStack itemStack = entityItem.getEntityItem();
        if (itemStack == null) {
            return;
        }
        // Skip items that carry extra NBT data (special drops should not be consolidated).
        if (itemStack.hasTagCompound()) {
            return;
        }

        int eX = (int) Math.floor(entity.posX);
        int eY = (int) Math.floor(entity.posY);
        int eZ = (int) Math.floor(entity.posZ);
        Point p = new Point(eX, eY, eZ);

        if (!minerServer.awaitingDrop(p)) {
            return;
        }

        // Only intercept drops that originate from MinerInstance.mineBlock to avoid
        // stealing normal entity spawns that happen to land on a pending position.
        if (!calledFromMinerInstance()) {
            return;
        }

        minerServer.addEntity(entityItem);
        cir.setReturnValue(false);
    }

    private static boolean calledFromMinerInstance() {
        StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (MinerInstance.class.getName().equals(element.getClassName())
                    && "mineBlock".equals(element.getMethodName())) {
                return true;
            }
        }
        return false;
    }
}
