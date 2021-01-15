package moriyashiine.bewitchment.client;

import com.terraformersmc.terraform.sign.SpriteIdentifierRegistry;
import moriyashiine.bewitchment.client.misc.SpriteIdentifiers;
import moriyashiine.bewitchment.client.model.armor.WitchArmorModel;
import moriyashiine.bewitchment.client.network.packet.*;
import moriyashiine.bewitchment.client.particle.CauldronBubbleParticle;
import moriyashiine.bewitchment.client.particle.IncenseSmokeParticle;
import moriyashiine.bewitchment.client.renderer.blockentity.BrazierBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.blockentity.PoppetShelfBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.blockentity.WitchAltarBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.blockentity.WitchCauldronBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.entity.SilverArrowEntityRenderer;
import moriyashiine.bewitchment.client.renderer.entity.living.*;
import moriyashiine.bewitchment.common.Bewitchment;
import moriyashiine.bewitchment.common.block.entity.BWChestBlockEntity;
import moriyashiine.bewitchment.common.registry.BWBlockEntityTypes;
import moriyashiine.bewitchment.common.registry.BWEntityTypes;
import moriyashiine.bewitchment.common.registry.BWObjects;
import moriyashiine.bewitchment.common.registry.BWParticleTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BewitchmentClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(CreateNonLivingEntityPacket.ID, CreateNonLivingEntityPacket::handle);
		ClientSidePacketRegistry.INSTANCE.register(SyncWitchAltarBlockEntity.ID, SyncWitchAltarBlockEntity::handle);
		ClientSidePacketRegistry.INSTANCE.register(SyncBrazierBlockEntity.ID, SyncBrazierBlockEntity::handle);
		ClientSidePacketRegistry.INSTANCE.register(SyncPoppetShelfBlockEntity.ID, SyncPoppetShelfBlockEntity::handle);
		ClientSidePacketRegistry.INSTANCE.register(SyncClientSerializableBlockEntity.ID, SyncClientSerializableBlockEntity::handle);
		ClientSidePacketRegistry.INSTANCE.register(SpawnSmokeParticlesPacket.ID, SpawnSmokeParticlesPacket::handle);
		ClientSidePacketRegistry.INSTANCE.register(SpawnPortalParticlesPacket.ID, SpawnPortalParticlesPacket::handle);
		ClientSidePacketRegistry.INSTANCE.register(SpawnExplosionParticlesPacket.ID, SpawnExplosionParticlesPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(SpawnBrazierParticlesPacket.ID, SpawnBrazierParticlesPacket::handle);

		ParticleFactoryRegistry.getInstance().register(BWParticleTypes.CAULDRON_BUBBLE, CauldronBubbleParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(BWParticleTypes.INCENSE_SMOKE, IncenseSmokeParticle.Factory::new);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0xffff00, BWObjects.GOLDEN_GLYPH);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0xc00000, BWObjects.FIERY_GLYPH);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0x8000a0, BWObjects.ELDRITCH_GLYPH);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? 0x7f0000 : 0xffffff, BWObjects.BOTTLE_OF_BLOOD);
		FabricModelPredicateProviderRegistry.register(BWObjects.HEDGEWITCH_HAT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity) -> stack.getName().asString().toLowerCase().contains("faith") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.ALCHEMIST_HAT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity) -> stack.getName().asString().toLowerCase().contains("faith") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.BESMIRCHED_HAT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity) -> stack.getName().asString().toLowerCase().contains("faith") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.TAGLOCK, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity) -> stack.hasTag() && stack.getOrCreateTag().contains("OwnerUUID") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.WAYSTONE, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity) -> stack.hasTag() && stack.getOrCreateTag().contains("LocationPos") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.DEMONIC_CONTRACT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity) -> stack.hasTag() && stack.getOrCreateTag().contains("OwnerUUID") ? 1 : 0);
		ArmorRenderingRegistry.registerModel((livingEntity, itemStack, equipmentSlot, bipedEntityModel) -> new WitchArmorModel<>(equipmentSlot, itemStack.getItem() == BWObjects.HEDGEWITCH_HOOD || itemStack.getItem() == BWObjects.ALCHEMIST_HOOD || itemStack.getItem() == BWObjects.BESMIRCHED_HOOD, !livingEntity.getEquippedStack(EquipmentSlot.FEET).isEmpty()), BWObjects.HEDGEWITCH_HOOD, BWObjects.HEDGEWITCH_HAT, BWObjects.HEDGEWITCH_ROBES, BWObjects.HEDGEWITCH_PANTS, BWObjects.ALCHEMIST_HOOD, BWObjects.ALCHEMIST_HAT, BWObjects.ALCHEMIST_ROBES, BWObjects.ALCHEMIST_PANTS, BWObjects.BESMIRCHED_HOOD, BWObjects.BESMIRCHED_HAT, BWObjects.BESMIRCHED_ROBES, BWObjects.BESMIRCHED_PANTS);
		Identifier WITCH_HAT_VARIANT = new Identifier(Bewitchment.MODID, "textures/entity/armor/witch_hat_variant.png");
		Identifier HEDGEWITCH = new Identifier(Bewitchment.MODID, "textures/entity/armor/hedgewitch.png");
		Identifier ALCHEMIST = new Identifier(Bewitchment.MODID, "textures/entity/armor/alchemist.png");
		Identifier BESMIRCHED = new Identifier(Bewitchment.MODID, "textures/entity/armor/besmirched.png");
		ArmorRenderingRegistry.registerTexture((livingEntity, itemStack, equipmentSlot, b, s, identifier) -> itemStack.getItem() == BWObjects.HEDGEWITCH_HAT && itemStack.getName().asString().toLowerCase().contains("faith") ? WITCH_HAT_VARIANT : HEDGEWITCH, BWObjects.HEDGEWITCH_HOOD, BWObjects.HEDGEWITCH_HAT, BWObjects.HEDGEWITCH_ROBES, BWObjects.HEDGEWITCH_PANTS);
		ArmorRenderingRegistry.registerTexture((livingEntity, itemStack, equipmentSlot, b, s, identifier) -> itemStack.getItem() == BWObjects.ALCHEMIST_HAT && itemStack.getName().asString().toLowerCase().contains("faith") ? WITCH_HAT_VARIANT : ALCHEMIST, BWObjects.ALCHEMIST_HOOD, BWObjects.ALCHEMIST_HAT, BWObjects.ALCHEMIST_ROBES, BWObjects.ALCHEMIST_PANTS);
		ArmorRenderingRegistry.registerTexture((livingEntity, itemStack, equipmentSlot, b, s, identifier) -> itemStack.getItem() == BWObjects.BESMIRCHED_HAT && itemStack.getName().asString().toLowerCase().contains("faith") ? WITCH_HAT_VARIANT : BESMIRCHED, BWObjects.BESMIRCHED_HOOD, BWObjects.BESMIRCHED_HAT, BWObjects.BESMIRCHED_ROBES, BWObjects.BESMIRCHED_PANTS);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.BW_CHEST, ChestBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.WITCH_ALTAR, WitchAltarBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.WITCH_CAULDRON, WitchCauldronBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.BRAZIER, BrazierBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.POPPET_SHELF, PoppetShelfBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.DRAGONS_BLOOD_CHEST, ChestBlockEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.JUNIPER_BOAT, (dispatcher, context) -> new BoatEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.CYPRESS_BOAT, (dispatcher, context) -> new BoatEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.ELDER_BOAT, (dispatcher, context) -> new BoatEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.DRAGONS_BLOOD_BOAT, (dispatcher, context) -> new BoatEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.SILVER_ARROW, (dispatcher, context) -> new SilverArrowEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.OWL, (dispatcher, context) -> new OwlEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.RAVEN, (dispatcher, context) -> new RavenEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.SNAKE, (dispatcher, context) -> new SnakeEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.TOAD, (dispatcher, context) -> new ToadEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.GHOST, (dispatcher, context) -> new GhostEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.BLACK_DOG, (dispatcher, context) -> new BlackDogEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.HELLHOUND, (dispatcher, context) -> new HellhoundEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.DEMON, (dispatcher, context) -> new DemonEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.LEONARD, (dispatcher, context) -> new LeonardEntityRenderer(dispatcher));
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.BAPHOMET, (dispatcher, context) -> new BaphometEntityRenderer(dispatcher));
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.SALT_LINE, BWObjects.TEMPORARY_COBWEB, BWObjects.GLYPH, BWObjects.GOLDEN_GLYPH, BWObjects.FIERY_GLYPH, BWObjects.ELDRITCH_GLYPH, BWObjects.SIGIL);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.ACONITE_CROP, BWObjects.BELLADONNA_CROP, BWObjects.GARLIC_CROP, BWObjects.MANDRAKE_CROP);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.JUNIPER_SAPLING, BWObjects.POTTED_JUNIPER_SAPLING, BWObjects.JUNIPER_DOOR, BWObjects.JUNIPER_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.CYPRESS_SAPLING, BWObjects.POTTED_CYPRESS_SAPLING, BWObjects.CYPRESS_DOOR, BWObjects.CYPRESS_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.ELDER_SAPLING, BWObjects.POTTED_ELDER_SAPLING, BWObjects.ELDER_DOOR, BWObjects.ELDER_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.DRAGONS_BLOOD_SAPLING, BWObjects.POTTED_DRAGONS_BLOOD_SAPLING, BWObjects.DRAGONS_BLOOD_DOOR, BWObjects.DRAGONS_BLOOD_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.SPANISH_MOSS, BWObjects.GLOWING_BRAMBLE, BWObjects.ENDER_BRAMBLE, BWObjects.FRUITING_BRAMBLE, BWObjects.SCORCHED_BRAMBLE, BWObjects.THICK_BRAMBLE, BWObjects.FLEETING_BRAMBLE);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.STONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.MOSSY_COBBLESTONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.PRISMARINE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.NETHER_BRICK_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.BLACKSTONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.GOLDEN_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.END_STONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.OBSIDIAN_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.PURPUR_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), BWObjects.CRYSTAL_BALL);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.BRAZIER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.JUNIPER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_JUNIPER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.JUNIPER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_JUNIPER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.JUNIPER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_JUNIPER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.CYPRESS);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_CYPRESS);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.CYPRESS_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_CYPRESS_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.CYPRESS_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_CYPRESS_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.ELDER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_ELDER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.ELDER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_ELDER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.ELDER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_ELDER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.DRAGONS_BLOOD);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_DRAGONS_BLOOD);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.DRAGONS_BLOOD_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_DRAGONS_BLOOD_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.DRAGONS_BLOOD_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_DRAGONS_BLOOD_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.JUNIPER_SIGN.getTexture()));
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.CYPRESS_SIGN.getTexture()));
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.ELDER_SIGN.getTexture()));
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.DRAGONS_BLOOD_SIGN.getTexture()));
		BlockEntity JUNIPER_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.JUNIPER, false);
		BlockEntity TRAPPED_JUNIPER_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.JUNIPER, true);
		BlockEntity CYPRESS_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.CYPRESS, false);
		BlockEntity TRAPPED_CYPRESS_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.CYPRESS, true);
		BlockEntity ELDER_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.ELDER, false);
		BlockEntity TRAPPED_ELDER_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.ELDER, true);
		BlockEntity DRAGONS_BLOOD_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.DRAGONS_BLOOD, false);
		BlockEntity TRAPPED_DRAGONS_BLOOD_CHEST = new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BWChestBlockEntity.Type.DRAGONS_BLOOD, true);
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.JUNIPER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(JUNIPER_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_JUNIPER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(TRAPPED_JUNIPER_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.CYPRESS_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(CYPRESS_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_CYPRESS_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(TRAPPED_CYPRESS_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.ELDER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(ELDER_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_ELDER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(TRAPPED_ELDER_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.DRAGONS_BLOOD_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(DRAGONS_BLOOD_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_DRAGONS_BLOOD_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> BlockEntityRenderDispatcher.INSTANCE.renderEntity(TRAPPED_DRAGONS_BLOOD_CHEST, matrices, vertexConsumers, light, overlay));
	}
}
