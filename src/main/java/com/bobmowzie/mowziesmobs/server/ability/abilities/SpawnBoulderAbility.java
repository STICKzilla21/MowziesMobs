package com.bobmowzie.mowziesmobs.server.ability.abilities;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.server.ability.Ability;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityBoulder;
import com.bobmowzie.mowziesmobs.server.potion.EffectGeomancy;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.INBT;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.math.BlockRayTraceResult;
import net.minecraft.resources.math.RayTraceContext;
import net.minecraft.resources.math.RayTraceResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class SpawnBoulderAbility extends Ability {
    private static int MAX_CHARGE = 60;
    public static final double SPAWN_BOULDER_REACH = 5;

    public BlockPos spawnBoulderPos = new BlockPos(0, 0, 0);
    public Vec3 lookPos = new Vec3(0, 0, 0);
    private BlockState spawnBoulderBlock = Blocks.DIRT.getDefaultState();
    private int spawnBoulderCharge = 0;

    public SpawnBoulderAbility(AbilityType<? extends Ability> abilityType, LivingEntity user) {
        super(abilityType, user,  new AbilitySection[] {
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, MAX_CHARGE),
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 12)
        });
    }

    @Override
    public void start() {
        super.start();
        playAnimation("spawn_boulder_start", false);
    }

    @Override
    public boolean tryAbility() {
        Vec3 from = getUser().getEyePosition(1.0f);
        Vec3 to = from.add(getUser().getLookVec().scale(SPAWN_BOULDER_REACH));
        BlockRayTraceResult result = getUser().world.rayTraceBlocks(new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, getUser()));
        if (result.getType() == RayTraceResult.Type.BLOCK) {
            this.lookPos = result.getHitVec();
        }

        this.spawnBoulderPos = result.getPos();
        this.spawnBoulderBlock = getUser().world.getBlockState(spawnBoulderPos);
        if (result.getFace() != Direction.UP) {
            BlockState blockAbove = getUser().world.getBlockState(spawnBoulderPos.up());
            if (blockAbove.isSuffocating(getUser().world, spawnBoulderPos.up()) || blockAbove.isAir(getUser().world, spawnBoulderPos.up()))
                return false;
        }
        return EffectGeomancy.isBlockDiggable(spawnBoulderBlock);
    }

    @Override
    public void tickUsing() {
        super.tickUsing();
        if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP) {
            spawnBoulderCharge++;
            if (spawnBoulderCharge > 1) getUser().addPotionEffect(new MobEffectInstance(MobEffects.SLOWNESS, 3, 3, false, false));
            if (spawnBoulderCharge == 1 && getUser().world.isClientSide) MowziesMobs.PROXY.playBoulderChargeSound(getUser());
            if ((spawnBoulderCharge + 10) % 10 == 0 && spawnBoulderCharge < 40) {
                if (getUser().world.isClientSide) {
                    AdvancedParticleBase.spawnParticle(getUser().world, ParticleHandler.RING2.get(), (float) getUser().getPosX(), (float) getUser().getPosY() + getUser().getHeight() / 2f, (float) getUser().getPosZ(), 0, 0, 0, false, 0, Math.PI / 2f, 0, 0, 3.5F, 0.83f, 1, 0.39f, 1, 1, 10, true, true, new ParticleComponent[]{
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(0f, 0.7f), false),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd((0.8f + 2.7f * spawnBoulderCharge / 60f) * 10f, 0), false)
                    });
                }
            }
            if (spawnBoulderCharge == 50) {
                if (getUser().world.isClientSide) {
                    AdvancedParticleBase.spawnParticle(getUser().world, ParticleHandler.RING2.get(), (float) getUser().getPosX(), (float) getUser().getPosY() + getUser().getHeight() / 2f, (float) getUser().getPosZ(), 0, 0, 0, true, 0, 0, 0, 0, 3.5F, 0.83f, 1, 0.39f, 1, 1, 20, true, true, new ParticleComponent[]{
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(0.7f, 0f), false),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd(0, 40f), false)
                    });
                }
                getUser().playSound(MMSounds.EFFECT_GEOMANCY_MAGIC_SMALL.get(), 1, 1f);
            }

            int size = getBoulderSize() + 1;
            EntityType<EntityBoulder> type = EntityHandler.BOULDERS[size];
            if (
                    !getUser().world.hasNoCollisions(type.getBoundingBoxWithSizeApplied(spawnBoulderPos.getX() + 0.5F, spawnBoulderPos.getY() + 2, spawnBoulderPos.getZ() + 0.5F))
                    || getUser().getDistanceSq(spawnBoulderPos.getX(), spawnBoulderPos.getY(), spawnBoulderPos.getZ()) > 36
            ) {
                nextSection();
            }
        }
    }

    @Override
    protected void beginSection(AbilitySection section) {
        if (section.sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
            spawnBoulder();
        }
    }

    private int getBoulderSize() {
        return (int) Math.min(Math.max(0, Math.floor(spawnBoulderCharge/10.f) - 1), 2);
    }

    private void spawnBoulder() {
        if (spawnBoulderCharge <= 2) {
            playAnimation("spawn_boulder_instant", false);
        }
        else {
            playAnimation("spawn_boulder_end", false);
        }

        int size = getBoulderSize();
        if (spawnBoulderCharge >= 60) size = 3;
        EntityBoulder boulder = new EntityBoulder(EntityHandler.BOULDERS[size], getUser().world, getUser(), spawnBoulderBlock, spawnBoulderPos);
        boulder.setPosition(spawnBoulderPos.getX() + 0.5F, spawnBoulderPos.getY() + 2, spawnBoulderPos.getZ() + 0.5F);
        if (!getUser().world.isClientSide && boulder.checkCanSpawn()) {
            getUser().world.addEntity(boulder);
        }

        if (spawnBoulderCharge > 2) {
            Vec3 playerEyes = getUser().getEyePosition(1);
            Vec3 vec = playerEyes.subtract(lookPos).normalize();
            float yaw = (float) Math.atan2(vec.z, vec.x);
            float pitch = (float) Math.asin(vec.y);
            getUser().getYRot() = (float) (yaw * 180f / Math.PI + 90);
            getUser().getXRot() = (float) (pitch * 180f / Math.PI);
        }
        spawnBoulderCharge = 0;
    }

    @Override
    public void onRightMouseUp(Player player) {
        super.onRightMouseUp(player);
        if (isUsing() && getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP) {
            if (player.getDistanceSq(spawnBoulderPos.getX(), spawnBoulderPos.getY(), spawnBoulderPos.getZ()) < 36) {
                nextSection();
            } else {
                spawnBoulderCharge = 0;
            }
        }
    }

    @Override
    public boolean canUse() {
        return EffectGeomancy.canUse(getUser()) && super.canUse();
    }

    @Override
    public void end() {
        spawnBoulderCharge = 0;
        super.end();
    }

    @Override
    public void readNBT(INBT nbt) {
        super.readNBT(nbt);
        if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP) spawnBoulderCharge = getTicksInSection();
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        super.onRightClickBlock(event);
        if (!event.getWorld().isClientSide()) AbilityHandler.INSTANCE.sendAbilityMessage(event.getEntityLiving(), AbilityHandler.SPAWN_BOULDER_ABILITY);
    }

    @Override
    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        super.onRightClickEmpty(event);
        AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getPlayer(), AbilityHandler.SPAWN_BOULDER_ABILITY);
    }

    @Override
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        super.onRenderTick(event);
        if (isUsing() && getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP && getTicksInSection() > 1) {
            Vec3 playerEyes = getUser().getEyePosition(Minecraft.getInstance().getRenderPartialTicks());
            Vec3 vec = playerEyes.subtract(lookPos).normalize();
            float yaw = (float) Math.atan2(vec.z, vec.x);
            float pitch = (float) Math.asin(vec.y);
            getUser().getYRot() = (float) (yaw * 180f / Math.PI + 90);
            getUser().getXRot() = (float) (pitch * 180f / Math.PI);
            getUser().getYRot()Head = getUser().getYRot();
            getUser().yRot0 = getUser().getYRot();
            getUser().xRot0 = getUser().getXRot();
            getUser().yHeadRot0 = getUser().getYRot()Head;
        }
    }
}
