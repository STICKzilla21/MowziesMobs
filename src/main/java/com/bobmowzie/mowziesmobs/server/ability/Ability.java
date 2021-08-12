package com.bobmowzie.mowziesmobs.server.ability;

import com.bobmowzie.mowziesmobs.server.capability.AbilityCapability;
import net.minecraft.entity.LivingEntity;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class Ability {
    private final AbilitySection[] sectionTrack;
    private final int cooldownMax;
    private final AbilityType<? extends Ability> abilityType;
    private final LivingEntity user;
    private final AbilityCapability.IAbilityCapability abilityCapability;

    private int ticksInUse;
    private int ticksInSection;
    private int currentSectionIndex;
    private boolean isUsing;
    private int cooldownTimer;

    public Ability(AbilityType<? extends Ability> abilityType, LivingEntity user, AbilitySection[] sectionTrack, int cooldownMax) {
        this.abilityType = abilityType;
        this.user = user;
        this.abilityCapability = AbilityHandler.INSTANCE.getAbilityCapability(user);
        this.sectionTrack = sectionTrack;
        this.cooldownMax = cooldownMax;
    }

    public Ability(AbilityType<? extends Ability> abilityType, LivingEntity user, AbilitySection[] sectionTrack) {
        this(abilityType, user, sectionTrack, 0);
    }

    public void start() {
        ticksInUse = 0;
        ticksInSection = 0;
        currentSectionIndex = 0;
        isUsing = true;
        if (!runsInBackground()) abilityCapability.setActiveAbility(this);
    }

    public void tick() {
        if (isUsing()) {
            if (!canContinueUsing()) end();

            tickUsing();

            ticksInUse++;
            ticksInSection++;
            AbilitySection section = getCurrentSection();
            if (section instanceof AbilitySectionInstant) {
                nextSection();
            } else if (section instanceof AbilitySectionDuration) {
                AbilitySectionDuration sectionDuration = (AbilitySectionDuration) section;
                if (ticksInSection > sectionDuration.duration) nextSection();
            }
        }
        else {
            tickNotUsing();
            if (getCooldownTimer() > 0) cooldownTimer--;
        }
    }

    public void tickUsing() {

    }

    public void tickNotUsing() {

    }

    public void end() {
        ticksInUse = 0;
        ticksInSection = 0;
        isUsing = false;
        cooldownTimer = getMaxCooldown();
        currentSectionIndex = 0;
        if (!runsInBackground()) abilityCapability.setActiveAbility(null);
    }

    public void interrupt() {
        end();
    }

    public void complete() {
        end();
    }

    /**
     * Server-only check to see if the user can use this ability. Checked before packet is sent.
     * @return Whether or not the ability can be used
     */
    public boolean canUse() {
        boolean nonBackgroundCheck = runsInBackground() || abilityCapability.getActiveAbility() == null || canCancelActiveAbility();
        return !isUsing() && cooldownTimer == 0 && nonBackgroundCheck;
    }

    /**
     * Both sides check and behavior when user tries to use this ability. Ability only starts if this returns true.
     * Called after packet is received.
     * @return Whether or not the ability try succeeded
     */
    public boolean tryAbility() {
        return true;
    }

    public boolean canCancelActiveAbility() {
        return false;
    }

    protected boolean canContinueUsing() {
        return true;
    }

    public boolean isUsing() {
        return isUsing;
    }

    public LivingEntity getUser() {
        return user;
    }

    public int getTicksInUse() {
        return ticksInUse;
    }

    public int getTicksInSection() {
        return ticksInSection;
    }

    public int getCooldownTimer() {
        return cooldownTimer;
    }

    public void nextSection() {
        jumpToSection(currentSectionIndex + 1);
    }

    public void jumpToSection(int sectionIndex) {
        currentSectionIndex = sectionIndex;
        ticksInSection = 0;
        if (currentSectionIndex >= getSectionTrack().length) {
            complete();
        }
        else {
            beginSection(getCurrentSection());
        }
    }

    protected void beginSection(AbilitySection section) {

    }

    public AbilitySection getCurrentSection() {
        if (currentSectionIndex >= getSectionTrack().length) return null;
        return getSectionTrack()[currentSectionIndex];
    }

    /**
     * Non-background abilities require no other non-background abilities running to run.
     * Only one non-background ability can run at once.
     * Background abilities can all run simultaneously
     * @return
     */
    public boolean runsInBackground() {
        return false;
    }

    public AbilitySection[] getSectionTrack() {
        return sectionTrack;
    }

    public int getMaxCooldown() {
        return cooldownMax;
    }

    // Events
    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {

    }

    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

    }

    public void onRightClickWithItem(PlayerInteractEvent.RightClickItem event) {

    }

    public void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {

    }

    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {

    }

    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

    }

    public void onLeftClickEntity(AttackEntityEvent event) {

    }

    public void onTakeDamage(LivingHurtEvent event) {

    }

    public void onJump(LivingEvent.LivingJumpEvent event) {

    }

    public void onRightMouseDown(PlayerEntity player) {

    }

    public void onLeftMouseDown(PlayerEntity player) {

    }

    public void onRightMouseUp(PlayerEntity player) {

    }

    public void onLeftMouseUp(PlayerEntity player) {

    }

    public void onSneakDown(PlayerEntity player) {

    }

    public void onSneakUp(PlayerEntity player) {

    }

    // Client events
    public void onRenderTick(TickEvent.RenderTickEvent event) {

    }
}
