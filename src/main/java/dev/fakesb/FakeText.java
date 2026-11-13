package dev.fakesb;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Podmienia wartosc w linii scoreboardu (np. "Kasa: $0" -> "Kasa: $999K"), zachowujac kolory. */
public final class FakeText {
    private FakeText() {}

    public static MutableText transform(MutableText in) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.enabled || in == null) return in;

        StringBuilder sb = new StringBuilder();
        List<Style> styles = new ArrayList<>();
        in.visit((style, s) -> {
            for (int i = 0; i < s.length(); i++) {
                sb.append(s.charAt(i));
                styles.add(style);
            }
            return Optional.empty();
        }, Style.EMPTY);

        String full = sb.toString();
        if (full.isEmpty()) return in;
        String low = full.toLowerCase(Locale.ROOT);

        for (ModConfig.Rule rule : cfg.rules) {
            if (!rule.enabled || rule.key == null || rule.key.isEmpty()) continue;
            String needle = rule.key.toLowerCase(Locale.ROOT) + ":";
            int idx = low.indexOf(needle);
            if (idx < 0 || low.length() != full.length()) continue;

            int vs = idx + needle.length();
            if (vs < full.length() && full.charAt(vs) == ' ') vs++;

            MutableText out = Text.empty();
            appendRange(out, full, styles, 0, vs);
            Style valueStyle = vs < styles.size() ? styles.get(vs) : styles.get(styles.size() - 1);
            out.append(Text.literal(rule.value == null ? "" : rule.value).setStyle(valueStyle));
            return out;
        }
        return in;
    }

    private static void appendRange(MutableText out, String s, List<Style> styles, int from, int to) {
        int i = from;
        while (i < to) {
            Style st = styles.get(i);
            int j = i;
            while (j < to && styles.get(j).equals(st)) j++;
            out.append(Text.literal(s.substring(i, j)).setStyle(st));
            i = j;
        }
    }
}
