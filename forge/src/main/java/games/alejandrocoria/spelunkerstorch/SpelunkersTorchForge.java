package games.alejandrocoria.spelunkerstorch;

import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClientForge;
import games.alejandrocoria.spelunkerstorch.common.command.CommandRecalculate;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.invoke.MethodHandles;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

@Mod(Constants.MOD_ID)
public class SpelunkersTorchForge {
    public static final DeferredRegister<Block> BLOCK = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    public static final DeferredRegister<Item> ITEM = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    public SpelunkersTorchForge(FMLJavaModLoadingContext context) {
        Constants.LOG.info("Spelunker's Torch common init");

        BusGroup modBusGroup = context.getModBusGroup();

        BLOCK.register(modBusGroup);
        ITEM.register(modBusGroup);
        BLOCK_ENTITY_TYPE.register(modBusGroup);
        SpelunkersTorch.init();
        Registry.init();

        MinecraftForge.EVENT_BUS.register(MethodHandles.lookup(), SpelunkersTorchForge.class);
        modBusGroup.register(MethodHandles.lookup(), SpelunkersTorchForge.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(MethodHandles.lookup(), SpelunkersTorchClientForge.class);
            modBusGroup.register(MethodHandles.lookup(), SpelunkersTorchClientForge.class);
        }

        Constants.LOG.info("Spelunker's Torch common init done");
    }

    @SubscribeEvent
    public static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TORCH_ITEM.get());
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandRecalculate.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        SpelunkersTorch.serverTick();
    }
}
