package com.armilp.ifreq.common.platform.fabric;

import com.armilp.ifreq.IfreqPlatform;
import com.armilp.ifreq.network.IfreqNetworkService;

public class IfreqFabricPlatform implements IfreqPlatform {


    private final IfreqFabricNetworkService networkService;

    public IfreqFabricPlatform(IfreqFabricNetworkService networkService) {
        this.networkService = networkService;
    }

    public IfreqFabricPlatform() {
        this.networkService = new IfreqFabricNetworkService();
    }


    public IfreqNetworkService getNetworkService() {
        return this.networkService;
    }

}
