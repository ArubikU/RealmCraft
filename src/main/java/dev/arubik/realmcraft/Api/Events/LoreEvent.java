package dev.arubik.realmcraft.Api.Events;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Lists;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.LoreParser;
import dev.arubik.realmcraft.Api.RealLore;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import lombok.Getter;

public class LoreEvent implements Depend {
    @Getter
    private static List<RealLore> lores = Lists.newArrayList();

    @Getter
    private static List<ItemBuildModifier> itemBuildModifiers = Lists.newArrayList();

    public static void reboot() {
        lores.clear();
        itemBuildModifiers.clear();
        OnRebuildLoreListener.CallEvent();
    }

    public static void addLore(RealLore lore) {
        lores.add(lore);
    }

    public static void removeLore(RealLore lore) {
        lores.remove(lore);
    }

    public static void addItemBuildModifier(ItemBuildModifier modifier) {
        itemBuildModifiers.add(modifier);
    }

    public static void removeItemBuildModifier(ItemBuildModifier modifier) {
        itemBuildModifiers.remove(modifier);
    }

    public static void addItemBuildModifier(ItemBuildModifier modifier, EventPriority index) {
        switch (index) {
            case HIGHEST:
                itemBuildModifiers.add(0, modifier);
                break;
            case LOWEST:
                itemBuildModifiers.add(modifier);
                break;
            default:
                itemBuildModifiers.add(modifier);
                break;
        }
    }

    public static PacketAdapter RECIPE_UPDATE = new PacketAdapter(realmcraft.getInstance(), ListenerPriority.HIGHEST,
            PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS,
            PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
        @Override
        public void onPacketSending(com.comphenix.protocol.events.PacketEvent event) {

            LoreParser parser = new LoreParser(event.getPlayer());
            PacketContainer packet = event.getPacket().deepClone();
            if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
                StructureModifier<List<ItemStack>> sm = packet.getItemListModifier();
                try {
                    sm.modify(0, parser.f1);
                } catch (java.lang.NoSuchMethodError e) {
                    sm.write(0, parser.f1.apply(sm.read(0)));
                }
                event.setPacket(packet);
            }

            if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                StructureModifier<ItemStack> sm = packet.getItemModifier();
                try {
                    sm.modify(0, parser.f);
                } catch (java.lang.NoSuchMethodError e) {
                    sm.write(0, parser.f.apply(sm.read(0)));
                }

                event.setPacket(packet);
            }

            if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW_MERCHANT
                    && realmcraft.getInteractiveConfig().getBoolean("module.merchant_packet", false)) {

                List<MerchantRecipe> recipeList = packet.getMerchantRecipeLists().read(0);
                for (int i = 0; i < recipeList.size(); i++) {
                    MerchantRecipe recipe = recipeList.get(i);
                    if (recipe.getResult().getType() == Material.ENCHANTED_BOOK) {
                        continue;
                    }
                    List<ItemStack> ingredients = recipe.getIngredients();
                    for (ItemStack ingredient : ingredients) {
                        if (ingredient == null)
                            continue;
                        ingredient = parser.forceApply(ingredient);
                    }
                    ItemStack result = recipe.getResult();
                    RealNBT nbt = new RealNBT(result);
                    result = nbt.getItemStack();
                    result = parser.forceApply(result);
                    MerchantRecipe recipeCopy = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(),
                            recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier());
                    recipeCopy.setIngredients(ingredients);
                    recipeList.set(i, recipeCopy);
                }
                StructureModifier<List<MerchantRecipe>> sm = packet.getMerchantRecipeLists();
                sm.write(0, recipeList);
                event.setPacket(packet);
            }
        }
    };

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "ProtocolLib", "MMOItems" };
    }

    public static PacketAdapter PLAYER_SOUND_CHANGUE = new PacketAdapter(realmcraft.getInstance(),
            PacketType.Play.Server.NAMED_SOUND_EFFECT) {
        @Override
        public void onPacketSending(com.comphenix.protocol.events.PacketEvent event) {

            if (event.getPacket().getSoundEffects().getValues().get(0).equals(Sound.ENTITY_PLAYER_HURT)) {
                event.getPacket().getSoundEffects().write(0, Sound.ENTITY_ARROW_HIT_PLAYER);
            }

        }
    };

    public static void registerListener() {
        if (!Depend.isPluginEnabled(new LoreEvent())) {
            RealMessage.nonFound("ProtocolLib or MMOItems is not installed, so LoreEvent will not work.");
            return;
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(RECIPE_UPDATE);
    }

    public static void unregisterListener() {
        ProtocolLibrary.getProtocolManager().removePacketListener(RECIPE_UPDATE);
    }

}
