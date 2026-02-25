package org.wilkinsonk.sbc.fabric.screen;

import java.util.List;

import org.wilkinsonk.sbc.fabric.channel.RequestServerConnect;
import org.wilkinsonk.sbc.model.ServerEntry;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ServerSelectionGui extends LightweightGuiDescription {
    private static final int ROOT_WIDTH   = 240;
    private static final int ROOT_HEIGHT  = 212;
    private static final int FONT_HEIGHT  = 12;
    private static final int ENTRY_HEIGHT = 28;

    public ServerSelectionGui(List<ServerEntry> servers) {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(ROOT_WIDTH, ROOT_HEIGHT);

        // Menu Title.
        WLabel title = new WLabel(Text.literal("Select Server"));
        root.add(title, 8, 6, 224, FONT_HEIGHT);

        // Selection List
        WListPanel<ServerEntry, ServerEntryWidget> list =
            new WListPanel<>(servers, ServerEntryWidget::new, (entry, widget) -> widget.setEntry(entry));
        list.setListItemHeight(ENTRY_HEIGHT);
        root.add(list, 0, 20, 236, 148);

        // Exit/Cancel button.
        WButton cancelButton = new WButton(Text.literal("Cancel"));
        cancelButton.setOnClick(() -> MinecraftClient.getInstance().setScreen(null));
        root.add(cancelButton, 70, ROOT_HEIGHT-20, 100, 16);

        root.validate(this);
    }

    public static class ServerEntryWidget extends WWidget {
        private ServerEntry entry;

        @Override
        public boolean canResize() {
            return true;
        }

        public void setEntry(ServerEntry entry) {
            this.entry = entry;
        }

        @Override
        public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
            if (entry == null) return;

            boolean hovered = entry.isOnline()
                           && !entry.isCurrentPlayerServer()
                           && mouseX >= 0 && mouseX < getWidth()
                           && mouseY >= 0 && mouseY < getHeight();

            // Button background
            context.fill(x, y, x + getWidth(), y + getHeight(), hovered ? 0xFF4A4A4A : 0xFF383838);

            // Online/offline status dot
            if (entry.isCurrentPlayerServer()) {
                context.fill(x + 5, y + 12, x + 9, y + 16,  0xFF0AF2EE);
            } else {
                context.fill(x + 5, y + 12, x + 9, y + 16, entry.isOnline() ? 0xFF55FF55 : 0xFFFF5555);
            }

            // Icon (if set)
            int textOffset = 14;
            if (!entry.iconMaterial().isEmpty()) {
                Item item = Registries.ITEM.get(Identifier.of(entry.iconMaterial()));
                context.drawItem(new ItemStack(item), x + 14, y + 6);
                textOffset = 34;
            }

            // Server name and ID
            int nameColor = entry.isOnline() ? 0xFFFFFFFF : 0xFF888888;
            var client = MinecraftClient.getInstance();
            context.drawText(client.textRenderer, Text.literal(entry.name()), x + textOffset, y + 6,  nameColor,  false);
            context.drawText(client.textRenderer, Text.literal(entry.id()),   x + textOffset, y + 16, 0xFF666666, false);
        }

        @Override
        public InputResult onClick(Click click, boolean doubled) {
            if (entry == null || !entry.isOnline() || entry.isCurrentPlayerServer()) return InputResult.IGNORED;
            ClientPlayNetworking.send(new RequestServerConnect(entry.id()));
            MinecraftClient.getInstance().setScreen(new CottonClientScreen(new LoadingGui("Connnecting... ")));
            return InputResult.PROCESSED;
        }
    }
}
