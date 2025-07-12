package games.alejandrocoria.spelunkerstorch.platform;

import games.alejandrocoria.spelunkerstorch.SpelunkersTorchForge;
import games.alejandrocoria.spelunkerstorch.platform.services.IPlatformHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModList;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Supplier<Block> registerBlock(String key, Supplier<Block> block) {
        return SpelunkersTorchForge.BLOCK.register(key, block);
    }

    @Override
    public Supplier<Item> registerItem(String key, Supplier<Item> item) {
        return SpelunkersTorchForge.ITEM.register(key, item);
    }

    @Override
    public Supplier<Item> createStandingAndWallBlockItem(Supplier<Block> standingBlock, Supplier<Block> wallBlock, Direction direction, Item.Properties properties) {
        return () -> new StandingAndWallBlockItem(standingBlock.get(), wallBlock.get(), direction, properties);
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String key, Supplier<BlockEntityType<T>> type) {
        return SpelunkersTorchForge.BLOCK_ENTITY_TYPE.register(key, type);
    }
}
