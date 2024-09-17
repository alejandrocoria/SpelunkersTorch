package games.alejandrocoria.spelunkerstorch;

import games.alejandrocoria.spelunkerstorch.common.command.CommandRecalculate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

public class SpelunkersTorchFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOG.info("Spelunker's Torch common init");

        SpelunkersTorch.init();
        Registry.init();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.accept(TORCH_ITEM.get());
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandRecalculate.register(dispatcher));

        ServerTickEvents.END_SERVER_TICK.register(server -> SpelunkersTorch.serverTick());

        Constants.LOG.info("Spelunker's Torch common init done");
    }
}
