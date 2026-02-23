package org.wilkinsonk.sbc.fabric;

import org.lwjgl.glfw.GLFW;
import org.wilkinsonk.sbc.fabric.payload.RequestServerConnect;
import org.wilkinsonk.sbc.fabric.payload.RequestServerList;
import org.wilkinsonk.sbc.fabric.payload.RespondServerList;
import org.wilkinsonk.sbc.fabric.screen.ServerSelectionGui;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class SBCClientFabric implements ClientModInitializer {
    public static KeyBinding keyBinding;
    public static final String KeyBindingName                  = "key.sbc.server_selection";
    public static final KeyBinding.Category KeyBindingCategory = KeyBinding.Category.create(Identifier.of("sbc", "general"));

    @SuppressWarnings("null")
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(RequestServerList.ID, RequestServerList.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestServerConnect.ID, RequestServerConnect.CODEC);
        PayloadTypeRegistry.playS2C().register(RespondServerList.ID, RespondServerList.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(RespondServerList.ID, (payload, context) -> {
            context.client().execute(() ->
                context.client().setScreen(new CottonClientScreen(new ServerSelectionGui(payload.servers())))
            );
        });

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            KeyBindingName,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KeyBindingCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                if (client.getNetworkHandler() != null) {
                    ClientPlayNetworking.send(new RequestServerList());
                }
            }
        });
    }
}
