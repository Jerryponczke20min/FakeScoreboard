package dev.fakesb;

import dev.fakesb.gui.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FakeSbClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfig.load();

        KeyBinding open = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fakesb.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of("fakesb", "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (open.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ConfigScreen());
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("fakesb").executes(ctx -> {
                    MinecraftClient mc = ctx.getSource().getClient();
                    mc.send(() -> mc.setScreen(new ConfigScreen()));
                    return 1;
                })));
    }
}
