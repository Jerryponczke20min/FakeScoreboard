package dev.fakesb.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

public class ColorSlider extends SliderWidget {
    private final String label;
    private final IntConsumer setter;

    public ColorSlider(int x, int y, int w, int h, String label, int initial, IntConsumer setter) {
        super(x, y, w, h, Text.empty(), initial / 255.0);
        this.label = label;
        this.setter = setter;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(label + ": " + (int) Math.round(value * 255)));
    }

    @Override
    protected void applyValue() {
        setter.accept((int) Math.round(value * 255));
    }

    public void sync(int v) {
        this.value = v / 255.0;
        updateMessage();
    }
}
