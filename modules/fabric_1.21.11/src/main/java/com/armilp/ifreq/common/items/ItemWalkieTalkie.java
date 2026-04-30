package com.armilp.ifreq.common.items;

import com.armilp.ifreq.common.registry.ModComponents;
import com.armilp.ifreq.common.registry.ModSounds;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemWalkieTalkie extends Item implements GeoItem {

    public static final ComponentType<Double> FREQUENCY = ModComponents.FREQUENCY;
    public static final ComponentType<Boolean> POWER = ModComponents.POWER;

    private static final RawAnimation OFF_STATE_ANIM = RawAnimation.begin().thenPlayAndHold("off_walkie_state");
    private static final RawAnimation ON_STATE_ANIM = RawAnimation.begin().thenPlayAndHold("on_walkie_state");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final double maxDistance;

    public ItemWalkieTalkie(Settings settings, double maxDistance) {
        super(settings);
        this.maxDistance = maxDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
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
            boolean newState = !isOn(stack);
            setOn(stack, newState);

            if (newState) {
                world.playSound(null, player.getBlockPos(),
                        ModSounds.RADIO_BEEP,
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        1.0f, 1.0f);
            } else {
                world.playSound(null, player.getBlockPos(),
                        ModSounds.RADIO_NOISE,
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        1.0f, 1.0f);
            }

            player.sendMessage(
                    Text.translatable("message.ifreq.walkie_talkie.frequency",
                            String.format("%.1f", getFrequency(stack))), true
            );
        }

        return ActionResult.SUCCESS;
    }




    public static void setFrequency(ItemStack stack, double frequency) {
        stack.set(FREQUENCY, frequency);
    }

    public static double getFrequency(ItemStack stack) {
        return stack.getOrDefault(FREQUENCY, 0.0D);
    }

    public static boolean isOn(ItemStack stack) {
        return stack.getOrDefault(POWER, false);
    }

    public static void setOn(ItemStack stack, boolean on) {
        stack.set(POWER, on);
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>( "StateController", 0, state -> {

            Item stack = state.getData(DataTickets.ITEM);

            if (stack == null || stack.getDefaultStack().isEmpty()) {
                return PlayState.STOP;
            }

            return state.setAndContinue(
                    isOn(stack.getDefaultStack()) ? ON_STATE_ANIM : OFF_STATE_ANIM
            );
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}