package dev.fakesb.gui;

import dev.fakesb.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.BooleanSupplier;

/** Przycisk z animacja hover i kolorem akcentu. Tryb "swatch" rysuje probke koloru. */
public class FancyButton extends ButtonWidget {
    private float hover;
    private long last = System.nanoTime();
    private BooleanSupplier lit = () -> false;
    private int swatch = -1;

    public FancyButton(int x, int y, int w, int h, Text msg, PressAction onPress) {
        super(x, y, w, h, msg, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    public FancyButton lit(BooleanSupplier s) { this.lit = s; return this; }

    public FancyButton swatch(int rgb) { this.swatch = rgb; return this; }

    @Override
    public void renderWidget(DrawContext c, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float dt = Math.min((now - last) / 1e9f, 0.1f);
        last = now;
        hover = MathHelper.clamp(hover + (isHovered() ? dt * 8f : -dt * 8f), 0f, 1f);

        float al = getAlpha();
        int acc = ModConfig.accentRgb();
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();

        if (swatch >= 0) {
            int g = (int) (hover * 2);
            c.fill(x, y, x + w, y + h, Gfx.argb(al, swatch));
            c.drawBorder(x - g, y - g, w + g * 2, h + g * 2, Gfx.argb(al, 0xFFFFFF));
            return;
        }

        boolean on = lit.getAsBoolean();
        int bg = Gfx.mix(0x1E1E2B, acc, 0.08f + 0.27f * hover + (on ? 0.10f : 0f));
        int border = Gfx.mix(0x3A3A4D, acc, Math.max(hover, on ? 0.6f : 0f));
        c.fill(x, y, x + w, y + h, Gfx.argb(al, bg));
        c.drawBorder(x, y, w, h, Gfx.argb(al, border));
        if (on) c.fill(x, y, x + 2, y + h, Gfx.argb(al, acc));

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        Text msg = getMessage();
        int tx = x + (w - tr.getWidth(msg)) / 2;
        int ty = y + (h - 8) / 2;
        c.drawTextWithShadow(tr, msg, tx, ty, Gfx.argb(al, Gfx.mix(0xDDDDDD, 0xFFFFFF, hover)));
    }
}
