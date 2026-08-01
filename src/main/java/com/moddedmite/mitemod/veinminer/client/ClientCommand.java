package com.moddedmite.mitemod.veinminer.client;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.network.PacketChangeMode;
import com.moddedmite.mitemod.veinminer.util.PreferredMode;
import moddedmite.rustedironcore.network.Network;
import net.minecraft.CommandBase;
import net.minecraft.I18n;
import net.minecraft.ICommand;
import net.minecraft.ICommandSender;
import net.minecraft.ServerPlayer;
import net.minecraft.WrongUsageException;

import java.util.List;

/**
 * Command for changing the client-side preferred mode.
 *
 * <p>In the original Forge mod this was a client-side command registered via
 * {@code ClientCommandHandler}. In MITE all commands are server-side, so this
 * command validates the input and sends a {@link PacketChangeMode} back to the
 * executing player's client, which then applies the new preferred mode.</p>
 */
public class ClientCommand extends CommandBase {
    private static final String[] modes = new String[]{"disabled", "pressed", "released", "sneak", "no_sneak"};

    @Override
    public String getCommandName() {
        return "veinminerc";
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
        return I18n.getString("command.veinminerc");
    }

    @Override
    public List getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) sender;
        if (args.length < 1) {
            throw new WrongUsageException(I18n.getString("command.veinminerc"));
        }

        short newMode;
        if (args[0].equals(modes[0])) {
            newMode = (short) PreferredMode.DISABLED;
        } else if (args[0].equals(modes[1])) {
            newMode = (short) PreferredMode.PRESSED;
        } else if (args[0].equals(modes[2])) {
            newMode = (short) PreferredMode.RELEASED;
        } else if (args[0].equals(modes[3])) {
            newMode = (short) PreferredMode.SNEAK_ACTIVE;
        } else if (args[0].equals(modes[4])) {
            newMode = (short) PreferredMode.SNEAK_INACTIVE;
        } else {
            throw new WrongUsageException(I18n.getString("command.veinminerc"));
        }

        // Send the new mode to the client; the client applies it in PacketChangeMode.apply().
        Network.sendToClient(player, new PacketChangeMode(newMode));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, modes);
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand command) {
        return this.getCommandName().compareTo(command.getCommandName());
    }
}
