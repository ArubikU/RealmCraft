package dev.arubik.realmcraft.market;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.willfp.eco.libs.jetbrains.annotations.Nullable;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import dev.arubik.realmcraft.FileManagement.InteractiveFolder;
import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import dev.arubik.realmcraft.Managers.Module;

public class MarketModule implements Module {

    private final realmcraft plugin;
    private InteractiveFolder folder;
    private InteractiveFile ConfigFile;
    private InteractiveFile DayFile;

    private Map<String, MarketItem> MarketItems = new HashMap<String, MarketItem>();
    private Map<String, MarketItemVariation> MarketItemVariations = new HashMap<String, MarketItemVariation>();

    private final MarketCommand command = new MarketCommand();

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LocalDate date;

    public MarketModule(realmcraft plugin) {
        this.plugin = plugin;
        this.date = LocalDate.now();
    }

    @Override
    public void register() {
        folder = new InteractiveFolder("Market", plugin);
        ConfigFile = new InteractiveFile("market-config.yml", plugin);
        DayFile = folder.createFile("market-day-" + date.format(formatter) + ".yml");
        command.register();
        initialize();
    }

    private void initialize() {
        initializeVariationData();
    }

    private void initializeVariationData() {
        InteractiveSection items = ConfigFile.getSection("items");
        for (String key : items.getKeys()) {
            InteractiveSection ItemSection = items.getSection(key);
            MarketItemVariations.putIfAbsent(key, MarketItemVariation.fromConfigurationSection(ItemSection));
        }
    }

    @Nullable
    private InteractiveFile goBackInTime(int days) {
        // get the days back in time
        String date = this.date.minusDays(days).format(formatter);
        for (String file : folder.getFiles().keySet()) {
            if (file.contains(date)) {
                return folder.getFiles().get(file);
            }
        }
        return null;
    }

    @Override
    public void unregister() {
        command.unregister();
    }

    @Override
    public String configId() {
        return "market";
    }

    @Override
    public String displayName() {
        return "Market";
    }

}
