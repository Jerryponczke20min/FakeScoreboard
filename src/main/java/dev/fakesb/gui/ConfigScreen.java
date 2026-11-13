
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

    private static final int PW = 460;
    private static final int PH = 232;

    private static final int[] PRESETS = {
            0xFFD400,
            0x00E5FF,
            0xFF3DDB,
            0x3DFF7A,
            0xFF4040,
            0xFFFFFF
    };

    private final ModConfig cfg = ModConfig.get();

    private final List<ClickableWidget> movers = new ArrayList<>();
    private final List<Integer> baseY = new ArrayList<>();

    private ColorSlider sr, sg, sb;
    private FancyButton rainbowBtn;

    private final long startNs = System.nanoTime();
    private long lastNs = startNs;

    private float open = 0f;
    private float ease = 0f;
    private float time = 0f;

    private int off = 24;
    private int px, py;

    public ConfigScreen() {
        super(Text.literal("Fake Scoreboard"));
    }

    private void add(ClickableWidget widget, int y) {
        addDrawableChild(widget);
        movers.add(widget);
        baseY.add(y);
    }

    private static Text onOff(boolean enabled) {
        return Text.literal(enabled ? "ON" : "OFF");
    }

    @Override
    protected void init() {
        movers.clear();
        baseY.clear();

        px = (width - PW) / 2;
        py = (height - PH) / 2;

        // LEWA KOLUMNA - REGUŁY
        int lx = px + 14;
        int y0 = py + 40;

        for (int i = 0; i < cfg.rules.size(); i++) {
            ModConfig.Rule rule = cfg.rules.get(i);

            int y = y0 + i * 22;

            FancyButton toggle = new FancyButton(
                    lx, y, 34, 16,
                    onOff(rule.enabled),
                    b -> {
                        rule.enabled = !rule.enabled;
                        b.setMessage(onOff(rule.enabled));
                    }
            ).lit(() -> rule.enabled);

            add(toggle, y);

            TextFieldWidget field = new TextFieldWidget(
                    textRenderer,
                    lx + 112,
                    y,
                    138,
                    16,
                    Text.literal(rule.key)
            );

            field.setMaxLength(40);
            field.setText(rule.value == null ? "" : rule.value);
            field.setChangedListener(value -> rule.value = value);

            add(field, y);
        }

        // PRAWA KOLUMNA - USTAWIENIA
        int rx = px + 280;
        int rw = 166;
        int y = py + 40;

        add(new FancyButton(
                rx, y, rw, 16,
                Text.literal("Mod: " + (cfg.enabled ? "ON" : "OFF")),
                b -> {
                    cfg.enabled = !cfg.enabled;
                    b.setMessage(Text.literal(
                            "Mod: " + (cfg.enabled ? "ON" : "OFF")
                    ));
                }
        ).lit(() -> cfg.enabled), y);

        y += 20;

        add(new FancyButton(
                rx, y, rw, 16,
                Text.literal("Animacje: " + (cfg.animations ? "ON" : "OFF")),
                b -> {
                    cfg.animations = !cfg.animations;
                    b.setMessage(Text.literal(
                            "Animacje: " + (cfg.animations ? "ON" : "OFF")
                    ));
                }
        ).lit(() -> cfg.animations), y);

        y += 20;

        rainbowBtn = new FancyButton(
                rx, y, rw, 16,
                Text.literal("Teczowy akcent: " + (cfg.rainbow ? "ON" : "OFF")),
                b -> {
                    cfg.rainbow = !cfg.rainbow;
                    b.setMessage(Text.literal(
                            "Teczowy akcent: " + (cfg.rainbow ? "ON" : "OFF")
                    ));
                }
        ).lit(() -> cfg.rainbow);

        add(rainbowBtn, y);

        // SUWAKI RGB
        y += 24;

        sr = new ColorSlider(
                rx, y, rw, 16,
                "R", cfg.r,
                value -> cfg.r = value
        );

        add(sr, y);

        y += 18;

        sg = new ColorSlider(
                rx, y, rw, 16,
                "G", cfg.g,
                value -> cfg.g = value
        );

        add(sg, y);

        y += 18;

        sb = new ColorSlider(
                rx, y, rw, 16,
                "B", cfg.b,
                value -> cfg.b = value
        );

        add(sb, y);

        // GOTOWE KOLORY
        y += 24;

        for (int i = 0; i < PRESETS.length; i++) {
            int color = PRESETS[i];

            add(new FancyButton(
                    rx + i * 28,
                    y,
                    22,
                    14,
                    Text.empty(),
                    b -> {
                        cfg.r = (color >> 16) & 255;
                        cfg.g = (color >> 8) & 255;
                        cfg.b = color & 255;

                        cfg.rainbow = false;

                        rainbowBtn.setMessage(
                                Text.literal("Teczowy akcent: OFF")
                        );

                        sr.sync(cfg.r);
                        sg.sync(cfg.g);
                        sb.sync(cfg.b);
                    }
            ).swatch(color), y);
        }

        // PRZYCISKI DOLNE
        int by = py + PH - 30;

        add(new FancyButton(
                rx, by, 80, 18,
                Text.literal("Reset"),
                b -> {
                    ModConfig.reset();
                    client.setScreen(new ConfigScreen());
                }
        ), by);

        add(new FancyButton(
                rx + 86, by, 80, 18,
                Text.literal("Gotowe"),
                b -> close()
        ), by);
    }

    @Override
    public void renderBackground(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Własne tło panelu.
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        long now = System.nanoTime();

        float dt = Math.min(
                (now - lastNs) / 1e9f,
                0.1f
        );

        lastNs = now;
        time = (now - startNs) / 1e9f;

        open = cfg.animations
                ? Math.min(1f, open + dt / 0.4f)
                : 1f;

        float inv = 1f - open;

        ease = 1f - inv * inv * inv;

        off = (int) ((1f - ease) * 24);

        for (int i = 0; i < movers.size(); i++) {
            ClickableWidget widget = movers.get(i);

            widget.setY(baseY.get(i) + off);
            widget.setAlpha(ease);
        }

        drawPanel(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawPanel(DrawContext context) {
        int accent = ModConfig.accentRgb();

        int x = px;
        int y = py + off;

        float pulse = cfg.animations
                ? 0.65f + 0.35f * (float) Math.sin(time * 2.5f)
                : 1f;

        // Przyciemnienie ekranu
        context.fill(
                0, 0, width, height,
                Gfx.argb(0.55f * ease, 0x000000)
        );

        // Poświata
        for (int i = 3; i >= 1; i--) {
            drawBorder(
                    context,
                    x - i,
                    y - i,
                    PW + i * 2,
                    PH + i * 2,
                    Gfx.argb(
                            ease * 0.10f * pulse * (4 - i),
                            accent
                    )
            );
        }

        // Tło panelu
        context.fill(
                x, y,
                x + PW, y + PH,
                Gfx.argb(ease * 0.95f, 0x14141C)
        );

        // Separator
        context.fill(
                x + 272,
                y + 36,
                x + 273,
                y + PH - 12,
                Gfx.argb(ease * 0.25f, accent)
        );

        // Animowany pasek górny
        for (int i = 0; i < PW; i += 4) {
            float k = cfg.animations
                    ? 0.55f + 0.45f * (float) Math.sin(time * 3f - i * 0.04f)
                    : 1f;

            context.fill(
                    x + i,
                    y,
                    Math.min(x + i + 4, x + PW),
                    y + 3,
                    Gfx.argb(
                            ease,
                            Gfx.mix(0x202030, accent, k)
                    )
            );
        }

        // Obramowanie
        drawBorder(
                context,
                x, y, PW, PH,
                Gfx.argb(ease * pulse, accent)
        );

        // Nagłówek
        context.drawTextWithShadow(
                textRenderer,
                Text.literal("Fake Scoreboard").formatted(Formatting.BOLD),
                x + 14,
                y + 12,
                Gfx.argb(ease, accent)
        );

        context.drawTextWithShadow(
                textRenderer,
                Text.literal("Zmiany tylko u Ciebie - serwer ich nie widzi"),
                x + 14,
                y + 24,
                Gfx.argb(ease, 0x8A8A99)
        );

        // Nazwy reguł
        for (int i = 0; i < cfg.rules.size(); i++) {
            context.drawTextWithShadow(
                    textRenderer,
                    Text.literal(cfg.rules.get(i).key),
                    px + 54,
                    py + 44 + i * 22 + off,
                    Gfx.argb(ease, 0xE0E0E0)
            );
        }
    }

    // Własne obramowanie zamiast nieistniejącego DrawContext.drawBorder()
    private void drawBorder(
            DrawContext context,
            int x,
            int y,
            int w,
            int h,
            int color
    ) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public void removed() {
        ModConfig.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
