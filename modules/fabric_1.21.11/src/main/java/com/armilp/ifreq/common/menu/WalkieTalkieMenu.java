package com.armilp.ifreq.common.menu;

import com.armilp.ifreq.common.registry.ModMenus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenHandler;

public class WalkieTalkieMenu extends ScreenHandler {

    public final ItemStack itemStack;

    public WalkieTalkieMenu(int syncId, PlayerInventory inv) {
        this(syncId, inv, inv.player.getMainHandStack());
    }

    // Server-side: direct creation
    public WalkieTalkieMenu(int syncId, PlayerInventory ignoredInv, ItemStack itemStack) {
        super(ModMenus.WALKIE_TALKIE_MENU, syncId);
        this.itemStack = itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }
}