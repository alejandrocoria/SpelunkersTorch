package games.alejandrocoria.spelunkerstorch.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import games.alejandrocoria.spelunkerstorch.SpelunkersTorch;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

public class CommandRecalculate {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spelunkerstorch").requires(
                (commandSource) -> commandSource.hasPermission(2))
                .then(Commands.literal("loaded").executes(CommandRecalculate::recalculateLoaded))
                .then(Commands.literal("chunk").executes(CommandRecalculate::recalculateChunk))
                .then(Commands.literal("section").executes(CommandRecalculate::recalculateSection))
                .then(Commands.literal("single").executes(CommandRecalculate::recalculateClosest))
                .then(Commands.literal("single").then(Commands.argument("location", Vec3Argument.vec3()).executes((commandSource) -> CommandRecalculate.recalculateSingle(commandSource, Vec3Argument.getCoordinates(commandSource, "location")))))
        );
    }

    private static int recalculateLoaded(CommandContext<CommandSourceStack> ctx) {
        int count = SpelunkersTorch.recalculateTorches(ctx.getSource().getLevel());
        if (count > 0 && ctx.getSource().isPlayer()) {
            ctx.getSource().getPlayer().sendSystemMessage(Component.translatable("chat.spelunkerstorch.recalculated_loaded", count));
        }
        return count;
    }

    private static int recalculateChunk(CommandContext<CommandSourceStack> ctx) {
        ChunkPos chunkPos = new ChunkPos(BlockPos.containing(ctx.getSource().getPosition()));
        int count = SpelunkersTorch.recalculateTorches(ctx.getSource().getLevel(), chunkPos);
        if (count > 0 && ctx.getSource().isPlayer()) {
            ctx.getSource().getPlayer().sendSystemMessage(Component.translatable("chat.spelunkerstorch.recalculated_chunk", chunkPos.toString(), count));
        }
        return count;
    }

    private static int recalculateSection(CommandContext<CommandSourceStack> ctx) {
        SectionPos sectionPos = SectionPos.of(ctx.getSource().getPosition());
        int count = SpelunkersTorch.recalculateTorches(ctx.getSource().getLevel(), sectionPos);
        if (count > 0 && ctx.getSource().isPlayer()) {
            ctx.getSource().getPlayer().sendSystemMessage(Component.translatable("chat.spelunkerstorch.recalculated_section", sectionPos.toShortString(), count));
        }
        return count;
    }

    private static int recalculateSingle(CommandContext<CommandSourceStack> ctx, Coordinates location) {
        BlockPos torchPos = location.getBlockPos(ctx.getSource());
        boolean result = SpelunkersTorch.recalculateTorch(ctx.getSource().getLevel(), torchPos);
        if (result && ctx.getSource().isPlayer()) {
            ctx.getSource().getPlayer().sendSystemMessage(Component.translatable("chat.spelunkerstorch.recalculated_single", torchPos.toShortString()));
        }
        return result ? 1 : 0;
    }

    private static int recalculateClosest(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getPlayer() != null) {
            BlockPos torchPos = SpelunkersTorch.recalculateClosestTorch(ctx.getSource().getLevel(), ctx.getSource().getPlayer().blockPosition());
            if (torchPos != null && ctx.getSource().isPlayer()) {
                ctx.getSource().getPlayer().sendSystemMessage(Component.translatable("chat.spelunkerstorch.recalculated_single", torchPos));
            }
            return torchPos == null ? 0 : 1;
        }
        return 0;
    }
}
