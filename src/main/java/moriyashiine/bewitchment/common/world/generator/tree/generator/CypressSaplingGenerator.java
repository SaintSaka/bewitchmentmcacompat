package moriyashiine.bewitchment.common.world.generator.tree.generator;

import moriyashiine.bewitchment.common.registry.BWWorldGenerators;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("unchecked")
public class CypressSaplingGenerator extends SaplingGenerator {
	@Nullable
	@Override
	protected ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random random, boolean bees) {
		return (ConfiguredFeature<TreeFeatureConfig, ?>) BWWorldGenerators.CYPRESS_TREE;
	}
}
