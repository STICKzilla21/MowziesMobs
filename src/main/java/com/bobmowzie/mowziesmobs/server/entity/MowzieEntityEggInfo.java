package com.bobmowzie.mowziesmobs.server.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobEntity;
import net.minecraft.resources.ResourceLocation;

public class MowzieEntityEggInfo {
    public final EntityType<? extends MobEntity> type;

    public final Class<? extends MobEntity> clazz;

    public final int primaryColor;

    public final int secondaryColor;

    public MowzieEntityEggInfo(EntityType<? extends MobEntity> type, Class<? extends MobEntity> clazz, int primaryColor, int secondaryColor) {
        this.type = type;
        this.clazz = clazz;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }
}
