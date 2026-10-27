package io.github.ranafangirl.dalektopia;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class DalekEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final TrackedData<NbtCompound> BUILD = DataTracker.registerData(DalekEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    public static final Identifier DEFAULT_TYPE = Identifier.of(Dalektopia.MOD_ID, "default_dalek");

    private Identifier randomTypeId() {
        List<Identifier> ids = new ArrayList<>(getWorld().getRegistryManager().get(DalekType.KEY).getIds());
        return ids.isEmpty() ? DEFAULT_TYPE : ids.get(getRandom().nextInt(ids.size()));
    }

    private Identifier typeId;
    private boolean built;
    private boolean warned;

    private DalekBuild build = DalekBuild.DEFAULT_CODEC;

    public DalekEntity(EntityType<? extends DalekEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createDalekAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, DalekStatistics.DEFAULT.maxHealth())
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, DalekStatistics.DEFAULT.speed())
                .add(EntityAttributes.GENERIC_SCALE, DalekStatistics.DEFAULT.scale());
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason, @Nullable EntityData data) {
        if (!built) assignType(typeId != null ? typeId : randomTypeId());
        return super.initialize(world, difficulty, reason, data);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(4, new LookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BUILD, new NbtCompound());
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        DalekBuild current = build != null ? build : DalekBuild.DEFAULT_CODEC;
        return EntityDimensions.fixed(current.stats().width(), current.stats().height());
    }

    private void applyStats(DalekStatistics stats) {
        Dalektopia.LOGGER.info("[dalek] applying {}", stats);
        setBaseAttribute(EntityAttributes.GENERIC_MAX_HEALTH, stats.maxHealth());
        setBaseAttribute(EntityAttributes.GENERIC_MOVEMENT_SPEED, stats.speed());
        setBaseAttribute(EntityAttributes.GENERIC_SCALE, stats.scale());
        if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
    }

    private void setBaseAttribute(RegistryEntry<EntityAttribute> attribute, double value) {
        EntityAttributeInstance instance = getAttributeInstance(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    public void setBuild(DalekBuild newBuild) {
        built = true;
        build = newBuild;
        applyStats(newBuild.stats());
        calculateDimensions();
        DalekBuild.BUILD_CODEC.encodeStart(NbtOps.INSTANCE, newBuild).resultOrPartial(Dalektopia.LOGGER::error).ifPresent(nbt -> dataTracker.set(BUILD, (NbtCompound) nbt));
    }

    public DalekBuild getBuild() {
        return build;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (!BUILD.equals(data)) return;
        NbtCompound nbt = dataTracker.get(BUILD);
        build = nbt.isEmpty() ? DalekBuild.DEFAULT_CODEC: DalekBuild.BUILD_CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial(err -> Dalektopia.LOGGER.error("[dalek] bad build data: {}", err)).orElse(DalekBuild.DEFAULT_CODEC);
        calculateDimensions();
    }

    private void assignType(Identifier id) {
        Registry<DalekType> types = getWorld().getRegistryManager().get(DalekType.KEY);
        DalekType type = types.get(id);
        if (type == null && !id.equals(DEFAULT_TYPE)) {
            Dalektopia.LOGGER.warn("[dalek] is an unknown type {}, using default fallback: {}", id, DEFAULT_TYPE);
            id = DEFAULT_TYPE;
            type = types.get(id);
        }
        if (type == null) {
            if (!warned) {
                warned = true;
                Dalektopia.LOGGER.warn("[dalek] type {} was not found. Currently loaded types types are: {}", id, types.getIds());
            }
            return;
        }
        typeId = id;
        Dalektopia.LOGGER.info("[dalek] type {} stats={}", id, type.stats());
        setBuild(type.resolve(getRandom()));
        setHealth(getMaxHealth());
    }

    @Override
    public void tick() {
        if (!built && !getWorld().isClient) {
            assignType(typeId != null ? typeId : randomTypeId());
        }
        super.tick();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (typeId != null) nbt.putString("Type", typeId.toString());
        NbtCompound saved = dataTracker.get(BUILD);
        if (!saved.isEmpty()) nbt.put("Build", saved.copy());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Type", NbtElement.STRING_TYPE)) typeId = Identifier.tryParse(nbt.getString("Type"));
        NbtCompound saved = nbt.getCompound("Build");
        if (!saved.isEmpty()) {
            DalekBuild.BUILD_CODEC.parse(NbtOps.INSTANCE, saved).resultOrPartial(err -> Dalektopia.LOGGER.error("[dalek] empty save for build codec: {}", err)).ifPresent(this::setBuild);
        }
    }
}