package dev.arubik.realmcraft.MMOItems.Durability;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.BookMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;

public class MMOListener implements Listener, Depend {

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MMOItems" };
    }

    public static void register() {
        MMOListener ml = new MMOListener();
        if (!Depend.isPluginEnabled(ml)) {
            RealMessage.nonFound("MMOItems is not installed, so MMOItems durability will not work.");
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(ml, realmcraft.getInstance());
    }

    public static final Integer input = 0;
    public static final Integer repair = 1;
    public static final Integer output = 2;

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void anvilClick(InventoryClickEvent e) {
        if (!(e.getClickedInventory() instanceof AnvilInventory))
            return;
        if (!(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.LEFT))
            return;
        if (!(e.getSlotType() == SlotType.RESULT))
            return;
        Player p = ((Player) e.getWhoClicked());
        if (!(e.getCursor().getType().equals(Material.AIR))) {
            return;
        }

        if (e.getInventory().getItem(input) != null && e.getInventory().getItem(repair) != null) {

            AnvilInventory inv = (AnvilInventory) e.getInventory();
            RealNBT nbt = new RealNBT(inv.getItem(input));
            if (!nbt.contains("MMOITEMS_REPAIR_MATERIAL"))
                return;
            if (!nbt.contains("MMOITEMS_DURABILITY"))
                return;
            RealNBT nbt2 = new RealNBT(inv.getItem(repair));
            String repairMaterial = nbt.getString("MMOITEMS_REPAIR_MATERIAL");
            if (!nbt2.contains("MMOITEMS_ITEM_TYPE")) {
                if (inv.getItem(repair) != null) {
                    if (!defaultRepairMaterial.contains(inv.getItem(repair).getType())) {
                        // verify if MMOITEMS_REPAIR_MATERIAL not contains MINECRAFT.${material}
                        if (!nbt.getString("MMOITEMS_REPAIR_MATERIAL")
                                .contains("MINECRAFT." + inv.getItem(repair).getType().toString())) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
            if (p.getLevel() < 5)
                return;
            RealMessage.sendConsoleMessage(DebugType.ANVILREPAIR,
                    "Reading nbt RealNBT<MMOITEMS_REPAIR_MATERIAL>: " + repairMaterial);
            String type = repairMaterial.split("\\.")[0];
            String id = repairMaterial.split("\\.")[1];
            Integer repairPercent = Integer.parseInt(repairMaterial.split("\\.")[2]);
            if (type.equalsIgnoreCase(nbt2.getString("MMOITEMS_ITEM_TYPE", nbt2.getType()))
                    && id.equalsIgnoreCase(nbt2.getString("MMOITEMS_ITEM_ID", nbt2.getId()))) {
                RealMessage.sendConsoleMessage(DebugType.ANVILREPAIR,
                        "Reading nbt RealNBT<MMOITEMS_ITEM_TYPE>: "
                                + nbt2.getString("MMOITEMS_ITEM_TYPE", nbt2.getType()) + "\n" +
                                "original type: " + type);
                RealMessage.sendConsoleMessage(DebugType.ANVILREPAIR,
                        "Reading nbt RealNBT<MMOITEMS_ITEM_ID>: " + nbt2.getString("MMOITEMS_ITEM_ID", nbt2.getId())
                                + "\n" +
                                "original id: " + id);
                Integer durability = nbt.getInt("MMOITEMS_DURABILITY");
                Integer maxDurability = nbt.getInt("MMOITEMS_MAX_DURABILITY");
                double a = maxDurability / 100;
                double b = repairPercent * a;
                Integer repairAmount = Utils.doubleToInt(b);
                if (durability + repairAmount > maxDurability) {
                    durability = maxDurability;
                } else {
                    durability += repairAmount;
                }
                nbt.setInt("MMOITEMS_DURABILITY", durability);
                e.setCurrentItem(nbt.getItemStack());
                e.setCursor(nbt.getItemStack());
                e.getInventory().setItem(input, RealNBT.Empty);
                e.getInventory().setItem(repair, nbt2.take1().getItemStack());
                e.getInventory().setItem(output, nbt.getItemStack());
                if (p.getLevel() - 5 < 0) {
                    p.setLevel(0);
                } else {
                    p.setLevel(p.getLevel() - 5);
                }
                e.setCancelled(true);
            }
        }
    }

    public static Set<Material> defaultRepairMaterial = new HashSet<Material>();
    static {
        defaultRepairMaterial.add(Material.IRON_INGOT);
        defaultRepairMaterial.add(Material.GOLD_INGOT);
        defaultRepairMaterial.add(Material.DIAMOND);
        defaultRepairMaterial.add(Material.EMERALD);
        defaultRepairMaterial.add(Material.NETHERITE_INGOT);
        defaultRepairMaterial.add(Material.COBBLESTONE);
        defaultRepairMaterial.add(Material.STONE);
        defaultRepairMaterial.add(Material.OAK_PLANKS);
        defaultRepairMaterial.add(Material.SPRUCE_PLANKS);
        defaultRepairMaterial.add(Material.BIRCH_PLANKS);
        defaultRepairMaterial.add(Material.JUNGLE_PLANKS);
        defaultRepairMaterial.add(Material.ACACIA_PLANKS);
        defaultRepairMaterial.add(Material.DARK_OAK_PLANKS);
        defaultRepairMaterial.add(Material.CRIMSON_PLANKS);
        defaultRepairMaterial.add(Material.WARPED_PLANKS);
        defaultRepairMaterial.add(Material.MANGROVE_PLANKS);

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void anvilCost(PrepareAnvilEvent e) {

        if (e.getInventory().getItem(input) != null && e.getInventory().getItem(repair) != null) {

            AnvilInventory inv = (AnvilInventory) e.getInventory();
            RealNBT nbt = new RealNBT(inv.getItem(input).clone());
            if (!nbt.contains("MMOITEMS_REPAIR_MATERIAL"))
                return;
            if (!nbt.contains("MMOITEMS_DURABILITY"))
                return;

            RealNBT nbt2 = new RealNBT(inv.getItem(repair).clone());

            // verify if the item is not a mmoitem and if the item is a default repair
            // material

            if (!nbt2.contains("MMOITEMS_ITEM_TYPE")) {
                if (inv.getItem(repair) != null) {
                    if (!defaultRepairMaterial.contains(inv.getItem(repair).getType())) {
                        // verify if MMOITEMS_REPAIR_MATERIAL not contains MINECRAFT.${material}
                        if (!nbt.getString("MMOITEMS_REPAIR_MATERIAL")
                                .contains("MINECRAFT." + inv.getItem(repair).getType().toString())) {
                            // set the result to null
                            e.getInventory().setItem(output, RealNBT.Empty);
                            return;
                        }
                    }
                }
            }

            String repairMaterial = nbt.getString("MMOITEMS_REPAIR_MATERIAL");
            RealMessage.sendConsoleMessage(DebugType.ANVILREPAIR,
                    "Reading nbt RealNBT<MMOITEMS_REPAIR_MATERIAL>: " + repairMaterial);
            String type = repairMaterial.split("\\.")[0];
            String id = repairMaterial.split("\\.")[1];
            Integer repairPercent = Integer.parseInt(repairMaterial.split("\\.")[2]);
            if (type.equalsIgnoreCase(nbt2.getString("MMOITEMS_ITEM_TYPE", nbt2.getType()))
                    && id.equalsIgnoreCase(nbt2.getString("MMOITEMS_ITEM_ID", nbt2.getId()))) {
                RealMessage.sendConsoleMessage(DebugType.ANVILREPAIR,
                        "Reading nbt RealNBT<MMOITEMS_ITEM_TYPE>: "
                                + nbt2.getString("MMOITEMS_ITEM_TYPE", nbt2.getType()) + "\n" +
                                "original type: " + type);
                RealMessage.sendConsoleMessage(DebugType.ANVILREPAIR,
                        "Reading nbt RealNBT<MMOITEMS_ITEM_ID>: " + nbt2.getString("MMOITEMS_ITEM_ID", nbt2.getId())
                                + "\n" +
                                "original id: " + id);
                Integer durability = nbt.getInt("MMOITEMS_DURABILITY");
                Integer maxDurability = nbt.getInt("MMOITEMS_MAX_DURABILITY");
                double a = maxDurability / 100;
                double b = repairPercent * a;
                Integer repairAmount = Utils.doubleToInt(b);
                if (durability + repairAmount > maxDurability) {
                    durability = maxDurability;
                } else {
                    durability += repairAmount;
                }
                nbt.setInt("MMOITEMS_DURABILITY", durability);
                e.setResult(nbt.getItemStack());
                e.getInventory().setRepairCostAmount(1);
                e.getInventory().setRepairCost(5);
                e.getInventory().setItem(output, nbt.getItemStack());
                e.getView().setItem(output, nbt.getItemStack());

                Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        e.setResult(nbt.getItemStack());
                        e.getInventory().setRepairCostAmount(1);
                        e.getInventory().setRepairCost(5);
                        e.getInventory().setItem(output, nbt.getItemStack());
                        e.getView().setItem(output, nbt.getItemStack());
                    }

                }, 1l);
            }
        }
    }

    // listen other events

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void bowUseEvent(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            RealNBT nbt = new RealNBT(e.getBow());
            if (nbt.contains("MMOITEMS_ITEM_TYPE") && nbt.contains("MMOITEMS_ITEM_ID")
                    && nbt.contains("MMOITEMS_MAX_DURABILITY")) {
                Integer durability = nbt.getInt("MMOITEMS_MAX_DURABILITY");
                if (nbt.contains("MMOITEMS_DURABILITY"))
                    durability = nbt.getInt("MMOITEMS_DURABILITY", durability);
                // get unbreaking enchantment level
                Integer unbreaking = nbt.getEnchantmentLevel(Enchantment.DURABILITY);
                // calculate chance to break
                double chance = unbreaking * 15;
                // random number
                Random random = new Random();
                // if random number is less than chance to break, break
                if ((random.nextInt(100) > chance) || unbreaking == 0) {
                    durability--;
                    nbt.setInt("MMOITEMS_DURABILITY", durability);
                    // e.getBow().setItemMeta(nbt.getItemMeta());
                    if (durability <= 0) {
                        p.getEquipment().setItem(e.getHand(), RealNBT.Empty);

                        p.updateInventory();
                    } else {
                        p.getEquipment().setItem(e.getHand(), nbt.getItemStack());
                        p.updateInventory();
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBookUse(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() == null)
                return;
            if (e.getItem().hasItemMeta()) {
                if (e.getItem().getItemMeta() instanceof BookMeta) {
                    if (NBTItem.get(e.getItem()).hasTag("MMOITEMS_DISABLE_INTERACTION")) {
                        e.setCancelled(true);
                        // open and close a fast inventory
                        try {
                            Inventory inv = Bukkit.createInventory(null, 9, " ");
                            e.getPlayer().openInventory(inv);
                            e.getPlayer().closeInventory();
                        } catch (Throwable e1) {
                            // e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
