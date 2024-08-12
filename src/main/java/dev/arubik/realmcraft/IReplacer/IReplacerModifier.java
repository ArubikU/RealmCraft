package dev.arubik.realmcraft.IReplacer;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.Events.LoreEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import dev.arubik.realmcraft.Api.LoreParser.ContextEvent;
import dev.arubik.realmcraft.Handlers.RealMessage;

public class IReplacerModifier implements ItemBuildModifier {

    @Override
    public RealNBT modifyItem(Player player, RealNBT item) {
        InternalReplacerStructure structure = IReplacerListener.match(item.getItemStack());
        if (structure == null) {
            return item;
        }

        switch (structure.getOutputType()) {
            case MMOITEMS: {
                if (structure.getOutputConfig().has("Item")) {
                    String line = structure.getOutputConfig().get("Item").getAsString();
                    String[] split = line.split(":");
                    List<String> types = MMOItems.plugin.getTypes().getAll().parallelStream()
                            .map(Type::getId).collect(Collectors.toList());
                    if (types.contains(split[0])) {
                        ItemStack stack;
                        stack = getItemPreviewMMOitems(split[0], split[1]);

                        if (stack == null) {
                            break;
                        }
                        stack.setAmount(item.getItemStack().getAmount());
                        if (structure.getOutputConfig().has("Pass-Enchantments")
                                && structure.getOutputConfig().get("Pass-Enchantments")
                                        .getAsBoolean()) {
                            stack.addUnsafeEnchantments(item.getItemStack().getEnchantments());
                        }
                        return RealNBT.fromItemStack(stack);
                    }
                }
                break;
            }
        }
        return item;
    }

    @Override
    public Boolean able(RealNBT nbt) {
        return nbt.contains("IS_MERCHANT_OUTPUT");
    }

    public ItemStack getItemPreviewMMOitems(String type, String id) {
        if (Type.isValid(type)) {
            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(Type.get(type), id);
            ConfigMMOItem configMMOItem = new ConfigMMOItem(template, 1);
            return configMMOItem.getPreview();
        }
        return null;
    }

    public static void Register() {
        // TO-DO Disabled until we can find a efficient way to handle this
        // LoreEvent.addItemBuildModifier(new IReplacerModifier(),
        // EventPriority.HIGHEST);
    }
}
