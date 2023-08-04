package dev.arubik.realmcraft.Managers.Command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.georgev22.skinoverlay.gson.JsonElement;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealStack;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.EmoteHandler.EmoteMain;
import dev.arubik.realmcraft.Handlers.Overlay;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.IReplacer.IReplacerListener;
import dev.arubik.realmcraft.LootGen.ContainerApi;
import dev.arubik.realmcraft.LootGen.ContainerInstance;
import dev.arubik.realmcraft.LootGen.LootTable;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.md_5.bungee.api.ChatColor;
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
                if (!Utils.checkPermission(data.sender, "overlay.add"))
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
                        if (!Utils.checkPermission(data.sender, "loot.spawn"))
                            return true;
                        if (data.args.length < 2)
                            return true;
                        String lootName = data.args[1];
                        if (LootTable.validLootTable(lootName) && data.sender instanceof Player player) {
                            // create a chest in player feet position
                            Location loc = player.getLocation();
                            loc.setY(loc.getY() - 1);

                            if (data.args.length > 4) {
                                loc.setX(Double.parseDouble(data.args[2]));
                                loc.setY(Double.parseDouble(data.args[3]));
                                loc.setZ(Double.parseDouble(data.args[4]));
                            }
                            if (data.args.length > 5) {
                                loc.setWorld(realmcraft.getInstance().getServer().getWorld(data.args[5]));
                            }

                            loc.getBlock().setType(Material.CHEST);
                            Chest chest = (Chest) loc.getBlock().getState();
                            LootTable table = new LootTable(lootName);
                            ItemStack[] contents = table.genLoot(InventoryType.BARREL, player);
                            chest.getInventory().setContents(contents);
                            table.setNameToContainer(loc);
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-placed;file=lang.yml}",
                                    lootName);
                        } else if (LootTable.validLootTable(lootName) && !(data.sender instanceof Player player)) {
                            // create a chest in player feet position
                            Location loc = new Location(realmcraft.getInstance().getServer().getWorld("world"), 0, 0,
                                    0);
                            loc.setY(loc.getY() - 1);

                            if (data.args.length > 4) {
                                loc.setX(Double.parseDouble(data.args[2]));
                                loc.setY(Double.parseDouble(data.args[3]));
                                loc.setZ(Double.parseDouble(data.args[4]));
                            }
                            if (data.args.length > 5) {
                                loc.setWorld(realmcraft.getInstance().getServer().getWorld(data.args[5]));
                            }

                            loc.getBlock().setType(Material.CHEST);
                            Chest chest = (Chest) loc.getBlock().getState();
                            LootTable table = new LootTable(lootName);
                            ItemStack[] contents = table.genLoot(InventoryType.BARREL);
                            chest.getInventory().setContents(contents);
                            table.setNameToContainer(loc);
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-placed;file=lang.yml}",
                                    lootName);
                        } else {
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-not-found;file=lang.yml}",
                                    lootName);
                        }
                        return true;
                    }));

            args.add(new Argument().setArg("giveloot").setCompletitions(LootTable.getLootTableNames())
                    .setFunction((data) -> {
                        if (!Utils.checkPermission(data.sender, "loot.spawn"))
                            return true;
                        if (data.args.length < 2)
                            return true;
                        String lootName = data.args[1];
                        if (LootTable.validLootTable(lootName) && data.sender instanceof Player player) {
                            // create a chest in player feet position

                            if (data.args.length > 2) {
                                player = realmcraft.getInstance().getServer().getPlayer(data.args[2]);
                            }

                            Location loc = player.getLocation();
                            LootTable table = new LootTable(lootName);
                            ItemStack[] contents = table.genLoot(InventoryType.BARREL, player);
                            // create a list with non null items
                            for (ItemStack itemStack : contents) {
                                if (itemStack == null)
                                    continue;
                                loc.getWorld().dropItem(loc, itemStack);
                            }
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-given;file=lang.yml}",
                                    lootName);
                        } else if (LootTable.validLootTable(lootName) && !(data.sender instanceof Player player)) {
                            // create a chest in player feet position
                            Location loc = new Location(realmcraft.getInstance().getServer().getWorld("world"), 0, 0,
                                    0);
                            loc.setY(loc.getY() - 1);
                            if (data.args.length > 2) {
                                loc = realmcraft.getInstance().getServer().getPlayer(data.args[2]).getLocation();
                            }
                            LootTable table = new LootTable(lootName);
                            ItemStack[] contents = table.genLoot(InventoryType.BARREL);
                            for (ItemStack itemStack : contents) {
                                if (itemStack == null)
                                    continue;
                                loc.getWorld().dropItem(loc, itemStack, null);
                            }
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-given-other;file=lang.yml}",
                                    lootName, data.args[2]);
                        } else {
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-not-found;file=lang.yml}",
                                    lootName);
                        }
                        return true;
                    }));

            args.add(new Argument().setArg("openLoot").setCompletitions(LootTable.getLootTableNames())
                    .setFunction((data) -> {
                        if (!Utils.checkPermission(data.sender, "loot.open"))
                            return true;
                        if (data.args.length < 2)
                            return true;
                        String lootName = data.args[1];
                        if (LootTable.validLootTable(lootName) && data.sender instanceof Player player) {
                            // create a chest in player feet position

                            if (data.args.length > 2) {
                                player = realmcraft.getInstance().getServer().getPlayer(data.args[2]);
                            }

                            LootTable table = new LootTable(lootName);
                            ContainerApi.fakeOpenChest(table, player);
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-open;file=lang.yml}",
                                    lootName);
                        } else if (LootTable.validLootTable(lootName) && !(data.sender instanceof Player player)) {
                            // create a chest in player feet position
                            if (data.args.length > 2) {
                                Player player = realmcraft.getInstance().getServer().getPlayer(data.args[2]);
                                LootTable table = new LootTable(lootName);
                                ContainerApi.fakeOpenChest(table, player);
                                RealMessage.sendMessage(data.sender,
                                        "{path=command.prefix;file=lang.yml} {path=command.loot-open-other;file=lang.yml}",
                                        lootName);
                            } else {
                                RealMessage.sendMessage(data.sender,
                                        "{path=command.prefix;file=lang.yml} {path=command.no-arguments;file=lang.yml}",
                                        "playerName");
                            }
                        } else {
                            RealMessage.sendMessage(data.sender,
                                    "{path=command.prefix;file=lang.yml} {path=command.loot-not-found;file=lang.yml}",
                                    lootName);
                        }
                        return true;
                    }));

            args.add(new Argument().setArg("addLootInstance")
                    .setFunction((data) -> {
                        if (!Utils.checkPermission(data.sender, "loot.instance.add"))
                            return true;
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
                        if (!Utils.checkPermission(data.sender, "loot.instance.remove"))
                            return true;
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
                        if (!Utils.checkPermission(data.sender, "loot.instance.refill"))
                            return true;
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
                        if (!Utils.checkPermission(data.sender, "loot.instance.refil.pack"))
                            return true;
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
                        if (!Utils.checkPermission(data.sender, "loot.instance.refill.all"))
                            return true;
                        ContainerApi.refillAllContainers();
                        return true;
                    }));
        }

        String[][] dumpArgs = Utils.from(Utils.from("{", ChatColor.RED + "{" + ChatColor.GREEN),
                Utils.from("}", ChatColor.RED + "}" + ChatColor.GREEN),
                Utils.from("\",\"", ChatColor.GREEN + "\"" + ChatColor.AQUA + "," + "\"" + ChatColor.GREEN));

        Set<String> slots = Set.copyOf(List.of(EquipmentSlot.values()).stream().map((a) -> a.name())
                .collect(Collectors.toList()));
        args.add(
                new Argument().setArg("getnbtdump").setNext(null).setType(ArgumentTypes.STRING).setFunction((data) -> {
                    // RealMessage.sendRaw(data.toJson());
                    if (!Utils.checkPermission(data.sender, "debug.nbtdump"))
                        return true;
                    if (data.args.length < 1)
                        return true;
                    RealMessage.sendMessage(data.sender, "Obtaining NBT Dump for " + data.args[0] + " " + data.args[1]);
                    EquipmentSlot slot = EquipmentSlot.valueOf(data.args[1]);
                    if (data.sender instanceof Player player) {
                        ItemStack item = player.getInventory().getItem(slot);
                        if (item != null) {
                            RealNBT nbt = new RealNBT(item);
                            RealMessage.sendRaw(data.sender,
                                    ChatColor.RED + "NBTData" + ChatColor.GREEN + ":" +
                                            nbt.dumpWithColor(dumpArgs));
                        }
                    }
                    return true;
                }).setCompletitions(slots));
        Set<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                .map(Type::getId).collect(Collectors.toSet());
        args.add(
                new Argument().setArg("getpreview").setNext(null).setType(ArgumentTypes.STRING).setFunction((data) -> {
                    if (data.args.length < 2)
                        return true;
                    if (!Utils.checkPermission(data.sender, "debug.givepreview"))
                        return true;

                    String type = data.args[1];
                    String id = data.args[2];
                    ItemStack item = IReplacerListener.getItemPreviewMMOitems(type, id);
                    RealNBT nbt = new RealNBT(item);
                    if (item != null) {
                        RealMessage.sendMessage(data.sender,
                                "{path=command.prefix;file=lang.yml} {path=command.preview-get;file=lang.yml}", type,
                                id);
                        if (data.sender instanceof Player player) {

                            // verify if player has space in inventory
                            if (player.getInventory().firstEmpty() == -1) {
                                RealMessage.sendMessage(data.sender,
                                        "{path=command.prefix;file=lang.yml} {path=command.preview-no-space;file=lang.yml}");
                                return true;
                            }
                            // nbt.removedInvisibleNBT();
                            // item = nbt.getItemStack();
                            player.getInventory().addItem(item);
                        }
                    }

                    return true;
                }).setCompletitions(types));

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
