package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MainEZ.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MainEZ.MODID);


    public static final RegistryObject<CreativeModeTab> EZFREQ_TAB = TABS.register("ezfreq_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.WALKIE_TALKIE.get()))
                    .title(Component.translatable("itemGroup.ifreq"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.WALKIE_TALKIE.get());
                    })
                    .build()
    );

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
