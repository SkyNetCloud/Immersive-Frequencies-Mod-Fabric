package com.armilp.ifreq.common.platform.fabric;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.armilp.ifreq.MainEZ.MODID;

public class IfreqFabricPayload<M> implements CustomPayload {
    private final M data;
    private final CustomPayload.Id<? extends CustomPayload> type;

    public IfreqFabricPayload(M data) {
        this.data = data;
        this.type = new CustomPayload.Id<>(Identifier.of(MODID, data.getClass().getSimpleName().toLowerCase()));
    }


    public CustomPayload.@NotNull Id<? extends CustomPayload> getId() {
        return this.type;
    }

    public M getData() {
        return this.data;
    }
}
