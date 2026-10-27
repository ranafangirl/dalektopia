package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.DalekEntity;
import io.github.ranafangirl.dalektopia.Dalektopia;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DalekParts {
    public static final String POINT_SUFFIX = "_point";

    public interface Motion {
        void apply(MatrixStack pose, Aim aim);
    }

    public record Aim(DalekEntity entity, float tick, float torso, float rest, float pitch) {
        public static Aim of(DalekEntity e, float tick) {
            float total = MathHelper.wrapDegrees(MathHelper.lerpAngleDegrees(tick, e.prevHeadYaw, e.headYaw) - MathHelper.lerpAngleDegrees(tick, e.prevBodyYaw, e.bodyYaw));
            float torso = MathHelper.clamp(total * TORSO_SHARE, -TORSO_MAX, TORSO_MAX);
            return new Aim(e, tick, torso, total - torso, e.getPitch(tick));
        }
    }

    private static final float TORSO_SHARE = 0.6f;
    private static final float TORSO_MAX = 70f, HEAD_MAX = 75f, NOD_MAX = 60f, AIM_MAX = 45f;

    private static final Motion STILL = (pose, aim) -> { };
    private static final Map<String, Motion> KINDS = new HashMap<>();
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    static {
        register("skirt", STILL);
        register("arm", STILL);
        register("torso", (pose, a) -> turn(pose, a.torso(), TORSO_MAX, 0, 0));
        register("head", (pose, a) -> turn(pose, a.rest(), HEAD_MAX, 0, 0));
        register("eye", (pose, a) -> turn(pose, 0, 0, a.pitch(), NOD_MAX));
        register("cannon", (pose, a) -> turn(pose, 0, 0, a.pitch(), NOD_MAX));
        register("gun", (pose, a) -> turn(pose, a.rest(), AIM_MAX, a.pitch(), AIM_MAX));
    }

    public static void register(String kind, Motion motion) {
        KINDS.put(kind, motion);
    }

    public static Motion motionOf(String kind) {
        return KINDS.getOrDefault(kind, STILL);
    }

    public static String kindOf(BakedGeoModel model) {
        List<GeoBone> tops = model.topLevelBones();
        return tops.isEmpty() ? "" : tops.get(0).getName();
    }

    public static void check(Identifier id, BakedGeoModel model) {
        if (!REPORTED.add(id.toString())) return;
        List<String> points = pointsOf(model).stream().map(GeoBone::getName).toList();
        String kind = kindOf(model);
        Dalektopia.LOGGER.info("[part] {}: kind={}, points={}", id, kind, points);

        if (model.topLevelBones().size() != 1) {
            Dalektopia.LOGGER.warn("[part] {}: expects exactly one point bone, but found {}", id, model.topLevelBones().size());
        }
        if (!KINDS.containsKey(kind)) {
            Dalektopia.LOGGER.warn("[part] {}: point bone '{}' is not a registered kind, so it will not be able to move", id, kind);
        }
    }

    public static void missingPoint(Identifier parent, String point) {
        if (REPORTED.add(parent + "#" + point)) {
            Dalektopia.LOGGER.warn("[part] {} has no mount point '{}'", parent, point);
        }
    }

    public static List<GeoBone> pointsOf(BakedGeoModel model) {
        List<GeoBone> out = new ArrayList<>();
        for (GeoBone top : model.topLevelBones()) collectPoints(top, out);
        return out;
    }

    private static void collectPoints(GeoBone bone, List<GeoBone> out) {
        if (bone.getName().endsWith(POINT_SUFFIX)) out.add(bone);
        for (GeoBone child : bone.getChildBones()) collectPoints(child, out);
    }

    private static void turn(MatrixStack pose, float yaw, float yawMax, float pitch, float pitchMax) {
        pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-MathHelper.clamp(yaw, -yawMax, yawMax)));
        pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-MathHelper.clamp(pitch, -pitchMax, pitchMax)));
    }
}
