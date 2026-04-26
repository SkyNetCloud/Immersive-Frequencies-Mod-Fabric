package com.armilp.ifreq.common.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemWalkieTalkie extends Item {

    private final double maxDistance;

    public ItemWalkieTalkie(Properties properties, double maxDistance) {
        super(properties);
        this.maxDistance = maxDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ifreq.walkie_talkie.tooltip.frequency",
                String.format("%.1f", getFrequency(stack))));
        tooltip.add(Component.translatable("item.ifreq.walkie_talkie.tooltip.range",
                (int) maxDistance));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            player.displayClientMessage(
                    Component.translatable("message.ifreq.walkie_talkie.frequency",
                            String.format("%.1f", getFrequency(stack))), true
            );
        }
        return InteractionResultHolder.success(stack);
    }

    public static void setFrequency(ItemStack stack, double frequency) {
        stack.getOrCreateTag().putDouble("Frequency", frequency);
    }

    public static double getFrequency(ItemStack stack) {
        if (!stack.hasTag()) return 0.0D;
        return stack.getTag().getDouble("Frequency");
    }

    public static boolean isOn(ItemStack stack) {
        if (!stack.hasTag()) return false;
        return stack.getTag().getBoolean("On");
    }

    public static void setOn(ItemStack stack, boolean on) {
        stack.getOrCreateTag().putBoolean("On", on);
    }
}