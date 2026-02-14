package com.pdrops.entity.state;

import com.pdrops.upgrades.PlayerUpgradeCollectionType;
import com.pdrops.upgrades.PlayerUpgradeInstance;
import net.minecraft.client.render.entity.state.EntityRenderState;

public class UpgradeEntityState extends EntityRenderState {

    public float hoverTicks;
    public PlayerUpgradeInstance upgrade;
    public PlayerUpgradeCollectionType collectionType;
}