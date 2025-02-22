package com.bobmowzie.mowziesmobs.server.entity.grottol;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.server.block.BlockGrottol;
import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import com.bobmowzie.mowziesmobs.server.message.MessageAddFreezeProgress;
import com.bobmowzie.mowziesmobs.server.message.MessageBlackPinkInYourArea;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.minecart.AbstractMinecart;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.function.BiConsumer;

public final class BlackPinkInYourArea implements BiConsumer<World, AbstractMinecart> {
    private BlackPinkInYourArea() {}

    @Override
    public void accept(World world, AbstractMinecart minecart) {
        /*BlockState state = minecart.getDisplayTile();
        if (state.getBlock() != BlockHandler.GROTTOL.get()) {
            state = BlockHandler.GROTTOL.get().getDefaultState();
            minecart.setDisplayTileOffset(minecart.getDefaultDisplayTileOffset());
        }
        minecart.setDisplayTile(state.with(BlockGrottol.VARIANT, BlockGrottol.Variant.BLACK_PINK));*/
        MowziesMobs.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> minecart), new MessageBlackPinkInYourArea(minecart));
    }

    public static BlackPinkInYourArea create() {
        return new BlackPinkInYourArea();
    }
}
