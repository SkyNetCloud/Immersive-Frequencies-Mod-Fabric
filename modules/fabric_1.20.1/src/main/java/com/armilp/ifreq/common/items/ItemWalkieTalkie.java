package com.armilp.ifreq.common.items;

import com.armilp.ifreq.client.geo.renderer.WalkieGeoItemRenderer;
import com.armilp.ifreq.common.config.IfreqConfig;
import com.armilp.ifreq.common.registry.ModSounds;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemWalkieTalkie extends Item implements GeoItem {
    private static final RawAnimation OFF_STATE_ANIM = RawAnimation.begin().thenPlayAndHold("off_walkie_state");
    private static final RawAnimation ON_STATE_ANIM = RawAnimation.begin().thenPlay("on_walkie_state");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    private final String configKey;

    public ItemWalkieTalkie(Settings settings, String configKey) {
        super(settings);
        this.configKey = configKey;
    }

    public double getMaxDistance() {
        return IfreqConfig.getRange(configKey);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.ifreq.walkie_talkie.tooltip.frequency",
                String.format("%.1f", getFrequency(stack))));
        tooltip.add(Text.translatable("item.ifreq.walkie_talkie.tooltip.range",
                (int) getMaxDistance()));
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            boolean currentlyOn = isOn(stack);
            boolean newState = !currentlyOn;
            setOn(stack, newState);


//            world.playSound(null, player.getBlockPos(),
//                        newState ? ModSounds.RADIO_BEEP : ModSounds.RADIO_BEEP,
//                        SoundCategory.PLAYERS, getVolume(stack), 0.1f);


            player.sendMessage(
                    Text.translatable("message.ifreq.walkie_talkie.frequency",
                            String.format("%.1f", getFrequency(stack))), true
            );
        }
        return TypedActionResult.success(stack);
    }


    private static NbtCompound getOrCreateNbt(ItemStack stack) {
        if (!stack.hasNbt()) {
            stack.setNbt(new NbtCompound());
        }
        return stack.getNbt();
    }

    @Nullable
    private static NbtCompound getNbt(ItemStack stack) {
        return stack.getNbt();
    }

    public static void setFrequency(ItemStack stack, double frequency) {
        NbtCompound nbt = getOrCreateNbt(stack);
        nbt.putDouble("Frequency", frequency);
    }

    public static double getFrequency(ItemStack stack) {
        NbtCompound nbt = getNbt(stack);
        if (nbt == null) return 0.0D;
        return nbt.getDouble("Frequency");
    }

    public static boolean isOn(ItemStack stack) {
        NbtCompound nbt = getNbt(stack);
        if (nbt == null) return false;
        return nbt.getBoolean("On");
    }

    public static void setOn(ItemStack stack, boolean on) {
        NbtCompound nbt = getOrCreateNbt(stack);
        nbt.putBoolean("On", on);
    }

    public static float getVolume(ItemStack stack) {
        if (!stack.hasNbt() || !stack.getNbt().contains("Volume")) return 1.0f;
        return stack.getNbt().getFloat("Volume");
    }

    public static void setVolume(ItemStack stack, float volume) {
        stack.getOrCreateNbt().putFloat("Volume", volume);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private WalkieGeoItemRenderer renderer;
            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
               if (this.renderer == null)
                   this.renderer = new WalkieGeoItemRenderer();

               return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "StateController", 0, state -> {

            ItemStack stack = state.getData(DataTickets.ITEMSTACK);

            if (stack == null) {
                return PlayState.STOP;
            }

            if (isOn(stack)) {
                return state.setAndContinue(ON_STATE_ANIM);
            } else {
                return state.setAndContinue(OFF_STATE_ANIM);
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}