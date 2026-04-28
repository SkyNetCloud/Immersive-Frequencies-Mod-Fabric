package com.armilp.ifreq;

import com.armilp.ifreq.common.WalkieHandler;
import com.armilp.ifreq.common.events.PlayerEventHandler;
import com.armilp.ifreq.common.registry.ModCreativeTab;
import com.armilp.ifreq.common.registry.ModItems;
import com.armilp.ifreq.common.registry.ModMenus;
import com.armilp.ifreq.common.registry.ModSounds;
import com.armilp.ifreq.network.IfreqNetwork;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;


public class MainEZ implements ModInitializer {

    public static final String MODID = "ifreq";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {

        ModItems.register();
        ModSounds.register();
        ModCreativeTab.register();
        IfreqNetwork.register();
        WalkieHandler.register();
        ModMenus.register();
        PlayerEventHandler.register();
    }


}
