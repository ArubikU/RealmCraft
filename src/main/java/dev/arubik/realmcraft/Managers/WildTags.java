package dev.arubik.realmcraft.Managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealNBT.KeyedTag;

public class WildTags {

    public enum VanillaWildtags {

        ACACIA_LOGS(Tag.ACACIA_LOGS),
        ALL_HANGING_SIGNS(Tag.ALL_HANGING_SIGNS),
        ALL_SIGNS(Tag.ALL_SIGNS),
        ANCIENT_CITY_REPLACEABLE(Tag.ANCIENT_CITY_REPLACEABLE),
        ANIMALS_SPAWNABLE_ON(Tag.ANIMALS_SPAWNABLE_ON),
        ANVIL(Tag.ANVIL),
        AXOLOTL_TEMP_ITEMS(Tag.AXOLOTL_TEMPT_ITEMS),
        AXOLOTLS_SPAWNABLE_ON(Tag.AXOLOTLS_SPAWNABLE_ON),
        AZALEA_GROWS_ON(Tag.AZALEA_GROWS_ON),
        AZALEA_ROOT_REPLACEABLE(Tag.AZALEA_ROOT_REPLACEABLE),
        BAMBOO_BLOCKS(Tag.BAMBOO_BLOCKS),
        BAMBOO_PLANTABLE_ON(Tag.BAMBOO_PLANTABLE_ON),
        BANNERS(Tag.BANNERS),
        BASE_STONE_NETHER(Tag.BASE_STONE_NETHER),
        BASE_STONE_OVERWORLD(Tag.BASE_STONE_OVERWORLD),
        BEACON_BASE_BLOCKS(Tag.BEACON_BASE_BLOCKS),
        BEDS(Tag.BEDS),
        BEE_GROWABLES(Tag.BEE_GROWABLES),
        BEEHIVES(Tag.BEEHIVES),
        BIG_DRIPLEAF_PLACEABLE(Tag.BIG_DRIPLEAF_PLACEABLE),
        BIRCH_LOGS(Tag.BIRCH_LOGS),
        BUTTONS(Tag.BUTTONS),
        CAMEL_SAND_STEP_SOUND_BLOCKS(Tag.CAMEL_SAND_STEP_SOUND_BLOCKS),
        CAMPFIRES(Tag.CAMPFIRES),
        CANDLE_CAKES(Tag.CANDLE_CAKES),
        CANDLES(Tag.CANDLES),
        CARPETS(Tag.CARPETS),
        CAULDRONS(Tag.CAULDRONS),
        CAVE_VINES(Tag.CAVE_VINES),
        CEILING_HANGING_SIGNS(Tag.CEILING_HANGING_SIGNS),
        CHERRY_LOGS(Tag.CHERRY_LOGS),
        CLIMBABLE(Tag.CLIMBABLE),
        CLUSTER_MAX_HARVESTABLES(Tag.CLUSTER_MAX_HARVESTABLES),
        COAL_ORES(Tag.COAL_ORES),
        COMBINATION_STEP_SOUND_BLOCKS(Tag.COMBINATION_STEP_SOUND_BLOCKS),
        COMPLETES_FIND_TREE_TUTORIAL(Tag.COMPLETES_FIND_TREE_TUTORIAL),
        CONCRETE_POWDER(Tag.CONCRETE_POWDER),
        CONVERTABLE_TO_MUD(Tag.CONVERTABLE_TO_MUD),
        COPPER_ORES(Tag.COPPER_ORES),
        CORAL_BLOCKS(Tag.CORAL_BLOCKS),
        CORAL_PLANTS(Tag.CORAL_PLANTS),
        CORALS(Tag.CORALS),
        CRIMSON_STEMS(Tag.CRIMSON_STEMS),
        CROPS(Tag.CROPS),
        CRYSTAL_SOUND_BLOCKS(Tag.CRYSTAL_SOUND_BLOCKS),
        DAMPENS_VIBRATIONS(Tag.DAMPENS_VIBRATIONS),
        DARK_OAK_LOGS(Tag.DARK_OAK_LOGS),
        DEAD_BUSH_MAY_PLACE_ON(Tag.DEAD_BUSH_MAY_PLACE_ON),
        DEEPSLATE_ORE_REPLACEABLES(Tag.DEEPSLATE_ORE_REPLACEABLES),
        DIAMOND_ORES(Tag.DIAMOND_ORES),
        DIRT(Tag.DIRT),
        DOORS(Tag.DOORS),
        DRAGON_IMMUNE(Tag.DRAGON_IMMUNE),
        DRAGON_TRANSPARENT(Tag.DRAGON_TRANSPARENT),
        DRIPSTONE_REPLACEABLE(Tag.DRIPSTONE_REPLACEABLE),
        EMERALD_ORES(Tag.EMERALD_ORES),
        ENCHANTMENT_POWER_PROVIDER(Tag.ENCHANTMENT_POWER_PROVIDER),
        ENCHANTMENT_POWER_TRANSMITTER(Tag.ENCHANTMENT_POWER_TRANSMITTER),
        ENDERMAN_HOLDABLE(Tag.ENDERMAN_HOLDABLE),
        FALL_DAMAGE_RESETTING(Tag.FALL_DAMAGE_RESETTING),
        FEATURES_CANNOT_REPLACE(Tag.FEATURES_CANNOT_REPLACE),
        FENCE_GATES(Tag.FENCE_GATES),
        FENCES(Tag.FENCES),
        FIRE(Tag.FIRE),
        FLOWER_POTS(Tag.FLOWER_POTS),
        FLOWERS(Tag.FLOWERS),
        FOX_FOOD(Tag.FOX_FOOD),
        FOXES_SPAWNABLE_ON(Tag.FOXES_SPAWNABLE_ON),
        FREEZE_IMMUNE_WEARABLES(Tag.FREEZE_IMMUNE_WEARABLES),
        FROGS_PREFER_JUMP_TO(Tag.FROG_PREFER_JUMP_TO),
        FROGS_SPAWNABLE_ON(Tag.FROGS_SPAWNABLE_ON),
        GEODE_INVALID_BLOCKS(Tag.GEODE_INVALID_BLOCKS),
        GOATS_SPAWNABLE_ON(Tag.GOATS_SPAWNABLE_ON),
        GOLD_ORES(Tag.GOLD_ORES),
        GUARDED_BY_PIGLINS(Tag.GUARDED_BY_PIGLINS),
        HOGLIN_REPELLENTS(Tag.HOGLIN_REPELLENTS),
        ICE(Tag.ICE),
        IGNORED_BY_PIGLIN_BABIES(Tag.IGNORED_BY_PIGLIN_BABIES),
        IMPERMEABLE(Tag.IMPERMEABLE),
        INFINIBURN_END(Tag.INFINIBURN_END),
        INFINIBURN_NETHER(Tag.INFINIBURN_NETHER),
        INFINIBURN_OVERWORLD(Tag.INFINIBURN_OVERWORLD),
        INSIDE_STEP_SOUND_BLOCKS(Tag.INSIDE_STEP_SOUND_BLOCKS),
        INVALID_SPAWN_INSIDE(Tag.INVALID_SPAWN_INSIDE),
        IRON_ORES(Tag.IRON_ORES),
        ITEMS_ARROWS(Tag.ITEMS_ARROWS),
        ITEMS_AXES(Tag.ITEMS_AXES),
        ITEMS_BANNERS(Tag.ITEMS_BANNERS),
        ITEMS_BEACON_PAYMENT_ITEMS(Tag.ITEMS_BEACON_PAYMENT_ITEMS),
        ITEMS_BOATS(Tag.ITEMS_BOATS),
        ITEMS_BOOKSHELF_BOOKS(Tag.ITEMS_BOOKSHELF_BOOKS),
        ITEMS_BREAKS_DECORATED_POTS(Tag.ITEMS_BREAKS_DECORATED_POTS),
        ITEMS_CHEST_BOATS(Tag.ITEMS_CHEST_BOATS),
        ITEMS_COALS(Tag.ITEMS_COALS),
        ITEMS_COMPASSES(Tag.ITEMS_COMPASSES),
        ITEMS_CREEPER_DROP_MUSIC_DISCS(Tag.ITEMS_CREEPER_DROP_MUSIC_DISCS),
        ITEMS_CREEPER_IGNITERS(Tag.ITEMS_CREEPER_IGNITERS),
        ITEMS_DECORATED_POT_INGREDIENTS(Tag.ITEMS_DECORATED_POT_INGREDIENTS),
        ITEMS_DECORATED_POT_SHERDS(Tag.ITEMS_DECORATED_POT_SHERDS),
        ITEMS_FISHES(Tag.ITEMS_FISHES),
        ITEMS_FURNACE_MATERIALS(Tag.ITEMS_FURNACE_MATERIALS),
        ITEMS_HANGING_SIGNS(Tag.ITEMS_HANGING_SIGNS),
        ITEMS_HOES(Tag.ITEMS_HOES),
        ITEMS_LECTERN_BOOKS(Tag.ITEMS_LECTERN_BOOKS),
        ITEMS_MUSIC_DISCS(Tag.ITEMS_MUSIC_DISCS),
        ITEMS_NON_FLAMMABLE_WOOD(Tag.ITEMS_NON_FLAMMABLE_WOOD),
        ITEMS_NOTE_BLOCK_TOP_INSTRUMENTS(Tag.ITEMS_NOTE_BLOCK_TOP_INSTRUMENTS),
        ITEMS_PICKAXES(Tag.ITEMS_PICKAXES),
        ITEMS_PIGLIN_LOVED(Tag.ITEMS_PIGLIN_LOVED),
        ITEMS_SHOVELS(Tag.ITEMS_SHOVELS),
        ITEMS_SNIFFER_FOOD(Tag.ITEMS_SNIFFER_FOOD),
        ITEMS_STONE_TOOL_MATERIALS(Tag.ITEMS_STONE_TOOL_MATERIALS),
        ITEMS_SWORDS(Tag.ITEMS_SWORDS),
        ITEMS_TOOLS(Tag.ITEMS_TOOLS),
        ITEMS_TRIM_MATERIALS(Tag.ITEMS_TRIM_MATERIALS),
        ITEMS_TRIM_TEMPLATES(Tag.ITEMS_TRIM_TEMPLATES),
        ITEMS_TRIMMABLE_ARMOR(Tag.ITEMS_TRIMMABLE_ARMOR),
        ITEMS_VILLAGER_PLANTABLE_SEEDS(Tag.ITEMS_VILLAGER_PLANTABLE_SEEDS),
        JUNGLE_LOGS(Tag.JUNGLE_LOGS),
        LAPIS_ORES(Tag.LAPIS_ORES),
        LAVA_POOL_STONE_CANNOT_REPLACE(Tag.LAVA_POOL_STONE_CANNOT_REPLACE),
        LEAVES(Tag.LEAVES),
        LOGS(Tag.LOGS),
        LOGS_THAT_BURN(Tag.LOGS_THAT_BURN),
        LUSH_GROUND_REPLACEABLE(Tag.LUSH_GROUND_REPLACEABLE),
        MANGROVE_LOGS(Tag.MANGROVE_LOGS),
        MANGROVE_LOGS_CAN_GROW_THROUGH(Tag.MANGROVE_LOGS_CAN_GROW_THROUGH),
        MANGROVE_ROOTS_CAN_GROW_THROUGH(Tag.MANGROVE_ROOTS_CAN_GROW_THROUGH),
        MINEABLE_AXE(Tag.MINEABLE_AXE),
        MINEABLE_HOE(Tag.MINEABLE_HOE),
        MINEABLE_PICKAXE(Tag.MINEABLE_PICKAXE),
        MINEABLE_SHOVEL(Tag.MINEABLE_SHOVEL),
        MOOSHROOMS_SPAWNABLE_ON(Tag.MOOSHROOMS_SPAWNABLE_ON),
        MOSS_REPLACEABLE(Tag.MOSS_REPLACEABLE),
        MUSHROOM_GROW_BLOCK(Tag.MUSHROOM_GROW_BLOCK),
        NEEDS_DIAMOND_TOOL(Tag.NEEDS_DIAMOND_TOOL),
        NEEDS_IRON_TOOL(Tag.NEEDS_IRON_TOOL),
        NEEDS_STONE_TOOL(Tag.NEEDS_STONE_TOOL),
        NETHER_CARVER_REPLACEABLES(Tag.NETHER_CARVER_REPLACEABLES),
        NYLIUM(Tag.NYLIUM),
        OAK_LOGS(Tag.OAK_LOGS),
        OCCLUDES_VIBRATION_SIGNALS(Tag.OCCLUDES_VIBRATION_SIGNALS),
        OVERWORLD_CARVER_REPLACEABLES(Tag.OVERWORLD_CARVER_REPLACEABLES),
        PARROTS_SPAWNABLE_ON(Tag.PARROTS_SPAWNABLE_ON),
        PIGLIN_FOOD(Tag.PIGLIN_FOOD),
        PIGLIN_REPELLENTS(Tag.PIGLIN_REPELLENTS),
        PLANKS(Tag.PLANKS),
        POLAR_BEARS_SPAWNABLE_ON_ALTERNATE(Tag.POLAR_BEARS_SPAWNABLE_ON_ALTERNATE),
        PORTALS(Tag.PORTALS),
        PRESSURE_PLATES(Tag.PRESSURE_PLATES),
        PREVENT_MOB_SPAWNING_INSIDE(Tag.PREVENT_MOB_SPAWNING_INSIDE),
        RABBITS_SPAWNABLE_ON(Tag.RABBITS_SPAWNABLE_ON),
        RAILS(Tag.RAILS),
        REDSTONE_ORES(Tag.REDSTONE_ORES),
        REPLACEABLE(Tag.REPLACEABLE),
        REPLACEABLE_BY_TREES(Tag.REPLACEABLE_BY_TREES),
        SAND(Tag.SAND),
        SAPLINGS(Tag.SAPLINGS),
        SCULK_REPLACEABLE(Tag.SCULK_REPLACEABLE),
        SCULK_REPLACEABLE_WORLD_GEN(Tag.SCULK_REPLACEABLE_WORLD_GEN),
        SHULKER_BOXES(Tag.SHULKER_BOXES),
        SIGNS(Tag.SIGNS),
        SLABS(Tag.SLABS),
        SMALL_DRIPLEAF_REPLACEABLE(Tag.SMALL_DRIPLEAF_PLACEABLE),
        SMALL_FLOWERS(Tag.SMALL_FLOWERS),
        SMELTS_TO_GLASS(Tag.SMELTS_TO_GLASS),
        SNAPS_GOAT_HORN(Tag.SNAPS_GOAT_HORN),
        SNOW(Tag.SNOW),
        SNOW_LAYER_CAN_SURVIVE_ON(Tag.SNOW_LAYER_CAN_SURVIVE_ON),
        SNOW_LAYER_CANNOT_SURVIVE_ON(Tag.SNOW_LAYER_CANNOT_SURVIVE_ON),
        SOUL_FIRE_BASE_BLOCKS(Tag.SOUL_FIRE_BASE_BLOCKS),
        SOUL_SPEED_BLOCKS(Tag.SOUL_SPEED_BLOCKS),
        SPRUCE_LOGS(Tag.SPRUCE_LOGS),
        STAIRS(Tag.STAIRS),
        STANDING_SIGNS(Tag.STANDING_SIGNS),
        STONE_BRICKS(Tag.STONE_BRICKS),
        STONE_BUTTONS(Tag.STONE_BUTTONS),
        STONE_ORE_REPLACEABLES(Tag.STONE_ORE_REPLACEABLES),
        STONE_PRESSURE_PLATES(Tag.STONE_PRESSURE_PLATES),
        STRIDER_WARM_BLOCKS(Tag.STRIDER_WARM_BLOCKS),
        TALL_FLOWERS(Tag.TALL_FLOWERS),
        TERRACOTTA(Tag.TERRACOTTA),
        TRAPDOORS(Tag.TRAPDOORS),
        UNDERWATER_BONEMEALS(Tag.UNDERWATER_BONEMEALS),
        UNSTABLE_BOTTOM_CENTER(Tag.UNSTABLE_BOTTOM_CENTER),
        VALID_SPAWN(Tag.VALID_SPAWN),
        VIBRATION_RESONATORS(Tag.VIBRATION_RESONATORS),
        WALL_CORALS(Tag.WALL_CORALS),
        WALL_HANGING_SIGNS(Tag.WALL_HANGING_SIGNS),
        WALL_POST_OVERRIDE(Tag.WALL_POST_OVERRIDE),
        WALL_SIGNS(Tag.WALL_SIGNS),
        WALLS(Tag.WALLS),
        WARPED_STEMS(Tag.WARPED_STEMS),
        WART_BLOCKS(Tag.WART_BLOCKS),
        WITHER_IMMUNE(Tag.WITHER_IMMUNE),
        WITHER_SUMMON_BASE_BLOCKS(Tag.WITHER_SUMMON_BASE_BLOCKS),
        WOLVES_SPAWNABLE_ON(Tag.WOLVES_SPAWNABLE_ON),
        WOODEN_BUTTONS(Tag.WOODEN_BUTTONS),
        WOODEN_DOORS(Tag.WOODEN_DOORS),
        WOODEN_FENCES(Tag.WOODEN_FENCES),
        WOODEN_PRESSURE_PLATES(Tag.WOODEN_PRESSURE_PLATES),
        WOODEN_SLABS(Tag.WOODEN_SLABS),
        WOODEN_STAIRS(Tag.STAIRS),
        WOODEN_TRAPDOORS(Tag.WOODEN_TRAPDOORS),
        WOOL(Tag.WOOL),
        WOOL_CARPETS(Tag.WOOL_CARPETS);

