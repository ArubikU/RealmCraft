package dev.arubik.realmcraft.Managers.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

public class Argument {
    public String arg;
    public String permission = null;
    public Argument next = null;
    @Getter
    public List<Argument> nexts = new ArrayList<>();
    @Getter
    public Set<String> Completitions = Set.of();
    public ArgumentTypes type = ArgumentTypes.STRING;
    public Function<CommandData, Boolean> function = (data) -> {
        return true;
    };

    public Argument setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public Argument setArg(String arg) {
        this.arg = arg;
        return this;
    }

    public Argument setNext(Argument next) {
        this.next = next;
        return this;
    }

    public Argument setNexts(List<Argument> nexts) {
        this.nexts = nexts;
        return this;
    }

    public Argument addNext(Argument next) {
        if (this.nexts == null) {
            this.nexts = List.of(next);
        } else {
            this.nexts.add(next);
        }
        return this;
    }

    public Argument setType(ArgumentTypes type) {
        this.type = type;
        return this;
    }

    public Argument setFunction(Function<CommandData, Boolean> function) {
        this.function = function;
        return this;
    }

    public Boolean execute(CommandData data) {
        return function.apply(data);
    }

    public Argument(String arg, Argument next, @Nullable ArgumentTypes type, @Nullable List<Argument> nexts) {
        this.arg = arg;
        this.next = next;
        this.nexts = nexts;
        this.type = type;
    }

    public Argument setCompletitions(Set<String> completitions) {
        Completitions = completitions;
        return this;
    }

    public Argument() {

    }

    public Argument getNext() {
        return next;
    }

    public static String[] selectors = { "@a", "@p", "@r", "@s" };

    public List<String> parseArg(@Nullable CommandSender player) {
        if (arg == null) {
            return List.of();
        }
        List<String> list = List.of(arg);
        switch (type) {
            case STRING:
                return list;
            case PLAYER:
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    list.add(p.getName());
                }
                list.addAll(List.of(selectors));
                return list;
            case UUID:
                if (player instanceof Player) {
                    Player a = (Player) player;
                    list.add(a.getUniqueId().toString());
                }
                list.addAll(List.of(selectors));
                return list;
            case LOCATION:
                if (player instanceof Player) {
                    Player a = (Player) player;
                    list.add(a.getLocation().toString());
                }
                return list;
            default:
                return list;
        }
    }
}
