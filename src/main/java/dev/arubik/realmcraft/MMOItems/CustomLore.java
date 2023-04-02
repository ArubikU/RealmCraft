package dev.arubik.realmcraft.MMOItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.willfp.eco.libs.mongodb.internal.connection.DescriptionHelper;
import com.willfp.ecoenchants.EcoEnchantsPlugin;
import com.willfp.ecoenchants.display.DisplayableEnchant;
import com.willfp.ecoenchants.display.EnchantDisplay;
import com.willfp.ecoenchants.display.EnchantSorter;
import com.willfp.ecoenchants.display.EnchantmentFormattingKt;
import com.willfp.ecoenchants.enchants.DescriptionPlaceholder;
import com.willfp.ecoenchants.enchants.EcoEnchant;
import com.willfp.ecoenchants.enchants.EcoEnchantLevel;
import com.willfp.ecoenchants.enchants.EcoEnchantLike;
import com.willfp.ecoenchants.enchants.VanillaEcoEnchantLike;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.DynamicLoreLine;
import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.LorePosition;
import dev.arubik.realmcraft.Api.RealLore;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.MMOItems.Durability.PlaceholderEnableLore;
import dev.arubik.realmcraft.Managers.Depend;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CustomLore implements ItemBuildModifier {

    EnchantDisplay display;

    @Override
    public Boolean able(ItemStack item) {

        List<String> Whitelist = realmcraft.getInteractiveConfig().getStringList("lore-whitelist", RealNBT.EmptyList());
        List<String> Blacklist = realmcraft.getInteractiveConfig().getStringList("lore-blacklist", RealNBT.EmptyList());
        List<String> NBTBlacklist = realmcraft.getInteractiveConfig().getStringList("nbt-blacklist",
                RealNBT.EmptyList());
        RealNBT nbt = new RealNBT(item);
        for (String NBTBlacklistLine : NBTBlacklist) {
            if (nbt.hasTag(NBTBlacklistLine)) {
                return false;
            }
        }
        for (String s : Blacklist) {
            if (nbt.getDisplayName().contains(s)) {
                return false;
            }
        }
        if (!Whitelist.isEmpty()) {
            for (String s : Whitelist) {
                if (nbt.getDisplayName().contains(s)) {
                    return !nbt.contains("NO_TOOLTIP");
                }
            }
            return false;
        }

        return !nbt.contains("NO_TOOLTIP");
    }

    @Override
    public ItemStack modifyItem(Player player, ItemStack item) {
        RealNBT nbt = new RealNBT(item);
        int debug = 0;
        // RealMessage.sendRaw(player, debug++ + " ");
        // get player viewing container
        if (player.getOpenInventory() != null) {
            if (player.getOpenInventory().getTopInventory() != null) {
                // get name of container
                InventoryView a = player.getOpenInventory();
                String tittle = a.getTitle();
                List<String> tittleBlacklist = realmcraft.getInteractiveConfig().getStringList("tittle-blacklist",
                        RealNBT.EmptyList());
                for (String tittleBlacklistLine : tittleBlacklist) {
                    if (tittle.contains(tittleBlacklistLine)) {
                        return item;
                    }
                }

            }
        }

        // RealMessage.sendRaw(player, debug++ + " ");
        if (nbt.hasDisplayName() && nbt.hasLore()) {
            if (nbt.hasEnchantments() && realmcraft.getInteractiveConfig().getBoolean("show-enchants", true)) {
                if (!item.getItemMeta().getItemFlags().contains(ItemFlag.HIDE_ENCHANTS)) {
                    item = getEnchantments(nbt, player);
                    nbt = new RealNBT(item).addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
            // RealMessage.sendRaw(player, debug++ + " ");
            nbt = nbt.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS);
            List<String> lore = nbt.getLore();
            String name = nbt.getDisplayName();

            String LoreSuffix = realmcraft.getMinecraftLang().getString("lore.suffix", "<white>");
            String LorePrefix = realmcraft.getMinecraftLang().getString("lore.prefix", "<white>");
            String LoreBottomPrefix = realmcraft.getMinecraftLang().getString("lore.prefix-bottom", "<white>");
            String LoreBottomSuffix = realmcraft.getMinecraftLang().getString("lore.suffix-bottom", "<white>");
            String NameOutside = realmcraft.getMinecraftLang().getString("name.outside", "<white>");

            // RealMessage.sendRaw(player, debug++ + " ");
            if (!nbt.hasTag("MMOITEMS_TIER")) {
                if (!nbt.hasTag("EPIC_TOOLTIP")) {
                    LoreSuffix = realmcraft.getMinecraftLang().getString("lore.simple-suffix", "<white>");
                    LorePrefix = realmcraft.getMinecraftLang().getString("lore.simple-prefix", "<white>");
                    LoreBottomPrefix = realmcraft.getMinecraftLang().getString("lore.simple-prefix-bottom", "<white>");
                    LoreBottomSuffix = realmcraft.getMinecraftLang().getString("lore.simple-suffix-bottom", "<white>");
                    NameOutside = realmcraft.getMinecraftLang().getString("name.simple-outside", "<white>");
                }
            }
            // RealMessage.sendRaw(player, debug++ + " ");
            if (nbt.hasTag("FORCE_SIMPLE_TOOLTIP")) {
                LoreSuffix = realmcraft.getMinecraftLang().getString("lore.simple-suffix", "<white>");
                LorePrefix = realmcraft.getMinecraftLang().getString("lore.simple-prefix", "<white>");
                LoreBottomPrefix = realmcraft.getMinecraftLang().getString("lore.simple-prefix-bottom", "<white>");
                LoreBottomSuffix = realmcraft.getMinecraftLang().getString("lore.simple-suffix-bottom", "<white>");
                NameOutside = realmcraft.getMinecraftLang().getString("name.simple-outside", "<white>");
            }
            // RealMessage.sendRaw(player, debug++ + " ");

            List<String> NewLore = new ArrayList<String>();

            // RealMessage.sendRaw(player, debug++ + " ");
            String color = getTierColor(nbt);
            LoreSuffix = LoreSuffix.replace("<tier-color>", color);
            LorePrefix = LorePrefix.replace("<tier-color>", color);
            LoreBottomPrefix = LoreBottomPrefix.replace("<tier-color>", color);
            LoreBottomSuffix = LoreBottomSuffix.replace("<tier-color>", color);
            NameOutside = NameOutside.replace("<tier-color>", color);

            // RealMessage.sendRaw(player, debug++ + " ");
            // get last line of lore
            String lastLine = lore.get(lore.size() - 1);
            String removedColor = removeColorCodes(lastLine);
            removedColor = removedColor.replace(" ", "");
            // if last lines is with content, add a new line
            // //RealMessage.sendRaw(player,"\"" + removedColor + "\"");
            // RealMessage.sendRaw(player, debug++ + " ");
            if (removedColor.length() > 0
                    && !removedColor.equalsIgnoreCase(" ") && !removedColor.equalsIgnoreCase("")) {
                lore.add("");
            }
            // RealMessage.sendRaw(player, debug++ + " ");

            for (int i = 0; i < lore.size(); i++) {
                // verify if is the last line
                if (i == lore.size() - 1) {

                    NewLore.add(LoreBottomPrefix + lore.get(i) + LoreBottomSuffix);
                } else {
                    NewLore.add(LorePrefix + lore.get(i) + LoreSuffix);
                }
            }
            // RealMessage.sendRaw(player, debug++ + " ");
            String newName = NameOutside.replace("<original>", name);
            // RealMessage.sendRaw(player, "SetDisplayName: " + newName);
            nbt.setDisplayName(newName);
            // RealMessage.sendRaw(player, "SetLore ");
            nbt.setLore(NewLore);
            // RealMessage.sendRaw(player, debug++ + " ");
            // remove from ram newName, NewLore , LorePrefix, LoreSuffix, LoreBottomPrefix,
            // LoreBottomSuffix, NameOutside, lore, name
            lore = null;
            name = null;
            LorePrefix = null;
            LoreSuffix = null;
            LoreBottomPrefix = null;
            LoreBottomSuffix = null;
            NameOutside = null;
            NewLore = null;
            newName = null;

            // RealMessage.sendRaw(player, debug++ + " ");
            return item;

        }

        return item;
    }

    public static void register() {
        LoreEvent.addItemBuildModifier(new CustomLore());
    }

    public ItemStack getEnchantments(RealNBT rnb, Player player) {

        Map<DisplayableEnchant, String> enchants = new HashMap<DisplayableEnchant, String>();
        ItemStack item = rnb.getItemStack();

        List<String> notMeetLines = new ArrayList<String>();

        List<Enchantment> sorted = EnchantSorter.INSTANCE.sortForDisplay(item.getEnchantments().keySet());
        String colorLevel = realmcraft.getMinecraftLang().getString("enchant.level-color", "<gray>");
        for (Enchantment enchant : sorted) {
            if (enchant instanceof EcoEnchant ecoench) {
                EcoEnchantLevel lev = ecoench.getLevel(item.getEnchantments().get(enchant));
                notMeetLines = (lev.getNotMetLines(player));

                enchants.put(new DisplayableEnchant(ecoench, item.getEnchantments().get(enchant)),
                        ecoench.getType().getFormat() + ecoench.getDisplayName() + " " + colorLevel
                                + formatToRoman(item.getEnchantments().get(enchant)));
            } else {
                VanillaEcoEnchantLike ench = new VanillaEcoEnchantLike(enchant, EcoEnchantsPlugin.getInstance());
                enchants.put(new DisplayableEnchant(ench, item.getEnchantments().get(enchant)),
                        "&7" + ench.getDisplayName() + " " + colorLevel
                                + formatToRoman(item.getEnchantments().get(enchant)));
            }
        }

        List<String> EnchantLore = new ArrayList<String>();

        Boolean shouldColapse = realmcraft.getInteractiveConfig().getBoolean("collapse-enchantments", true);
        int shouldCOlapsePerLine = realmcraft.getInteractiveConfig().getInteger("collapse-enchantments-per-line",
                3);

        Boolean forceColapse = realmcraft.getInteractiveConfig().getBoolean("force-collapse-enchantments", false);
        Boolean shouldDescribe = realmcraft.getInteractiveConfig().getBoolean("describe-enchantments", true);
        if ((shouldColapse && enchants.size() > shouldCOlapsePerLine) || forceColapse) {
            String delimiter = realmcraft.getInteractiveConfig().getString("collapse-enchantments-delimiter",
                    "<gray>, ");
            // create packs of 3 enchants per line
            List<List<String>> enchantsPerLine = new ArrayList<List<String>>();
            List<String> enchantsPerLineTemp = new ArrayList<String>();
            int i = 0;
            for (DisplayableEnchant enchant : enchants.keySet()) {
                if (i == shouldCOlapsePerLine) {
                    enchantsPerLine.add(enchantsPerLineTemp);
                    enchantsPerLineTemp = new ArrayList<String>();
                    i = 0;
                }
                enchantsPerLineTemp.add(enchants.get(enchant));
                i++;
            }
            enchantsPerLine.add(enchantsPerLineTemp);

            // create the lore

            String prefix = realmcraft.getMinecraftLang().getString("lore.enchantments-prefix", "  ");
            for (List<String> enchantsPerLineTemp2 : enchantsPerLine) {
                EnchantLore.add(prefix + String.join(delimiter, enchantsPerLineTemp2));
            }
        } else {
            int maxLenght = realmcraft.getInteractiveConfig().getInteger("max-lenght-per-line", 30);
            for (DisplayableEnchant enchant : enchants.keySet()) {
                String prefix = realmcraft.getMinecraftLang().getString("lore.enchantments-prefix", "  ");
                EnchantLore.add(prefix + enchants.get(enchant));
                if (shouldDescribe) {
                    List<String> tempdesc = EnchantmentFormattingKt.getFormattedDescription(enchant.getEnchant(),
                            item.getEnchantmentLevel(enchant.getEnchant().getEnchant()));
                    String description = String.join(" ", tempdesc);
                    description = description.replace(LegacyComponentSerializer.SECTION_CHAR, '&');
                    String descriptionwithoutcolor = removeColorCodes(description);

                    // split the description if it is too long
                    if (descriptionwithoutcolor.length() > maxLenght) {
                        String[] words = description.replace("&r", "<dark_gray>").split(" ");
                        List<String> lines = new ArrayList<String>();
                        String line = "<dark_gray>";
                        for (String word : words) {
                            String wordwithoutcolor = removeColorCodes(word);
                            String lineWithoutColor = removeColorCodes(line);

                            if (lineWithoutColor.length() + wordwithoutcolor.length() > maxLenght) {
                                lines.add(line);
                                line = "<dark_gray>";
                            }
                            line += "<dark_gray>" + word + " <dark_gray>";
                        }
                        line = line.replace("&r", "&8");
                        line = line.replace("&8", "<dark_gray>");
                        lines.add(line);
                        // add "<gray>" to the start of each line
                        for (int i = 0; i < lines.size(); i++) {
                            lines.set(i, "<dark_gray>" + lines.get(i));
                        }
                        EnchantLore.addAll(lines);
                    } else {
                        EnchantLore.add(description);
                    }

                }
            }
        }

        List<String> addLinesTopEnchants = realmcraft.getInteractiveConfig().getStringList("add-lines-top-enchants",
                new ArrayList<String>());
        addLinesTopEnchants
                .replaceAll(line -> line.replace("#max#", "7").replace("#current#", enchants.size() + ""));
        if (addLinesTopEnchants.size() > 0) {
            EnchantLore.addAll(0, addLinesTopEnchants);
        }

        List<String> lore = rnb.getLore();
        if (notMeetLines.size() > 0) {
            lore.add("");
            lore.addAll(notMeetLines);
        }

        // Replace old minecraft format with minimessage like &7 to <gray>
        for (int i = 0; i < EnchantLore.size(); i++) {
            String line = EnchantLore.get(i);
            line = formatToMini(line);
            EnchantLore.set(i, line);
        }

        String position = realmcraft.getInteractiveConfig().getString("enchantments-position", "top");
        if (position.equalsIgnoreCase("top")) {
            EnchantLore.addAll(lore);
            rnb.setLore(EnchantLore);
        } else {
            // verify if is a number
            try {
                int pos = Integer.parseInt(position);
                if (pos < 0) {
                    pos = 0;
                }
                if (pos > lore.size()) {
                    pos = lore.size();
                }
                lore.remove(pos);
                lore.addAll(pos, EnchantLore);
            } catch (NumberFormatException e) {
                lore.addAll(EnchantLore);
            }
            rnb.setLore(lore);
        }

        return rnb.getItemStack();

    }

    public String formatToMini(String line) {

        line = line.replace("&0", "<black>");
        line = line.replace("&1", "<dark_blue>");
        line = line.replace("&2", "<dark_green>");
        line = line.replace("&3", "<dark_aqua>");
        line = line.replace("&4", "<dark_red>");
        line = line.replace("&5", "<dark_purple>");
        line = line.replace("&6", "<gold>");
        line = line.replace("&7", "<gray>");
        line = line.replace("&8", "<dark_gray>");
        line = line.replace("&9", "<blue>");
        line = line.replace("&a", "<green>");
        line = line.replace("&b", "<aqua>");
        line = line.replace("&c", "<red>");
        line = line.replace("&d", "<light_purple>");
        line = line.replace("&e", "<yellow>");
        line = line.replace("&f", "<white>");
        line = line.replace("&k", "<obfuscated>");
        line = line.replace("&l", "<bold>");
        line = line.replace("&m", "<strikethrough>");
        line = line.replace("&n", "<underline>");
        line = line.replace("&o", "<italic>");
        line = line.replace("&r", "<reset>");
        return line;
    }

    public String removeColorCodes(String line) {
        line = formatToMini(line);
        line = line.replace("<black>", "");
        line = line.replace("<dark_blue>", "");
        line = line.replace("<dark_green>", "");
        line = line.replace("<dark_aqua>", "");
        line = line.replace("<dark_red>", "");
        line = line.replace("<dark_purple>", "");
        line = line.replace("<gold>", "");
        line = line.replace("<gray>", "");
        line = line.replace("<dark_gray>", "");
        line = line.replace("<blue>", "");
        line = line.replace("<green>", "");
        line = line.replace("<aqua>", "");
        line = line.replace("<red>", "");
        line = line.replace("<light_purple>", "");
        line = line.replace("<yellow>", "");
        line = line.replace("<white>", "");
        line = line.replace("<obfuscated>", "");
        line = line.replace("<bold>", "");
        line = line.replace("<strikethrough>", "");
        line = line.replace("<underline>", "");
        line = line.replace("<italic>", "");
        line = line.replace("<reset>", "");

        return line;

    }

    public static String getTierColor(RealNBT item) {
        if (item.contains("MMOITEMS_TIER")) {
            String tier = item.getString("MMOITEMS_TIER");

            if (realmcraft.getInteractiveConfig().has("colorcode." + tier)) {
                return realmcraft.getInteractiveConfig().getString(tier, "<#ffffff>");
            }

            if (tier.equalsIgnoreCase("TRASH"))
                return "<#808080>";
            if (tier.equalsIgnoreCase("COMMON"))
                return "<#808080>";
            if (tier.equalsIgnoreCase("UNCOMMON"))
                return "<#808080>";
            if (tier.equalsIgnoreCase("RARE"))
                return "<gold>";
            if (tier.equalsIgnoreCase("VERY_RARE"))
                return "<yellow>";
            if (tier.equalsIgnoreCase("LEGENDARY"))
                return "<aqua>";
            if (tier.equalsIgnoreCase("MYTHICAL"))
                return "<dark_purple>";
            if (tier.equalsIgnoreCase("EPIC"))
                return "<red>";
            if (tier.equalsIgnoreCase("MAGICAL"))
                return "<dark_green>";
        }

        if (item.contains("COLOR_CODE")) {
            return item.getString("COLOR_CODE");
        }

        return realmcraft.getMinecraftLang().getString("default_color", "<#ffffff>");
    }

    public String formatToRoman(int i) {

        if (realmcraft.getMinecraftLang().has("enchants.roman." + i)) {
            return realmcraft.getMinecraftLang().getString("enchants.roman." + i);
        }

        if (i == 1)
            return "I";
        if (i == 2)
            return "II";
        if (i == 3)
            return "III";
        if (i == 4)
            return "IV";
        if (i == 5)
            return "V";
        if (i == 6)
            return "VI";
        if (i == 7)
            return "VII";
        if (i == 8)
            return "VIII";
        if (i == 9)
            return "IX";
        if (i == 10)
            return "X";
        if (i == 11)
            return "XI";
        if (i == 12)
            return "XII";
        if (i == 13)
            return "XIII";
        if (i == 14)
            return "XIV";
        if (i == 15)
            return "XV";
        if (i == 16)
            return "XVI";
        if (i == 17)
            return "XVII";
        if (i == 18)
            return "XVIII";
        if (i == 19)
            return "XIX";
        if (i == 20)
            return "XX";
        if (i == 21)
            return "XXI";
        if (i == 22)
            return "XXII";
        if (i == 23)
            return "XXIII";
        if (i == 24)
            return "XXIV";
        if (i == 25)
            return "XXV";
        if (i == 26)
            return "XXVI";
        if (i == 27)
            return "XXVII";
        if (i == 28)
            return "XXVIII";
        if (i == 29)
            return "XXIX";
        if (i == 30)
            return "XXX";
        if (i == 31)
            return "XXXI";
        if (i == 32)
            return "XXXII";
        if (i == 33)
            return "XXXIII";
        if (i == 34)
            return "XXXIV";
        if (i == 35)
            return "XXXV";
        if (i == 36)
            return "XXXVI";
        if (i == 37)
            return "XXXVII";
        if (i == 38)
            return "XXXVIII";
        if (i == 39)
            return "XXXIX";
        if (i == 40)
            return "XL";
        if (i == 41)
            return "XLI";
        if (i == 42)
            return "XLII";
        if (i == 43)
            return "XLIII";
        if (i == 44)
            return "XLIV";
        if (i == 45)
            return "XLV";
        if (i == 46)
            return "XLVI";
        if (i == 47)
            return "XLVII";
        if (i == 48)
            return "XLVIII";
        if (i == 49)
            return "XLIX";
        if (i == 50)
            return "L";
        return i + "";
    }

}
