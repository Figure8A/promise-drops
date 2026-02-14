package com.pdrops;

import com.pdrops.damage.DropsDamageTypes;
import com.pdrops.data.DropsDamageTypeTagProvider;
import com.pdrops.data.DropsGeneralRegistryProvider;
import com.pdrops.data.DropsWaypointStyleProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class PromiseDropsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(DropsGeneralRegistryProvider::new);
		pack.addProvider(DropsWaypointStyleProvider::new);
		pack.addProvider(DropsDamageTypeTagProvider::new);
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE, DropsDamageTypes::bootstrap);
	}
}
