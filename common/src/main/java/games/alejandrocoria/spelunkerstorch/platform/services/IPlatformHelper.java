package games.alejandrocoria.spelunkerstorch.platform.services;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IPlatformHelper {
    String getPlatformName();

    boolean isModLoaded(String modId);

    Supplier<Block> registerBlock(String key, Supplier<Block> block);

    Supplier<Item> registerItem(String key, Supplier<Item> item);

    Supplier<Item> createStandingAndWallBlockItem(Supplier<Block> standingBlock, Supplier<Block> wallBlock, Direction direction, Item.Properties properties);

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String key, Supplier<BlockEntityType<T>> type);
}
