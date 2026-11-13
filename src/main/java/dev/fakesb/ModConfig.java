
package dev.fakesb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("fakescoreboard.json");

    private static ModConfig instance = new ModConfig();

    public boolean enabled = true;
    public boolean animations = true;
    public boolean rainbow = false;

    public int r = 255;
    public int g = 212;
    public int b = 0;

    public List<Rule> rules = defaultRules();

    public static class Rule {
        public String key;
        public boolean enabled;
        public String value;

        public Rule() {}

        public Rule(String key, String value) {
            this.key = key;
            this.value = value;
            this.enabled = false;
        }
    }

    private static List<Rule> defaultRules() {
        List<Rule> list = new ArrayList<>();

        list.add(new Rule("Kasa", "$0"));
        list.add(new Rule("Shardy", "0"));
        list.add(new Rule("Zabójstwa", "0"));
        list.add(new Rule("Śmierci", "0"));
        list.add(new Rule("Życia", "5"));
        list.add(new Rule("Keyall", "44m"));
        list.add(new Rule("Czas gry", "0m"));
        list.add(new Rule("Team", "Brak"));

        return list;
    }

    public static ModConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                instance = new ModConfig();
                save();
                return;
            }

            String json = Files.readString(FILE);

            ModConfig config = GSON.fromJson(json, ModConfig.class);

            if (config == null) {
                instance = new ModConfig();
                save();
                return;
            }

            if (config.rules == null) {
                config.rules = new ArrayList<>();
            }

            // Uzupełnianie brakujących reguł
            List<Rule> defaults = defaultRules();

            for (Rule defaultRule : defaults) {
                boolean exists = config.rules.stream()
                        .anyMatch(rule ->
                                rule != null &&
                                defaultRule.key.equals(rule.key));

                if (!exists) {
                    config.rules.add(defaultRule);
                }
            }

            // Ochrona przed nieprawidłowymi wartościami RGB
            config.r = Math.max(0, Math.min(255, config.r));
            config.g = Math.max(0, Math.min(255, config.g));
            config.b = Math.max(0, Math.min(255, config.b));

            instance = config;

        } catch (Exception e) {
            System.err.println(
                    "[fakesb] Nie udalo sie wczytac configu: " + e
            );

            instance = new ModConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            Files.writeString(
                    FILE,
                    GSON.toJson(instance)
            );

        } catch (Exception e) {
            System.err.println(
                    "[fakesb] Nie udalo sie zapisac configu: " + e
            );
        }
    }

    public static void reset() {
        instance = new ModConfig();
        save();
    }

    /**
     * Kolor akcentu RGB.
     * W trybie teczy plynnie zmienia odcien.
     */
    public static int accentRgb() {
        ModConfig config = instance;

        if (config.rainbow) {
            float hue =
                    (System.currentTimeMillis() % 6000L) / 6000f;

            return Color.HSBtoRGB(
                    hue,
                    0.75f,
                    1f
            ) & 0xFFFFFF;
        }

        return ((config.r & 255) << 16)
                | ((config.g & 255) << 8)
                | (config.b & 255);
    }
}
