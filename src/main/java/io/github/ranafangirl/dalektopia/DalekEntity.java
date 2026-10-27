package io.github.ranafangirl.dalektopia;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
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
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2);
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

    public void setBuild(DalekBuild newBuild) {
        built = true;
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
        build = nbt.isEmpty() ? DalekBuild.DEFAULT_CODEC: DalekBuild.BUILD_CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial(Dalektopia.LOGGER::error).orElse(DalekBuild.DEFAULT_CODEC);
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
        setBuild(type.resolve(getRandom()));
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