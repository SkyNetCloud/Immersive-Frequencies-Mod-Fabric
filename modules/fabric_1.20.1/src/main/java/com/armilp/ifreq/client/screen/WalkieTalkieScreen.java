package com.armilp.ifreq.client.screen;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import com.armilp.ifreq.network.ModPackets;
import com.armilp.ifreq.network.WalkieFrequencyPacket;
import com.armilp.ifreq.network.WalkiePowerPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class WalkieTalkieScreen extends AbstractContainerScreen<WalkieTalkieMenu> {

    private static final ResourceLocation WALKIE_GUI =
            new ResourceLocation(MainEZ.MODID, "textures/gui/walkie_gui.png");
    private static final ResourceLocation TEX_ON =
            new ResourceLocation(MainEZ.MODID, "textures/item/walkie_talkie_on.png");
    private static final ResourceLocation TEX_OFF =
            new ResourceLocation(MainEZ.MODID, "textures/item/walkie_talkie_off.png");

    private static final int GUI_W = 162;
    private static final int GUI_H = 162;

    private static final double FM_MIN = 87.5;
    private static final double FM_MAX = 108.0;

    // Sección frecuencia
    private static final int TITLE_Y        = 5;
    private static final int FREQ_LABEL_Y   = 18;
    private static final int RANGE_LABEL_Y  = 27;

    private static final int EDITBOX_W = 60;
    private static final int EDITBOX_H = 18;
    private static final int EDITBOX_X = (GUI_W - EDITBOX_W) / 2;  // 51
    private static final int EDITBOX_Y = 44;

    private static final int SET_BTN_W = 50;
    private static final int SET_BTN_H = 16;
    private static final int SET_BTN_X = (GUI_W - SET_BTN_W) / 2;  // 56
    private static final int SET_BTN_Y = 67;

    // Sección encendido (centrada simétricamente: margen=16, gap=30, margen=16)
    private static final int POWER_LABEL_Y = 93;
    private static final int ICON_SIZE     = 16;
    private static final int ICON_Y        = 105;

    private static final int TOGGLE_W = 50;
    private static final int TOGGLE_H = 16;
    private static final int TOGGLE_Y = 125;
    private static final int OFF_X    = 16;
    private static final int ON_X     = 96;   // 16 + 50 + 30 (gap) = 96

    private static final int OFF_ICON_X = OFF_X + (TOGGLE_W - ICON_SIZE) / 2;  // 33
    private static final int ON_ICON_X  = ON_X  + (TOGGLE_W - ICON_SIZE) / 2;  // 113

    private static final int TEXT_COLOR = 0x404040;

    private double frequency;
    private boolean isOn;

    private EditBox frequencyInput;
    private Button  setButton;
    private Button  onButton;
    private Button  offButton;

    public WalkieTalkieScreen(WalkieTalkieMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;

        ItemStack  stack = menu.itemStack;
        CompoundTag tag  = stack.getTag();
        this.frequency = (tag != null && tag.contains("Frequency")) ? tag.getDouble("Frequency") : FM_MIN;
        this.isOn      = tag != null && tag.getBoolean("On");
        this.frequency = clampFM(frequency);
    }

    @Override
    protected void init() {
        super.init();

        frequencyInput = new EditBox(
                this.minecraft.font,
                leftPos + EDITBOX_X, topPos + EDITBOX_Y,
                EDITBOX_W, EDITBOX_H,
                Component.empty()
        );
        frequencyInput.setMaxLength(5);
        frequencyInput.setFilter(str -> str.matches("[0-9.,]*"));
        frequencyInput.setValue(String.format("%.1f", frequency));
        frequencyInput.setTextColor(0xFFFFFF);
        frequencyInput.setResponder(this::onFrequencyInputChanged);

        setButton = Button.builder(Component.translatable("gui.ifreq.walkie_talkie.set"), btn -> applyFrequency())
                .bounds(leftPos + SET_BTN_X, topPos + SET_BTN_Y, SET_BTN_W, SET_BTN_H)
                .build();

        offButton = Button.builder(Component.translatable("gui.ifreq.walkie_talkie.off"), btn -> setPowerState(false))
                .bounds(leftPos + OFF_X, topPos + TOGGLE_Y, TOGGLE_W, TOGGLE_H)
                .build();

        onButton = Button.builder(Component.translatable("gui.ifreq.walkie_talkie.on"), btn -> setPowerState(true))
                .bounds(leftPos + ON_X, topPos + TOGGLE_Y, TOGGLE_W, TOGGLE_H)
                .build();

        this.addRenderableWidget(frequencyInput);
        this.addRenderableWidget(setButton);
        this.addRenderableWidget(offButton);
        this.addRenderableWidget(onButton);

        setInitialFocus(frequencyInput);
        updatePowerButtons();
    }

    private void onFrequencyInputChanged(String raw) {
        String text  = raw.replace(',', '.');
        boolean valid = isValidFM(text);
        frequencyInput.setTextColor(valid ? 0xFFFFFF : 0xFF5555);
        setButton.active = valid;
    }

    private void applyFrequency() {
        try {
            double newFreq = clampFM(Double.parseDouble(frequencyInput.getValue().replace(',', '.')));
            menu.itemStack.getOrCreateTag().putDouble("Frequency", newFreq);
            ModPackets.CHANNEL2.sendToServer(new WalkieFrequencyPacket(newFreq));
            this.minecraft.player.closeContainer();
        } catch (NumberFormatException ignored) {}
    }

    private void setPowerState(boolean on) {
        this.isOn = on;
        menu.itemStack.getOrCreateTag().putBoolean("On", on);
        ModPackets.CHANNEL2.sendToServer(new WalkiePowerPacket(on));
        updatePowerButtons();
    }

    private void updatePowerButtons() {
        onButton.active  = !isOn;
        offButton.active = isOn;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(WALKIE_GUI, leftPos, topPos, 0, 0, GUI_W, GUI_H, GUI_W, GUI_H);

        g.blit(TEX_OFF, leftPos + OFF_ICON_X, topPos + ICON_Y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        g.blit(TEX_ON,  leftPos + ON_ICON_X,  topPos + ICON_Y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        String title = Component.translatable("gui.ifreq.walkie_talkie.title").getString();
        g.drawString(font, title, (GUI_W - font.width(title)) / 2, TITLE_Y, TEXT_COLOR, false);

        float scale = 0.8f;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);

        int lx = (int)(10 / scale);
        g.drawString(font, Component.translatable("gui.ifreq.walkie_talkie.frequency_label").getString(),
                lx, (int)(FREQ_LABEL_Y / scale), TEXT_COLOR, false);
        g.drawString(font, Component.translatable("gui.ifreq.walkie_talkie.range_label",
                        String.format("%.1f", FM_MIN), String.format("%.1f", FM_MAX)).getString(),
                lx, (int)(RANGE_LABEL_Y / scale), TEXT_COLOR, false);

        String powerLabel = Component.translatable("gui.ifreq.walkie_talkie.power_label").getString();
        int pw = font.width(powerLabel);
        g.drawString(font, powerLabel, (int)((GUI_W / scale - pw) / 2), (int)(POWER_LABEL_Y / scale), TEXT_COLOR, false);

        g.pose().popPose();
    }


    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (frequencyInput.isFocused() && frequencyInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (frequencyInput.isFocused() && frequencyInput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    private boolean isValidFM(String text) {
        try {
            double v = Double.parseDouble(text);
            return v >= FM_MIN && v <= FM_MAX && roundToTenth(v) == v;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static double roundToTenth(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double clampFM(double v) {
        return roundToTenth(Math.max(FM_MIN, Math.min(FM_MAX, v)));
    }
}