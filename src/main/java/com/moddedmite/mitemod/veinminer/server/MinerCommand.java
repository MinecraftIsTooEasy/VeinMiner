package com.moddedmite.mitemod.veinminer.server;

import com.moddedmite.mitemod.veinminer.configuration.ConfigurationSettings;
import com.moddedmite.mitemod.veinminer.util.BlockID;
import com.moddedmite.mitemod.veinminer.util.PlayerStatus;
import net.minecraft.CommandBase;
import net.minecraft.CommandException;
import net.minecraft.I18n;
import net.minecraft.ICommand;
import net.minecraft.ICommandSender;
import net.minecraft.ServerPlayer;
import net.minecraft.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Server command so clients can control VeinMiner settings.
 */
public class MinerCommand extends CommandBase {
    private MinerServer minerServer;

    public static final int COMMAND_MODE = 0;
    public static final int COMMAND_BLOCKLIST = 1;
    public static final int COMMAND_TOOLLIST = 2;
    public static final int COMMAND_BLOCKLIMIT = 3;
    public static final int COMMAND_RANGE = 4;
    public static final int COMMAND_PER_TICK = 5;
    public static final int COMMAND_SAVE = 6;
    public static final int COMMAND_RELOAD = 7;
    public static final int COMMAND_HELP = 8;
    private static final String[] commands = new String[]{"mode", "blocklist", "toollist", "blocklimit", "radius", "per_tick", "saveconfig", "reloadconfig", "help"};
    private static final String[] modes = new String[]{"auto", "sneak", "no_sneak"};

    public MinerCommand(MinerServer minerServerInstance) {
        minerServer = minerServerInstance;
    }

    @Override
    public String getCommandName() {
        return "veinminer";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return I18n.getString("command.veinminer");
    }

