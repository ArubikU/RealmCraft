package dev.arubik.realmcraft.IReplacer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;

import com.google.gson.JsonObject;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealStack;
import dev.arubik.realmcraft.Api.Utils;
import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import dev.arubik.realmcraft.IReplacer.ReplacementContext.GenerationType;
import dev.arubik.realmcraft.Managers.Depend;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.lib.math3.ml.neuralnet.twod.util.LocationFinder.Location;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class InternalReplacerStructure {

    @Getter
    public ReplacementTypes type;
    @Getter
    public OutputTypes outputType;
    @Getter
    public JsonObject TypeConfig;
    @Getter
    public JsonObject outputConfig;
    @Getter
    public List<String> IgnoreNBT;
    @Getter
    public String key;

    @Getter
    public InteractiveSection section;

    public InternalReplacerStructure() {
        TypeConfig = new JsonObject();
        outputConfig = new JsonObject();
        IgnoreNBT = new ArrayList<String>();
    }

    private ItemStack PassAble(ItemStack old, ItemStack newStack, JsonObject config) {
        if (config.has("Pass-Enchantments") && config.get("Pass-Enchantments").getAsBoolean()) {
            newStack.addUnsafeEnchantments(old.getEnchantments());
        }
        if (config.has("Pass-CMD") && config.get("Pass-CMD").getAsBoolean()) {
            Integer custommoldeldata = old.getItemMeta().getCustomModelData();
            newStack.editMeta(meta -> {
                meta.setCustomModelData(custommoldeldata);
            });
        }
        if (config.has("Pass-ArmorTrims") && config.get("Pass-Trims").getAsBoolean()) {
            if (old.getItemMeta() instanceof ArmorMeta && newStack.getItemMeta() instanceof ArmorMeta) {
                ArmorMeta oldMeta = (ArmorMeta) old.getItemMeta();
                ArmorMeta newMeta = (ArmorMeta) newStack.getItemMeta();
                if (oldMeta.hasTrim()) {
                    newMeta.setTrim(oldMeta.getTrim());
                }
            }
        }
        return newStack;
    }

    public void apply(ItemStack item, Consumer<ItemStack> onApply, ReplacementContext context) {
        Entity entity = context.getEntity();
        switch (outputType) {
            case COMMAND:
                if (outputConfig.has("Command")) {
                    String command = outputConfig.get("Command").getAsString();
                    command = Utils.parsePlaceholders(command, entity, item);
                    if (entity != null && entity instanceof Player) {
                        if (realmcraft.getInstance().getServer().getPluginManager()
                                .getPlugin("PlaceholderAPI") != null) {
                            command = me.clip.placeholderapi.PlaceholderAPI
                                    .setPlaceholders((Player) entity, command);
                        }
                    }
                    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
                    if (outputConfig.has("Remove-Item")
                            && outputConfig.get("Remove-Item").getAsBoolean()) {
                        onApply.accept(RealNBT.Empty);
                    } else {
                        onApply.accept(item);
                    }
                }
                break;
            case REALSTACK: {
                RealStack stack = RealStack
                        .fromInteractiveSection(section.getSection("Output-Config"));
                ItemStack output = stack.buildItemStack();
                item.setAmount(item.getAmount());

                item = PassAble(item, output, outputConfig);
                onApply.accept(output);
                break;
            }
            case MMOITEMS: {
                if (outputConfig.has("Item")) {
                    String line = outputConfig.get("Item").getAsString();
                    String[] split = line.split(":");
                    if (MMOItems.plugin.getTypes().has(split[0])) {
                        Type type = MMOItems.plugin.getTypes().get(split[0]);
                        ItemStack stack;
                        if (context.getEvent() instanceof PrepareItemCraftEvent) {
                            stack = Utils.getItemPreviewMMOitems(split[0], split[1]);
                        } else {
                            if (entity != null && entity instanceof Player) {
                                stack = MMOItems.plugin.getItem(type, split[1],
                                        PlayerData.get((Player) entity));
                            } else {
                                stack = IReplacerListener.getCacheMMO().get(type + ":" + split[1]);
                            }
                            if (stack == null) {
                                break;
                            }
                            stack = PassAble(item, stack, outputConfig);
                        }

                        if (context.getGenType() == GenerationType.NATURALLY) {

                        }
                        stack.setAmount(item.getAmount());

                        onApply.accept(stack);
                    }
                }
                break;
            }
            case MYTHICMOBS: {
                if (outputConfig.has("Item")) {
                    String line = outputConfig.get("Item").getAsString();
                    String[] split = line.split(":");
                    if (MythicBukkit.inst().getItemManager().getItem(split[0]).isPresent()) {
                        MythicItem mythicItem = MythicBukkit.inst().getItemManager().getItem(split[0]).get();
                        ItemStack stack = BukkitAdapter
                                .adapt(mythicItem.generateItemStack(Integer.parseInt(split[1])));
                        if (stack == null) {
                            break;
                        }
                        stack.setAmount(item.getAmount());
                        stack = PassAble(item, stack, outputConfig);
                        onApply.accept(stack);
                    }
                }
                break;
            }

            case VANILLA:
                if (section.has("Output-Config.Material")) {
                    if (Material
                            .valueOf(section.get("Output-Config.Material").toString()) == null) {
                        break;
                    }
                    ItemStack stack = new ItemStack(
                            Material.valueOf(section.get("Output-Config.Material").toString()));
                    stack.setAmount(item.getAmount());
                    stack = PassAble(item, stack, outputConfig);
                    onApply.accept(stack);
                }
            default:
                break;

        }
    }
}
