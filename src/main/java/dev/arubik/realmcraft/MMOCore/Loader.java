package dev.arubik.realmcraft.MMOCore;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

import dev.arubik.realmcraft.Handlers.RealMessage;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.objective.ClickonObjective;
import net.Indyuce.mmocore.api.quest.objective.GoToObjective;
import net.Indyuce.mmocore.api.quest.objective.KillMobObjective;
import net.Indyuce.mmocore.api.quest.objective.MineBlockObjective;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.CommandTrigger;
import net.Indyuce.mmocore.api.quest.trigger.ExperienceTrigger;
import net.Indyuce.mmocore.api.quest.trigger.ItemTrigger;
import net.Indyuce.mmocore.api.quest.trigger.MessageTrigger;
import net.Indyuce.mmocore.api.quest.trigger.SoundTrigger;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.comp.citizens.GetItemObjective;
import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.chest.condition.ConditionInstance;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropTableDropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.GoldDropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.NoteDropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.VanillaDropItem;
import net.Indyuce.mmocore.manager.DropTableManager;

public class Loader extends MMOLoader {
    @Override
    public Trigger loadTrigger(MMOLineConfig config) {
        ;

        if (config.getKey().equals("expentity")) {

            return new Trigger(config) {
                @Override
                public void apply(PlayerData playerData) {
                    // get first block in view
                    Block b = playerData.getPlayer().getTargetBlock(null, 100);
                    if (b != null) {
                        // get the amount of exp to give
                        int exp = config.getInt("exp");
                        // give the player the exp
                        // summon experience orb
                        playerData.getPlayer().getWorld()
                                .spawn(playerData.getPlayer().getLocation(), ExperienceOrb.class).setExperience(exp);
                    }
                }
            };
        }

        return null;
    }

    @Override
    public Condition loadCondition(MMOLineConfig config) {
        if (config.getKey().equalsIgnoreCase("statgreatherthan")) {

            // RealMessage.sendRaw("Loading statgreatherthan condition");

            return new Condition(config) {
                @Override
                public boolean isMet(ConditionInstance arg0) {
                    if (arg0.getEntity() instanceof Player) {
                        PlayerData data = PlayerData.get((Player) arg0.getEntity());
                        if (data.getStats().getStat(config.getString("stat")) > config.getInt("value")) {
                            return true;
                        }
                    }
                    return false;
                }
            };

        }
        return null;
    }

}
