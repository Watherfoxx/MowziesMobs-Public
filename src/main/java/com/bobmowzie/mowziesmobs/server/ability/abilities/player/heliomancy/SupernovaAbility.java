package com.bobmowzie.mowziesmobs.server.ability.abilities.player.heliomancy;

import com.bobmowzie.mowziesmobs.server.ability.Ability;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.RawAnimation;

public class SupernovaAbility extends HeliomancyAbilityBase {
    private boolean leftClickDown;
    private boolean rightClickDown;
    private int timeSinceLeftUp;
    private int timeSinceRightUp;
    private static final int BUFFER = 5;
    private Vec3[] particleEmitter;

    public SupernovaAbility(AbilityType<Player, SupernovaAbility> abilityType, Player user) {
        super(abilityType, user, EntityUmvuthi.SupernovaAbility.SECTION_TRACK);
        particleEmitter = new Vec3[1];
    }

    private static final RawAnimation SUPERNOVA_ANIM = RawAnimation.begin().thenPlay("supernova");

    @Override
    public void start() {
    }

    @Override
    public void tickUsing() {
    }

    @Override
    protected void beginSection(AbilitySection section) {
    }

    @Override
    public void onLeftMouseDown(Player player) {
        super.onLeftMouseDown(player);
        if (player == getUser()) leftClickDown = true;
    }

    @Override
    public void onLeftMouseUp(Player player) {
        super.onLeftMouseUp(player);
        if (player == getUser()) {
            leftClickDown = false;
            timeSinceLeftUp = BUFFER;
        }
    }

    @Override
    public void onRightMouseDown(Player player) {
        super.onRightMouseDown(player);
        if (player == getUser()) rightClickDown = true;
    }

    @Override
    public void onRightMouseUp(Player player) {
        super.onRightMouseUp(player);
        if (player == getUser()) {
            rightClickDown = false;
            timeSinceRightUp = BUFFER;
        }
    }

    public boolean isRightClickDown() {
        return rightClickDown || timeSinceRightUp > 0;
    }

    public boolean isLeftClickDown() {
        return leftClickDown || timeSinceLeftUp > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (getUser().isShiftKeyDown() && isLeftClickDown() && isRightClickDown()) {
            AbilityHandler.INSTANCE.sendAbilityMessage(getUser(), AbilityHandler.SUPERNOVA_ABILITY);
        }
        if (timeSinceRightUp > 0) timeSinceRightUp--;
        if (timeSinceLeftUp > 0) timeSinceLeftUp--;
    }

    @Override
    public boolean canCancelActiveAbility() {
        Ability ability = getActiveAbility();
        return ability != null && (ability.getAbilityType() == AbilityHandler.SOLAR_FLARE_ABILITY || ability.getAbilityType() == AbilityHandler.SOLAR_BEAM_ABILITY) && ability.getTicksInUse() < 5;
    }
}
