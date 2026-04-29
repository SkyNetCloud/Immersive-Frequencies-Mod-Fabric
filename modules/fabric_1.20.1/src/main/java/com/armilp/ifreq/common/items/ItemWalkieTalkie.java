package com.armilp.ifreq.common.items;

import com.armilp.ifreq.client.geo.renderer.WalkieGeoItemRenderer;
import com.armilp.ifreq.common.registry.ModSounds;
import io.wispforest.accessories.api.AccessoryItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemWalkieTalkie extends AccessoryItem implements GeoItem {
    private static final RawAnimation OFF_STATE_ANIM = RawAnimation.begin().thenPlay("off_state");
    private static final RawAnimation ON_STATE_ANIM = RawAnimation.begin().thenPlay("on_state");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    private final double maxDistance;

    public ItemWalkieTalkie(Settings settings, double maxDistance) {
        super(settings);
        this.maxDistance = maxDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.ifreq.walkie_talkie.tooltip.frequency",
                String.format("%.1f", getFrequency(stack))));
        tooltip.add(Text.translatable("item.ifreq.walkie_talkie.tooltip.range",
                (int) maxDistance));
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            boolean currentlyOn = isOn(stack);
            boolean newState = !currentlyOn;
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

    // ── API for 1.20.1 ─────────────────────────────────────────────────────────

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

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}