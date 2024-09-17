package games.alejandrocoria.spelunkerstorch.common.mixin.server;

import games.alejandrocoria.spelunkerstorch.SpelunkersTorch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method="sendBlockUpdated", at=@At(value="FIELD", target="Lnet/minecraft/server/level/ServerLevel;isUpdatingNavigations:Z", opcode=181, ordinal=0)) // 181 = Opcodes=PUTFIELD
    public void spelunkerstorch$sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags, CallbackInfo ci) {
        SpelunkersTorch.onBlockUpdated((ServerLevel) (Object) this, pPos, pNewState);
    }
}
