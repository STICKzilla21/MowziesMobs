package com.bobmowzie.mowziesmobs.client;

import com.bobmowzie.mowziesmobs.client.render.entity.*;
import com.bobmowzie.mowziesmobs.client.render.entity.layer.SunblockLayer;
import com.bobmowzie.mowziesmobs.client.sound.*;
import com.bobmowzie.mowziesmobs.server.ServerProxy;
import com.bobmowzie.mowziesmobs.server.capability.AbilityCapability;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.*;
import com.bobmowzie.mowziesmobs.server.entity.naga.EntityNaga;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayer;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.minecart.AbstractMinecart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.IItemPropertyGetter;
import net.minecraft.world.item.ItemModelsProperties;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends ServerProxy {
    private Entity referencedMob = null;

    @Override
    public void init(final IEventBus modbus) {
        super.init(modbus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_CONFIG);

        modbus.register(MMModels.class);
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FrozenRenderHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(AbilityCapability.AbilityClientEventHandler.INSTANCE);
    }

    @Override
    public void onLateInit(final IEventBus modbus) {
        for (EntityRenderer<?> entityRenderer : Minecraft.getInstance().getRenderManager().renderers.values()) {
            if (entityRenderer instanceof LivingRenderer) {
                LivingRenderer livingRenderer = (LivingRenderer) entityRenderer;
                livingRenderer.addLayer(new FrozenRenderHandler.LayerFrozen(livingRenderer));
                livingRenderer.addLayer(new SunblockLayer(livingRenderer));
            }
        }
        for (PlayerRenderer playerRenderer : Minecraft.getInstance().getRenderManager().getSkinMap().values()) {
            playerRenderer.addLayer(new FrozenRenderHandler.LayerFrozen(playerRenderer));
            playerRenderer.addLayer(new SunblockLayer(playerRenderer));
        }
        IItemPropertyGetter pulling = ItemModelsProperties.func_239417_a_(Items.BOW, new ResourceLocation("pulling"));
        ItemModelsProperties.registerProperty(ItemHandler.BLOWGUN.asItem(), new ResourceLocation("pulling"), pulling);
    }

    @Override
    public void playSunstrikeSound(EntitySunstrike strike) {
        Minecraft.getInstance().getSoundHandler().play(new SunstrikeSound(strike));
    }

    @Override
    public void playIceBreathSound(Entity entity) {
        Minecraft.getInstance().getSoundHandler().play(new IceBreathSound(entity));
    }

    @Override
    public void playBoulderChargeSound(LivingEntity player) {
        Minecraft.getInstance().getSoundHandler().play(new SpawnBoulderChargeSound(player));
    }

    @Override
    public void playNagaSwoopSound(EntityNaga naga) {
        Minecraft.getInstance().getSoundHandler().play(new NagaSwoopSound(naga));
    }

    @Override
    public void playBlackPinkSound(AbstractMinecart entity) {
        Minecraft.getInstance().getSoundHandler().play(new BlackPinkSound(entity));
    }

    @Override
    public void playSunblockSound(LivingEntity entity) {
        Minecraft.getInstance().getSoundHandler().play(new SunblockSound(entity));
    }

    @Override
    public void minecartParticles(ClientLevel world, AbstractMinecart minecart, float scale, double x, double y, double z, BlockState state, BlockPos pos) {
        final int size = 3;
        float offset =  -0.5F * scale;
        for (int ix = 0; ix < size; ix++) {
            for (int iy = 0; iy < size; iy++) {
                for (int iz = 0; iz < size; iz++) {
                    double dx = (double) ix / size * scale;
                    double dy = (double) iy / size * scale;
                    double dz = (double) iz / size * scale;
                    Vec3 minecartMotion = minecart.getMotion();
                    Minecraft.getInstance().particles.addEffect(new DiggingParticle(
                            world,
                            x + dx + offset, y + dy + offset, z + dz + offset,
                            dx + minecartMotion.getX(), dy + minecartMotion.getY(), dz + minecartMotion.getZ(),
                            state
                    ) {}.setBlockPos(pos));
                }
            }
        }
    }

    public void setTPS(float tickRate) {

    }

    public Entity getReferencedMob() {
        return referencedMob;
    }

    public void setReferencedMob(Entity referencedMob) {
        this.referencedMob = referencedMob;
    }
}
