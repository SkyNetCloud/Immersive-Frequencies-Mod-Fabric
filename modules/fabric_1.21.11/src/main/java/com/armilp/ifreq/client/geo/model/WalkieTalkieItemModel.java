package com.armilp.ifreq.client.geo.model;

import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static com.armilp.ifreq.MainEZ.MODID;

public class WalkieTalkieItemModel extends GeoModel<ItemWalkieTalkie> {

    private final Identifier model = Identifier.of(MODID, "geo/walkie_talkie.geo.json");
    private final Identifier animations = Identifier.of(MODID, "animations/walkie_talkie.geo.animation.json");
    private final Identifier texture = Identifier.of(MODID, "textures/item/walkie_talkie_texture_model.png");


    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texture;
    }

    @Override
    public Identifier getAnimationResource(ItemWalkieTalkie animatable) {
        return animations;
    }
}
