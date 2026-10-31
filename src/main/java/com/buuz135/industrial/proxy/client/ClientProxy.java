/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.block.IndustrialBlock;
import com.buuz135.industrial.block.generator.tile.MycelialReactorTile;
import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.buuz135.industrial.block.transportstorage.tile.ConveyorTile;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.entity.InfinityLauncherProjectileEntity;
import com.buuz135.industrial.entity.InfinityNukeEntity;
import com.buuz135.industrial.entity.InfinityTridentEntity;
import com.buuz135.industrial.entity.client.*;
import com.buuz135.industrial.module.*;
import com.buuz135.industrial.proxy.CommonProxy;
import com.buuz135.industrial.proxy.client.event.IFClientEvents;
import com.buuz135.industrial.proxy.client.particle.ParticleVex;
import com.buuz135.industrial.proxy.client.render.*;
import com.buuz135.industrial.proxy.client.render.item.HydroponicSimProcessorModel;
import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.BlockWithTile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Calendar;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public static Identifier GUI = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/machines.png");

    public static KeyMapping OPEN_BACKPACK;

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerAreaRender(event, ModuleCore.FLUID_EXTRACTOR);
        registerAreaRender(event, ModuleAgricultureHusbandry.PLANT_GATHERER);
        registerAreaRender(event, ModuleAgricultureHusbandry.PLANT_SOWER);
        registerAreaRender(event, ModuleAgricultureHusbandry.SEWER);
        registerAreaRender(event, ModuleAgricultureHusbandry.PLANT_FERTILIZER);
        registerAreaRender(event, ModuleAgricultureHusbandry.SLAUGHTER_FACTORY);
        registerAreaRender(event, ModuleAgricultureHusbandry.ANIMAL_RANCHER);
        registerAreaRender(event, ModuleAgricultureHusbandry.ANIMAL_FEEDER);
        registerAreaRender(event, ModuleAgricultureHusbandry.ANIMAL_BABY_SEPARATOR);
        registerAreaRender(event, ModuleAgricultureHusbandry.MOB_CRUSHER);
        registerAreaRender(event, ModuleAgricultureHusbandry.WITHER_BUILDER);
        registerAreaRender(event, ModuleMisc.STASIS_CHAMBER);
        registerAreaRender(event, ModuleResourceProduction.LASER_DRILL);
        registerAreaRender(event, ModuleAgricultureHusbandry.MOB_DUPLICATOR);
        registerAreaRender(event, ModuleMisc.MOB_DETECTOR);

        event.registerBlockEntityRenderer((BlockEntityType<? extends MycelialReactorTile>) ModuleGenerator.MYCELIAL_REACTOR.type().get(), MycelialReactorTESR::new);

        event.registerEntityRenderer((EntityType<? extends InfinityTridentEntity>) ModuleTool.TRIDENT_ENTITY_TYPE.value(), InfinityTridentRenderer::new);
        event.registerEntityRenderer((EntityType<? extends InfinityNukeEntity>) ModuleTool.INFINITY_NUKE_ENTITY_TYPE.value(), InfinityNukeRenderer::new);
        event.registerEntityRenderer((EntityType<? extends InfinityLauncherProjectileEntity>) ModuleTool.INFINITY_LAUNCHER_PROJECTILE_ENTITY_TYPE.value(), InfinityLauncherProjectileRenderer::new);

        event.registerBlockEntityRenderer(((BlockEntityType<? extends TransporterTile>) ModuleTransportStorage.TRANSPORTER.type().get()), TransporterTESR::new);

        event.registerBlockEntityRenderer(((BlockEntityType<? extends ConveyorTile>) ModuleTransportStorage.CONVEYOR.type().get()), FluidConveyorTESR::new);
    }

    private static void registerAreaRender(EntityRenderersEvent.RegisterRenderers event, BlockWithTile pair) {
        event.registerBlockEntityRenderer((BlockEntityType<? extends IndustrialAreaWorkingTile>) pair.type().get(), WorkingAreaTESR::new);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            if (event.getPlayerRenderer(skin) instanceof AvatarRenderer<?> playerRenderer) {
                playerRenderer.addLayer(new ContributorsCatEarsRender(playerRenderer));
                playerRenderer.addLayer(new InfinityLauncherProjectileArmorLayer(playerRenderer));
            }
        }
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(InfinityTridentRenderer.TRIDENT_LAYER, InfinityTridentModel::createBodyLayer);
        event.registerLayerDefinition(InfinityNukeRenderer.NUKE_LAYER, InfinityNukeModel::createBodyLayer);
        event.registerLayerDefinition(InfinityNukeRenderer.NUKE_ARMED_LAYER, () -> InfinityNukeModelArmed.createBodyLayer(new CubeDeformation(0f)));
        event.registerLayerDefinition(InfinityNukeRenderer.NUKE_ARMED_BIG_LAYER, () -> InfinityNukeModelArmed.createBodyLayer(new CubeDeformation(0.2f)));
        event.registerLayerDefinition(InfinityLauncherProjectileRenderer.PROJECTILE_LAYER, InfinityLauncherProjectileModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(ContributorsCatEarsRender.EARS_MODEL, SimpleUnbakedStandaloneModel.simpleModelWrapper(ContributorsCatEarsRender.EARS_MODEL_ID));
    }

    // 26.3 item models, tints and properties are data driven (assets/industrialforegoing/items/*.json):
    // infinity tools tint layer 0 with industrialforegoing:infinity_tier, the mob imprisonment tool layers 1-3 with
    // industrialforegoing:mob_imprisonment, ore meat buckets layer 1 with industrialforegoing:fluid_contents, the
    // launcher's 1.21 "cooldown" property is minecraft:cooldown and the copier's "stored" is minecraft:has_component
    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "hydroponic_simulation_processor"), HydroponicSimProcessorModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        IFTintSources.register(event);
    }

    @SubscribeEvent
    public static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return 0xFFFFFFFF;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                if (level.getBlockEntity(pos) instanceof ConveyorTile conveyorTile) {
                    return ARGB.opaque(conveyorTile.getColor());
                }
                return 0xFFFFFFFF;
            }
        }), ModuleTransportStorage.CONVEYOR.getBlock());
    }

    @SubscribeEvent
    public static void registerParticleGroups(RegisterParticleGroupsEvent event) {
        event.register(ParticleVex.RENDER, ParticleVex.Group::new);
    }

    @Override
    public void run() {

        NeoForge.EVENT_BUS.register(new IFClientEvents());

        //((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(resourceManager -> FluidUtils.colorCache.clear());

        // Chunk render layers come from the block models' materials on 26.3 (no ItemBlockRenderTypes); conveyor
        // tint, item colours and item properties are registered above / in the item JSON

        EventManager.forge(ItemTooltipEvent.class).filter(event -> BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).getNamespace().equals(Reference.MOD_ID)).process(event -> {
            if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1 && Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL) {
                event.getToolTip().add(Component.literal(Component.translatable("text.industrialforegoing.proxy.client.alt_f4").getString()).withStyle(ChatFormatting.DARK_AQUA));
            }
        }).subscribe();
        EventManager.forge(ItemTooltipEvent.class).filter(event -> event.getItemStack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IndustrialBlock<?>).process(event -> {
            ((IndustrialBlock<?>) ((BlockItem) event.getItemStack().getItem()).getBlock()).appendHoverText(event.getItemStack(), event.getContext(), event.getToolTip(), event.getFlags());
        }).subscribe();

    }

}
