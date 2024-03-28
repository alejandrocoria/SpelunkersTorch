package games.alejandrocoria.spelunkerstorch;

import net.fabricmc.api.ModInitializer;
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

        Constants.LOG.info("Spelunker's Torch common init done");
    }
}
