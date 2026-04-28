package com.armilp.ifreq;

import com.armilp.ifreq.network.IfreqNetworkService;

import java.util.ServiceLoader;

public interface IfreqPlatform {

    IfreqPlatform INSTANCE = load(IfreqPlatform.class);

    static IfreqPlatform getInstance() {
        return INSTANCE;
    }


    private static <T> T load(Class<T> clazz) {
        T loadedService = (T) ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new RuntimeException("Failed to load service: " + clazz.getName()));
        return loadedService;
    }

    IfreqNetworkService getNetworkService();


}
