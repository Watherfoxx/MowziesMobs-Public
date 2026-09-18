package com.bobmowzie.mowziesmobs.client;

import com.bobmowzie.mowziesmobs.client.particle.ParticleCloud;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.ParticleRing;
import com.bobmowzie.mowziesmobs.client.particle.ParticleSnowFlake;
import com.bobmowzie.mowziesmobs.client.render.block.SculptorBlockMarking;
import com.bobmowzie.mowziesmobs.client.render.entity.FrozenRenderHandler;
import com.bobmowzie.mowziesmobs.client.sound.*;
import com.bobmowzie.mowziesmobs.server.ServerProxy;
import com.bobmowzie.mowziesmobs.server.ability.AbilityClientEventHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySolarBeam;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySunstrike;
import com.bobmowzie.mowziesmobs.server.entity.naga.EntityNaga;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends ServerProxy {
    private static final List<SunblockSound> sunblockSounds = new ArrayList<>();
    public static final Map<UUID, ResourceLocation> bossBarRegistryNames = new HashMap<>();

    public static final Long2ObjectMap<SculptorBlockMarking> sculptorMarkedBlocks = new Long2ObjectOpenHashMap<>();

    private Entity referencedMob = null;

    @Override
    public void init(final IEventBus modbus) {
        super.init(modbus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_CONFIG);

        modbus.register(MMModels.class);
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FrozenRenderHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(AbilityClientEventHandler.INSTANCE);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientLayerRegistry::onAddLayers);
    }

    @Override
    public void onLateInit(final IEventBus modbus) {
        ItemPropertyFunction pulling = ItemProperties.getProperty(Items.BOW, new ResourceLocation("pulling"));
        ItemProperties.register(ItemHandler.BLOWGUN.get().asItem(), new ResourceLocation("pulling"), pulling);
    }

    @Override
    public void playSunstrikeSound(EntitySunstrike strike) {
        Minecraft.getInstance().getSoundManager().play(new SunstrikeSound(strike));
    }

    @Override
    public void playIceBreathSound(Entity entity) {
        Minecraft.getInstance().getSoundManager().play(new IceBreathSound(entity));
    }

    @Override
    public void spawnIceBreathParticles(Level level, double x, double y, double z, float yRot, float xRot, RandomSource random, int particleTick) {
        float yaw = (float) Math.toRadians(-yRot);
        float pitch = (float) Math.toRadians(-xRot);
        float spread = 0.25f;
        float speed = 0.56f;
        float xComp = (float) (Math.sin(yaw) * Math.cos(pitch));
        float yComp = (float) Math.sin(pitch);
        float zComp = (float) (Math.cos(yaw) * Math.cos(pitch));
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        boolean overrideLimiter = camera.getPosition().distanceToSqr(x, y, z) < 64 * 64;

        if (particleTick % 8 == 0) {
            level.addAlwaysVisibleParticle(new ParticleRing.RingData(yaw, -pitch, 40, 1f, 1f, 1f, 1f, 110f * spread, false, ParticleRing.EnumRingBehavior.GROW), overrideLimiter, x, y, z, 0.5f * xComp, 0.5f * yComp, 0.5f * zComp);
        }

        for (int i = 0; i < 6; i++) {
            level.addParticle(new ParticleSnowFlake.SnowflakeData(37f, true), x, y, z, speed * xComp, speed * yComp, speed * zComp);
        }
        for (int i = 0; i < 5; i++) {
            double xSpeed = speed * xComp + spread * 0.7 * (random.nextFloat() * 2 - 1) * Math.sqrt(1 - xComp * xComp);
            double ySpeed = speed * yComp + spread * 0.7 * (random.nextFloat() * 2 - 1) * Math.sqrt(1 - yComp * yComp);
            double zSpeed = speed * zComp + spread * 0.7 * (random.nextFloat() * 2 - 1) * Math.sqrt(1 - zComp * zComp);
            float value = random.nextFloat() * 0.15f;
            level.addAlwaysVisibleParticle(new ParticleCloud.CloudData(ParticleHandler.CLOUD.get(), 0.75f + value, 0.75f + value, 1f, 10f + random.nextFloat() * 20f, 40, ParticleCloud.EnumCloudBehavior.GROW, 1f), overrideLimiter, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    public void playBoulderChargeSound(LivingEntity player) {
        Minecraft.getInstance().getSoundManager().play(new SpawnBoulderChargeSound(player));
    }

    @Override
    public void playNagaSwoopSound(EntityNaga naga) {
        Minecraft.getInstance().getSoundManager().play(new NagaSwoopSound(naga));
    }

    @Override
    public void playBlackPinkSound(AbstractMinecart entity) {
        Minecraft.getInstance().getSoundManager().play(new BlackPinkSound(entity));
    }

    @Override
    public void playSunblockSound(LivingEntity entity) {
        if (ConfigHandler.CLIENT.doUmvuthanaCraneHealSound.get()) {
            sunblockSounds.removeIf(e -> e == null || e.isStopped());
            if (sunblockSounds.size() < 10) {
                SunblockSound sunblockSound = new SunblockSound(entity);
                sunblockSounds.add(sunblockSound);
                try {
                    Minecraft.getInstance().getSoundManager().play(sunblockSound);
                } catch (ConcurrentModificationException ignored) {

                }
            }
        }
    }

    @Override
    public void playSolarBeamSound(EntitySolarBeam entity) {
        Minecraft.getInstance().getSoundManager().play(new SolarBeamSound(entity, false));
        Minecraft.getInstance().getSoundManager().play(new SolarBeamSound(entity, true));
    }

    public void playGeomancyRumbleSound(IGeomancyRumbler rumbler) {
        Minecraft.getInstance().getSoundManager().play(new EarthRumbleLoopSound(rumbler));
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
                    Vec3 minecartMotion = minecart.getDeltaMovement();
                    Minecraft.getInstance().particleEngine.add(new TerrainParticle(
                            world,
                            x + dx + offset, y + dy + offset, z + dz + offset,
                            dx + minecartMotion.x(), dy + minecartMotion.y(), dz + minecartMotion.z(),
                            state
                    ) {}.updateSprite(state, pos));
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

    public void sculptorMarkBlock(int id, BlockPos pos) {
        SculptorBlockMarking blockMarking = sculptorMarkedBlocks.get(pos.asLong());
        if (blockMarking == null) {
            blockMarking = new SculptorBlockMarking(pos);
            sculptorMarkedBlocks.put(pos.asLong(), blockMarking);
        }
        else {
            blockMarking.resetTick();
        }
    }

    public void updateMarkedBlocks() {
        Iterator<SculptorBlockMarking> iterator = sculptorMarkedBlocks.values().iterator();

        while(iterator.hasNext()) {
            SculptorBlockMarking blockMarking = iterator.next();
            int i = blockMarking.getTicks();
            if (i > blockMarking.getDuration()) {
                iterator.remove();
            }
            blockMarking.tick();
        }
    }

    @Override
    public Player getPlayer() {
        return Minecraft.getInstance().player;
    }
}
