package dev.arubik.realmcraft.Api.Listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import dev.arubik.realmcraft.Handlers.RealMessage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatListener implements Listener {
    InteractiveFile file;
    List<Replacement> replacements = new ArrayList<Replacement>();

    public ChatListener(Plugin plugin) {
        this.file = new InteractiveFile("chatreplacements.yml", plugin);
        if (this.file.has("replacements")) {
            for (InteractiveSection section : this.file.getSections("replacements")) {
                this.replacements.add(Replacement.fromSection(this, section));
                RealMessage
                        .sendConsoleMessage("<green>Loaded replacement: " + section.get("toReplace") + " -> "
                                + section.get("replacement"));
            }
        }

        

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public class Replacement {
        public String toReplace;
        public String replacement;
        public String permission;

        public static Replacement fromSection(ChatListener listener, InteractiveSection section) {
            Replacement replacement = listener.new Replacement();
            replacement.toReplace = section.get("toReplace").toString();
            replacement.replacement = section.get("replacement").toString();
            replacement.permission = section.get("permission").toString();
            return replacement;
        }
    }

    @EventHandler
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Boolean mdfied = false;
        for (Replacement replacement : this.replacements) {
            if (event.getPlayer().hasPermission(replacement.permission)) {

                if (StringUtils.containsIgnoreCase(message, replacement.toReplace)) {
                    mdfied = true;
                    message = StringUtils.replaceIgnoreCase(message, replacement.toReplace, replacement.replacement);
                }
            } else {
                if (message.toLowerCase()
                        .replace(LegacyComponentSerializer.SECTION_CHAR + "",
                                LegacyComponentSerializer.AMPERSAND_CHAR + "")
                        .contains(replacement.replacement.toLowerCase())) {

                    String newToReplace = replacement.replacement.toLowerCase()
                            .replaceAll(LegacyComponentSerializer.AMPERSAND_CHAR + "",
                                    LegacyComponentSerializer.SECTION_CHAR + "");
                    mdfied = true;
                    message = StringUtils.replaceIgnoreCase(message, newToReplace, replacement.toReplace);
                }
            }
        }
        if (mdfied) {

            event.setMessage(message);
        }
    }


    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Collection<String> completisionToAdd = new ArrayList<String>();
        for (Replacement replacement : this.replacements) {
            if (event.getPlayer().hasPermission(replacement.permission)) {
                completisionToAdd.add(replacement.toReplace);
            }
        }

        event.getPlayer().addAdditionalChatCompletions(completisionToAdd);
    }

}