        ;

        Tag<org.bukkit.Material> tag;

        public Tag<org.bukkit.Material> getTag() {
            return tag;
        }

        VanillaWildtags(Tag<org.bukkit.Material> tag) {
            this.tag = tag;
        }

        public static VanillaWildtags getTag(String name) {
            // name example "minecraft:planks", "#planks"
            for (VanillaWildtags tag : VanillaWildtags.values()) {
                if (tag.getTag().getKey().toString().equalsIgnoreCase(name)
                        || tag.getTag().getKey().toString().equalsIgnoreCase("#" + name)) {
                    return tag;
                }
            }
            return null;
        }

        public static VanillaWildtags[] getValidTags(ItemStack item) {
            List<VanillaWildtags> tags = new ArrayList<>();
            for (VanillaWildtags tag : VanillaWildtags.values()) {
                if (tag.getTag().isTagged(item.getType())) {
                    tags.add(tag);
                }
            }
            return tags.toArray(new VanillaWildtags[0]);
        }

        public boolean validate(ItemStack item) {
            return tag.isTagged(item.getType());
        }
    }

    public static Tag<KeyedTag> fromKey(String namespace, String key) {
        return new Tag<KeyedTag>() {

            @Override
            public @NotNull NamespacedKey getKey() {
                return new NamespacedKey(namespace, key.toUpperCase());
            }

            @Override
            public @NotNull Set<KeyedTag> getValues() {
                return Set.of();
            }

            @Override
            public boolean isTagged(@NotNull KeyedTag arg0) {
                return arg0.getKey().getKey().equalsIgnoreCase(getKey().getKey());
            }
        };
    }

