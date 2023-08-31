package dev.arubik.realmcraft.MythicMobs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.LootGen.ContainerApi;
import dev.arubik.realmcraft.LootGen.LootTable;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.MMOCoreAPI;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;

public class LootTableSkill implements ITargetedEntitySkill, Depend {

    private String type = "OPEN"; // open,drop,place
    private String id = "NONE";
    private InventoryType inventoryType = InventoryType.BARREL;

    public LootTableSkill(MythicLineConfig config) {

        type = config.getString("Action", type);
        id = config.getString("Id");

        inventoryType = InventoryType.valueOf(config.getString("SizeType", "BARREL"));

    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        LivingEntity LivingMob = (LivingEntity) BukkitAdapter.adapt(target);
        RealMessage.sendRaw(id);
        LootTable table = new LootTable(id);

        Location loc = LivingMob.getLocation();
        switch (type) {
            case "OPEN": {
                ContainerApi.fakeOpenChest(table, (Player) LivingMob, inventoryType);
                break;
            }
            case "DROP": {
                if (LivingMob instanceof Player) {
                    ItemStack[] items = table.genLoot(inventoryType, (Player) LivingMob);
                    for (ItemStack stack : items) {
                        if (stack != null) {
                            if (stack.getType() != Material.AIR) {
                                loc.getWorld().dropItem(loc, stack);
                            }
                        }
                    }
                } else {
                    ItemStack[] items = table.genLoot(inventoryType);
                    for (ItemStack stack : items) {
                        if (stack != null) {
                            if (stack.getType() != Material.AIR) {
                                loc.getWorld().dropItem(loc, stack);
                            }
                        }
                    }
                }
                break;
            }
            default:
                break;
        }

        return SkillResult.SUCCESS;
    }

    @Override
    public String[] getDependatsPlugins() {

        throw new UnsupportedOperationException("Unimplemented method 'getDependatsPlugins'");
    }
}