package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.IItemTier;
import net.minecraft.world.item.ToolItem;

import java.util.Set;

public abstract class MowzieToolItem extends ToolItem {
    public MowzieToolItem(float attackDamageIn, float attackSpeedIn, IItemTier tier, Set<Block> effectiveBlocksIn, Properties builderIn) {
        super(attackDamageIn, attackSpeedIn, tier, effectiveBlocksIn, builderIn);
    }

    public void getAttributesFromConfig() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getConfig().attackDamage.get() - 1.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", getConfig().attackSpeed.get() - 4.0, AttributeModifier.Operation.ADDITION));
        this.toolAttributes = builder.build();
    }

    @Override
    public float getAttackDamage() {
        return getConfig().attackDamage.get().floatValue();
    }

    public abstract ConfigHandler.ToolConfig getConfig();
}
