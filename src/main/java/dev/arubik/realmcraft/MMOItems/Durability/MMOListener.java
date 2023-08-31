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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;

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
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.source.RepairItemExperienceSource;
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
                    // set the result to null

                    // get second param
                    String type = repairMaterial.split("\\.")[0];
                    String id = repairMaterial.split("\\.")[1];
                    if (type.equalsIgnoreCase("MINECRAFT")
                            && (id.equalsIgnoreCase("stones") || id.equalsIgnoreCase("woods"))) {
                        if (id.equalsIgnoreCase("stones")) {
                            if (!stones.contains(inv.getItem(repair).getType())) {
                                e.getInventory().setItem(output, RealNBT.Empty);
                                return;
                            }
                        } else if (id.equalsIgnoreCase("woods")) {
                            if (!woods.contains(inv.getItem(repair).getType())) {
                                e.getInventory().setItem(output, RealNBT.Empty);
                                return;
                            }
                        }
                    }
                    if (!nbt.getString("MMOITEMS_REPAIR_MATERIAL")
                            .contains("MINECRAFT." + inv.getItem(repair).getType().toString())) {
                        e.getInventory().setItem(output, RealNBT.Empty);
                        return;
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
                    repairAmount = maxDurability - durability;
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

                double exp = realmcraft.getInteractiveConfig().getDouble(
                        "repairs." + nbt.getString("MMOITEMS_ITEM_TYPE") + ":" + nbt.getString("MMOITEMS_ITEM_ID"),
                        30.0);
                // exp = exp * repairPercent;
                // get number in range of repairPercent(-20%) repairPercent(+20%)
                double r = Utils.randomNumer(0, 40) - 20;
                // verify if is negative
                if (r < 0) {
                    // get the absolute value
                    r = Math.abs(r);
                    // get the percentage of the absolute value
                    r = r / 100;
                    // get the percentage of the exp
                    r = exp * r;
                    // remove the percentage of the exp
                    exp = exp - r;
                } else {
                    exp = exp * (exp * r) / 100;
                }
                exp *= 0.1;
                // round to 1 decimal
                exp = Utils.roundDouble(exp, 1);
                PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(p);
                MMOCore.plugin.professionManager.get("smithing").giveExperience(data, exp, null, EXPSource.VANILLA);
                // RealMessage.sendRaw(p, "&7You have gained &e" + exp + " &7smithing
                // experience.");
            }
        }
    }

    public static Set<Material> woods = new HashSet<Material>();
    public static Set<Material> stones = new HashSet<Material>();
    static {
        stones.add(Material.COBBLESTONE);
        stones.add(Material.STONE);
        stones.add(Material.ANDESITE);
        stones.add(Material.DIORITE);
        stones.add(Material.GRANITE);
        stones.add(Material.BLACKSTONE);
        woods.add(Material.OAK_PLANKS);
        woods.add(Material.SPRUCE_PLANKS);
        woods.add(Material.BIRCH_PLANKS);
        woods.add(Material.JUNGLE_PLANKS);
        woods.add(Material.ACACIA_PLANKS);
        woods.add(Material.DARK_OAK_PLANKS);
        woods.add(Material.CRIMSON_PLANKS);
        woods.add(Material.WARPED_PLANKS);
        woods.add(Material.MANGROVE_PLANKS);

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
                    // set the result to null

                    String repairMaterial = nbt.getString("MMOITEMS_REPAIR_MATERIAL");
                    // get second param
                    String type = repairMaterial.split("\\.")[0];
                    String id = repairMaterial.split("\\.")[1];
                    if (type.equalsIgnoreCase("MINECRAFT")
                            && (id.equalsIgnoreCase("stones") || id.equalsIgnoreCase("woods"))) {
                        if (id.equalsIgnoreCase("stones")) {
                            if (!stones.contains(inv.getItem(repair).getType())) {
                                e.getInventory().setItem(output, RealNBT.Empty);
                                return;
                            }
                        } else if (id.equalsIgnoreCase("woods")) {
                            if (!woods.contains(inv.getItem(repair).getType())) {
                                e.getInventory().setItem(output, RealNBT.Empty);
                                return;
                            }
                        }
                    }
                    if (!nbt.getString("MMOITEMS_REPAIR_MATERIAL")
                            .contains("MINECRAFT." + inv.getItem(repair).getType().toString())) {
                        e.getInventory().setItem(output, RealNBT.Empty);
                        return;
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
}
