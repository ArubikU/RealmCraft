package dev.arubik.realmcraft.IReplacer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealCache.RealCacheMap;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import dev.arubik.realmcraft.FileManagement.InteractiveFile.FileType;
import dev.arubik.realmcraft.Handlers.JsonBuilder;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import io.lumine.mythic.bukkit.utils.gson.JsonBuilder.JsonArrayBuilder;
import io.lumine.mythic.lib.gson.JsonArray;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.craftbukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class IReplacer {
    private InteractiveFolder folder;
    @Getter
    private static Map<String, InternalReplacerStructure> replacers = new HashMap<String, InternalReplacerStructure>();

    public IReplacer(Plugin plugin) {
        folder = new InteractiveFolder("ireplacer", plugin);
    }

    public static String getName(RealNBT item) {
        // RealMessage.sendRaw(
        // item.getItemMeta().getDisplayName().replaceAll(LegacyComponentSerializer.SECTION_CHAR
        // + "", "&"));
        return item.getItemMeta().getDisplayName().replaceAll(LegacyComponentSerializer.SECTION_CHAR + "", "&");
        // try {
        //
        // String a = item.getItemMeta().getDisplayName();
        // if
        // (item.getItemMeta().getDisplayName().contains(LegacyComponentSerializer.SECTION_CHAR
        // + "")) {
        // a = a.replaceAll(LegacyComponentSerializer.SECTION_CHAR + "", "&");
        // // a = LegacyComponentSerializer.legacyAmpersand()
        // // .serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(a));
        // } else {
        // a = LegacyComponentSerializer.legacySection()
        // .serialize(LegacyComponentSerializer.legacySection().deserialize(a));
        // }
        // return a;
        // } catch
        // (net.kyori.adventure.text.minimessage.internal.parser.ParsingExceptionImpl e)
        // {
        // return item.getItemMeta().getDisplayName();
        // }
    }

    public void setup() {
        replacers.clear();
        IReplacerListener.clearCache();
        for (InteractiveFile file : folder.getFiles().values()) {
            if (file.getType() == FileType.UNKNOWN) {
                continue;
            }
            for (String key : file.getKeys()) {
                InternalReplacerStructure structure = new InternalReplacerStructure();
                InteractiveSection section = new InteractiveSection(file, key);
                section.has("Replacement-Type", () -> {
                    structure.type = ReplacementTypes.fromString(section.getOrDefault("Replacement-Type", "NAME"));
                });
                section.has("Output-Type", () -> {
                    structure.outputType = OutputTypes.fromString(section.getOrDefault("Output-Type", "VANILLA"));
                });
                section.has("Type-Config", () -> {
                    for (String keyA : section.getSection("Type-Config").getKeys()) {
                        if (keyA.equals("Lore")) {
                            List<String> lore = section.getStringList("Type-Config.Lore");
                            com.google.gson.JsonArray array = new com.google.gson.JsonArray();
                            for (String s : lore) {
                                array.add(s);
                            }
                            structure.TypeConfig.add(key, array);
                        } else {

                            structure.TypeConfig.addProperty(keyA,
                                    section.get("Type-Config." + keyA).toString());
                        }
                    }
                });
                section.has("Output-Config", () -> {
                    for (String keyA : section.getSection("Output-Config").getKeys()) {
                        if (keyA.equals("Lore")) {
                            List<String> lore = section.getStringList("Output-Config.Lore");
                            com.google.gson.JsonArray array = new com.google.gson.JsonArray();
                            for (String s : lore) {
                                array.add(s);
                            }
                            structure.outputConfig.add(keyA, array);
                        } else {
                            structure.outputConfig.addProperty(keyA,
                                    section.get("Output-Config." + keyA).toString());
                        }
                    }
                });
                section.has("Ignore-NBT", () -> {
                    structure.IgnoreNBT = section.getStringList("Ignore-NBT");
                });
                // dump data of thw TypeConfig and outputConfig
                RealMessage.sendConsoleMessage(DebugType.IREPLACER, "TypeConfig: " + structure.TypeConfig.toString());
                RealMessage.sendConsoleMessage(DebugType.IREPLACER,
                        "OutputConfig: " + structure.outputConfig.toString());
                structure.key = key;
                structure.section = section;
                replacers.put(key, structure);
            }
        }
        // sort map puting ReplacementTypes.VANILLA at the end
        Map<String, InternalReplacerStructure> temp = new HashMap<String, InternalReplacerStructure>();
        Map<String, InternalReplacerStructure> temp2 = new HashMap<String, InternalReplacerStructure>();
        for (String key : replacers.keySet()) {
            if (replacers.get(key).type == ReplacementTypes.VANILLA) {
                temp2.put(key, replacers.get(key));
            } else {
                temp.put(key, replacers.get(key));
            }
        }
        temp.putAll(temp2);
        replacers = temp;

    }
}
