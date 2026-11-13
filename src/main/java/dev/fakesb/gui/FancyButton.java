
package dev.fakesb.gui;

import dev.fakesb.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.math.MathHelper;

import java.util.function.BooleanSupplier;

public class FancyButton extends ButtonWidget {

    private float hover;
    private long last = System.nanoTime();

    private BooleanSupplier lit = () -> false;
    private int swatch = -1;

    public FancyButton(
            int x,
            int y,
            int w,
            int h,
            net.minecraft.text.Text msg,
            ButtonWidget.PressAction onPress
    ) {
        super(x, y, w, h, msg, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    public FancyButton lit(BooleanSupplier s) {
        this.lit = s;
        return this;
    }

    public FancyButton swatch(int rgb) {
        this.swatch = rgb;
        return this;
    }

    @Override
    protected void drawIcon(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        long now = System.nanoTime();

        float dt = Math.min(
                (now - last) / 1e9f,
                0.1f
        );

        last = now;

        hover = MathHelper.clamp(
                hover + (isHovered() ? dt * 8f : -dt * 8f),
                0f,
                1f
        );

        float alpha = getAlpha();

        int acc = ModConfig.accentRgb();

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Próbnik kolorów
        if (swatch >= 0) {
            int glow = (int) (hover * 2);

            context.fill(
                    x, y, x + w, y + h,
                    Gfx.argb(alpha, swatch)
            );

            drawOutline(
                    context,
                    x - glow,
                    y - glow,
                    w + glow * 2,
                    h + glow * 2,
                    Gfx.argb(alpha, 0xFFFFFF)
            );

            return;
        }

        boolean on = lit.getAsBoolean();

        int bg = Gfx.mix(
                0x1E1E2B,
                acc,
                0.08f + 0.27f * hover + (on ? 0.10f : 0f)
        );

        int border = Gfx.mix(
                0x3A3A4D,
                acc,
                Math.max(hover, on ? 0.6f : 0f)
        );

        // Tło
        context.fill(
                x, y, x + w, y + h,
                Gfx.argb(alpha, bg)
        );

        // Obramowanie
        drawOutline(
                context,
                x,
                y,
                w,
                h,
                Gfx.argb(alpha, border)
        );

        // Pasek aktywnego przycisku
        if (on) {
            context.fill(
                    x,
                    y,
                    x + 2,
                    y + h,
                    Gfx.argb(alpha, acc)
            );
        }
    }

    private void drawOutline(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        context.fill(
                x, y,
                x + width, y + 1,
                color
        );

        context.fill(
                x, y + height - 1,
                x + width, y + height,
                color
        );

        context.fill(
                x, y,
                x + 1, y + height,
                color
        );

        context.fill(
                x + width - 1, y,
                x + width, y + height,
                color
        );
    }
}
