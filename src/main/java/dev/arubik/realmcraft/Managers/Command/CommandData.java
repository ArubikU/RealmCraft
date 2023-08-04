package dev.arubik.realmcraft.Managers.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.georgev22.skinoverlay.gson.JsonElement;

import dev.arubik.realmcraft.Handlers.JsonBuilder;

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

    public String toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.append("command", command.command.getName());
        builder.append("sender", sender.getName());
        builder.append("args", args);
        return builder.toString();
    }
}
