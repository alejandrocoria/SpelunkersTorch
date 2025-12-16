package games.alejandrocoria.spelunkerstorch.common.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static games.alejandrocoria.spelunkerstorch.common.block.Torch.HAS_TARGET;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WallTorch extends WallTorchBlock implements EntityBlock {
    public static final double H_OFFSET = 0.27;
    public static final double V_OFFSET = 0.22;
    public static final MapCodec<WallTorchBlock> CODEC = simpleCodec(WallTorch::new);

    public WallTorch(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_TARGET, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<WallTorchBlock> codec() {
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_TARGET, FACING);
    }
}
