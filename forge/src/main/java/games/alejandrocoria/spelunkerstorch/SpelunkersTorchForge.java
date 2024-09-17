package games.alejandrocoria.spelunkerstorch;

import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClientForge;
import games.alejandrocoria.spelunkerstorch.common.command.CommandRecalculate;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

@Mod(Constants.MOD_ID)
public class SpelunkersTorchForge {
    public static final DeferredRegister<Block> BLOCK = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    public static final DeferredRegister<Item> ITEM = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    public SpelunkersTorchForge() {
        Constants.LOG.info("Spelunker's Torch common init");

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCK.register(eventBus);
        ITEM.register(eventBus);
        BLOCK_ENTITY_TYPE.register(eventBus);
        SpelunkersTorch.init();
        Registry.init();

        MinecraftForge.EVENT_BUS.addListener(SpelunkersTorchForge::registerCommands);
        eventBus.addListener(SpelunkersTorchForge::onCreativeTabsBuild);
        eventBus.addListener(SpelunkersTorchClientForge::clientSetup);
        MinecraftForge.EVENT_BUS.addListener(SpelunkersTorchForge::serverTick);

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

    public static void serverTick(TickEvent.ServerTickEvent event) {
        SpelunkersTorch.serverTick();
    }
}
