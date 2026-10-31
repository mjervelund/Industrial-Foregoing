package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.api.conveyor.ConveyorUpgradeFactory;
import com.buuz135.industrial.api.transporter.TransporterTypeFactory;
import com.buuz135.industrial.block.transportstorage.ConveyorBlock;
import com.buuz135.industrial.module.ModuleTransportStorage;
import com.buuz135.industrial.proxy.client.model.ConveyorBlockModel;
import com.buuz135.industrial.proxy.client.model.TransporterBlockModel;
import com.hrznstudio.titanium.event.handler.EventManager;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

/**
 * Conveyor/transporter model hooks, moved out of ModuleTransportStorage: on 26.3 {@code @OnlyIn} no longer strips
 * methods, and verifying them on a dedicated server loads client model classes (NoClassDefFoundError ResolvableModel).
 */
public final class TransportStorageClientEvents {
    private TransportStorageClientEvents() {
    }

    public static void register() {
        EventManager.mod(ModelEvent.RegisterStandalone.class).process(TransportStorageClientEvents::registerStandalone).subscribe();
        EventManager.mod(ModelEvent.ModifyBakingResult.class).process(TransportStorageClientEvents::modifyBakingResult).subscribe();
    }

    private static void registerStandalone(ModelEvent.RegisterStandalone event) {
        // Upgrade models are standalone models added to the conveyor/transporter block model per installed upgrade
        for (TransporterTypeFactory transporterTypeFactory : TransporterTypeFactory.FACTORIES) {
            for (Direction upgradeFacing : transporterTypeFactory.getValidFacings()) {
                for (TransporterTypeFactory.TransporterAction actions : TransporterTypeFactory.TransporterAction.values()) {
                    Identifier id = transporterTypeFactory.getModel(upgradeFacing, actions);
                    StandaloneModelKey<BlockStateModelPart> key = new StandaloneModelKey<>(id::toString);
                    ModuleTransportStorage.TRANSPORTER_CACHE.put(id, key);
                    event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(id));
                }
            }
        }
        for (ConveyorUpgradeFactory conveyorUpgradeFactory : ConveyorUpgradeFactory.FACTORIES) {
            for (Direction upgradeFacing : conveyorUpgradeFactory.getValidFacings()) {
                for (Direction conveyorFacing : ConveyorBlock.FACING.getPossibleValues()) {
                    Identifier id = conveyorUpgradeFactory.getModel(upgradeFacing, conveyorFacing);
                    StandaloneModelKey<BlockStateModelPart> key = new StandaloneModelKey<>(id::toString);
                    ModuleTransportStorage.CONVEYOR_UPGRADES_CACHE.put(id, key);
                    event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(id));
                }
            }
        }
    }

    private static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models = event.getBakingResult().blockStateModels();
        for (BlockState state : ModuleTransportStorage.TRANSPORTER.getBlock().getStateDefinition().getPossibleStates()) {
            models.computeIfPresent(state, (s, model) -> new TransporterBlockModel(model));
        }
        for (BlockState state : ModuleTransportStorage.CONVEYOR.getBlock().getStateDefinition().getPossibleStates()) {
            models.computeIfPresent(state, (s, model) -> new ConveyorBlockModel(model));
        }
    }
}
