package com.armilp.ifreq.common.items;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class ItemWalkieTalkie extends Item {

    private final double maxDistance;

    public ItemWalkieTalkie(Item.Settings settings, double maxDistance) {
        super(settings);
        this.maxDistance = maxDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return super.getTooltipData(stack);
    }

        @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.translatable("item.ifreq.walkie_talkie.tooltip.frequency",
                String.format("%.1f", getFrequency(stack))));
        textConsumer.accept(Text.translatable("item.ifreq.walkie_talkie.tooltip.range",
                (int) maxDistance));
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient()) {
            player.sendMessage(
                    Text.translatable("message.ifreq.walkie_talkie.frequency",
                            String.format("%.1f", getFrequency(stack))), true
            );
        }
        return ActionResult.SUCCESS;
    }

    // ── Helpers to read/write custom data via CUSTOM_DATA component ──────────

    private static NbtCompound getOrCreateNbt(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();
        return nbt;
    }

    private static void saveNbt(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static @Nullable NbtCompound getNbt(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.copyNbt() : null;
    }

    // ── API ──────────────────────────────────────────────────────────────────

    public static void setFrequency(ItemStack stack, double frequency) {
        NbtCompound nbt = getOrCreateNbt(stack);
        nbt.putDouble("Frequency", frequency);
        saveNbt(stack, nbt);
    }

    public static double getFrequency(ItemStack stack) {
        NbtCompound nbt = getNbt(stack);
        if (nbt == null) return 0.0D;
        return nbt.getDouble("Frequency").orElse(0.0D);
    }

    public static boolean isOn(ItemStack stack) {
        NbtCompound nbt = getNbt(stack);
        if (nbt == null) return false;
        return nbt.getBoolean("On").orElse(false);
    }

    public static void setOn(ItemStack stack, boolean on) {
        NbtCompound nbt = getOrCreateNbt(stack);
        nbt.putBoolean("On", on);
        saveNbt(stack, nbt);
    }
}