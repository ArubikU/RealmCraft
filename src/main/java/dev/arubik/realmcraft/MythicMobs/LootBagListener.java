package dev.arubik.realmcraft.MythicMobs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.comphenix.net.bytebuddy.dynamic.TypeResolutionStrategy.Active;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.Indyuce.mmoitems.util.Pair;
import net.seyarada.pandeloot.Constants;
import net.seyarada.pandeloot.drops.ActiveDrop;
import net.seyarada.pandeloot.drops.IDrop;
import net.seyarada.pandeloot.drops.ItemDrop;
import net.seyarada.pandeloot.drops.LootDrop;
import net.seyarada.pandeloot.drops.containers.ContainerManager;
import net.seyarada.pandeloot.flags.FlagPack;
import net.seyarada.pandeloot.flags.FlagPackFactory;
import net.seyarada.pandeloot.flags.enums.FlagTrigger;
import net.seyarada.pandeloot.utils.ItemUtils;

public class LootBagListener implements Listener {

    public HashMap<UUID, UUID> LockPlayer = new HashMap<UUID, UUID>();
    public HashMap<UUID, Boolean> Spread = new HashMap<UUID, Boolean>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent e) {

        if (realmcraft.getInteractiveConfig().getBoolean("ploot", true))
            return;
        if (e.getEntity().getKiller() != null) {
            Player player = e.getEntity().getKiller();
            LivingEntity LivingMob = e.getEntity();
            LivingEntity target = e.getEntity();
            if (!LivingMob.hasMetadata("PLOOT")) {
                return;
            }
            String ploot = LivingMob.getMetadata("PLOOT").get(0).asString();
            String type = LivingMob.getMetadata("TYPE").get(0).asString();
            Double chance = LivingMob.getMetadata("CHANCE").get(0).asDouble();
            Boolean lockPlayer = LivingMob.getMetadata("LOCKPLAYER").get(0).asString().equalsIgnoreCase("True");
            int ticksLocked = LivingMob.getMetadata("TICKSLOCKED").get(0).asInt();
            Boolean spread = LivingMob.getMetadata("SPREAD").get(0).asString().equalsIgnoreCase("True");
            if (LivingMob.getLastDamageCause() != null) {
                EntityDamageEvent event = LivingMob.getLastDamageCause();
                if (event instanceof EntityDamageByEntityEvent event2) {
                    if (event2.getDamager() instanceof Player) {
                        ItemStack item = player.getEquipment().getItemInMainHand();
                        if (item != null) {
                            if (!item.getType().isAir()) {
                                if (item.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_MOBS)) {
                                    int level = item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS);
                                    chance += level * 0.045;
                                }
                            }
                        }
                        chance *= 100;
                        double weight = 0;
                        if (MythicBukkit.inst().getMobManager().isMythicMob(target)) {
                            weight = MythicBukkit.inst().getMobManager().getMythicMobInstance(target).getLevel();
                            if (weight > 90) {
                                weight = 10;
                            } else if (weight > 70) {
                                weight = 9;
                            } else if (weight > 50) {
                                weight = 7;
                            } else if (weight > 30) {
                                weight = 6;
                            } else if (weight > 10) {
                                weight = 5;
                            } else {
                                weight = 4;
                            }

                        } else {
                            weight = 2;
                        }
                        weight *= 1.6;
                        List<IDrop> drop = new ArrayList<IDrop>();
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            ItemStack slotItem = LivingMob.getEquipment().getItem(slot);
                            if (slotItem == null)
                                continue;
                            if (slotItem.getType() == Material.AIR)
                                continue;
                            if (Utils.Chance(chance, 100)) {
                                RealNBT nbt = RealNBT.fromItemStack(slotItem);
                                Boolean Superier = false;
                                Boolean rare = false;
                                String tier = nbt.getString("MMOITEMS_TIER", "COMMON");
                                if (tier.equalsIgnoreCase("COMMON") || tier.equalsIgnoreCase("TRASH")) {
                                    weight += 1;
                                } else if (tier.equalsIgnoreCase("UNCOMMON") || tier.equalsIgnoreCase("JUNK")) {
                                    weight += 2;
                                } else if (tier.equalsIgnoreCase("RARE")) {
                                    weight += 3;
                                    rare = true;
                                } else if (tier.equalsIgnoreCase("VERY_RARE")) {
                                    weight += 4;
                                    Superier = true;
                                } else if (tier.equalsIgnoreCase("LEGENDARY")) {
                                    weight += 6;
                                    Superier = true;
                                } else if (tier.equalsIgnoreCase("UNIQUE") || tier.equalsIgnoreCase("MYTHICAL")) {
                                    weight += 7;
                                    Superier = true;
                                } else if (tier.equalsIgnoreCase("EPIC")) {
                                    weight += 8;
                                    Superier = true;
                                }
                                String flags = "{explode=true;glow=true;color=" + getColorFromTier(tier.toUpperCase())
                                        + ";beam=1}";

                                if (Superier) {

                                    ItemStack a = nbt.regenerate(player, weight * 4.5);
                                    ItemDrop itemDro = new ItemDrop(a, FlagPackFactory.getPack(flags));
                                    drop.add(itemDro);
                                } else if (rare) {
                                    ItemStack a = nbt.regenerate(player, weight * 2.5);
                                    ItemDrop itemDro = new ItemDrop(a, FlagPackFactory.getPack(flags));
                                    drop.add(itemDro);
                                } else {
                                    ItemStack a = nbt.regenerate(player, weight);
                                    ItemDrop itemDro = new ItemDrop(a, FlagPackFactory.getPack(flags));
                                    drop.add(itemDro);
                                }

                            }
                            // create a function calculate the tier of the lootbox depending the weight
                            // [COMMON,UNCOMMON,RARE,EPIC,VERY_RARE,LEGENDARY,UNIQUE,MYTHICAL]
                        }

                        List<ItemStack> itemsToRemove = new ArrayList<ItemStack>();

                        for (int i = 0; i < e.getDrops().size(); i++) {
                            ItemStack drops = e.getDrops().get(i);
                            if (drops == null || drops.getType() == Material.AIR) {
                                continue;
                            }
                            if (RealNBT.fromItemStack(drops).contains("MMOITEMS_TIER")) {
                                drop
                                        .add(new ItemDrop(drops,
                                                FlagPackFactory
                                                        .getPack(
                                                                "{explode=true;glow=true;color="
                                                                        + getColorFromTier(RealNBT
                                                                                .fromItemStack(drops)
                                                                                .getString("MMOITEMS_TIER",
                                                                                        "COMMON")
                                                                                .toUpperCase())
                                                                        + ";beam=1}")));
                                // remove the item from the drop list
                                itemsToRemove.add(drops);
                            }
                        }
                        for (ItemStack itemT : itemsToRemove) {
                            e.getDrops().remove(itemT);
                        }
                        String LootBoxTier = calculateLootBoxTier(weight);
                        String lootLine = ploot + "_" + LootBoxTier;
                        // pandeloot drop ArubikU lb:MyExampleTable{glow=true;color=BLUE}
                        String flags = "{explode=true;glow=true;color=" + getColorFromTier(LootBoxTier)
                                + ";beam=1;preventpickup=true}";
                        lootLine += flags;
                        LootDrop loot = null;
                        try {
                            loot = new LootDrop(lootLine, player, event.getEntity().getLocation())
                                    .build();
                            String lootlinetwo = ploot.replace("lb:", "lt:") + "_" + LootBoxTier;
                            String flags2 = "{explode=true;glow=true;color=" + getColorFromTier(LootBoxTier)
                                    + ";beam=1}";
                            lootlinetwo += flags2;
                            LootDrop loot2 = new LootDrop(
                                    lootlinetwo, player,
                                    event.getEntity().getLocation())
                                    .build();
                            Class<?> clazz = loot.getClass();
                            Class<?> Lootclazz = loot2.getClass();
                            Field field = clazz.getDeclaredField("itemDrops");
                            field.setAccessible(true);
                            ArrayList<IDrop> baseDropList = (ArrayList<IDrop>) field.get(loot);
                            ItemDrop lootBag = (ItemDrop) baseDropList.get(0);

                            Field fieldt = Lootclazz.getDeclaredField("itemDrops");
                            fieldt.setAccessible(true);
                            baseDropList = (ArrayList<IDrop>) fieldt.get(loot2);
                            ArrayList<IDrop> newDropList = new ArrayList<IDrop>(1000);
                            for (IDrop iDrop : baseDropList) {
                                if (iDrop instanceof ItemDrop) {
                                    ItemDrop itemDrop = new ItemDrop(iDrop.getItemStack(), FlagPackFactory
                                            .getPack("{" + iDrop.getFlagPack().flagString.flagSection + "}"));
                                    newDropList.add(itemDrop);
                                } else {
                                    newDropList.add(iDrop);
                                }
                            }
                            for (IDrop iDrop : drop) {
                                newDropList.add(iDrop);
                            }

                            if (newDropList.isEmpty()) {
                                return;
                            }
                            Pair<ActiveDrop, Entity> pair = run(loot, lootBag);
                            ActiveDrop activeDrop = pair.getKey();
                            clazz = activeDrop.getClass();
                            field = clazz.getDeclaredField("lootDrop");
                            field.setAccessible(true);
                            LootDrop lootDrop = (LootDrop) field.get(activeDrop);
                            clazz = lootDrop.getClass();

                            field = clazz.getDeclaredField("itemDrops");
                            field.setAccessible(true);
                            field.set(lootDrop, newDropList);
                            field.setAccessible(true);
                            clazz = activeDrop.getClass();
                            field = clazz.getDeclaredField("lootDrop");
                            field.setAccessible(true);
                            field.set(activeDrop, lootDrop);

                            if (lockPlayer) {
                                LockPlayer.put(pair.getValue().getUniqueId(), player.getUniqueId());
                                Bukkit.getScheduler().runTaskLaterAsynchronously(realmcraft.getInstance(), () -> {
                                    LockPlayer.remove(pair.getValue().getUniqueId());
                                }, ticksLocked);
                            }
                            if (spread) {
                                Spread.put(pair.getValue().getUniqueId(), spread);
                            }
                        } catch (Throwable error) {
                            error.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    public String calculateLootBoxTier(double weight) {
        double x = weight / 4;
        double log2x = Math.log(x) / Math.log(2);
        double floorLog2x = Math.floor(log2x);
        double fracLog2x = log2x - floorLog2x;
        double pow2fracLog2x = Math.pow(2, fracLog2x);
        double ceilPow2fracLog2x = Math.ceil(pow2fracLog2x);
        double tierWeight = floorLog2x + ceilPow2fracLog2x / 2.0;
        if (tierWeight >= 8) {
            return "MYTHICAL";
        } else if (tierWeight >= 7) {
            return "UNIQUE";
        } else if (tierWeight >= 6) {
            return "LEGENDARY";
        } else if (tierWeight >= 5) {
            return "VERY_RARE";
        } else if (tierWeight >= 4) {
            return "EPIC";
        } else if (tierWeight >= 3) {
            return "RARE";
        } else if (tierWeight >= 2) {
            return "UNCOMMON";
        } else {
            return "COMMON";
        }
    }

    public Pair<ActiveDrop, Entity> run(LootDrop lootDrop, ItemDrop drop) {
        Location dropLocation = lootDrop.getLocation();
        if (dropLocation == null) {
            RealMessage
                    .sendRaw((String) "Unable to find where to drop " + ((Object[]) new Object[] { drop }).toString());
            return null;
        }
        drop.getFlagPack().trigger(FlagTrigger.onprespawn, null, lootDrop, (IDrop) drop);
        if (drop.item.getType() != Material.AIR) {
            Item i = dropLocation.getWorld().dropItemNaturally(dropLocation, drop.item);
            for (Map.Entry<NamespacedKey, String> entry : drop.data.entrySet()) {
                i.getPersistentDataContainer().set(entry.getKey(), PersistentDataType.STRING,
                        ((Object) entry.getValue()).toString());
            }

            return Pair.of(new ActiveDrop((IDrop) drop, (Entity) i, lootDrop.p, drop.pack, lootDrop), (Entity) i);
        } else {
            return Pair.of(new ActiveDrop((IDrop) drop, null, lootDrop.p, drop.pack, lootDrop), null);
        }
    }

    public ActiveDrop addLoot(ActiveDrop activeDrop, Pair<ItemStack, String>... items) {
        LootDrop lootDrop = null;
        Class<?> Activeclazz = activeDrop.getClass();
        Field lootDropField = null;
        try {
            lootDropField = Activeclazz.getDeclaredField("lootDrop");
            lootDropField.setAccessible(true);
            lootDrop = (LootDrop) lootDropField.get(activeDrop);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (lootDrop == null) {
            return activeDrop;
        }
        Class<?> clazz = lootDrop.getClass();
        Field field = null;
        try {
            field = clazz.getDeclaredField("itemDrops");
            field.setAccessible(true);
            ArrayList<IDrop> baseDropList = (ArrayList<IDrop>) field.get(lootDrop);
            ArrayList<IDrop> newDropList = new ArrayList<IDrop>(1000);
            for (IDrop iDrop : baseDropList) {
                if (iDrop instanceof ItemDrop) {
                    ItemDrop itemDrop = (ItemDrop) iDrop;
                    newDropList.add(new ItemDrop(itemDrop.getItemStack(), FlagPackFactory
                            .getPack("{" + itemDrop.getFlagPack().flagString.flagSection + "}")));
                }
                if (iDrop instanceof LootDrop) {
                    newDropList.add(iDrop);
                }
            }
            for (Pair<ItemStack, String> item : items) {
                ItemDrop itemDrop = new ItemDrop(item.getKey(), FlagPackFactory.getPack(item.getValue()));
                newDropList.add(itemDrop);
            }
            field = clazz.getDeclaredField("itemDrops");
            field.setAccessible(true);
            field.set(lootDrop, newDropList);
            field.setAccessible(true);
            clazz = activeDrop.getClass();
            field = clazz.getDeclaredField("lootDrop");
            field.setAccessible(true);
            field.set(activeDrop, lootDrop);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return activeDrop;
    }

    public String getColorFromTier(String tier) {
        if (tier.equalsIgnoreCase("COMMON") || tier.equalsIgnoreCase("TRASH")) {
            return "GRAY";
        } else if (tier.equalsIgnoreCase("UNCOMMON") || tier.equalsIgnoreCase("JUNK")) {
            return "GREEN";
        } else if (tier.equalsIgnoreCase("RARE")) {
            return "YELLOW";
        } else if (tier.equalsIgnoreCase("VERY_RARE")) {
            return "GOLD";
        } else if (tier.equalsIgnoreCase("LEGENDARY")) {
            return "AQUA";
        } else if (tier.equalsIgnoreCase("UNIQUE")) {
            return "RED";
        } else if (tier.equalsIgnoreCase("MYTHICAL")) {
            return "DARK_PURPLE";
        } else if (tier.equalsIgnoreCase("EPIC")) {
            return "DARK_RED";
        }
        return "GRAY";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void openDroppedLootBag(PlayerInteractEvent e) {
        if (realmcraft.getInteractiveConfig().getBoolean("ploot", true)) {
            return;
        }
        PersistentDataContainer data;
        ItemStack iS = e.getPlayer().getEquipment().getItemInMainHand();
        if (iS.getType() == Material.AIR) {
            iS = e.getPlayer().getEquipment().getItemInOffHand();
        }
        if (iS.hasItemMeta() && (data = iS.getItemMeta().getPersistentDataContainer()).has(Constants.LOOTBAG_KEY,
                PersistentDataType.STRING)) {
            String id = (String) data.get(Constants.LOOTBAG_KEY, PersistentDataType.STRING);
            if (data.has(Constants.KEY, PersistentDataType.STRING)) {
                FlagPack flagPack = FlagPack
                        .fromCompact((String) ((String) data.get(Constants.KEY, PersistentDataType.STRING)));
                if (flagPack.passesConditions(FlagTrigger.onopen, null, e.getPlayer())) {
                    flagPack.trigger(FlagTrigger.onopen, null, e.getPlayer());
                } else {
                    return;
                }
            }
            iS.setAmount(iS.getAmount() - 1);
            new LootDrop((IDrop) ContainerManager.get((String) id), e.getPlayer(), e.getPlayer().getLocation()).build()
                    .drop();
            return;
        }
        if (e.getClickedBlock() == null) {
            return;
        }
        Location loc = e.getClickedBlock().getLocation();
        for (Entity i : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
            if (!(i instanceof Item))
                continue;
            Item item = (Item) i;
            iS = item.getItemStack();
            data = iS.getItemMeta().getPersistentDataContainer();
            ActiveDrop aDrop = ActiveDrop.get((Entity) item);
            if (aDrop == null) {
                continue;
            }

            if (LockPlayer.containsKey(i.getUniqueId())) {
                if (!LockPlayer.get(i.getUniqueId()).equals(e.getPlayer().getUniqueId())

                        && e.getPlayer().hasPermission("realmcraft.lootbag.bypass") == false) {
                    RealMessage.sendMessage(e.getPlayer(), "<red>No puedes abrir esta bolsa en este momento.");
                    return;
                }
            }

            boolean isLocked = data.has(Constants.LOCK_LOOTBAG, PersistentDataType.STRING);
            String id = null;
            if (isLocked) {
                aDrop.triggerRollBag(FlagTrigger.onspawn);
                aDrop.stopLootBagRunnable();
                ItemUtils.removeData((ItemStack) iS, (NamespacedKey) Constants.LOCK_LOOTBAG);
            }
            if (!isLocked) {
                if (!data.has(Constants.LOOTBAG_KEY, PersistentDataType.STRING))
                    continue;
                id = (String) data.get(Constants.LOOTBAG_KEY, PersistentDataType.STRING);
            }
            if (aDrop.amountOpened >= iS.getAmount()) {
                return;
            }
            if (data.has(Constants.KEY, PersistentDataType.STRING)) {
                FlagPack flagPack = FlagPack
                        .fromCompact((String) ((String) data.get(Constants.KEY, PersistentDataType.STRING)));
                if (flagPack.passesConditions(FlagTrigger.onopen, (Entity) item, e.getPlayer())) {
                    flagPack.trigger(FlagTrigger.onopen, (Entity) item, e.getPlayer());
                } else {
                    return;
                }
            }
            ++aDrop.amountOpened;
            LootBagListener.playArm(e.getPlayer());
            if (isLocked || id == null) {
                return;
            }
            iS.setAmount(iS.getAmount() - 1);
            if (iS.getAmount() <= 0) {
                item.remove();
            }
            Class<?> clazz = aDrop.getClass();
            try {
                Field field = clazz.getDeclaredField("lootDrop");
                field.setAccessible(true);
                LootDrop lootDrop = (LootDrop) field.get(aDrop);
                clazz = lootDrop.getClass();
                field = clazz.getDeclaredField("l");
                field.setAccessible(true);
                field.set(lootDrop, item.getLocation());

                if (!Spread.containsKey(i.getUniqueId())) {
                    Class<?> LootDropClazz = lootDrop.getClass();
                    Field itemDropsField = LootDropClazz.getField("itemDrops");
                    itemDropsField.setAccessible(true);
                    ArrayList<IDrop> drops = (ArrayList<IDrop>) itemDropsField.get(lootDrop);
                    PlayerInventory pinv = e.getPlayer().getInventory();
                    for (IDrop dropc : drops) {
                        pinv.addItem(dropc.getItemStack());
                    }
                } else {
                    lootDrop.drop();
                    Spread.remove(i.getUniqueId());
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
                new LootDrop((IDrop) ContainerManager.get((String) id), e.getPlayer(), item.getLocation()).build()
                        .drop();

            }
            e.setCancelled(true);
            return;
        }
    }

    public static void playArm(Player player) {
        Material mainHand = player.getInventory().getItemInMainHand().getType();
        Material offHand = player.getInventory().getItemInOffHand().getType();
        if (mainHand == Material.AIR && offHand == Material.AIR) {
            player.swingMainHand();
        }
    }

    public static void register() {
        Bukkit.getPluginManager().registerEvents(new LootBagListener(), realmcraft.getInstance());
    }

    @EventHandler
    public void onInventoryPickupItemEvent(InventoryPickupItemEvent e) {
        if (e.getItem().getItemStack().hasItemMeta()) {
            PersistentDataContainer data = e.getItem().getItemStack().getItemMeta().getPersistentDataContainer();
            if (data.has(Constants.LOOTBAG_KEY, PersistentDataType.STRING)) {
                e.setCancelled(true);
            }
        }
    }
}
