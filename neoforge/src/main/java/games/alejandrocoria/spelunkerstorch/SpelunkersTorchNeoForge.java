package games.alejandrocoria.spelunkerstorch;

import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClientNeoForge;
import games.alejandrocoria.spelunkerstorch.common.command.CommandRecalculate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

@Mod(Constants.MOD_ID)
public class SpelunkersTorchNeoForge {
    public static final DeferredRegister.Blocks BLOCK = DeferredRegister.createBlocks(Constants.MOD_ID);
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public SpelunkersTorchNeoForge(IEventBus eventBus) {
        Constants.LOG.info("Spelunker's Torch common init");

        BLOCK.register(eventBus);
        ITEM.register(eventBus);
        BLOCK_ENTITY_TYPE.register(eventBus);
        SpelunkersTorch.init();
        Registry.init();

        NeoForge.EVENT_BUS.addListener(SpelunkersTorchNeoForge::registerCommands);
        eventBus.addListener(SpelunkersTorchNeoForge::onCreativeTabsBuild);
        eventBus.addListener((FMLClientSetupEvent event) -> SpelunkersTorchClientNeoForge.clientSetup(event, eventBus));
        NeoForge.EVENT_BUS.addListener(SpelunkersTorchNeoForge::serverTick);

        Constants.LOG.info("Spelunker's Torch common init done");
    }

    public static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TORCH_ITEM.get());
        }
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        CommandRecalculate.register(event.getDispatcher());
    }

    public static void serverTick(ServerTickEvent.Post event) {
        SpelunkersTorch.serverTick();
    }
}
