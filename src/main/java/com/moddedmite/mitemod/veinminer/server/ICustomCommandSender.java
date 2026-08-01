package com.moddedmite.mitemod.veinminer.server;

public interface ICustomCommandSender {
    void sendProperChat(String translationString, Object... params);
    boolean canRunConfigs();
    String localise(String input);
}
