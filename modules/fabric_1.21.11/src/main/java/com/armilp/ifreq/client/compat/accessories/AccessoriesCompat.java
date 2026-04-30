//package com.armilp.ifreq.client.compat.accessories;
//
//import com.armilp.ifreq.client.compat.accessories.renderer.WalkieAccessoryRenderer;
//import com.armilp.ifreq.common.registry.ModItems;
//import io.wispforest.accessories.api.AccessoriesCapability;
//import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
//import net.fabricmc.loader.api.FabricLoader;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.server.network.ServerPlayerEntity;
//
//public class AccessoriesCompat {
//
//    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("accessories");
//
//
//    public static void initClient() {
//        if (!LOADED) return;
//
//        AccessoriesRendererRegistry.registerRenderer(
//                ModItems.WALKIE_TALKIE,
//                WalkieAccessoryRenderer::new
//        );
//    }
//
//
//    public static boolean hasWalkie(PlayerEntity player) {
//        if (!LOADED) return false;
//
//        var cap = AccessoriesCapability.get(player);
//        return cap != null && cap.isEquipped(ModItems.WALKIE_TALKIE);
//    }
//
//
//    public static ItemStack getEquippedWalkie(ServerPlayerEntity player) {
//        if (!LOADED) return null;
//
//        var cap = AccessoriesCapability.get(player);
//        if (cap == null) return null;
//
//        for (var ref : cap.getEquipped(ModItems.WALKIE_TALKIE.asItem())) {
//            return ref.stack();
//        }
//
//        return null;
//    }
//
//
//    public static boolean isLoaded() {
//        return LOADED;
//    }
//}