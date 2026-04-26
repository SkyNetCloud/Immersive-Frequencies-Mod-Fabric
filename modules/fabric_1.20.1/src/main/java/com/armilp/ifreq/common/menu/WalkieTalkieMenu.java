package com.armilp.ifreq.common.menu;

import com.armilp.ifreq.common.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class WalkieTalkieMenu extends AbstractContainerMenu {

    public final ItemStack itemStack;

    public WalkieTalkieMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.getMainHandItem());
    }

    public WalkieTalkieMenu(int id, Inventory inv, ItemStack itemStack) {
        super(ModMenus.WALKIE_TALKIE_MENU.get(), id);
        this.itemStack = itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
