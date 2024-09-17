package games.alejandrocoria.spelunkerstorch.platform;

import games.alejandrocoria.spelunkerstorch.SpelunkersTorchNeoForge;
import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClientNeoForge;
import games.alejandrocoria.spelunkerstorch.platform.services.IPlatformHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Supplier<Block> registerBlock(String key, Supplier<Block> block) {
        return SpelunkersTorchNeoForge.BLOCK.register(key, block);
    }

    @Override
    public Supplier<Item> registerItem(String key, Supplier<Item> item) {
        return SpelunkersTorchNeoForge.ITEM.register(key, item);
    }

    @Override
    public Supplier<Item> createStandingAndWallBlockItem(Supplier<Block> standingBlock, Supplier<Block> wallBlock, Item.Properties properties, Direction direction) {
        return () -> new StandingAndWallBlockItem(standingBlock.get(), wallBlock.get(), properties, direction) {
            @Override
            public void initializeClient(Consumer<IClientItemExtensions> consumer) {
                Item item = this;

                consumer.accept(new IClientItemExtensions() {
                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        BlockEntityWithoutLevelRenderer renderer = SpelunkersTorchClientNeoForge.getItemRenderer(item);
                        return renderer == null ? IClientItemExtensions.super.getCustomRenderer() : renderer;
                    }
                });
            }
        };
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String key, Supplier<BlockEntityType<T>> type) {
        return SpelunkersTorchNeoForge.BLOCK_ENTITY_TYPE.register(key, type);
    }
}
