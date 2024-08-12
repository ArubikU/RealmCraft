package dev.arubik.realmcraft.Api.Listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import dev.arubik.realmcraft.Handlers.RealMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.PermissionNode;

public class ChatListener implements Listener {
    public static InteractiveFile file;
    public static List<Replacement> replacements = new ArrayList<Replacement>();

    public ChatListener(Plugin plugin) {
        this.file = new InteractiveFile("chatreplacements.yml", plugin);
        if (this.file.has("replacements")) {
            for (InteractiveSection section : this.file.getSections("replacements")) {
                Replacement rep = Replacement.fromSection(section);
                this.replacements.add(rep);
                RealMessage
                        .sendConsoleMessage("<green>Loaded replacement: " + section.get("toReplace") + " -> "
                                + section.get("replacement"));
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void loadGroups() {
        LuckPerms api = LuckPermsProvider.get();
        for (Replacement replacement : ChatListener.replacements) {
            if (replacement.lpgroup.equalsIgnoreCase("none"))
                continue;
            if (api.getGroupManager().getGroup(replacement.lpgroup) == null) {
                api.getGroupManager().createAndLoadGroup(replacement.lpgroup).join();
            }
            api.getGroupManager().modifyGroup(replacement.lpgroup, group -> {
                group.data().add(PermissionNode.builder().permission(replacement.permission).build());
            });

        }
    }

    public static class Replacement {
        public String toReplace;
        public String replacement;
        public String lpgroup;
        public String permission;
        public Boolean forcewhite;

        public static Replacement fromSection(InteractiveSection section) {
            Replacement replacement = new Replacement();
            replacement.toReplace = section.get("toReplace").toString();
            replacement.replacement = section.get("replacement").toString();
            replacement.permission = section.get("permission").toString();
            replacement.forcewhite = section.getOrDefault("force-white", false).toString().equalsIgnoreCase("true");
            replacement.lpgroup = section.getOrDefault("lp-group", "none");
            return replacement;
        }
    }

    @EventHandler
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Boolean mdfied = false;
        Player player = event.getPlayer();
        for (Replacement replacement : this.replacements) {
            if (player.hasPermission(replacement.permission)
                    || player.hasPermission("chatreplacements.*")
                    || player.hasPermission("group." + replacement.lpgroup)) {

                if (StringUtils.containsIgnoreCase(message, replacement.toReplace)) {
                    mdfied = true;
                    message = StringUtils.replaceIgnoreCase(message, replacement.toReplace,
                            forcedWhite(replacement) + replacement.replacement);
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
                    message = StringUtils.replaceIgnoreCase(message, newToReplace,
                            forcedWhite(replacement) + replacement.toReplace);
                }
            }
        }
        if (mdfied) {

            event.setMessage(message);
        }

        // add hover?

    }

    // on sign book
    // on stop writing a sign
    @EventHandler
    public void onSignChange(SignChangeEvent event) {

        Player plr = event.getPlayer();
        Block block = event.getBlock();
        Sign s = (Sign) block.getState();
        String[] lines = s.getTargetSide(plr).getLines();
        for (int i = 0; i < lines.length; i++) {
            String message = lines[i];

            Boolean mdfied = false;
            Player player = event.getPlayer();
            for (Replacement replacement : this.replacements) {
                if (player.hasPermission(replacement.permission)
                        || player.hasPermission("chatreplacements.*")
                        || player.hasPermission("group." + replacement.lpgroup)) {

                    if (StringUtils.containsIgnoreCase(message, replacement.toReplace)) {
                        mdfied = true;
                        message = StringUtils.replaceIgnoreCase(message, replacement.toReplace,
                                forcedWhite(replacement) + replacement.replacement);
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
                        message = StringUtils.replaceIgnoreCase(message, newToReplace,
                                forcedWhite(replacement) + replacement.toReplace);
                    }
                }
            }
            if (mdfied) {

                s.getTargetSide(plr).setLine(i, message);
            }
        }
    }

    public String forcedWhite(Replacement replacement) {
        if (replacement.forcewhite) {
            return LegacyComponentSerializer.SECTION_CHAR + "f";
        }
        return "";
    }

    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Collection<String> completisionToAdd = new ArrayList<String>();
        for (Replacement replacement : this.replacements) {
            Player player = event.getPlayer();
            if (player.hasPermission(replacement.permission)
                    || player.hasPermission("chatreplacements.*")
                    || player.hasPermission("group." + replacement.lpgroup)) {
                completisionToAdd.add(replacement.toReplace);
            }
        }

        event.getPlayer().addAdditionalChatCompletions(completisionToAdd);
    }

    public static void onReload() {
        Collection<String> completisionToAdd = new ArrayList<String>();

        for (Replacement replacement : ChatListener.replacements) {
            completisionToAdd.add(replacement.toReplace);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removeAdditionalChatCompletions(completisionToAdd);
            for (Replacement replacement : ChatListener.replacements) {
                if (player.hasPermission(replacement.permission)
                        || player.hasPermission("chatreplacements.*")
                        || player.hasPermission("group." + replacement.lpgroup)) {
                    completisionToAdd.add(replacement.toReplace);
                }
            }

            player.addAdditionalChatCompletions(completisionToAdd);
        }
    }

}
