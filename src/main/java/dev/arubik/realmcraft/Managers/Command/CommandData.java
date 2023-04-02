package dev.arubik.realmcraft.Managers.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandData {
    CommandSender sender;
    String[] args;
    RealCommand command;
    Command commandClass;

    public CommandData(RealCommand command, CommandSender sender, String[] args, Command commandClass) {
        this.command = command;
        this.sender = sender;
        this.args = args;
        this.commandClass = commandClass;
    }
}
