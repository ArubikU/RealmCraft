package dev.arubik.realmcraft.MythicMobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.RealNBT;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.targeters.IEntityTargeter;
import io.lumine.mythic.bukkit.BukkitAdapter;

public class FilteredItemsInRadius implements IEntityTargeter {

    String Filter = "all";
    Double Radius = 0.0;
    int maxStackSize = 64;
    int minStackSize = 1;
    int maxItems = 100;

    public FilteredItemsInRadius(MythicLineConfig config) {
        Filter = config.getString("filter", "all");
        Radius = config.getDouble("radius", 0.0);
        maxStackSize = config.getInteger("maxStackSize", 64);
        minStackSize = config.getInteger("minStackSize", 1);
        maxItems = config.getInteger("maxItems", 100);

    }

    @Override
    public Collection<AbstractEntity> getEntities(SkillMetadata arg0) {
        List<AbstractEntity> entities = arg0.getOrigin().getWorld().getLivingEntities();
        // remove all entities that are not items and are too far away
        entities.removeIf(
                entity -> !(entity instanceof Item) || entity.getLocation().distance(arg0.getOrigin()) > Radius);
        List<AbstractEntity> items = new ArrayList<>();
        for (AbstractEntity abstracte : entities) {
            Entity entity = BukkitAdapter.adapt(abstracte);
            if (Filter.equalsIgnoreCase("all")) {
                ItemStack item = ((Item) entity).getItemStack();
                if (item.getAmount() >= minStackSize && item.getAmount() <= maxStackSize) {
                    items.add(BukkitAdapter.adapt(entity));
                }
            } else {
                if (Filter.startsWith("mmoitems.")) {
                    ItemStack item = ((Item) entity).getItemStack();
                    if (item.getAmount() >= minStackSize && item.getAmount() <= maxStackSize) {
                        String[] split = Filter.split("\\.");
                        // identifier type id
                        if (split.length == 2) {
                            String type = split[1];
                            String id = split[2];
                            RealNBT nbt = new RealNBT(item);
                            if (nbt.contains("MMOITEMS_TYPE")) {
                                if (nbt.getString("MMOITEMS_TYPE").equalsIgnoreCase(type)) {
                                    if (nbt.contains("MMOITEMS_ITEM_ID")) {
                                        if (nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(id)) {
                                            items.add(BukkitAdapter.adapt(entity));
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
                if (Filter.startsWith("mythictype.")) {
                    ItemStack item = ((Item) entity).getItemStack();
                    if (item.getAmount() >= minStackSize && item.getAmount() <= maxStackSize) {
                        String[] split = Filter.split("\\.");
                        // identifier id
                        if (split.length == 1) {
                            String id = split[1];
                            RealNBT nbt = new RealNBT(item);
                            if (nbt.contains("MYTHIC_TYPE")) {
                                if (nbt.getString("MythicType").equalsIgnoreCase(id)) {
                                    items.add(BukkitAdapter.adapt(entity));
                                }
                            }
                        }
                    }
                }
            }

        }

        if (items.size() > maxItems) {
            items = items.subList(0, maxItems);
        }

        return items;
    }

}
