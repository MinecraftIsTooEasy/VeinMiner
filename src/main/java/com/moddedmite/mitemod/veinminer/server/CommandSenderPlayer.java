package com.moddedmite.mitemod.veinminer.server;

import com.moddedmite.mitemod.veinminer.util.PreferredMode;
import net.minecraft.ChatMessageComponent;
import net.minecraft.I18n;
import net.minecraft.ICommandSender;
import net.minecraft.ServerPlayer;

/**
 * Allow MinerCommand to work with Players.
 */
public class CommandSenderPlayer implements ICustomCommandSender {
    private MinerServer minerServer;
    private ServerPlayer player;

    public CommandSenderPlayer(MinerServer minerServerInstance, ServerPlayer player) {
        this.minerServer = minerServerInstance;
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void sendProperChat(String incomingMessage, Object... params) {
        ChatMessageComponent message;
        if (minerServer.playerHasClient(player.getUniqueID())) {
            message = ChatMessageComponent.createFromTranslationWithSubstitutions(incomingMessage, params);
        } else {
            String rawMessage = I18n.getString(incomingMessage);
            message = ChatMessageComponent.createFromText(String.format(rawMessage, params));
        }
        player.sendChatToPlayer(message);
    }

    @Override
    public boolean canRunConfigs() {
        return !player.mcServer.isDedicatedServer() || player.canCommandSenderUseCommand(0, "veinminer");
    }

    @Override
    public String localise(String input) {
        if (!minerServer.playerHasClient(player.getUniqueID())) {
            return I18n.getString(input);
        }
        return input;
    }
}
