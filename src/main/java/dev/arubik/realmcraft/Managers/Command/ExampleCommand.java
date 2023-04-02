package dev.arubik.realmcraft.Managers.Command;

import java.util.ArrayList;
import java.util.List;

import dev.arubik.realmcraft.realmcraft;

public class ExampleCommand {
    private static RealCommand command;

    public static void Initialize() {
        command = new RealCommand("real-example3", "example command", "to test some commands");

        List<Argument> args = new ArrayList<>();

        Argument arg1 = new Argument();
        arg1.setArg("test")
                .setNext(null)
                .setType(ArgumentTypes.STRING)
                .setFunction((data) -> {
                    data.sender.sendMessage("test");
                    return true;
                });
        List<Argument> nextArgs = new ArrayList<>();
        nextArgs.add(new Argument()
                .setArg("null")
                .setNext(null)
                .setType(ArgumentTypes.STRING)
                .setFunction((data) -> {
                    data.sender.sendMessage("nullable");
                    return true;
                }));
        arg1.setNexts(nextArgs);
        args.add(arg1);
        command.setArguments(args);
        command.setMainFunction((data) -> {
            data.sender.sendMessage("null");
            return true;
        });

        realmcraft.getInstance().getCommandMapper().register(command);
    }

    public static void Uninitialize() {
        realmcraft.getInstance().getCommandMapper().unregister(command);
    }
}
