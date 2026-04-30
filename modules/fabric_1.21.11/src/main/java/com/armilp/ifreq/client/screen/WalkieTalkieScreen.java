package com.armilp.ifreq.client.screen;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import com.armilp.ifreq.network.WalkieFrequencyPacket;
import com.armilp.ifreq.network.WalkiePowerPacket;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class WalkieTalkieScreen extends HandledScreen<WalkieTalkieMenu> {

    private static final Identifier WALKIE_GUI =
            Identifier.of(MainEZ.MODID, "textures/gui/walkie_gui.png");
    private static final Identifier TEX_ON =
            Identifier.of(MainEZ.MODID, "textures/item/walkie_talkie_on.png");
    private static final Identifier TEX_OFF =
            Identifier.of(MainEZ.MODID, "textures/item/walkie_talkie_off.png");

    private static final int GUI_W = 162;
    private static final int GUI_H = 162;

    private static final double FM_MIN = 87.5;
    private static final double FM_MAX = 108.0;

    private static final int TITLE_Y       = 5;
    private static final int FREQ_LABEL_Y  = 18;
    private static final int RANGE_LABEL_Y = 27;

    private static final int EDITBOX_W = 60;
    private static final int EDITBOX_H = 18;
    private static final int EDITBOX_X = (GUI_W - EDITBOX_W) / 2;
    private static final int EDITBOX_Y = 44;

    private static final int SET_BTN_W = 50;
    private static final int SET_BTN_H = 16;
    private static final int SET_BTN_X = (GUI_W - SET_BTN_W) / 2;
    private static final int SET_BTN_Y = 67;

    private static final int POWER_LABEL_Y = 93;
    private static final int ICON_SIZE     = 16;
    private static final int ICON_Y        = 105;

    private static final int TOGGLE_W = 50;
    private static final int TOGGLE_H = 16;
    private static final int TOGGLE_Y = 125;
    private static final int OFF_X    = 16;
    private static final int ON_X     = 96;

    private static final int OFF_ICON_X = OFF_X + (TOGGLE_W - ICON_SIZE) / 2;
    private static final int ON_ICON_X  = ON_X  + (TOGGLE_W - ICON_SIZE) / 2;

    private static final int TEXT_COLOR = -1;



    private double frequency;
    private boolean isOn;

    private TextFieldWidget frequencyInput;
    private ButtonWidget setButton;
    private ButtonWidget onButton;
    private ButtonWidget offButton;

    public WalkieTalkieScreen(WalkieTalkieMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title);
        this.backgroundWidth  = GUI_W;
        this.backgroundHeight = GUI_H;

        ItemStack stack = menu.itemStack;
        this.frequency = ItemWalkieTalkie.getFrequency(stack);
        this.isOn      = ItemWalkieTalkie.isOn(stack);
        this.frequency = clampFM(frequency);
    }

    @Override
    protected void init() {
        super.init();

        frequencyInput = new TextFieldWidget(
                this.textRenderer,
                x + EDITBOX_X, y + EDITBOX_Y,
                EDITBOX_W, EDITBOX_H,
                Text.empty()
        );
        frequencyInput.setMaxLength(5);
        frequencyInput.setText(String.format("%.1f", frequency));
        frequencyInput.setEditableColor(-1);
        frequencyInput.setChangedListener(this::onFrequencyInputChanged);

        setButton = ButtonWidget.builder(
                Text.translatable("gui.ifreq.walkie_talkie.set"),
                btn -> applyFrequency()
        ).dimensions(x + SET_BTN_X, y + SET_BTN_Y, SET_BTN_W, SET_BTN_H).build();

        offButton = ButtonWidget.builder(
                Text.translatable("gui.ifreq.walkie_talkie.off"),
                btn -> setPowerState(false)
        ).dimensions(x + OFF_X, y + TOGGLE_Y, TOGGLE_W, TOGGLE_H).build();

        onButton = ButtonWidget.builder(
                Text.translatable("gui.ifreq.walkie_talkie.on"),
                btn -> setPowerState(true)
        ).dimensions(x + ON_X, y + TOGGLE_Y, TOGGLE_W, TOGGLE_H).build();

        this.addDrawableChild(frequencyInput);
        this.addDrawableChild(setButton);
        this.addDrawableChild(offButton);
        this.addDrawableChild(onButton);

        setInitialFocus(frequencyInput);
        updatePowerButtons();
    }

    private void onFrequencyInputChanged(String raw) {
        String text  = raw.replace(',', '.');
        boolean valid = isValidFM(text);
        frequencyInput.setEditableColor(valid ? -1 : 1673355);
        setButton.active = valid;
    }

    private void applyFrequency() {
        try {
            double newFreq = clampFM(Double.parseDouble(
                    frequencyInput.getText().replace(',', '.')
            ));
            ItemWalkieTalkie.setFrequency(handler.itemStack, newFreq);
            ClientPlayNetworking.send(new WalkieFrequencyPacket(newFreq));
            this.client.player.closeHandledScreen();
        } catch (NumberFormatException ignored) {}
    }

    private void setPowerState(boolean on) {
        this.isOn = on;
        ItemWalkieTalkie.setOn(handler.itemStack, on);
        ClientPlayNetworking.send(new WalkiePowerPacket(on));
        updatePowerButtons();
    }

    private void updatePowerButtons() {
        onButton.active  = !isOn;
        offButton.active = isOn;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderPipeline pipline = RenderPipelines.GUI_TEXTURED;

        context.drawTexture(pipline,WALKIE_GUI, x, y, 0, 0, GUI_W, GUI_H, GUI_W, GUI_H);

        context.drawTexture(pipline,TEX_OFF, x + OFF_ICON_X, y + ICON_Y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        context.drawTexture(pipline,TEX_ON,  x + ON_ICON_X,  y + ICON_Y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        String title = Text.translatable("gui.ifreq.walkie_talkie.title").getString();
        context.drawText(textRenderer, title, (GUI_W - textRenderer.getWidth(title)) / 2, TITLE_Y, TEXT_COLOR, false);

        float scale = 0.8f;
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        int lx = (int)(10 / scale);
        context.drawText(textRenderer,
                Text.translatable("gui.ifreq.walkie_talkie.frequency_label").getString(),
                lx, (int)(FREQ_LABEL_Y / scale), TEXT_COLOR, false);
        context.drawText(textRenderer,
                Text.translatable("gui.ifreq.walkie_talkie.range_label",
                        String.format("%.1f", FM_MIN), String.format("%.1f", FM_MAX)).getString(),
                lx, (int)(RANGE_LABEL_Y / scale), TEXT_COLOR, false);

        String powerLabel = Text.translatable("gui.ifreq.walkie_talkie.power_label").getString();
        int pw = textRenderer.getWidth(powerLabel);
        context.drawText(textRenderer, powerLabel,
                (int)((GUI_W / scale - pw) / 2), (int)(POWER_LABEL_Y / scale), TEXT_COLOR, false);

        context.getMatrices().popMatrix();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context,mouseX,mouseY,delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
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