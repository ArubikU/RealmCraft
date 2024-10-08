package dev.arubik.realmcraft.MMOItems.Durability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.ItemBuildModifier;
import dev.arubik.realmcraft.Api.Locale;
import dev.arubik.realmcraft.Api.RealNBT;
import dev.arubik.realmcraft.Api.RealProtocol.PacketAdapter;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import dev.arubik.realmcraft.Handlers.PlaceholderConfigParser;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;

public class MinimessageFormat implements ItemBuildModifier {

    public static Map<UUID, String> Playerslang = new HashMap<UUID, String>();

    public Locale getLang(Player player) {
        UUID uuid = player.getUniqueId();
        if (!Playerslang.containsKey(uuid)) {
            return Locale.ES;
        }
        String lang = Playerslang.get(uuid);
        if (lang == null) {
            return Locale.EN;
        }
        if (lang.toLowerCase().contains("en")) {
            return Locale.EN;
        }
        if (lang.toLowerCase().contains("es")) {
            return Locale.ES;
        }
        if (lang.toLowerCase().contains("fr")) {
            return Locale.FR;
        }
        if (lang.toLowerCase().contains("de")) {
            return Locale.DE;
        }
        if (lang.toLowerCase().contains("it")) {
            return Locale.IT;
        }
        if (lang.toLowerCase().contains("pt")) {
            return Locale.PT;
        }
        if (lang.toLowerCase().contains("ru")) {
            return Locale.RU;
        }
        if (lang.toLowerCase().contains("ja")) {
            return Locale.JA;
        }
        if (lang.toLowerCase().contains("ko")) {
            return Locale.KO;
        }
        if (lang.toLowerCase().contains("zh")) {
            return Locale.ZH;
        }
        if (lang.toLowerCase().contains("zh_tw")) {
            return Locale.ZH_TW;
        }
        if (lang.toLowerCase().contains("pl")) {
            return Locale.PL;
        }
        if (lang.toLowerCase().contains("nl")) {
            return Locale.NL;
        }
        return Locale.ES;
    }

    public static PacketAdapter PlayerSettingListener = new PacketAdapter(realmcraft.getInstance(),
            PacketType.Play.Client.CLIENT_SETTINGS) {
        @Override
        public void onPacketReceiving(PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
                WrapperPlayClientSettings packet = new WrapperPlayClientSettings(event);
                String lang = packet.getLocale();
                UUID uuid = ((Player) event.getPlayer()).getUniqueId();
                Playerslang.put(uuid, lang);
            }
        }
    };

    @Override
    public Boolean able(RealNBT nbt) {
        return nbt.contains("LANG_ENABLED");
    }

    public static Map<String, InteractiveFile> langs = new HashMap<String, InteractiveFile>();

    public static void register() {
        // TO-DO Disabled until we can find a efficient way to handle this
        // LoreEvent.addItemBuildModifier(new MinimessageFormat());
        if (!Depend.isPluginEnabled("ProtocolLib")) {
            RealMessage.nonFound("ProtocolLib or MMOItems is not installed, so LoreEvent will not work.");
            return;
        }
        RealMessage.Found("LangFormater is registered");
        PlayerSettingListener.register();
        regenLang();
    }

    public static void unregister() {
        PlayerSettingListener.unregister();
    }

    public static void regenLang() {
        InteractiveFolder langFolder = new InteractiveFolder("langs", realmcraft.getInstance());
        langFolder.getFiles().forEach((name, file) -> {
            langs.put(file.getSimpleName(), file);
        });
        for (InteractiveFile lang : langs.values()) {
            RealMessage.sendConsoleMessage("Lang: " + lang.getSimpleName() + " is loaded");
        }

    }

    @Override
    public RealNBT modifyItem(Player player, RealNBT nbt) {
        List<String> lore = nbt.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                if (line.contains("<lang>")) {
                    line = line.replace("<lang>", "");
                    // replace '&<' with { and replace '&>' with }
                    if (line.contains("\\")) {
                        line = line.replace("\\", "");
                    }
                    if (line.contains("<white></white>")) {
                        line = line.replace("<white></white>", "");
                    }
                    if (line.contains("</white><white>")) {
                        line = line.replace("</white><white>", "");
                    }
                    if (line.contains("<white>&<")) {
                        line = line.replace("<white>&<", "<white>{");
                    }
                    line = line.replace("&<", "{");
                    line = line.replace("&>", "}");
                    Locale locale = getLang(player);
                    line = line.replace("?lang_key?", locale.getLang());
                    // RealMessage.sendRaw(line);
                    if (langs.containsKey(locale.getLang())) {
                        line = PlaceholderConfigParser.parser(line, langs.get(locale.getLang()));
                        lore.set(i, line);
                    } else {
                        line = PlaceholderConfigParser.parser(line);
                        lore.set(i, line);
                    }
                }
            }
            nbt.setLore(lore);

        }
        return nbt;
    }
}
