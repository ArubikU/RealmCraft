package dev.arubik.realmcraft.Handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.LineConfig;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;

public class PlaceholderConfigParser {

    private static final Pattern BRACKET_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^{}]+)[}]");

    public static String parser(String text) {

        Matcher matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String placeholder = matcher.group();
            LineConfig config = new LineConfig("config" + placeholder);
            String path = config.getString("path");
            String configFile = config.getString("file", "config.yml");
            if (configFile.equals("config.yml")) {
                text = text.replace(placeholder,
                        realmcraft.getInteractiveConfig().getString(path, "Not found key: " + path));
            } else if (configFile.equals("lang.yml")) {
                text = text.replace(placeholder,
                        realmcraft.getMinecraftLang().getString(path,
                                "Not found key: " + path + " in file: " + configFile));
            } else {
                InteractiveFile file = new InteractiveFile(configFile, realmcraft.getInstance());
                text = text.replace(placeholder,
                        file.getString(path, "Not found key: " + path + " in file: " + configFile));
            }
            matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(text);
        }

        return text;
    }

    public static String parser(String text, InteractiveFile file) {

        Matcher matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(text);
        try {
            while (matcher.find()) {
                String placeholder = matcher.group();
                LineConfig config = new LineConfig("config" + placeholder);
                String path = config.getString("path");
                String configFile = config.getString("file", file.getName());
                if (configFile.equals("config.yml")) {
                    text = text.replace(placeholder,
                            realmcraft.getInteractiveConfig().getString(path, "Not found key: " + path));
                } else if (configFile.equals("lang.yml")) {
                    text = text.replace(placeholder,
                            realmcraft.getMinecraftLang().getString(path,
                                    "Not found key: " + path + " in file: " + configFile));
                } else {
                    text = text.replace(placeholder,
                            file.getString(path, "Not found key: " + path + " in file: " + configFile));
                }
                matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(text);
            }
        } catch (Throwable e) {
            return text;
        }

        return text;
    }

}
