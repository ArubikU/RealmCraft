package dev.arubik.realmcraft.Api;

import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

public class RealProtocol {

    public abstract class InnerRealProtocol {

    }

    // Fast and No class implementation of PacketEvents
    public static class PacketAdapter extends PacketListenerAbstract {
        Set<PacketTypeCommon> packets;
        JavaPlugin identifier;

        public PacketAdapter(JavaPlugin plugin, PacketTypeCommon... packets) {
            super(PacketListenerPriority.NORMAL);
            this.identifier = plugin;
            this.packets = Set.of(packets);
        }

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (packets.contains(event.getPacketType())) {
                this.onPacketReceiving(event);
            }
        }

        public void onPacketReceiving(PacketReceiveEvent event) {
        };

        @Override
        public void onPacketSend(PacketSendEvent event) {
            if (packets.contains(event.getPacketType())) {
                this.onPacketSending(event);
            }
        }

        public void onPacketSending(PacketSendEvent event) {
        };

        public PacketAdapter(JavaPlugin plugin, PacketListenerPriority priority, PacketTypeCommon... packets) {
            super(priority);
            this.identifier = plugin;
            this.packets = Set.of(packets);
        }

        public void register() {
            PacketEvents.getAPI().getEventManager().registerListener(this);
        }

        public void unregister() {
            PacketEvents.getAPI().getEventManager().unregisterListener(this);
        }

    }
}
