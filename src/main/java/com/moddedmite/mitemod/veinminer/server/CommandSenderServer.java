package com.moddedmite.mitemod.veinminer.server;

import net.minecraft.ChatMessageComponent;
import net.minecraft.I18n;
import net.minecraft.server.MinecraftServer;

/**
 * Allow MinerCommand to work with server consoles.
 */
public class CommandSenderServer implements ICustomCommandSender {
    private MinecraftServer server;

    public CommandSenderServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void sendProperChat(String incomingMessage, Object... params) {
        String rawMessage = I18n.getString(incomingMessage);
        ChatMessageComponent message = ChatMessageComponent.createFromText(String.format(rawMessage, params));
        server.sendChatToPlayer(message);
    }

    @Override
    public boolean canRunConfigs() {
        return true;
    }

    @Override
    public String localise(String input) {
        return I18n.getString(input);
    }
}
