package dev.arubik.realmcraft.market;

import java.util.ArrayList;
import java.util.List;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Managers.Module;
import dev.arubik.realmcraft.Managers.Command.Argument;
import dev.arubik.realmcraft.Managers.Command.RealCommand;

class MarketCommand extends RealCommand implements Module {
    public MarketCommand() {
        super("market", "Economy command", "Command to manage economy placeholder");
    }

    @Override
    public void register() {
        List<Argument> args = new ArrayList<>();

        realmcraft.getInstance().getCommandMapper().register(this);
    }

    @Override
    public void unregister() {
        realmcraft.getInstance().getCommandMapper().unregister(this);
    }

    @Override
    public String configId() {
        return "market";
    }

    @Override
    public String displayName() {
        return "Market Command";
    }
}
