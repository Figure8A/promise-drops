package com.pdrops.upgrades;

public class PlayerUpgradeGuiElement {

    private PlayerUpgradeCollectionType renderType;
    private PlayerUpgradeInstance upgradeInstance;

    public PlayerUpgradeGuiElement(PlayerUpgradeInstance upgradeInstance) {
        this(PlayerUpgradeCollectionType.UPGRADE);
        this.upgradeInstance = upgradeInstance;
    }
    public PlayerUpgradeGuiElement(PlayerUpgradeCollectionType renderType) {
        this.renderType = renderType;
    }

    public PlayerUpgradeInstance getUpgradeInstance() {
        return upgradeInstance;
    }

    public PlayerUpgradeCollectionType getRenderType() {
        return renderType;
    }


    public int mapToFirstLevel() {
        if (this.getUpgradeInstance() != null) {
            return this.getUpgradeInstance().getLevel() == 1 ? 1 : 0;
        }
        return 0;
    }
}
