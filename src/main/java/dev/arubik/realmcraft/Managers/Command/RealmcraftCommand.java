package dev.arubik.realmcraft.Managers.Command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealStack;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.EmoteHandler.EmoteMain;
import dev.arubik.realmcraft.Handlers.Overlay;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.LootGen.ContainerApi;
import dev.arubik.realmcraft.LootGen.ContainerInstance;
import dev.arubik.realmcraft.LootGen.LootTable;
import xyz.larkyy.aquaticmodelengine.api.model.spawned.player.PlayerModel;

public class RealmcraftCommand {
    private static RealCommand command;
    private static RealCommand emoteCommand;

    public static void Initialize() {
        command = new RealCommand("realmcraft", "Main command", "Main command");
        if (realmcraft.getInteractiveConfig().getBoolean("modules.emotes", true)) {
            emoteCommand = new RealCommand("emote", "Emote command", "Emote command");
        }
        List<Argument> args = new ArrayList<>();

        Argument reloadArg = new Argument();
        reloadArg.setArg("reload")
                .setNext(null)
                .setType(ArgumentTypes.STRING)
                .setFunction((data) -> {
                    return true;
                });

        reloadArg.addNext(new Argument().setArg("config")
                .setNext(null)
                .setType(ArgumentTypes.STRING)
                .setFunction((data) -> {
                    if (Utils.checkPermission(data.sender, "reload.config")) {
                        realmcraft.getInstance().reloadConfig();
                        RealMessage.sendMessage(data.sender,
                                "{path=command.prefix;file=lang.yml} {path=command.reload;file=lang.yml}");
                    }
                    return true;
                }));
        if (realmcraft.getInteractiveConfig().getBoolean("modules.ireplacer", true)) {
            reloadArg.addNext(new Argument().setArg("ireplacer").setType(ArgumentTypes.STRING).setFunction((data) -> {
                if (Utils.checkPermission(data.sender, "reload.ireplacer")) {
                    realmcraft.getInstance().getReplacer().setup();
                    RealMessage.sendMessage(data.sender,
                            "{path=command.prefix;file=lang.yml} {path=command.reload-replacer;file=lang.yml}");
                }
                return true;
            }));
        }
        if (realmcraft.getInteractiveConfig().getBoolean("modules.emotes", true)) {
            reloadArg.addNext(new Argument().setArg("emote").setType(ArgumentTypes.STRING).setFunction((data) -> {
                if (Utils.checkPermission(data.sender, "reload.emote")) {
                    EmoteMain.LoadModelTemplates();
                    RealMessage.sendMessage(data.sender,
                            "{path=command.prefix;file=lang.yml} {path=command.reload-emote;file=lang.yml}");
                }
                return true;
            }));
        }
        if (realmcraft.getInteractiveConfig().getBoolean("modules.loot", true)) {
            reloadArg.addNext(new Argument().setArg("loot").setType(ArgumentTypes.STRING).setFunction((data) -> {
                if (Utils.checkPermission(data.sender, "reload.loot")) {
                    LootTable.reload();
                    RealMessage.sendMessage(data.sender,
                            "{path=command.prefix;file=lang.yml} {path=command.reload-loot;file=lang.yml}");
                }
                return true;
            }));
        }
        args.add(reloadArg);
        Function<CommandData, Boolean> functionA = new Function<CommandData, Boolean>() {

            @Override
            public Boolean apply(CommandData data) {
                RealMessage.sendConsoleMessage(data.commandClass.getName() + " " + Arrays.toString(data.args));
                if (!(data.sender instanceof Player))
                    return true;
                Player player = (Player) data.sender;
                if (data.args[0] != "emote") {
                    String[] newArgs = new String[data.args.length + 1];
                    newArgs[0] = "emote";
                    for (int i = 0; i < data.args.length; i++) {
                        newArgs[i + 1] = data.args[i];
                    }
                    data.args = newArgs;
                }
                // RealMessage.sendMessage(player, data.commandClass.getName() + "START");
                // RealMessage.sendMessage(player, Arrays.toString(data.args));
                // RealMessage.sendMessage(player, data.args.length + " ");
                // verify if arg 1 is "emote" and if not add it
                if (data.args.length < 2)
                    return true;

                if (Utils.checkPermission(data.sender, "emote." + data.args[1])) {
                    if (EmoteMain.getEmotes().contains(data.args[1])) {
                        Boolean head = false;
                        if (data.args.length >= 3) {
                            Player target = realmcraft.getInstance().getServer().getPlayer(data.args[2]);
                            if (target != null) {
                                player = target;
                            }
                        }
                        if (data.args.length >= 4) {
                            if (data.args[3].equalsIgnoreCase("rotatehead")) {
                                head = true;
                            }
                        }
                        if (data.args.length >= 5) {
                            String url = data.args[4];
                            if (url.startsWith("url:")) {
                                url = url.replace("url:", "");
                                if (url.endsWith(".png"))
                                    EmoteMain.playAnimationURL(player, data.args[1], head, url);
                                return true;
                            }
                        }

                        if (data.args[1].equalsIgnoreCase("stop")) {
                            EmoteMain.cancelEmote(player);
                            return true;
                        }
                        PlayerModel model = EmoteMain.playAnimation(player, data.args[1], head);
                        // RealStack stack = new RealStack(Material.PAPER, 14);

                        String emotename = realmcraft.getInteractiveConfig().getString(
                                "emotes." + data.args[1] + ".name",
                                data.args[1]);

                        RealMessage.sendMessage(data.sender,
                                "{path=command.prefix;file=lang.yml} {path=command.emote-play;file=lang.yml}",
                                emotename);

                    } else {
                        RealMessage.sendMessage(data.sender,
                                "{path=command.prefix;file=lang.yml} {path=command.emote-not-found;file=lang.yml}",
                                data.args[1]);
                    }
                } else {
                    RealMessage.sendMessage(data.sender,
                            "{path=command.prefix;file=lang.yml} {path=command.no-permission;file=lang.yml}",
                            data.args[1]);

                }
                return true;

            }

        };
        if (realmcraft.getInteractiveConfig().getBoolean("modules.emotes", true)) {
            Argument emote = new Argument().setArg("emote").setType(ArgumentTypes.STRING)
                    .setFunction(functionA).setCompletitions(EmoteMain.getEmotes());
            args.add(emote);
        }
        if (realmcraft.getInteractiveConfig().getBoolean("modules.overlay", true)) {
            args.add(new Argument().setArg("addlayer").setFunction((data) -> {
                if (data.args.length < 2)
                    return true;
                String emoteName = data.args[1];
                try {
                    Overlay.addLayer((Player) data.sender, emoteName);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
                        | IOException e) {
                    e.printStackTrace();
                }
                return true;
            }));
        }
        if (realmcraft.getInteractiveConfig().getBoolean("modules.emotes", true)) {
            emoteCommand.setMainFunction(functionA);
            emoteCommand.setTabCompleter((a) -> {
                return new ArrayList<>(EmoteMain.getEmotes());
            });
        }

        if (realmcraft.getInteractiveConfig().getBoolean("modules.loot", true)) {
            args.add(new Argument().setArg("loot").setCompletitions(LootTable.getLootTableNames())
                    .setFunction((data) -> {
                        if (data.args.length < 2)
                            return true;
                        String lootName = data.args[1];
                        if (LootTable.validLootTable(lootName) && data.sender instanceof Player player) {
                            // create a chest in player feet position
                            Location loc = player.getLocation();
                            loc.setY(loc.getY() - 1);
                            loc.getBlock().setType(Material.CHEST);
                            Chest chest = (Chest) loc.getBlock().getState();
                            LootTable table = new LootTable(lootName);
                            ItemStack[] contents = table.genLoot(InventoryType.BARREL, player);
                            chest.getInventory().setContents(contents);
                            table.setNameToContainer(loc);
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-given;file=lang.yml}",
                                    lootName);
                        } else {
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-not-found;file=lang.yml}",
                                    lootName);
                        }
                        return true;
                    }));

            args.add(new Argument().setArg("addLootInstance")
                    .setFunction((data) -> {
                        if (data.args.length < 3)
                            return true;
                        String containerPack = data.args[1];
                        String lootName = data.args[2];
                        if (LootTable.validLootTable(lootName) && data.sender instanceof Player player) {
                            ContainerInstance a = ContainerApi.fromPlayerView(player);
                            a.setLootTable(lootName);
                            a.setContainerPack(containerPack);
                            ContainerApi.saveContainer(a);
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-given;file=lang.yml}",
                                    lootName);
                        } else {
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-not-found;file=lang.yml}",
                                    lootName);
                        }
                        return true;
                    }));
            // removeLootInstance
            args.add(new Argument().setArg("removeLootInstance")
                    .setFunction((data) -> {
                        if (data.sender instanceof Player player) {
                            ContainerInstance a = ContainerApi.fromPlayerView(player);
                            ContainerApi.removeContainer(a);
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-given;file=lang.yml}");
                        }
                        return true;
                    }));

            // refillLootInstance
            args.add(new Argument().setArg("refillLootInstance")
                    .setFunction((data) -> {
                        if (data.sender instanceof Player player) {
                            ContainerInstance a = ContainerApi.fromPlayerView(player);
                            ContainerApi.refillContainer(a);
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-given;file=lang.yml}");
                        }
                        return true;
                    }));
            // refillLootInstance based on ContainerPack
            args.add(new Argument().setArg("refillLootInstanceByPack")
                    .setFunction((data) -> {
                        // parse first arg
                        if (data.args.length < 2)
                            return true;
                        String containerPack = data.args[1];
                        ContainerApi.refillContainer(containerPack);
                        return true;
                    }));
            // refill all loot Instances
            args.add(new Argument().setArg("refillAllLootInstance")
                    .setFunction((data) -> {
                        ContainerApi.refillAllContainers();
                        return true;
                    }));
        }

        command.setArguments(args);
        command.setMainFunction((data) -> {
            return true;
        });

        realmcraft.getInstance().getCommandMapper().register(command);
        if (realmcraft.getInteractiveConfig().getBoolean("modules.emotes", true)) {
            realmcraft.getInstance().getCommandMapper().register(emoteCommand);
        }
    }

    public static void Uninitialize() {
        realmcraft.getInstance().getCommandMapper().unregister(command);
        if (realmcraft.getInteractiveConfig().getBoolean("modules.emotes", true)) {
            realmcraft.getInstance().getCommandMapper().unregister(emoteCommand);
        }
    }
}
