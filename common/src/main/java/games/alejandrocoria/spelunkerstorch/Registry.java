package games.alejandrocoria.spelunkerstorch;

import games.alejandrocoria.spelunkerstorch.common.block.Torch;
import games.alejandrocoria.spelunkerstorch.common.block.WallTorch;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import games.alejandrocoria.spelunkerstorch.platform.Services;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class Registry {
    public static final Supplier<Block> TORCH_BLOCK = Services.PLATFORM.registerBlock("torch",
            () -> new Torch(BlockBehaviour.Properties.of()
                    .instabreak()
                    .noCollission()
                    .lightLevel(s -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));

    public static final Supplier<Block> WALL_TORCH_BLOCK = Services.PLATFORM.registerBlock("wall_torch",
            () -> new WallTorch(BlockBehaviour.Properties.of()
                    .instabreak()
                    .noCollission()
                    .lightLevel(s -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
                    .dropsLike(TORCH_BLOCK.get())));

    public static final Supplier<Item> TORCH_ITEM = Services.PLATFORM.registerItem("torch",
            Services.PLATFORM.createStandingAndWallBlockItem(TORCH_BLOCK, WALL_TORCH_BLOCK, new Item.Properties(), Direction.DOWN));

    public static final Supplier<BlockEntityType<TorchEntity>> TORCH_ENTITY = Services.PLATFORM.registerBlockEntity("torch",
            () -> BlockEntityType.Builder.of(TorchEntity::new, TORCH_BLOCK.get(), WALL_TORCH_BLOCK.get()).build(null));

    public static void init() {

    }
}
