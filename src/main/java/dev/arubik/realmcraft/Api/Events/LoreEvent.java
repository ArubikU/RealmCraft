package dev.arubik.realmcraft.Api.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
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

    public static PacketAdapter LORE_UPDATE = new PacketAdapter(realmcraft.getInstance(), ListenerPriority.HIGHEST,
            new ArrayList<PacketType>() {
                {
                    add(PacketType.Play.Server.SET_SLOT);
                    add(PacketType.Play.Server.WINDOW_ITEMS);
                }
            }, ListenerOptions.ASYNC) {

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
                boolean isPlayerInventory = packet.getIntegers().read(0) == 0;
                if (PACKET_CUSTOM_SENDER && isPlayerInventory) {
                    // put to the last item in the packets map <UUID, List<PacketContainer>>
                    if (packets.containsKey(event.getPlayer().getUniqueId())) {
                        // verify if exist a packet in the list with the same type
                        List<PacketContainer> list = packets.get(event.getPlayer().getUniqueId());
                        list.removeIf(packetContainerT -> packetContainerT.getType() == packet.getType());

                        list.add(packet);
                        packets.put(event.getPlayer().getUniqueId(), list);
                    } else {
                        List<PacketContainer> list = new ArrayList<>();
                        list.add(packet);
                        packets.put(event.getPlayer().getUniqueId(), list);
                    }
                    event.setCancelled(true);
                } else {

                    event.setPacket(packet);
                }

            }
        }
    };

    public static PacketAdapter MERCHANT = new PacketAdapter(realmcraft.getInstance(), ListenerPriority.HIGHEST,
            PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
        @Override
        public void onPacketSending(com.comphenix.protocol.events.PacketEvent event) {

            LoreParser parser = new LoreParser(event.getPlayer());
            PacketContainer packet = event.getPacket().deepClone();

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
            }
            event.setPacket(packet);
        }
    };

    private static Map<UUID, List<PacketContainer>> packets = new HashMap<>();

    private static boolean PACKET_CUSTOM_SENDER = false;

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
        PACKET_CUSTOM_SENDER = realmcraft.getInteractiveConfig().getBoolean("module.packet_custom_sender", true);
        ProtocolLibrary.getProtocolManager().addPacketListener(LORE_UPDATE);
        ProtocolLibrary.getProtocolManager().addPacketListener(MERCHANT);

        if (PACKET_CUSTOM_SENDER) {
            // create a scheduler to send one packet per 5 ticks
            realmcraft.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(realmcraft.getInstance(),
                    () -> {

                        if (packets.isEmpty()) {
                            return;
                        }

                        Map<UUID, List<PacketContainer>> ClonePackets = new HashMap<>(packets);
                        ClonePackets.putAll(packets);

                        for (Map.Entry<UUID, List<PacketContainer>> entry : ClonePackets.entrySet()) {
                            UUID uuid = entry.getKey();
                            List<PacketContainer> packetsList = new ArrayList<>(entry.getValue());
                            packetsList.addAll(entry.getValue());
                            if (realmcraft.getInstance().getServer().getPlayer(uuid) == null) {
                                continue;
                            }
                            if (realmcraft.getInstance().getServer().getPlayer(uuid)
                                    .getGameMode() == GameMode.SPECTATOR) {
                                continue;
                            }
                            for (PacketContainer packet : packetsList) {
                                try {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(
                                            realmcraft.getInstance().getServer().getPlayer(uuid), packet);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        packets.clear();

                    }, 0, 5);
        }

    }

    public static void unregisterListener() {
        ProtocolLibrary.getProtocolManager().removePacketListener(LORE_UPDATE);
        ProtocolLibrary.getProtocolManager().removePacketListener(MERCHANT);
    }

}
