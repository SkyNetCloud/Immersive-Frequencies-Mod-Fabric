package com.armilp.ifreq.client.geo.renderer;

import com.armilp.ifreq.client.geo.model.WalkieTalkieItemModel;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class WalkieGeoItemRenderer extends GeoItemRenderer<ItemWalkieTalkie>  {

    public <I extends ItemWalkieTalkie> WalkieGeoItemRenderer() {
        super(new WalkieTalkieItemModel());
    }

}
