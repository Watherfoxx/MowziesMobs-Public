package com.bobmowzie.mowziesmobs.server.ability.abilities.player.heliomancy;

import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import software.bernie.geckolib.core.animation.RawAnimation;

public class SolarFlareAbility extends HeliomancyAbilityBase {

    public SolarFlareAbility(AbilityType<Player, SolarFlareAbility> abilityType, Player user) {
        super(abilityType, user, EntityUmvuthi.SolarFlareAbility.SECTION_TRACK);
    }

    private static final RawAnimation SOLAR_FLARE_ANIM = RawAnimation.begin().thenPlay("solar_flare");

    @Override
    public void start() {
    }

    @Override
    public boolean canUse() {
        if (getUser() == null || !getUser().getInventory().getSelected().isEmpty()) return false;
        return getUser().hasEffect(EffectHandler.SUNS_BLESSING.get()) && super.canUse();
    }

    @Override
    public void tickUsing() {
    }

    @Override
    protected void beginSection(AbilitySection section) {
    }

    @Override
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        super.onLeftClickEmpty(event);
        if (event.getEntity() == getUser() && event.getEntity().isShiftKeyDown()) AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getEntity(), AbilityHandler.SOLAR_FLARE_ABILITY);
    }

    @Override
    public void onLeftClickEntity(AttackEntityEvent event) {
        super.onLeftClickEntity(event);
        if (event.getEntity() == getUser() && event.getEntity().isShiftKeyDown()) AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getEntity(), AbilityHandler.SOLAR_FLARE_ABILITY);
    }
}
