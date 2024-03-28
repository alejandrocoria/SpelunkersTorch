package games.alejandrocoria.spelunkerstorch.common.block;

import com.mojang.serialization.MapCodec;
import games.alejandrocoria.spelunkerstorch.SpelunkersTorch;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Torch extends TorchBlock implements EntityBlock {
    public static final BooleanProperty HAS_TARGET = BooleanProperty.create("has_target");
    public static final MapCodec<Torch> CODEC = simpleCodec(Torch::new);

    public Torch(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_TARGET, false));
    }

    public MapCodec<? extends Torch> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new TorchEntity(blockPos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return TorchEntity.createTicker(level, blockEntityType);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState.is(blockState2.getBlock())) {
            SpelunkersTorch.updateNearbyOnRemove(level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_TARGET);
    }
}
