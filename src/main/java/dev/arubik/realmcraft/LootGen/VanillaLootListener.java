package dev.arubik.realmcraft.LootGen;

import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.datafixers.util.Pair;
import com.pseudoforce.PlayerBiomes.FetchBiome;

import dev.arubik.realmcraft.Handlers.RealMessage;
import net.minecraft.resources.ResourceLocation;

public class VanillaLootListener {
    static Method getBiomeKey;

    static {

        // SETUP ON FIRST LOAD?

        Class<FetchBiome> fetchBiome = FetchBiome.class;
        // set public getBiomeKey
        try {
            getBiomeKey = fetchBiome.getDeclaredMethod("getBiomeKey", Location.class);
            getBiomeKey.setAccessible(true);
            RealMessage.sendConsoleMessage("Reflection: FetchBiome.getBiomeKey(Location) method found.");

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Pair<String, String> getBiomeKey(Location location) {
        try {
            ResourceLocation biomeKey = (ResourceLocation) getBiomeKey.invoke(null, location);
            return Pair.of(biomeKey.getNamespace(), biomeKey.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<>("minecraft", "plains");
    }

    public static List<ItemStack> verifyLoot(List<ItemStack> loot, LootGenerateEvent event) {
        Location location;

        if (event.getInventoryHolder() instanceof Container container) {
            location = container.getLocation();
        } else {
            location = event.getEntity().getLocation();
        }
        Pair<String, String> biome = getBiomeKey(location);
        return loot;
    }
}