    public enum MmoitemsWildtags {

        SWORD(fromKey("MMOITEMS_ITEM_TYPE", "sword")),
        DAGGER(fromKey("MMOITEMS_ITEM_TYPE", "dagger")),
        SPEAR(fromKey("MMOITEMS_ITEM_TYPE", "spear")),
        HAMMER(fromKey("MMOITEMS_ITEM_TYPE", "hammer")),
        GAUNTLET(fromKey("MMOITEMS_ITEM_TYPE", "gauntlet")),
        WHIP(fromKey("MMOITEMS_ITEM_TYPE", "whip")),
        STAFF(fromKey("MMOITEMS_ITEM_TYPE", "staff")),
        BOW(fromKey("MMOITEMS_ITEM_TYPE", "bow")),
        CROSSBOW(fromKey("MMOITEMS_ITEM_TYPE", "crossbow")),
        MUSKET(fromKey("MMOITEMS_ITEM_TYPE", "musket")),
        LUTE(fromKey("MMOITEMS_ITEM_TYPE", "lute")),
        CATALYST(fromKey("MMOITEMS_ITEM_TYPE", "catalyst")),
        OFF_CATALYST(fromKey("MMOITEMS_ITEM_TYPE", "off_catalyst")),
        MAIN_CATALYST(fromKey("MMOITEMS_ITEM_TYPE", "main_catalyst")),
        ORNAMENT(fromKey("MMOITEMS_ITEM_TYPE", "ornament")),
        ACCESSORY(fromKey("MMOITEMS_ITEM_TYPE", "accessory")),
        ARMOR(fromKey("MMOITEMS_ITEM_TYPE", "armor")),
        TOOL(fromKey("MMOITEMS_ITEM_TYPE", "tool")),
        CONSUMABLE(fromKey("MMOITEMS_ITEM_TYPE", "consumable")),
        MISCELLANEOUS(fromKey("MMOITEMS_ITEM_TYPE", "miscellaneous")),
        SKIN(fromKey("MMOITEMS_ITEM_TYPE", "skin")),
        GEM_STONE(fromKey("MMOITEMS_ITEM_TYPE", "gem_stone")),
        BLOCK(fromKey("MMOITEMS_ITEM_TYPE", "block")),
        GREATSWORD(fromKey("MMOITEMS_ITEM_TYPE", "greatsword")),
        LONG_SWORD(fromKey("MMOITEMS_ITEM_TYPE", "long_sword")),
        KATANA(fromKey("MMOITEMS_ITEM_TYPE", "katana")),
        THRUSTING_SWORD(fromKey("MMOITEMS_ITEM_TYPE", "thrusting_sword")),
        AXE(fromKey("MMOITEMS_ITEM_TYPE", "axe")),
        GREATAXE(fromKey("MMOITEMS_ITEM_TYPE", "greataxe")),
        HALBERD(fromKey("MMOITEMS_ITEM_TYPE", "halberd")),
        LANCE(fromKey("MMOITEMS_ITEM_TYPE", "lance")),
        GREATHAMMER(fromKey("MMOITEMS_ITEM_TYPE", "greathammer")),
        GREATSTAFF(fromKey("MMOITEMS_ITEM_TYPE", "greatstaff")),
        STAVE(fromKey("MMOITEMS_ITEM_TYPE", "stave")),
        TOME(fromKey("MMOITEMS_ITEM_TYPE", "tome")),
        TALISMAN(fromKey("MMOITEMS_ITEM_TYPE", "talisman")),
        WAND(fromKey("MMOITEMS_ITEM_TYPE", "wand")),
        GREATBOW(fromKey("MMOITEMS_ITEM_TYPE", "greatbow")),
        SHIELD(fromKey("MMOITEMS_ITEM_TYPE", "shield")),
        BLUEPRINT(fromKey("MMOITEMS_ITEM_TYPE", "blueprint")),
        BOSS_ARMOR(fromKey("MMOITEMS_ITEM_TYPE", "boss_armor")),
        BOTAS(fromKey("MMOITEMS_ITEM_TYPE", "botas")),
        BRACELET(fromKey("MMOITEMS_ITEM_TYPE", "bracelet")),
        CARD(fromKey("MMOITEMS_ITEM_TYPE", "card")),
        CASCO(fromKey("MMOITEMS_ITEM_TYPE", "casco")),
        COIN(fromKey("MMOITEMS_ITEM_TYPE", "coin")),
        CONBUFF(fromKey("MMOITEMS_ITEM_TYPE", "conbuff")),
        CUSTOM_ARMOR(fromKey("MMOITEMS_ITEM_TYPE", "custom_armor")),
        DATA_MODEL(fromKey("MMOITEMS_ITEM_TYPE", "data_model")),
        ENCHANT(fromKey("MMOITEMS_ITEM_TYPE", "enchant")),
        FAKECONSUMABLE(fromKey("MMOITEMS_ITEM_TYPE", "fakeconsumable")),
        FAKE_STAFF(fromKey("MMOITEMS_ITEM_TYPE", "fake_staff")),
        FISH(fromKey("MMOITEMS_ITEM_TYPE", "fish")),
        FOOD(fromKey("MMOITEMS_ITEM_TYPE", "food")),
        GREAT_ARMOR(fromKey("MMOITEMS_ITEM_TYPE", "great_armor")),
        HEAD(fromKey("MMOITEMS_ITEM_TYPE", "head")),
        HOE(fromKey("MMOITEMS_ITEM_TYPE", "hoe")),
        KNIFE(fromKey("MMOITEMS_ITEM_TYPE", "knife")),
        LIGHTBOW(fromKey("MMOITEMS_ITEM_TYPE", "lightbow")),
        MODIFIER(fromKey("MMOITEMS_ITEM_TYPE", "modifier")),
        OBJECT_ARMOR(fromKey("MMOITEMS_ITEM_TYPE", "object_armor")),
        ORB(fromKey("MMOITEMS_ITEM_TYPE", "orb")),
        ORES(fromKey("MMOITEMS_ITEM_TYPE", "ores")),
        PANTA(fromKey("MMOITEMS_ITEM_TYPE", "panta")),
        PETO(fromKey("MMOITEMS_ITEM_TYPE", "peto")),
        PICKAXE(fromKey("MMOITEMS_ITEM_TYPE", "pickaxe")),
        POTION(fromKey("MMOITEMS_ITEM_TYPE", "potion")),
        REWARD(fromKey("MMOITEMS_ITEM_TYPE", "reward")),
        TRIDENT(fromKey("MMOITEMS_ITEM_TYPE", "trident")),
        RING(fromKey("MMOITEMS_ITEM_TYPE", "ring")),
        SHOVEL(fromKey("MMOITEMS_ITEM_TYPE", "shovel")),
        SHRUBS(fromKey("MMOITEMS_ITEM_TYPE", "shrubs")),
        TOKEN(fromKey("MMOITEMS_ITEM_TYPE", "token")),
        WEIGHTBOW(fromKey("MMOITEMS_ITEM_TYPE", "weightbow")),
        AMULET(fromKey("MMOITEMS_ITEM_TYPE", "amulet")),
        ARTIFACT(fromKey("MMOITEMS_ITEM_TYPE", "artifact"));

