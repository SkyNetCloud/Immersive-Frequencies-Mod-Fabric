package com.armilp.ifreq.common.menu;

import com.armilp.ifreq.common.registry.ModMenus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class WalkieTalkieMenu extends ScreenHandler {

    public final ItemStack itemStack;

    // Client constructor (called via network)
    public WalkieTalkieMenu(int syncId, PlayerInventory inv) {
        this(syncId, inv, inv.player.getMainHandStack());
    }

    // Server constructor
    public WalkieTalkieMenu(int syncId, PlayerInventory inv, ItemStack itemStack) {
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