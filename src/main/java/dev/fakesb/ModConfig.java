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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fakescoreboard.json");
    private static ModConfig instance = new ModConfig();

    public boolean enabled = true;
    public boolean animations = true;
    public boolean rainbow = false;
    public int r = 255, g = 212, b = 0;
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
        List<Rule> l = new ArrayList<>();
        l.add(new Rule("Kasa", "$0"));
        l.add(new Rule("Shardy", "0"));
        l.add(new Rule("Zabójstwa", "0"));
        l.add(new Rule("Śmierci", "0"));
        l.add(new Rule("Życia", "5"));
        l.add(new Rule("Keyall", "44m"));
        l.add(new Rule("Czas gry", "0m"));
        l.add(new Rule("Team", "Brak"));
        return l;
    }

    public static ModConfig get() { return instance; }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                ModConfig c = GSON.fromJson(Files.readString(FILE), ModConfig.class);
                if (c != null) {
                    if (c.rules == null || c.rules.isEmpty()) c.rules = defaultRules();
                    instance = c;
                }
            }
        } catch (Exception e) {
            System.err.println("[fakesb] Nie udalo sie wczytac configu: " + e);
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(instance));
        } catch (Exception e) {
            System.err.println("[fakesb] Nie udalo sie zapisac configu: " + e);
        }
    }

    public static void reset() {
        instance = new ModConfig();
        save();
    }

    /** Kolor akcentu (RGB, bez alfy). W trybie teczy plynnie zmienia odcien. */
    public static int accentRgb() {
        ModConfig c = instance;
        if (c.rainbow) {
            float hue = (System.currentTimeMillis() % 6000L) / 6000f;
            return Color.HSBtoRGB(hue, 0.75f, 1f) & 0xFFFFFF;
        }
        return ((c.r & 255) << 16) | ((c.g & 255) << 8) | (c.b & 255);
    }
}
