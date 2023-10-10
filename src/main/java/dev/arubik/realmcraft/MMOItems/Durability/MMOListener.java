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
import dev.arubik.realmcraft.Managers.Module;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.source.RepairItemExperienceSource;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;

public class MMOListener implements Listener, Depend, Module {

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MMOItems" };
    }

    @Override
    public void register() {
        if (!Depend.isPluginEnabled(this)) {
            RealMessage.nonFound("MMOItems is not installed, so MMOItems durability will not work.");
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(this, realmcraft.getInstance());
    }

    public static final Integer input = 0;
    public static final Integer repair = 1;
    public static final Integer output = 2;

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

    public int getRepairPercent(String repairMaterial, Player p) {
        return Integer.parseInt(repairMaterial.split("\\.")[2]);
    }

    public int repairPercent(RealNBT Tool, int repairPercent, int amount) {
        // consume all the repair material and return the left amount of the repair
        // material
        int durability = Tool.getInt(MMOITEMS_DURABILITY);
        int maxDurability = Tool.getInt(MMOITEMS_MAX_DURABILITY);
        int repairAmount = maxDurability / 100 * repairPercent;
        int consumed = 0;
        while (durability <= maxDurability && consumed <= amount) {
            durability += repairAmount;
            if (durability <= maxDurability && consumed <= amount) {
                consumed += 1;
            }
        }

        if (durability > maxDurability) {
            durability = maxDurability;
        }
        int newDur = durability;

        Tool.setInt(MMOITEMS_DURABILITY, durability);

        Tool.editMeta(Damageable.class, (Damageable meta) -> {
            int maxVanillDurability = Tool.getMaterial().getMaxDurability();
            // get what percent is durability of max durability
            double percent = (double) newDur / (double) maxDurability;
            // get the new durability
            int newVanillaDurability = (int) (maxVanillDurability * percent);
            // set the new durability
            meta.setDamage(maxVanillDurability - newVanillaDurability);

        });
        return consumed;

    }

    public int applyPenalty(RealNBT ToolNBT) {
        if (ToolNBT.contains(MMOITEMS_ANVIL_PENALTY)) {
            int penalty = ToolNBT.getInt(MMOITEMS_ANVIL_PENALTY);
            int anvilUses = ToolNBT.getInt(MMOITEMS_ANVIL_USES_TIMES);
            Double rqUses = penalty * 1.5 + 1;
            if (anvilUses + 1 > rqUses) {
                ToolNBT.setInt(MMOITEMS_ANVIL_PENALTY, penalty + 1);
                ToolNBT.setInt(MMOITEMS_ANVIL_USES_TIMES, 0);
                return penalty + 1;
            }
            ToolNBT.setInt(MMOITEMS_ANVIL_USES_TIMES, anvilUses + 1);
            return penalty;
        } else {
            ToolNBT.setInt(MMOITEMS_ANVIL_PENALTY, 1);
            ToolNBT.setInt(MMOITEMS_ANVIL_USES_TIMES, 0);
            return 1;
        }
    }

    public int getLevelExpConsume(int penalty) {
        return 2 + ((Double) Math.pow(2, penalty)).intValue();
    }

    public boolean isValid(String repairMaterialID, RealNBT ToolNBT) {

        String type = repairMaterialID.split("\\.")[0];
        String id = repairMaterialID.split("\\.")[1];
        if (ToolNBT.contains(MMOITEMS_ANVIL_PENALTY)) {
            int penalty = ToolNBT.getInt(MMOITEMS_ANVIL_PENALTY);

            int levels = 2 + ((Double) Math.pow(2, penalty)).intValue();
            if (levels > 39) {
                return false;
            }
        }
        if (ToolNBT.contains(MMOITEMS_ITEM_TYPE)) {
            if (type.equalsIgnoreCase(ToolNBT.getString(MMOITEMS_ITEM_TYPE, ToolNBT.getType()))) {
                if (id.equalsIgnoreCase(ToolNBT.getString(MMOITEMS_ITEM_ID, ToolNBT.getId()))) {
                    return true;
                }
            }
            return false;
        } else {
            if (type.equalsIgnoreCase("MINECRAFT")) {
                if (id.equalsIgnoreCase("stones") || id.equalsIgnoreCase("woods")) {
                    if (id.equalsIgnoreCase("stones")) {
                        if (stones.contains(ToolNBT.getMaterial())) {
                            return true;
                        }
                    } else if (id.equalsIgnoreCase("woods")) {
                        if (woods.contains(ToolNBT.getMaterial())) {
                            return true;
                        }
                    }
                }
                if (id.equalsIgnoreCase(ToolNBT.getMaterial().toString())) {
                    return true;
                }
            }
            return false;
        }

    }

    public void giveSmithingEXP(Player player, String type, String id) {
        double exp = realmcraft.getInteractiveConfig().getDouble(
                "repairs." + type + ":" + id,
                30.0);
        double r = Utils.randomNumer(0, 40) - 20;
        if (r < 0) {
            r = Math.abs(r);
            r = r / 100;
            r = exp * r;
            exp = exp - r;
        } else {
            exp = exp * (exp * r) / 100;
        }
        exp *= 0.1;
        exp = Utils.roundDouble(exp, 1);
        PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
        MMOCore.plugin.professionManager.get("smithing").giveExperience(data, exp, null, EXPSource.VANILLA);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void anvilCost(PrepareAnvilEvent e) {

        if (e.getInventory().getItem(input) != null && e.getInventory().getItem(repair) != null) {

            AnvilInventory inv = (AnvilInventory) e.getInventory();
            RealNBT ToolNBT = new RealNBT(inv.getItem(input).clone());
            if (!ToolNBT.contains(MMOITEMS_DURABILITY))
                return;

            RealNBT RepairNBT = new RealNBT(inv.getItem(repair).clone());
            if (ToolNBT.contains(MMOITEMS_ITEM_TYPE)) {
                String typeid = ToolNBT.getString(MMOITEMS_ITEM_TYPE) + ":" + ToolNBT.getString(MMOITEMS_ITEM_ID);
                String secondid = RepairNBT.getString(MMOITEMS_ITEM_TYPE) + ":"
                        + RepairNBT.getString(MMOITEMS_ITEM_ID);
                if (typeid.equalsIgnoreCase(secondid)) {

                    // plus the two durabilities and set the result to the first item but full
                    // durability
                    int durability = ToolNBT.getInt(MMOITEMS_DURABILITY);
                    int maxDurability = ToolNBT.getInt("MMOITEMS_MAX_DURABILITY");
                    int durability2 = RepairNBT.getInt(MMOITEMS_DURABILITY);

                    int applyPenalty = applyPenalty(ToolNBT);
                    int levelsConsumed = getLevelExpConsume(applyPenalty);

                    int amount = durability + durability2;
                    if (amount > maxDurability) {
                        amount = maxDurability;
                    }
                    if (inv.getItem(output) != null) {
                        RealNBT outputnbt = new RealNBT(inv.getItem(output).clone());
                        applyPenalty = applyPenalty(outputnbt);
                        outputnbt.setInt(MMOITEMS_DURABILITY, amount);
                        e.setResult(outputnbt.getItemStack());
                        e.getInventory().setRepairCostAmount(1);
                        e.getInventory().setRepairCost(levelsConsumed);
                        e.getInventory().setItem(output, outputnbt.getItemStack());
                        e.getView().setItem(output, outputnbt.getItemStack());

                        Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                e.setResult(outputnbt.getItemStack());
                                e.getInventory().setRepairCostAmount(1);
                                e.getInventory().setRepairCost(levelsConsumed);
                                e.getInventory().setItem(output, outputnbt.getItemStack());
                                e.getView().setItem(output, outputnbt.getItemStack());
                            }

                        }, 1l);
                    } else {

                        ToolNBT.setInt(MMOITEMS_DURABILITY, amount);
                        e.setResult(ToolNBT.getItemStack());
                        e.getInventory().setRepairCostAmount(1);
                        e.getInventory().setRepairCost(levelsConsumed);
                        e.getInventory().setItem(output, ToolNBT.getItemStack());
                        e.getView().setItem(output, ToolNBT.getItemStack());

                        Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                e.setResult(ToolNBT.getItemStack());
                                e.getInventory().setRepairCostAmount(1);
                                e.getInventory().setRepairCost(levelsConsumed);
                                e.getInventory().setItem(output, ToolNBT.getItemStack());
                                e.getView().setItem(output, ToolNBT.getItemStack());
                            }

                        }, 1l);
                    }
                    return;
                }
            }

            if (!ToolNBT.contains(MMOITEMS_REPAIR_MATERIAL))
                return;

            // verify if the item is not a mmoitem and if the item is a default repair
            // material

            if (isValid(ToolNBT.getString(MMOITEMS_REPAIR_MATERIAL), RepairNBT)) {

                int applyPenalty = applyPenalty(ToolNBT);
                int levelExpConsume = getLevelExpConsume(applyPenalty);

                repairPercent(ToolNBT,
                        getRepairPercent(ToolNBT.getString(MMOITEMS_REPAIR_MATERIAL), (Player) e.getViewers().get(0)),
                        RepairNBT.getAmount());

                e.setResult(ToolNBT.getItemStack());
                e.getInventory().setRepairCostAmount(1);
                e.getInventory().setRepairCost(levelExpConsume);
                e.getInventory().setItem(output, ToolNBT.getItemStack());
                e.getView().setItem(output, ToolNBT.getItemStack());

                Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), new Runnable() {

                    @Override
                    public void run() {
                        e.setResult(ToolNBT.getItemStack());
                        e.getInventory().setRepairCostAmount(1);
                        e.getInventory().setRepairCost(levelExpConsume);
                        e.getInventory().setItem(output, ToolNBT.getItemStack());
                        e.getView().setItem(output, ToolNBT.getItemStack());
                    }

                }, 1l);
            }
        }
    }

    private static final String MMOITEMS_DURABILITY = "MMOITEMS_DURABILITY";
    private static final String MMOITEMS_REPAIR_MATERIAL = "MMOITEMS_REPAIR_MATERIAL";
    private static final String MMOITEMS_MAX_DURABILITY = "MMOITEMS_MAX_DURABILITY";
    private static final String MMOITEMS_ITEM_TYPE = "MMOITEMS_ITEM_TYPE";
    private static final String MMOITEMS_ITEM_ID = "MMOITEMS_ITEM_ID";
    private static final String MMOITEMS_ANVIL_USES_TIMES = "MMOITEMS_ANVIL_USES_TIMES";
    private static final String MMOITEMS_ANVIL_PENALTY = "MMOITEMS_ANVIL_PENALTY";

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void anvilClickNew(InventoryClickEvent e) {
        if (!(e.getClickedInventory() instanceof AnvilInventory))
            return;
        if (!(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.LEFT))
            return;
        if (!(e.getSlotType() == SlotType.RESULT))
            return;
        if (!(e.getCursor().getType().equals(Material.AIR))) {
            return;
        }

        if (e.getInventory().getItem(input) != null && e.getInventory().getItem(repair) != null) {
            AnvilInventory inv = (AnvilInventory) e.getInventory();
            RealNBT InputNBT = new RealNBT(inv.getItem(input).clone());
            RealNBT RepairNBT = new RealNBT(inv.getItem(repair).clone());
            if (e.getInventory().getItem(repair).getType().equals(Material.ENCHANTED_BOOK)) {
                return;
            }
            if (!InputNBT.contains(MMOITEMS_DURABILITY))
                return;

            String repairMaterial = InputNBT.getString(MMOITEMS_REPAIR_MATERIAL);

            if (isValid(repairMaterial, RepairNBT)) {

                int applyPenalty = applyPenalty(InputNBT);
                int levelExpConsume = getLevelExpConsume(applyPenalty);
                if (((Player) e.getWhoClicked()).getLevel() < levelExpConsume) {
                    e.setCancelled(true);
                    return;
                }

                int consumedMaterials = repairPercent(InputNBT,
                        getRepairPercent(repairMaterial, (Player) e.getWhoClicked()),
                        RepairNBT.getAmount());

                e.setCurrentItem(InputNBT.getItemStack());
                e.setCursor(InputNBT.getItemStack());
                e.getInventory().setItem(input, RealNBT.Empty);
                e.getInventory().setItem(repair, RepairNBT.take(consumedMaterials));
                e.getInventory().setItem(output, InputNBT.getItemStack());
                if (((Player) e.getWhoClicked()).getLevel() - levelExpConsume < 0) {
                    ((Player) e.getWhoClicked()).setLevel(0);
                } else {
                    ((Player) e.getWhoClicked()).setLevel(((Player) e.getWhoClicked()).getLevel() - levelExpConsume);
                }

                e.setCancelled(true);

            }

        }
    }

    // listen other events

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void bowUseEvent(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            RealNBT ToolNBT = new RealNBT(e.getBow());
            if (ToolNBT.contains(MMOITEMS_ITEM_TYPE) && ToolNBT.contains(MMOITEMS_ITEM_ID)
                    && ToolNBT.contains("MMOITEMS_MAX_DURABILITY")) {
                Integer durability = ToolNBT.getInt("MMOITEMS_MAX_DURABILITY");
                if (ToolNBT.contains(MMOITEMS_DURABILITY))
                    durability = ToolNBT.getInt(MMOITEMS_DURABILITY, durability);
                // get unbreaking enchantment level
                Integer unbreaking = ToolNBT.getEnchantmentLevel(Enchantment.DURABILITY);
                // calculate chance to break
                double chance = unbreaking * 15;
                // random number
                Random random = new Random();
                // if random number is less than chance to break, break
                if ((random.nextInt(100) > chance) || unbreaking == 0) {
                    durability--;
                    ToolNBT.setInt(MMOITEMS_DURABILITY, durability);
                    // e.getBow().setItemMeta(ToolNBT.getItemMeta());
                    if (durability <= 0) {
                        p.getEquipment().setItem(e.getHand(), RealNBT.Empty);

                        p.updateInventory();
                    } else {
                        p.getEquipment().setItem(e.getHand(), ToolNBT.getItemStack());
                        p.updateInventory();
                    }
                }
            }
        }
    }

    @Override
    public String configId() {
        return "durability";
    }

    @Override
    public String displayName() {
        return "MMOItem Durability";
    }
}
