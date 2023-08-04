package dev.arubik.realmcraft.CustomSouds;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.GenericGameEvent;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.Managers.Depend;
import net.Indyuce.mmoitems.api.CustomSound;

public class WeaponListener implements Listener, Depend {

    @EventHandler
    public void onWeaponHit(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageByEntityEvent.DamageCause.ENTITY_SWEEP_ATTACK
                || event.getCause() == EntityDamageByEntityEvent.DamageCause.ENTITY_ATTACK) {
            if (event.getDamager() instanceof LivingEntity) {
                RealNBT mainhand = new RealNBT(((LivingEntity) event.getDamager()).getEquipment().getItemInMainHand());
                if (mainhand.contains("MMOITEMS_ITEM_TYPE")) {
                    playSound(mainhand.getString("MMOITEMS_ITEM_TYPE") + ":" +
                            mainhand.getString("MMOITEMS_ITEM_ID"), event.getEntity(), CustomSound.ON_ATTACK);
                } else {
                    if (event.getDamager() instanceof Player) {
                        playSound("generic", event.getEntity(), CustomSound.ON_ATTACK);
                    }
                    // playSound("generic", event.getEntity(), CustomSound.ON_ATTACK);
                }
            }
        }
    }

    // on left click air
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR) {
            if (event.getItem() != null) {
                RealNBT mainhand = new RealNBT(event.getItem());
                if (mainhand.contains("MMOITEMS_ITEM_TYPE")) {
                    playSound(mainhand.getString("MMOITEMS_ITEM_TYPE") + ":" +
                            mainhand.getString("MMOITEMS_ITEM_ID"), event.getPlayer(), CustomSound.ON_LEFT_CLICK);
                }
            }
        }
    }

    public void playSound(String weaponID, Entity entity, CustomSound sound) {

        SoundCategory soundCategory = SoundCategory.PLAYERS;
        if (!(entity instanceof Player)) {
            soundCategory = SoundCategory.HOSTILE;
        }

        if (weaponID.equals("generic")) {
            entity.getWorld().playSound(entity.getLocation(), "minecraft:entity.player.attack.rq.sweep", soundCategory,
                    1f, 1f);
            return;
        }
        if (!realmcraft.getInteractiveConfig().has("customsounds." + weaponID)) {

            Boolean foundedPattern = false;

            for (String key : realmcraft.getInteractiveConfig().getKeys("customsounds")) {
                String[] keySplit = key.split(":");
                String type = "in";
                if (keySplit[1].startsWith("*") && keySplit[1].endsWith("*")) {
                    type = "contains";
                    keySplit[1] = keySplit[1].replace("*", "");
                }
                if (keySplit[1].startsWith("*")) {
                    type = "end";
                    keySplit[1] = keySplit[1].substring(1);
                }
                if (keySplit[1].endsWith("*")) {
                    type = "start";
                    keySplit[1] = keySplit[1].substring(0, keySplit[1].length() - 1);
                }
                if (type.equals("in")) {
                    if (keySplit[1].equals(weaponID.split(":")[1])) {
                        weaponID = key;
                        foundedPattern = true;
                        break;
                    }
                }
                if (type.equals("contains")) {
                    if (weaponID.split(":")[1].contains(keySplit[1])) {
                        weaponID = key;
                        foundedPattern = true;
                        break;
                    }
                }
                if (type.equals("end")) {
                    if (weaponID.split(":")[1].endsWith(keySplit[1])) {
                        weaponID = key;
                        foundedPattern = true;
                        break;
                    }
                }
                if (type.equals("start")) {
                    if (weaponID.split(":")[1].startsWith(keySplit[1])) {
                        weaponID = key;
                        foundedPattern = true;
                        break;
                    }
                }

            }

            if (!foundedPattern) {
                String newWeaponID = weaponID.split(":")[0];
                weaponID = newWeaponID + ":generic";
            }
        }

        // RealMessage.sendConsoleMessage(DebugType.DEBUG,
        // "customsounds." + weaponID + "." + sound.toString().toLowerCase());

        String soundLine = realmcraft.getInteractiveConfig().getString(
                "customsounds." + weaponID + "." + sound.toString().toLowerCase(),
                "minecraft:entity.player.attack.rq.sweep 1 1");
        String[] soundLineSplit = soundLine.split(" ");
        String soundName = soundLineSplit[0];

        // the sound volume and pitch can be in format 0.5to1.0
        float volume = 1f;
        float pitch = 1f;
        if (soundLineSplit[1].contains("to")) {
            String[] volumeSplit = soundLineSplit[1].split("to");
            volume = (float) (Math.random() * (Float.parseFloat(volumeSplit[1]) - Float.parseFloat(volumeSplit[0]))
                    + Float.parseFloat(volumeSplit[0]));
        } else {
            volume = Float.parseFloat(soundLineSplit[1]);
        }
        if (soundLineSplit[2].contains("to")) {
            String[] pitchSplit = soundLineSplit[2].split("to");
            pitch = (float) (Math.random() * (Float.parseFloat(pitchSplit[1]) - Float.parseFloat(pitchSplit[0]))
                    + Float.parseFloat(pitchSplit[0]));
        } else {
            pitch = Float.parseFloat(soundLineSplit[2]);
        }
        entity.getWorld().playSound(entity.getLocation(), soundName, soundCategory, volume, pitch);
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "MMOItems" };
    }

    public static void register() {
        realmcraft.getInstance().getServer().getPluginManager().registerEvents(new WeaponListener(),
                realmcraft.getInstance());
    }
}
