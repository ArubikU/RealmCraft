package dev.arubik.realmcraft.Api.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.recipe.data.MerchantOffer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.google.common.collect.Lists;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.LoreParser;
import dev.arubik.realmcraft.Api.RealLore;
import dev.arubik.realmcraft.Api.RealProtocol.PacketAdapter;
import dev.arubik.realmcraft.Api.Listeners.ChangeGamemode;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
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

    public static PacketAdapter LORE_UPDATE = new PacketAdapter(realmcraft.getInstance(),
            PacketListenerPriority.HIGHEST,

            PacketType.Play.Server.SET_SLOT,
            PacketType.Play.Server.WINDOW_ITEMS) {

        @Override
        public void onPacketSending(PacketSendEvent event) {
            Player player = (Player) event.getPlayer();
            LoreParser parser = new LoreParser(player);

            if (ChangeGamemode.lastMined.containsKey(event.getUser().getUUID()) && LAST_MINE) {
                if (Bukkit.getCurrentTick() - ChangeGamemode.lastMined.get(event.getUser().getUUID()) < 4) {
                    return;
                }
            }

            if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
                WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
                List<ItemStack> items = new ArrayList<>();
                packet.getItems().forEach(item -> items.add(SpigotConversionUtil.toBukkitItemStack(item)));
                List<com.github.retrooper.packetevents.protocol.item.ItemStack> items3 = new ArrayList<>();
                for (ItemStack item : parser.f1.apply(items)) {
                    items3.add(SpigotConversionUtil.fromBukkitItemStack(item));
                }
                packet.setItems(items3);
                event.setLastUsedWrapper(packet);
            }

            if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);
                boolean isPlayerInventory = packet.getWindowId() == 0;
                if (PACKET_CUSTOM_SENDER && isPlayerInventory) {
                    event.setCancelled(true);

                    // put to the last item in the packets map <UUID, List<PacketWrapper>>
                    if (packets.containsKey(event.getUser().getUUID())) {
                        // verify if exist a packet in the list with the same type
                        List<PacketWrapper> list = packets.get(event.getUser().getUUID());
                        list.add(packet);
                        packets.put(event.getUser().getUUID(), list);
                    } else {
                        List<PacketWrapper> list = new ArrayList<>();
                        list.add(packet);
                        packets.put(event.getUser().getUUID(), list);
                    }
                } else {
                    packet.setItem(SpigotConversionUtil.fromBukkitItemStack(
                            parser.f.apply(SpigotConversionUtil.toBukkitItemStack(packet.getItem()))));
                    event.setLastUsedWrapper(packet);
                }

            }
        }
    };

    public static PacketAdapter MERCHANT = new PacketAdapter(realmcraft.getInstance(), PacketListenerPriority.HIGHEST,
            PacketType.Play.Server.MERCHANT_OFFERS) {
        @Override
        public void onPacketSending(PacketSendEvent event) {

            LoreParser parser = new LoreParser((Player) event.getPlayer());

            if (realmcraft.getInteractiveConfig().getBoolean("module.merchant_packet", false)) {
                WrapperPlayServerMerchantOffers packet = new WrapperPlayServerMerchantOffers(event);

                List<MerchantOffer> recipeList = packet.getMerchantOffers();
                for (int i = 0; i < recipeList.size(); i++) {
                    MerchantOffer recipe = recipeList.get(i);
                    {
                        ItemStack output = SpigotConversionUtil.toBukkitItemStack(recipe.getOutputItem());
                        if (output.getType() == Material.ENCHANTED_BOOK) {
                            continue;
                        }
                        output = parser.forceApply(output);
                        recipe.setOutputItem(SpigotConversionUtil.fromBukkitItemStack(output));
                    }
                    {
                        // first ingredient
                        ItemStack first = SpigotConversionUtil.toBukkitItemStack(recipe.getFirstInputItem());
                        first = parser.forceApply(first);
                        recipe.setOutputItem(SpigotConversionUtil.fromBukkitItemStack(first));

                    }
                    {
                        // first ingredient
                        if (!recipe.getSecondInputItem().isEmpty()) {

                            ItemStack second = SpigotConversionUtil.toBukkitItemStack(recipe.getSecondInputItem());
                            second = parser.forceApply(second);
                            recipe.setOutputItem(SpigotConversionUtil.fromBukkitItemStack(second));
                        }

                    }
                    recipeList.set(i, recipe);

                }
                packet.setMerchantOffers(recipeList);
                event.setLastUsedWrapper(packet);
            }
        }
    };

    private static Map<UUID, List<PacketWrapper>> packets = new HashMap<>();

    public static void clearPackets(UUID uuid) {
        if (packets.containsKey(uuid)) {
            RealMessage.sendRaw("Clearing packets for " + Bukkit.getPlayer(uuid).getName() + " (" + uuid + ")");
            packets.remove(uuid);
        }
    }

    private static boolean PACKET_CUSTOM_SENDER = false;
    private static boolean LAST_MINE = false;

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "ProtocolLib", "MMOItems" };
    }

    public static void registerListener() {
        if (!Depend.isPluginEnabled(new LoreEvent())) {
            RealMessage.nonFound("ProtocolLib or MMOItems is not installed, so LoreEvent will not work.");
            return;
        }
        PACKET_CUSTOM_SENDER = realmcraft.getInteractiveConfig().getBoolean("module.packet_custom_sender", true);
        LAST_MINE = realmcraft.getInteractiveConfig().getBoolean("module.last_mine", true);
        LORE_UPDATE.register();
        MERCHANT.register();

        if (PACKET_CUSTOM_SENDER) {
            // create a scheduler to send one packet per 5 ticks
            realmcraft.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(realmcraft.getInstance(),
                    () -> {
                        sendPackets();

                    }, 0, 4);
        }

    }

    public static void sendPackets() {

        // TODO
        /*
         * if (!PACKET_CUSTOM_SENDER)
         * return;
         * if (packets.isEmpty()) {
         * return;
         * }
         * 
         * Map<UUID, List<PacketWrapper>> ClonePackets = new HashMap<>(packets);
         * ClonePackets.putAll(packets);
         * 
         * for (UUID uuid : ClonePackets.keySet()) {
         * List<PacketWrapper> packetsList = new ArrayList<>();
         * packetsList.addAll(ClonePackets.get(uuid));
         * if (realmcraft.getInstance().getServer().getPlayer(uuid) == null) {
         * continue;
         * }
         * if (realmcraft.getInstance().getServer().getPlayer(uuid)
         * .getGameMode() == GameMode.SPECTATOR) {
         * continue;
         * }
         * if (ChangeGamemode.lastMined.containsKey(uuid)) {
         * if (Bukkit.getCurrentTick() - ChangeGamemode.lastMined.get(uuid) < 4) {
         * continue;
         * }
         * }
         * LoreParser parser = new
         * LoreParser(realmcraft.getInstance().getServer().getPlayer(uuid));
         * RealMessage.sendRaw("Sending " + packetsList.size() + " packets to "
         * + realmcraft.getInstance().getServer().getPlayer(uuid).getName() + " (" +
         * uuid + ")");
         * for (PacketWrapper packet : packetsList) {
         * try {
         * StructureModifier<ItemStack> sm = packet.getItemModifier();
         * 
         * try {
         * sm.modify(0, parser.f);
         * } catch (java.lang.NoSuchMethodError e) {
         * sm.write(0, parser.f.apply(sm.read(0)));
         * }
         * 
         * ProtocolLibrary.getProtocolManager().sendServerPacket(
         * realmcraft.getInstance().getServer().getPlayer(uuid), packet);
         * } catch (Exception e) {
         * e.printStackTrace();
         * }
         * }
         * }
         * packets.clear();
         */
    }

    public static void unregisterListener() {
        LORE_UPDATE.unregister();
        MERCHANT.unregister();
    }

}
