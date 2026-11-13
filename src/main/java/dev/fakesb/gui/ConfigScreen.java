package dev.fakesb.gui;

import dev.fakesb.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private static final int PW = 460, PH = 232;
    private static final int[] PRESETS = {0xFFD400, 0x00E5FF, 0xFF3DDB, 0x3DFF7A, 0xFF4040, 0xFFFFFF};

    private final ModConfig cfg = ModConfig.get();
    private final List<ClickableWidget> movers = new ArrayList<>();
    private final List<Integer> baseY = new ArrayList<>();
    private ColorSlider sr, sg, sb;
    private FancyButton rainbowBtn;

    private final long startNs = System.nanoTime();
    private long lastNs = startNs;
    private float open = 0f, ease = 0f, time = 0f;
    private int off = 24, px, py;

    public ConfigScreen() {
        super(Text.literal("Fake Scoreboard"));
    }

    private void add(ClickableWidget w, int y) {
        addDrawableChild(w);
        movers.add(w);
        baseY.add(y);
    }

    private static Text onOff(boolean b) { return Text.literal(b ? "ON" : "OFF"); }

    @Override
    protected void init() {
        movers.clear();
        baseY.clear();
        px = (width - PW) / 2;
        py = (height - PH) / 2;

        // lewa kolumna: reguly
        int lx = px + 14, y0 = py + 40;
        for (int i = 0; i < cfg.rules.size(); i++) {
            ModConfig.Rule r = cfg.rules.get(i);
            int y = y0 + i * 22;
            add(new FancyButton(lx, y, 34, 16, onOff(r.enabled), b -> {
                r.enabled = !r.enabled;
                b.setMessage(onOff(r.enabled));
            }).lit(() -> r.enabled), y);

            TextFieldWidget tf = new TextFieldWidget(textRenderer, lx + 112, y, 138, 16, Text.literal(r.key));
            tf.setMaxLength(40);
            tf.setText(r.value == null ? "" : r.value);
            tf.setChangedListener(s -> r.value = s);
            add(tf, y);
        }

        // prawa kolumna: ustawienia i kolory
        int rx = px + 280, rw = 166, y = py + 40;
        add(new FancyButton(rx, y, rw, 16, Text.literal("Mod: " + (cfg.enabled ? "ON" : "OFF")), b -> {
            cfg.enabled = !cfg.enabled;
            b.setMessage(Text.literal("Mod: " + (cfg.enabled ? "ON" : "OFF")));
        }).lit(() -> cfg.enabled), y);

        y += 20;
        add(new FancyButton(rx, y, rw, 16, Text.literal("Animacje: " + (cfg.animations ? "ON" : "OFF")), b -> {
            cfg.animations = !cfg.animations;
            b.setMessage(Text.literal("Animacje: " + (cfg.animations ? "ON" : "OFF")));
        }).lit(() -> cfg.animations), y);

        y += 20;
        rainbowBtn = new FancyButton(rx, y, rw, 16, Text.literal("Teczowy akcent: " + (cfg.rainbow ? "ON" : "OFF")), b -> {
            cfg.rainbow = !cfg.rainbow;
            b.setMessage(Text.literal("Teczowy akcent: " + (cfg.rainbow ? "ON" : "OFF")));
        }).lit(() -> cfg.rainbow);
        add(rainbowBtn, y);

        y += 24;
        sr = new ColorSlider(rx, y, rw, 16, "R", cfg.r, v -> cfg.r = v);
        add(sr, y);
        y += 18;
        sg = new ColorSlider(rx, y, rw, 16, "G", cfg.g, v -> cfg.g = v);
        add(sg, y);
        y += 18;
        sb = new ColorSlider(rx, y, rw, 16, "B", cfg.b, v -> cfg.b = v);
        add(sb, y);

        y += 24;
        for (int i = 0; i < PRESETS.length; i++) {
            int col = PRESETS[i];
            add(new FancyButton(rx + i * 28, y, 22, 14, Text.empty(), b -> {
                cfg.r = (col >> 16) & 255;
                cfg.g = (col >> 8) & 255;
                cfg.b = col & 255;
                cfg.rainbow = false;
                rainbowBtn.setMessage(Text.literal("Teczowy akcent: OFF"));
                sr.sync(cfg.r);
                sg.sync(cfg.g);
                sb.sync(cfg.b);
            }).swatch(col), y);
        }

        int by = py + PH - 30;
        add(new FancyButton(rx, by, 80, 18, Text.literal("Reset"), b -> {
            ModConfig.reset();
            client.setScreen(new ConfigScreen());
        }), by);
        add(new FancyButton(rx + 86, by, 80, 18, Text.literal("Gotowe"), b -> close()), by);
    }

    // Tlo/panel rysujemy sami w render(), wiec wylaczamy domyslny blur.
    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float dt = Math.min((now - lastNs) / 1e9f, 0.1f);
        lastNs = now;
        time = (now - startNs) / 1e9f;

        open = cfg.animations ? Math.min(1f, open + dt / 0.4f) : 1f;
        float inv = 1f - open;
        ease = 1f - inv * inv * inv;
        off = (int) ((1f - ease) * 24);

        for (int i = 0; i < movers.size(); i++) {
            ClickableWidget w = movers.get(i);
            w.setY(baseY.get(i) + off);
            w.setAlpha(ease);
        }

        drawPanel(c);
        super.render(c, mouseX, mouseY, delta);
    }

    private void drawPanel(DrawContext c) {
        int acc = ModConfig.accentRgb();
        int x = px, y = py + off;
        float pulse = cfg.animations ? 0.65f + 0.35f * (float) Math.sin(time * 2.5f) : 1f;

        c.fill(0, 0, width, height, Gfx.argb(0.55f * ease, 0x000000));

        for (int i = 3; i >= 1; i--) {
            c.drawBorder(x - i, y - i, PW + i * 2, PH + i * 2, Gfx.argb(ease * 0.10f * pulse * (4 - i), acc));
        }
        c.fill(x, y, x + PW, y + PH, Gfx.argb(ease * 0.95f, 0x14141C));
        c.fill(x + 272, y + 36, x + 273, y + PH - 12, Gfx.argb(ease * 0.25f, acc));

        // animowany pasek na gorze
        for (int i = 0; i < PW; i += 4) {
            float k = cfg.animations ? 0.55f + 0.45f * (float) Math.sin(time * 3f - i * 0.04f) : 1f;
            c.fill(x + i, y, Math.min(x + i + 4, x + PW), y + 3, Gfx.argb(ease, Gfx.mix(0x202030, acc, k)));
        }
        c.drawBorder(x, y, PW, PH, Gfx.argb(ease * pulse, acc));

        c.drawTextWithShadow(textRenderer, Text.literal("Fake Scoreboard").formatted(Formatting.BOLD),
                x + 14, y + 12, Gfx.argb(ease, acc));
        c.drawTextWithShadow(textRenderer, Text.literal("Zmiany tylko u Ciebie - serwer ich nie widzi"),
                x + 14, y + 24, Gfx.argb(ease, 0x8A8A99));

        for (int i = 0; i < cfg.rules.size(); i++) {
            c.drawTextWithShadow(textRenderer, Text.literal(cfg.rules.get(i).key),
                    px + 14 + 40, py + 40 + i * 22 + 4 + off, Gfx.argb(ease, 0xE0E0E0));
        }
    }

    @Override
    public void removed() {
        ModConfig.save();
    }

    @Override
    public boolean shouldPause() { return false; }
}