    @Override
    public List getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring) {
        ICustomCommandSender senderPlayer;
        if (icommandsender instanceof ServerPlayer) {
            senderPlayer = new CommandSenderPlayer(minerServer, (ServerPlayer) icommandsender);
        } else if (icommandsender instanceof MinecraftServer) {
            senderPlayer = new CommandSenderServer((MinecraftServer) icommandsender);
        } else {
            throw new CommandException(I18n.getString("command.veinminer.cannotuse"));
        }

        if (astring.length > 0) {
            if (astring[0].equals(commands[COMMAND_MODE])) {
                runCommandMode(senderPlayer, astring);
            } else if (astring[0].equals(commands[COMMAND_BLOCKLIST])) {
                needAdmin(senderPlayer);
                runCommandBlocklist(senderPlayer, astring);
            } else if (astring[0].equals(commands[COMMAND_TOOLLIST])) {
                needAdmin(senderPlayer);
                runCommandToollist(senderPlayer, astring);
            } else if (astring[0].equals(commands[COMMAND_BLOCKLIMIT])) {
                needAdmin(senderPlayer);
                runCommandBlocklimit(senderPlayer, astring);
            } else if (astring[0].equals(commands[COMMAND_RANGE])) {
                needAdmin(senderPlayer);
                runCommandRange(senderPlayer, astring);
            } else if (astring[0].equals(commands[COMMAND_PER_TICK])) {
                needAdmin(senderPlayer);
                runCommandPerTick(senderPlayer, astring);
            } else if (astring[0].equals(commands[COMMAND_SAVE])) {
                needAdmin(senderPlayer);
                runCommandSave(senderPlayer);
            } else if (astring[0].equals(commands[COMMAND_RELOAD])) {
                needAdmin(senderPlayer);
                runCommandReload(senderPlayer);
            } else if (astring[0].equals(commands[COMMAND_HELP])) {
                runCommandHelp(senderPlayer, astring);
            } else {
                showUsageError("command.veinminer");
            }
        } else {
            showUsageError("command.veinminer");
        }
    }

    private void showUsageError(String errorKey) throws WrongUsageException {
        throw new WrongUsageException(I18n.getString(errorKey));
    }

    private void showUsageError(String errorKey, Object... params) throws WrongUsageException {
        throw new WrongUsageException(I18n.getString(errorKey), params);
    }

    private void needAdmin(ICustomCommandSender sender) throws CommandException {
        if (sender instanceof CommandSenderPlayer) {
            CommandSenderPlayer player = (CommandSenderPlayer) sender;
            MinecraftServer server = player.getPlayer().mcServer;
            // MITE's MinecraftServer has no getOpPermissionLevel(); use op level 2 (admin) directly.
            if (server.isDedicatedServer() && !player.getPlayer().canCommandSenderUseCommand(2, "veinminer.admin")) {
                throw new CommandException(I18n.getString("command.veinminer.permissionDenied"));
            }
        }
    }

    private void commandAction(String[] commandString, String commandName) throws WrongUsageException {
        if (commandString.length < 3 || (!"add".equals(commandString[2]) && !"remove".equals(commandString[2]))) {
            showUsageError("command.veinminer." + commandName + ".actionerror", commandString[1]);
        }
    }

    private void runCommandMode(ICustomCommandSender sender, String[] astring) throws CommandException {
        if (sender instanceof CommandSenderPlayer) {
            CommandSenderPlayer senderPlayer = (CommandSenderPlayer) sender;
            UUID player = senderPlayer.getPlayer().getUniqueID();

            if (astring.length == 1) {
                showUsageError("command.veinminer.enable");
            } else if (minerServer.playerHasClient(player)) {
                showUsageError("command.veinminer.hasclient");
            } else if (astring[1].equals(modes[0])) {
                minerServer.setPlayerStatus(player, PlayerStatus.INACTIVE);
                senderPlayer.sendProperChat("command.veinminer.set.auto");
            } else if (astring[1].equals(modes[1])) {
                minerServer.setPlayerStatus(player, PlayerStatus.SNEAK_ACTIVE);
                senderPlayer.sendProperChat("command.veinminer.set.sneak");
            } else if (astring[1].equals(modes[2])) {
                minerServer.setPlayerStatus(player, PlayerStatus.SNEAK_INACTIVE);
                senderPlayer.sendProperChat("command.veinminer.set.nosneak");
            }
        } else if (sender instanceof CommandSenderServer) {
            throw new CommandException(sender.localise("command.veinminer.permissionDenied"));
        }
    }

    private void runCommandBlocklist(ICustomCommandSender senderPlayer, String[] astring) throws WrongUsageException {
        ConfigurationSettings settings = minerServer.getConfigurationSettings();
        Set<String> toolsSet = settings.getToolTypeNames();
        StringBuilder toolsSlashed = new StringBuilder();
        for (String t : toolsSet) {
            if (toolsSlashed.length() > 0) toolsSlashed.append("/");
            toolsSlashed.append(t);
        }

        if (astring.length == 1) {
            showUsageError("command.veinminer.blocklist", toolsSlashed.toString());
        }

        String tool;
        if (toolsSet.contains(astring[1])) {
            tool = astring[1];
        } else {
            showUsageError("command.veinminer.blocklist", toolsSlashed.toString());
            return;
        }

        String toolString = settings.getToolTypeName(tool);
        commandAction(astring, "blocklist");
        String action = astring[2];

        if (astring.length < 4) {
            showUsageError("command.veinminer.blocklist.itemerror", toolString, action);
        }

        int metadata = -1;
        if (astring.length >= 5) {
            try {
                metadata = Integer.parseInt(astring[4]);
            } catch (NumberFormatException ignored) {
            }
        }

        BlockID blockID = new BlockID(astring[3], metadata);
        if (blockID.name.isEmpty()) {
            showUsageError("command.veinminer.blocklist.itemerror", toolString, action);
        }

        if ("add".equals(action)) {
            settings.addBlockToWhitelist(tool, blockID);
            senderPlayer.sendProperChat("command.veinminer.blocklist.add", blockID.name, blockID.metadata, toolString);
        } else if ("remove".equals(action)) {
            settings.removeBlockFromWhitelist(tool, blockID);
            senderPlayer.sendProperChat("command.veinminer.blocklist.remove", blockID.name, blockID.metadata, toolString);
        }
    }

    private void runCommandToollist(ICustomCommandSender senderPlayer, String[] astring) throws WrongUsageException {
        ConfigurationSettings settings = minerServer.getConfigurationSettings();
        Set<String> toolsSet = settings.getToolTypeNames();
        StringBuilder toolsSlashed = new StringBuilder();
        for (String t : toolsSet) {
            if (toolsSlashed.length() > 0) toolsSlashed.append("/");
            toolsSlashed.append(t);
        }

        if (astring.length == 1) {
            showUsageError("command.veinminer.toollist", toolsSlashed.toString());
        }

        String tool;
        if (toolsSet.contains(astring[1])) {
            tool = astring[1];
        } else {
            showUsageError("command.veinminer.toollist", toolsSlashed.toString());
            return;
        }

        String toolString = settings.getToolTypeName(tool);
        commandAction(astring, "toollist");
        String action = astring[2];

        if (astring.length < 4) {
            showUsageError("command.veinminer.toollist.itemerror", toolString, action);
        }

        String toolId = astring[3];
        if (toolId.isEmpty()) {
            showUsageError("command.veinminer.toollist.itemerror", toolString, action);
        }

        if ("add".equals(action)) {
            settings.addTool(tool, toolId);
            senderPlayer.sendProperChat("command.veinminer.toollist.add", toolId, toolString);
        } else if ("remove".equals(action)) {
            settings.removeTool(tool, toolId);
            senderPlayer.sendProperChat("command.veinminer.toollist.remove", toolId, toolString);
        }
    }

    private void runCommandBlocklimit(ICustomCommandSender senderPlayer, String[] astring) throws WrongUsageException {
        if (astring.length == 1) {
            showUsageError("command.veinminer.blocklimit");
        }
        int newBlockLimit = 0;
        try {
            newBlockLimit = Integer.parseInt(astring[1]);
        } catch (NumberFormatException e) {
            showUsageError("command.veinminer.blocklimit");
        }
        minerServer.getConfigurationSettings().setBlockLimit(newBlockLimit);
        int actual = minerServer.getConfigurationSettings().getBlockLimit();
        senderPlayer.sendProperChat("command.veinminer.blocklimit.set", actual);
    }

    private void runCommandRange(ICustomCommandSender senderPlayer, String[] astring) throws WrongUsageException {
        if (astring.length == 1) {
            showUsageError("command.veinminer.range");
        }
        int newRange = 0;
        try {
            newRange = Integer.parseInt(astring[1]);
        } catch (NumberFormatException e) {
            showUsageError("command.veinminer.range");
        }
        minerServer.getConfigurationSettings().setRadiusLimit(newRange);
        int actualRange = minerServer.getConfigurationSettings().getRadiusLimit();
        senderPlayer.sendProperChat("command.veinminer.range.set", actualRange);
    }

    private void runCommandPerTick(ICustomCommandSender senderPlayer, String[] astring) throws WrongUsageException {
        if (astring.length == 1) {
            showUsageError("command.veinminer.pertick");
        }
        int newRate = 0;
        try {
            newRate = Integer.parseInt(astring[1]);
        } catch (NumberFormatException e) {
            showUsageError("command.veinminer.pertick");
        }
        minerServer.getConfigurationSettings().setBlocksPerTick(newRate);
        int actualRate = minerServer.getConfigurationSettings().getBlocksPerTick();
        senderPlayer.sendProperChat("command.veinminer.pertick.set", actualRate);
    }

    private void runCommandSave(ICustomCommandSender senderPlayer) {
        minerServer.getConfigurationSettings().saveConfigs();
        senderPlayer.sendProperChat("command.veinminer.saveconfig");
    }

    private void runCommandReload(ICustomCommandSender senderPlayer) {
        minerServer.getConfigurationSettings().reloadConfigFile();
        senderPlayer.sendProperChat("command.veinminer.loadconfig");
    }

    private void runCommandHelp(ICustomCommandSender senderPlayer, String[] astring) {
        if (astring.length > 1) {
            if (astring[1].equals(commands[COMMAND_MODE])) {
                senderPlayer.sendProperChat("command.veinminer.help.enable1");
                senderPlayer.sendProperChat("command.veinminer.help.enable2");
                senderPlayer.sendProperChat("command.veinminer.help.enable3");
                senderPlayer.sendProperChat("command.veinminer.help.enable4");
            }
        } else {
            senderPlayer.sendProperChat("command.veinminer.help1");
            senderPlayer.sendProperChat("command.veinminer.help2");
            senderPlayer.sendProperChat("command.veinminer.help3");
            senderPlayer.sendProperChat("command.veinminer.help4");
            senderPlayer.sendProperChat("command.veinminer.help5");
            senderPlayer.sendProperChat("command.veinminer.help6");
            senderPlayer.sendProperChat("command.veinminer.help7");
            senderPlayer.sendProperChat("command.veinminer.help8");
            senderPlayer.sendProperChat("command.veinminer.help9");
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return getListOfStringsMatchingLastWord(args, commands);
            case 2:
                if (args[0].equals(commands[COMMAND_MODE])) {
                    return getListOfStringsMatchingLastWord(args, modes);
                } else if (args[0].equals(commands[COMMAND_BLOCKLIST]) || args[0].equals(commands[COMMAND_TOOLLIST])) {
                    Set<String> toolsSet = minerServer.getConfigurationSettings().getToolTypeNames();
                    String[] tools = toolsSet.toArray(new String[]{});
                    Arrays.sort(tools);
                    return getListOfStringsMatchingLastWord(args, tools);
                }
            case 3:
                if (args[0].equals(commands[COMMAND_BLOCKLIST]) || args[0].equals(commands[COMMAND_TOOLLIST])) {
                    return getListOfStringsMatchingLastWord(args, "add", "remove");
                }
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand par1ICommand) {
        return this.getCommandName().compareTo(par1ICommand.getCommandName());
    }
}