        Tag<KeyedTag> tag;

        public Tag<KeyedTag> getTag() {
            return tag;
        }

        MmoitemsWildtags(Tag<KeyedTag> tag) {
            this.tag = tag;
        }

        public static MmoitemsWildtags getTag(String name) {
            // name example "minecraft:planks", "#planks"
            for (MmoitemsWildtags tag : MmoitemsWildtags.values()) {
                if (tag.getTag().getKey().toString().equalsIgnoreCase(name)
                        || tag.getTag().getKey().toString().equalsIgnoreCase("#" + name)) {
                    return tag;
                }
            }
            return null;
        }

        public static MmoitemsWildtags[] getValidTags(RealNBT item) {
            List<MmoitemsWildtags> tags = new ArrayList<>();
            for (MmoitemsWildtags tag : MmoitemsWildtags.values()) {
                if (tag.getTag().isTagged(item.get("MMOITEMS_ITEM_TYPE").toKeyed())) {
                    tags.add(tag);
                }
            }
            return tags.toArray(new MmoitemsWildtags[0]);
        }

    }

    public static boolean Valid(RealNBT nbt, String... tags) {

        return false;

    }

    public List<Tag> getTags(RealNBT item) {
        List<Tag> list = new ArrayList<>();
        for (VanillaWildtags tag : VanillaWildtags.values()) {
            if (tag.getTag().isTagged(item.getMaterial())) {
                list.add(tag.getTag());
            }
        }
        for (MmoitemsWildtags tag : MmoitemsWildtags.values()) {
            if (tag.getTag().isTagged(item.get("MMOITEMS_ITEM_TYPE").toKeyed())) {
                list.add(tag.getTag());
            }
        }
        return list;
    }
}
