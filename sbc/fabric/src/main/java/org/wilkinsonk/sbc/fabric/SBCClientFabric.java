package org.wilkinsonk.sbc.fabric;

import org.lwjgl.glfw.GLFW;
import org.wilkinsonk.sbc.fabric.channel.RequestServerConnect;
import org.wilkinsonk.sbc.fabric.channel.RequestServerList;
import org.wilkinsonk.sbc.fabric.channel.RespondServerList;
import org.wilkinsonk.sbc.fabric.screen.LoadingGui;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class SBCClientFabric implements ClientModInitializer {
    public static KeyBinding keyBinding;
    public static final String KeyBindingName                  = "key.sbc.server_selection";
    public static final KeyBinding.Category KeyBindingCategory = KeyBinding.Category.create(Identifier.of("sbc", "general"));

    @Override
    public void onInitializeClient() {
        RequestServerList.register();
        RequestServerConnect.register();
        RespondServerList.register();

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            KeyBindingName,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KeyBindingCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                if (client.getNetworkHandler() != null) {
                    client.setScreen(new CottonClientScreen(new LoadingGui("Fetching Server List... ")));
                    ClientPlayNetworking.send(new RequestServerList());
                }
            }
        });
    }
}
