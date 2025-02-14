package com.bobmowzie.mowziesmobs.server.entity.effects;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.model.tools.ControlledAnimation;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.ParticleOrb;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.client.render.entity.player.GeckoPlayer;
import com.bobmowzie.mowziesmobs.client.render.entity.player.GeckoRenderPlayer;
import com.bobmowzie.mowziesmobs.server.capability.CapabilityHandler;
import com.bobmowzie.mowziesmobs.server.capability.PlayerCapability;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.damage.DamageUtil;
import com.bobmowzie.mowziesmobs.server.entity.LeaderSunstrikeImmune;
import com.bobmowzie.mowziesmobs.server.entity.barakoa.EntityBarako;
import com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.world.level.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayer;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.Direction;
import net.minecraft.resources.math.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntitySolarBeam extends Entity {
    public static final double RADIUS_BARAKO = 30;
    public static final double RADIUS_PLAYER = 20;
    public LivingEntity caster;
    public double endPosX, endPosY, endPosZ;
    public double collidePosX, collidePosY, collidePosZ;
    public double prevCollidePosX, prevCollidePosY, prevCollidePosZ;
    public float renderYaw, renderPitch;
    public ControlledAnimation appear = new ControlledAnimation(3);

    public boolean on = true;

    public Direction blockSide = null;

    private static final EntityDataAccessor<Float> YAW = EntityDataManager.createKey(EntitySolarBeam.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> PITCH = EntityDataManager.createKey(EntitySolarBeam.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> DURATION = EntityDataManager.createKey(EntitySolarBeam.class, EntityDataSerializers.VARINT);

    private static final EntityDataAccessor<Boolean> HAS_PLAYER = EntityDataManager.createKey(EntitySolarBeam.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> CASTER = EntityDataManager.createKey(EntitySolarBeam.class, EntityDataSerializers.VARINT);

    public float prevYaw;
    public float prevPitch;

    @OnlyIn(Dist.CLIENT)
    private Vec3[] attractorPos;

    public EntitySolarBeam(EntityType<? extends EntitySolarBeam> type, World world) {
        super(type, world);
        ignoreFrustumCheck = true;
        if (world.isClientSide) {
            attractorPos = new Vec3[] {new Vec3(0, 0, 0)};
        }
    }

    public EntitySolarBeam(EntityType<? extends EntitySolarBeam> type, World world, LivingEntity caster, double x, double y, double z, float yaw, float pitch, int duration) {
        this(type, world);
        this.caster = caster;
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.setDuration(duration);
        this.setPosition(x, y, z);
        this.calculateEndPos();
        this.playSound(MMSounds.LASER.get(), 2f, 1);
        if (!world.isClientSide) {
            this.setCasterID(caster.getEntityId());
        }
    }

    @Override
    public PushReaction getPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void tick() {
        super.tick();
        prevCollidePosX = collidePosX;
        prevCollidePosY = collidePosY;
        prevCollidePosZ = collidePosZ;
        prevYaw = renderYaw;
        prevPitch = renderPitch;
        if (ticksExisted == 1 && world.isClientSide) {
            caster = (LivingEntity) world.getEntityByID(getCasterID());
        }
        if (getHasPlayer()) {
            if (!world.isClientSide) {
                this.updateWithPlayer();
            }
        }
        if (caster != null) {
            renderYaw = (float) ((caster.getYRot()Head + 90.0d) * Math.PI / 180.0d);
            renderPitch = (float) (-caster.getXRot() * Math.PI / 180.0d);
        }

        if (!on && appear.getTimer() == 0) {
            this.remove();
        }
        if (on && ticksExisted > 20) {
            appear.increaseTimer();
        } else {
            appear.decreaseTimer();
        }

        if (caster != null && !caster.isAlive()) remove();

        if (world.isClientSide && ticksExisted <= 10 && caster != null) {
            int particleCount = 8;
            while (--particleCount != 0) {
                double radius = 2f * caster.getWidth();
                double yaw = rand.nextFloat() * 2 * Math.PI;
                double pitch = rand.nextFloat() * 2 * Math.PI;
                double ox = radius * Math.sin(yaw) * Math.sin(pitch);
                double oy = radius * Math.cos(pitch);
                double oz = radius * Math.cos(yaw) * Math.sin(pitch);
                double rootX = caster.getPosX();
                double rootY = caster.getPosY() + caster.getHeight() / 2f + 0.3f;
                double rootZ = caster.getPosZ();
                if (getHasPlayer()) {
                    if (caster instanceof Player && !(caster == Minecraft.getInstance().player && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)) {
                        GeckoPlayer geckoPlayer = GeckoPlayer.getGeckoPlayer((Player) caster, GeckoPlayer.Perspective.THIRD_PERSON);
                        if (geckoPlayer != null) {
                            GeckoRenderPlayer renderPlayer = (GeckoRenderPlayer) geckoPlayer.getPlayerRenderer();
                            if (renderPlayer.betweenHandsPos != null) {
                                rootX += renderPlayer.betweenHandsPos.getX();
                                rootY += renderPlayer.betweenHandsPos.getY();
                                rootZ += renderPlayer.betweenHandsPos.getZ();
                            }
                        }
                    }
                }
                attractorPos[0] = new Vec3(rootX, rootY, rootZ);
                AdvancedParticleBase.spawnParticle(world, ParticleHandler.ORB2.get(), rootX + ox, rootY + oy, rootZ + oz, 0, 0, 0, true, 0, 0, 0, 0, 5F, 1, 1, 1, 1, 1, 7, true, false, new ParticleComponent[]{
                        new ParticleComponent.Attractor(attractorPos, 1.7f, 0.0f, ParticleComponent.Attractor.EnumAttractorBehavior.EXPONENTIAL),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.KeyTrack(
                                new float[]{0f, 0.8f},
                                new float[]{0f, 1f}
                        ), false),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.KeyTrack(
                                new float[]{3f, 6f},
                                new float[]{0f, 1f}
                        ), false)
                });
            }
        }
        if (ticksExisted > 20) {
            this.calculateEndPos();
            List<LivingEntity> hit = raytraceEntities(world, new Vec3(getPosX(), getPosY(), getPosZ()), new Vec3(endPosX, endPosY, endPosZ), false, true, true).entities;
            if (blockSide != null) {
                spawnExplosionParticles(2);
            }
            if (!world.isClientSide) {
                for (LivingEntity target : hit) {
                    if (caster instanceof EntityBarako && target instanceof LeaderSunstrikeImmune) {
                        continue;
                    }
                    float damageFire = 1f;
                    float damageMob = 1.5f;
                    if (caster instanceof EntityBarako) {
                        damageFire *= ConfigHandler.COMMON.MOBS.BARAKO.combatConfig.attackMultiplier.get();
                        damageMob *= ConfigHandler.COMMON.MOBS.BARAKO.combatConfig.attackMultiplier.get();
                    }
                    if (caster instanceof Player) {
                        damageFire *= ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SUNS_BLESSING.sunsBlessingAttackMultiplier.get();
                        damageMob *= ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SUNS_BLESSING.sunsBlessingAttackMultiplier.get();
                    }
                    DamageUtil.dealMixedDamage(target, DamageSource.causeIndirectDamage(this, caster), damageMob, DamageSource.ON_FIRE, damageFire);
                }
            } else {
                if (ticksExisted - 15 < getDuration()) {
                    int particleCount = 4;
                    while (particleCount --> 0) {
                        double radius = 1f;
                        double yaw = (float) (rand.nextFloat() * 2 * Math.PI);
                        double pitch = (float) (rand.nextFloat() * 2 * Math.PI);
                        double ox = (float) (radius * Math.sin(yaw) * Math.sin(pitch));
                        double oy = (float) (radius * Math.cos(pitch));
                        double oz = (float) (radius * Math.cos(yaw) * Math.sin(pitch));
                        double o2x = (float) (-1 * Math.cos(getYaw()) * Math.cos(getPitch()));
                        double o2y = (float) (-1 * Math.sin(getPitch()));
                        double o2z = (float) (-1 * Math.sin(getYaw()) * Math.cos(getPitch()));
                        world.addParticle(new ParticleOrb.OrbData((float) (collidePosX + o2x + ox), (float) (collidePosY + o2y + oy), (float) (collidePosZ + o2z + oz), 15), getPosX() + o2x + ox, getPosY() + o2y + oy, getPosZ() + o2z + oz, 0, 0, 0);
                    }
                    particleCount = 4;
                    while (particleCount --> 0) {
                        double radius = 2f;
                        double yaw = rand.nextFloat() * 2 * Math.PI;
                        double pitch = rand.nextFloat() * 2 * Math.PI;
                        double ox = radius * Math.sin(yaw) * Math.sin(pitch);
                        double oy = radius * Math.cos(pitch);
                        double oz = radius * Math.cos(yaw) * Math.sin(pitch);
                        double o2x = -1 * Math.cos(getYaw()) * Math.cos(getPitch());
                        double o2y = -1 * Math.sin(getPitch());
                        double o2z = -1 * Math.sin(getYaw()) * Math.cos(getPitch());
                        world.addParticle(new ParticleOrb.OrbData((float) (collidePosX + o2x + ox), (float) (collidePosY + o2y + oy), (float) (collidePosZ + o2z + oz), 20), collidePosX + o2x, collidePosY + o2y, collidePosZ + o2z, 0, 0, 0);
                    }
                }
            }
        }
        if (ticksExisted - 20 > getDuration()) {
            on = false;
        }
    }

    private void spawnExplosionParticles(int amount) {
        for (int i = 0; i < amount; i++) {
            final float velocity = 0.1F;
            float yaw = (float) (rand.nextFloat() * 2 * Math.PI);
            float motionY = rand.nextFloat() * 0.08F;
            float motionX = velocity * MathHelper.cos(yaw);
            float motionZ = velocity * MathHelper.sin(yaw);
            world.addParticle(ParticleTypes.FLAME, collidePosX, collidePosY + 0.1, collidePosZ, motionX, motionY, motionZ);
        }
        for (int i = 0; i < amount / 2; i++) {
            world.addParticle(ParticleTypes.LAVA, collidePosX, collidePosY + 0.1, collidePosZ, 0, 0, 0);
        }
    }

    @Override
    protected void registerData() {
        getDataManager().register(YAW, 0F);
        getDataManager().register(PITCH, 0F);
        getDataManager().register(DURATION, 0);
        getDataManager().register(HAS_PLAYER, false);
        getDataManager().register(CASTER, -1);
    }

    public float getYaw() {
        return getDataManager().get(YAW);
    }

    public void setYaw(float yaw) {
        getDataManager().set(YAW, yaw);
    }

    public float getPitch() {
        return getDataManager().get(PITCH);
    }

    public void setPitch(float pitch) {
        getDataManager().set(PITCH, pitch);
    }

    public int getDuration() {
        return getDataManager().get(DURATION);
    }

    public void setDuration(int duration) {
        getDataManager().set(DURATION, duration);
    }

    public boolean getHasPlayer() {
        return getDataManager().get(HAS_PLAYER);
    }

    public void setHasPlayer(boolean player) {
        getDataManager().set(HAS_PLAYER, player);
    }

    public int getCasterID() {
        return getDataManager().get(CASTER);
    }

    public void setCasterID(int id) {
        getDataManager().set(CASTER, id);
    }

    @Override
    protected void readAdditional(CompoundNBT nbt) {}

    @Override
    protected void writeAdditional(CompoundNBT nbt) {}

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void calculateEndPos() {
        double radius = caster instanceof EntityBarako ? RADIUS_BARAKO : RADIUS_PLAYER;
        if (world.isClientSide()) {
            endPosX = getPosX() + radius * Math.cos(renderYaw) * Math.cos(renderPitch);
            endPosZ = getPosZ() + radius * Math.sin(renderYaw) * Math.cos(renderPitch);
            endPosY = getPosY() + radius * Math.sin(renderPitch);
        }
        else {
            endPosX = getPosX() + radius * Math.cos(getYaw()) * Math.cos(getPitch());
            endPosZ = getPosZ() + radius * Math.sin(getYaw()) * Math.cos(getPitch());
            endPosY = getPosY() + radius * Math.sin(getPitch());
        }
    }

    public HitResult raytraceEntities(World world, Vec3 from, Vec3 to, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        HitResult result = new HitResult();
        result.setBlockHit(world.rayTraceBlocks(new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this)));
        if (result.blockHit != null) {
            Vec3 hitVec = result.blockHit.getHitVec();
            collidePosX = hitVec.x;
            collidePosY = hitVec.y;
            collidePosZ = hitVec.z;
            blockSide = result.blockHit.getFace();
        } else {
            collidePosX = endPosX;
            collidePosY = endPosY;
            collidePosZ = endPosZ;
            blockSide = null;
        }
        List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(Math.min(getPosX(), collidePosX), Math.min(getPosY(), collidePosY), Math.min(getPosZ(), collidePosZ), Math.max(getPosX(), collidePosX), Math.max(getPosY(), collidePosY), Math.max(getPosZ(), collidePosZ)).grow(1, 1, 1));
        for (LivingEntity entity : entities) {
            if (entity == caster) {
                continue;
            }
            float pad = entity.getCollisionBorderSize() + 0.5f;
            AxisAlignedBB aabb = entity.getBoundingBox().grow(pad, pad, pad);
            Optional<Vec3> hit = aabb.rayTrace(from, to);
            if (aabb.contains(from)) {
                result.addEntityHit(entity);
            } else if (hit.isPresent()) {
                result.addEntityHit(entity);
            }
        }
        return result;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 1024;
    }

    private void updateWithPlayer() {
        this.setYaw((float) ((caster.getYRot()Head + 90) * Math.PI / 180.0d));
        this.setPitch((float) (-caster.getXRot() * Math.PI / 180.0d));
        this.setPosition(caster.getPosX(), caster.getPosY() + 1.2f, caster.getPosZ());
    }

    @Override
    public void remove() {
        super.remove();
        if (caster instanceof Player) {
            PlayerCapability.IPlayerCapability playerCapability = CapabilityHandler.getCapability(caster, PlayerCapability.PlayerProvider.PLAYER_CAPABILITY);
            if (playerCapability != null) {
                playerCapability.setUsingSolarBeam(false);
            }
        }
    }

    public static class HitResult {
        private BlockRayTraceResult blockHit;

        private final List<LivingEntity> entities = new ArrayList<>();

        public BlockRayTraceResult getBlockHit() {
            return blockHit;
        }

        public void setBlockHit(RayTraceResult rayTraceResult) {
            if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK)
                this.blockHit = (BlockRayTraceResult) rayTraceResult;
        }

        public void addEntityHit(LivingEntity entity) {
            entities.add(entity);
        }
    }
}
