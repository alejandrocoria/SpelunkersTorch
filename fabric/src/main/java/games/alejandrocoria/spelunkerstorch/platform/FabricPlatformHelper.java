package games.alejandrocoria.spelunkerstorch.platform;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Supplier<Block> registerBlock(String key, Supplier<Block> block) {
        Block b = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, key), block.get());
        return () -> b;
    }

    @Override
    public Supplier<Item> registerItem(String key, Supplier<Item> item) {
        Item i = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, key), item.get());
        return () -> i;
    }

    @Override
    public Supplier<Item> createStandingAndWallBlockItem(Supplier<Block> standingBlock, Supplier<Block> wallBlock, Item.Properties properties, Direction direction) {
        return () -> new StandingAndWallBlockItem(standingBlock.get(), wallBlock.get(), properties, direction);
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String key, Supplier<BlockEntityType<T>> type) {
        BlockEntityType<T> t = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, key), type.get());
        return () -> t;
    }
}
