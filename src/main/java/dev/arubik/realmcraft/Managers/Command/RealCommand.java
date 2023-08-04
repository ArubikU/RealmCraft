package dev.arubik.realmcraft.Managers.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import lombok.Getter;
import lombok.Setter;

public class RealCommand implements Listener {
    public Command command;
    @Getter
    @Setter
    private String fallbackPrefix = "realmcraft";
    public String permission = null;
    public static String[] selectors = { "@a", "@p", "@r", "@s" };

    private List<Argument> arguments = new ArrayList<Argument>();
    @Setter
    private Function<Void, List<String>> tabCompleter;

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public void setArguments(Set<String> arguments, ArgumentTypes type) {
        for (String arg : arguments) {
            this.arguments.add(new Argument().setArg(arg).setType(type));
        }
    }

    public List<String> getArgumentsList(List<Argument> arguments) {
        List<String> liste = new ArrayList<>();
        for (Argument arg : arguments) {
            if (arg.arg != null) {
                liste.add(arg.arg);
            }
        }
        return liste;
    }

    public List<String> getArgumentsList(List<Argument> arguments, Player p) {
        List<String> liste = new ArrayList<>();
        for (Argument arg : arguments) {
            if (arg.arg != null) {
                switch (arg.type) {
                    case PLAYER: {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            liste.add(player.getName());
                        }
                        break;
                    }
                    case STRING:
                        liste.add(arg.arg);
                        break;
                    case UUID: {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            liste.add(player.getUniqueId().toString());
                        }
                        break;
                    }
                    case LOCATION: {
                        // get block view location
                        Block block = p.getTargetBlock((Set<Material>) null, 100);
                        if (block == null) {
                            liste.add("0_0_0");
                            liste.add("~_~_~");
                            break;
                        }
                        liste.add(block.getX() + "_" + block.getY() + "_" + block.getZ());
                    }
                    default:
                        liste.add(arg.arg);
                        break;
                }
            }
        }
        return liste;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer();
        // RealMessage.sendConsoleMessage(buffer);
        if (!event.isCommand() && !buffer.startsWith("/")) {
            return;
        }
        String[] args = buffer.split(" ");

        String commandLabel = args[0];
        if (commandLabel.startsWith("/")) {
            commandLabel = commandLabel.substring(1);
        }
        // RealMessage.sendConsoleMessage(Arrays.toString(args));
        // RealMessage.sendConsoleMessage(args[0].replaceFirst("/", ""));
        // RealMessage.sendConsoleMessage(this.command.getName());
        if (args[0].replaceFirst("/", "").equalsIgnoreCase(this.command.getName())

                || args[0].replaceFirst("/", "").equalsIgnoreCase(this.fallbackPrefix + ":" + this.command.getName())) {

            if (args.length == 1) {
                // RealMessage.sendConsoleMessage(Arrays.toString(getArgumentsList(arguments).toArray()));
                event.setCompletions(getArgumentsList(arguments));
                if (tabCompleter != null) {
                    event.setCompletions(tabCompleter.apply(null));
                }
            }
            if (args.length == 2) {
                List<Argument> argsList = new ArrayList<>();
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[1])) {
                        if (arg.nexts != null)
                            argsList = arg.nexts;
                        if (!arg.Completitions.isEmpty()) {
                            event.setCompletions(new ArrayList<>(arg.Completitions));
                            return;
                        }
                    }
                }

                // Parse if current args[1] is part from the old arguments
                List<String> oldArgs = getArgumentsList(arguments);
                if (tabCompleter != null) {
                    oldArgs = tabCompleter.apply(null);
                }
                List<String> matchArgs = new ArrayList<>();
                for (String arg : oldArgs) {
                    if (arg.contains(args[1]) && args[1] != "" && args[1] != " " && args[1].length() >= 1
                            && !arg.equalsIgnoreCase(args[1])) {
                        matchArgs.add(arg);
                    }
                }
                if (!matchArgs.isEmpty()) {
                    event.setCompletions(matchArgs);
                    return;
                }

                event.setCompletions(getArgumentsList(argsList));
                // RealMessage.sendConsoleMessage(Arrays.toString(getArgumentsList(argsList).toArray()));
            }
            if (args.length == 3) {
                List<Argument> argsList = new ArrayList<>();
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[1])) {
                        if (arg.nexts == null)
                            return;

                        for (Argument arg2 : arg.nexts) {
                            if (arg2.nexts == null)
                                return;
                            if (arg2.arg.equalsIgnoreCase(args[2])) {
                                argsList = arg2.nexts;
                            }
                            if (arg2.Completitions != null) {

                                event.setCompletions(new ArrayList<>(arg2.Completitions));
                                return;
                            }
                        }

                    }
                }

                // Parse if current args[2] is part from the old arguments
                List<Argument> oldArgs = new ArrayList<>();
                List<String> completionsOld = new ArrayList<>();
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[1])) {
                        if (arg.nexts != null) {
                            oldArgs = arg.nexts;
                        }
                        if (!arg.Completitions.isEmpty()) {
                            completionsOld = new ArrayList<>(arg.Completitions);
                        }
                        break;
                    }
                }
                List<String> matchArgs = new ArrayList<>();

                for (Argument arg : oldArgs) {
                    if (arg.arg.contains(args[2]) && args[2] != "" && args[2] != " " &&
                            args[2].length() >= 1 && !arg.arg.equalsIgnoreCase(args[2])) {
                        matchArgs.add(arg.arg);
                    }
                }
                for (String arg : completionsOld) {
                    if (arg.contains(args[2]) && args[2] != "" && args[2] != " " &&
                            args[2].length() >= 1 && !arg.equalsIgnoreCase(args[2])) {
                        matchArgs.add(arg);
                    }
                }
                if (!matchArgs.isEmpty()) {
                    event.setCompletions(matchArgs);
                    return;
                }

                event.setCompletions(getArgumentsList(argsList));
                // RealMessage.sendConsoleMessage(Arrays.toString(getArgumentsList(argsList).toArray()));
            }
            if (args.length == 4) {
                List<Argument> argsList = new ArrayList<>();
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[1])) {
                        if (arg.nexts == null)
                            return;
                        for (Argument arg2 : arg.nexts) {
                            if (arg2.nexts == null)
                                return;
                            for (Argument arg3 : arg2.nexts) {
                                if (arg3.nexts == null)
                                    return;
                                if (arg3.arg.equalsIgnoreCase(args[3])) {
                                    argsList = arg3.nexts;
                                }
                                if (arg3.Completitions != null) {

                                    event.setCompletions(new ArrayList<>(arg3.Completitions));
                                    return;
                                }
                            }
                        }
                    }
                }
                event.setCompletions(getArgumentsList(argsList));
                // RealMessage.sendConsoleMessage(Arrays.toString(getArgumentsList(argsList).toArray()));
            }

        }
    }

    public RealCommand(String name, String description, String usage) {
        command = new Command(name, description, usage, List.of()) {
            @Override
            public boolean execute(@NotNull CommandSender arg0, @NotNull String arg1, @NotNull String[] args) {
                RealMessage.sendRaw(Arrays.toString(args));
                RealMessage.sendRaw(arg1);
                onCommand(arg0, command, args);
                return true;
            }
        };
    }

    private Function<CommandData, Boolean> mainfunction = new Function<CommandData, Boolean>() {
        @Override
        public Boolean apply(CommandData data) {
            return false;
        }
    };

    public void setMainFunction(Function<CommandData, Boolean> function) {
        mainfunction = function;
    }

    // @EventHandler
    // public void onCommandEvent(PlayerCommandPreprocessEvent event) {
    // String[] args = event.getMessage().split(" ");
    // if (args[0].replaceFirst("/", "").equalsIgnoreCase(this.command.getName())
    // || args[0].replaceFirst("/", "").equalsIgnoreCase(this.fallbackPrefix + ":" +
    // this.command.getName())) {
    // Command command =
    // CommandMapper.getCommands().getOrDefault(args[0].replaceFirst("/", ""),
    // this.command);
    // // create a new array without the first element
    // String[] newArgs = new String[args.length - 1];
    // System.arraycopy(args, 1, newArgs, 0, args.length - 1);
    // onCommand(event.getPlayer(), command, newArgs);
    // }
    // }
    //
    // @EventHandler
    // public void onServerCommand(ServerCommandEvent event) {
    // String[] args = event.getCommand().split(" ");
    // if (args[0].replaceFirst("/", "").equalsIgnoreCase(this.command.getName())
    // || args[0].replaceFirst("/", "").equalsIgnoreCase(this.fallbackPrefix + ":" +
    // this.command.getName())) {
    // Command command = CommandMapper.getCommands().getOrDefault(args[0],
    // this.command);
    // // create a new array without the first element
    // String[] newArgs = new String[args.length - 1];
    // System.arraycopy(args, 1, newArgs, 0, args.length - 1);
    // onCommand(event.getSender(), command, newArgs);
    // }
    // }

    public void onCommand(CommandSender sender, Command command, String[] args) {
        CommandData data = new CommandData(this, sender, args, command);
        RealMessage.sendConsoleMessage(command.getLabel());
        if (command.getName().equalsIgnoreCase(this.command.getName())) {
            int size = List.of(args).size();
            if (size == 0) {
                mainfunction.apply(data);
            } else if (size == 1) {
                mainfunction.apply(data);
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[0])) {
                        arg.function.apply(data);
                    }
                }
            } else if (size == 2) {
                mainfunction.apply(data);
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[0])) {
                        arg.function.apply(data);
                        if (arg.next != null) {
                            if (arg.next.arg.equalsIgnoreCase(args[1])) {
                                arg.next.function.apply(data);
                            }
                        }
                        if (arg.nexts != null) {
                            for (Argument arg2 : arg.nexts) {
                                if (arg2.arg.equalsIgnoreCase(args[1])) {
                                    arg2.function.apply(data);
                                }
                            }
                        }
                    }
                }
            } else if (size == 3) {
                mainfunction.apply(data);
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[0])) {
                        arg.function.apply(data);
                        if (arg.next != null) {
                            if (arg.next.arg.equalsIgnoreCase(args[1])) {
                                if (arg.next.next != null) {
                                    if (arg.next.next.arg.equalsIgnoreCase(args[2])) {
                                        arg.next.next.function.apply(data);
                                    }
                                }
                                if (arg.next.nexts != null) {
                                    for (Argument arg2 : arg.next.nexts) {
                                        if (arg2.arg.equalsIgnoreCase(args[2])) {
                                            arg2.function.apply(data);
                                        }
                                    }
                                }
                            }
                        }
                        if (arg.nexts != null) {
                            for (Argument arg2 : arg.nexts) {
                                if (arg2.arg.equalsIgnoreCase(args[1])) {
                                    if (arg2.next != null) {
                                        if (arg2.next.arg.equalsIgnoreCase(args[2])) {
                                            arg2.next.function.apply(data);
                                        }
                                    }
                                    if (arg2.nexts != null) {
                                        for (Argument arg3 : arg2.nexts) {
                                            if (arg3.arg.equalsIgnoreCase(args[2])) {
                                                arg3.function.apply(data);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (size == 4) {
                mainfunction.apply(data);
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[0])) {
                        arg.function.apply(data);
                        if (arg.next != null) {
                            if (arg.next.arg.equalsIgnoreCase(args[1])) {
                                arg.next.function.apply(data);
                                if (arg.next.next != null) {
                                    if (arg.next.next.arg.equalsIgnoreCase(args[2])) {
                                        arg.next.next.function.apply(data);
                                        if (arg.next.next.next != null) {
                                            if (arg.next.next.next.arg.equalsIgnoreCase(args[3])) {
                                                arg.next.next.next.function.apply(data);
                                            }
                                        }
                                        if (arg.next.next.nexts != null) {
                                            for (Argument arg2 : arg.next.next.nexts) {
                                                if (arg2.arg.equalsIgnoreCase(args[3])) {
                                                    arg2.function.apply(data);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (arg.next.nexts != null) {
                                    for (Argument arg2 : arg.next.nexts) {
                                        if (arg2.arg.equalsIgnoreCase(args[2])) {
                                            if (arg2.next != null
                                                    && arg2.next.arg.equalsIgnoreCase(args[3])) {
                                                arg2.next.function.apply(data);
                                            }
                                            if (arg2.nexts != null) {
                                                for (Argument arg3 : arg2.nexts) {
                                                    if (arg3.arg.equalsIgnoreCase(args[3])) {
                                                        arg3.function.apply(data);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (size == 5) {
                mainfunction.apply(data);
                for (Argument arg : arguments) {
                    if (arg.arg.equalsIgnoreCase(args[0])) {
                        arg.function.apply(data);
                        if (arg.next != null) {
                            if (arg.next.arg.equalsIgnoreCase(args[1])) {
                                arg.next.function.apply(data);
                                if (arg.next.next != null) {
                                    if (arg.next.next.arg.equalsIgnoreCase(args[2])) {
                                        arg.next.next.function.apply(data);
                                        if (arg.next.next.next != null) {
                                            if (arg.next.next.next.arg.equalsIgnoreCase(args[3])) {
                                                arg.next.next.next.function.apply(data);
                                                if (arg.next.next.next.next != null) {
                                                    if (arg.next.next.next.next.arg.equalsIgnoreCase(args[4])) {
                                                        arg.next.next.next.next.function
                                                                .apply(data);
                                                    }
                                                }
                                                if (arg.next.next.next.nexts != null) {
                                                    for (Argument arg2 : arg.next.next.next.nexts) {
                                                        if (arg2.arg.equalsIgnoreCase(args[4])) {
                                                            arg2.function.apply(data);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (arg.next.next.nexts != null) {
                                            for (Argument arg2 : arg.next.next.nexts) {
                                                if (arg2.arg.equalsIgnoreCase(args[3])) {
                                                    if (arg2.next != null
                                                            && arg2.next.arg.equalsIgnoreCase(args[4])) {
                                                        arg2.next.function.apply(data);
                                                    }
                                                    if (arg2.nexts != null) {
                                                        for (Argument arg3 : arg2.nexts) {
                                                            if (arg3.arg.equalsIgnoreCase(args[4])) {
                                                                arg3.function.apply(data);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (arg.next.nexts != null) {
                                    for (Argument arg2 : arg.next.nexts) {
                                        if (arg2.arg.equalsIgnoreCase(args[2])) {
                                            if (arg2.next != null) {
                                                if (arg2.next.arg.equalsIgnoreCase(args[3])) {
                                                    if (arg2.next.next != null) {
                                                        if (arg2.next.next.arg.equalsIgnoreCase(args[4])) {
                                                            arg2.next.next.function
                                                                    .apply(data);
                                                        }
                                                    }
                                                    if (arg2.next.nexts != null) {
                                                        for (Argument arg3 : arg2.next.nexts) {
                                                            if (arg3.arg.equalsIgnoreCase(args[4])) {
                                                                arg3.function.apply(data);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }

        }
    }

}
