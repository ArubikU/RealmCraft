package dev.arubik.realmcraft.MythicLib.Passive;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.Utils;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SoulCollector extends SkillHandler<AttackSkillResult> implements Listener {

    @Override
    public String getLowerCaseId() {
        return getId().toLowerCase() + "_PRIVATESKILL";
    }

    public SoulCollector() {
        super(false);
        registerModifiers("soul-chance"); // Register the soul-chance modifier
    }

    private static Map<UUID, UUID> LastKill = new HashMap<UUID, UUID>();

    @EventHandler
    public void onEntityDeath(PlayerKillEntityEvent event) {

        Player player = event.getPlayer();

        if (LastKill.containsKey(player.getUniqueId())) {
            if (LastKill.get(player.getUniqueId()) == event.getTarget().getUniqueId()) {
                return;
            } else {
                LastKill.put(player.getUniqueId(), event.getTarget().getUniqueId());
            }
        } else {
            LastKill.put(player.getUniqueId(), event.getTarget().getUniqueId());
        }

        MMOPlayerData playerData = MMOPlayerData.get(player);
        if (playerData.getPassiveSkillMap().getSkill(this) != null) {
            if (player.getInventory().getItemInOffHand().getType() == Material.GLASS_BOTTLE) {
                int amount = player.getInventory().getItemInOffHand().getAmount();
                if (amount > 0) {
                    Skill meta = playerData.getPassiveSkillMap().getSkill(this).getTriggeredSkill();
                    double soulChance = meta.getModifier("soul-chance");
                    if (Utils.Chance(soulChance * 100, 100)) {
                        giveItem(player, 1); // Call the method to give the soul item
                        if (amount == 1) {
                            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR)); // Remove the glass
                        } else {
                            player.getInventory().getItemInOffHand().setAmount(amount - 1); // Decrease the amount
                        }
                        // bottle
                        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                        // play break glass particle
                        player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, player.getLocation(), 4,
                                0.5, 0.5, 0.5, 0.1, Material.GLASS.createBlockData());
                    }
                }
            }
        }

    }

    static ItemStack stack = null;

    private void giveItem(Player player, int amount) {

        if (stack == null) {
            Type type = MMOItems.plugin.getTypes().get("MATERIAL");
            stack = MMOItems.plugin.getItem(type, "SOUL");
        }

        ItemStack clone = stack.clone();
        clone.setAmount(amount);
        player.getInventory().addItem(clone);
    }

    @Override
    public void whenCast(AttackSkillResult arg0, SkillMetadata arg1) {

        throw new UnsupportedOperationException("Unimplemented method 'whenCast'");
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata arg0) {

        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }
}
