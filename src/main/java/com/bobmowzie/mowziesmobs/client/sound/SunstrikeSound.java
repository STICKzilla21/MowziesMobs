package com.bobmowzie.mowziesmobs.client.sound;

import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySunstrike;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.resources.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SunstrikeSound extends TickableSound {
    private final EntitySunstrike sunstrike;

    public SunstrikeSound(EntitySunstrike sunstrike) {
        super(MMSounds.SUNSTRIKE.get(), SoundCategory.NEUTRAL);
        this.sunstrike = sunstrike;
        volume = 1.5F;
        pitch = 1.1F;
        x = (float) sunstrike.getPosX();
        y = (float) sunstrike.getPosY();
        z = (float) sunstrike.getPosZ();
    }

    @Override
    public void tick() {
        if (!sunstrike.isAlive()) {
            finishPlaying();
        }
    }
}
