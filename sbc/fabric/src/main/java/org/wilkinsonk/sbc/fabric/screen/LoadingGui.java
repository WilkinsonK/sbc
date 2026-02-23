package org.wilkinsonk.sbc.fabric.screen;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class LoadingGui extends LightweightGuiDescription {
    private static final int ROOT_WIDTH  = 240;
    private static final int ROOT_HEIGHT = 80;

    public LoadingGui(String message) {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(ROOT_WIDTH, ROOT_HEIGHT);

        WLabel title = new WLabel(Text.literal("Select Server"));
        root.add(title, 8, 6, 224, 12);

        root.add(new WSpinner(message), 0, 24, ROOT_WIDTH, 20);

        WButton cancelButton = new WButton(Text.literal("Cancel"));
        cancelButton.setOnClick(() -> MinecraftClient.getInstance().setScreen(null));
        root.add(cancelButton, 70, ROOT_HEIGHT - 20, 100, 16);

        root.validate(this);
    }

    private static class WSpinner extends WWidget {
        private static final char[] FRAMES = { '|', '/', '-', '\\' };
        private final String message;

        WSpinner(String m) {
            message = m;
        }

        @Override
        public boolean canResize() { return true; }

        @Override
        public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
            int frame = (int) ((System.currentTimeMillis() / 150) % FRAMES.length);
            var client = MinecraftClient.getInstance();
            String text = message + FRAMES[frame];
            int textWidth = client.textRenderer.getWidth(text);
            context.drawText(
                client.textRenderer,
                Text.literal(text),
                x + (getWidth() - textWidth) / 2,
                y + (getHeight() - 8) / 2,
                0xFF888888,
                false
            );
        }
    }
}
