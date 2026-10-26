package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.DalekEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.List;

public class DalekLayer extends GeoRenderLayer<DalekEntity> {
    private static final float TORSO = 0.6f;
    private static final float TORSO_MAX = 70f, HEAD_MAX = 75f, EYE_MAX = 60f, ARM_MAX = 45f;

    // Temporary
    private static final List<DalekPart> MOUNTED = List.of(
            DalekPart.of("default_dalek_torso", "torso_point", List.of(
                    DalekPart.of("default_dalek_head", "head_point", List.of(
                            DalekPart.of("default_dalek_eye", "eye_point", List.of())
                    )),
                    DalekPart.aiming("default_dalek_gun", "left_arm_point", List.of()),
                    DalekPart.of("default_dalek_arm", "right_arm_point", List.of())
            ))
    );

    private record Aim(float torso, float rest, float pitch) { }

    public DalekLayer(GeoRenderer<DalekEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(MatrixStack pose, DalekEntity entity, GeoBone bone, RenderLayer type, VertexConsumerProvider vcp, VertexConsumer vc, float tick, int light, int overlay) {
        for (DalekPart part: MOUNTED) {
            if (!part.point().equals(bone.getName())) continue;
            Aim aim = aimOf(entity, tick);
            pose.push();
            RenderUtil.translateToPivotPoint(pose, bone);
            draw(part, aim, pose, entity, vcp, tick, light, overlay);
            pose.pop();
        }
    }

    private void draw(DalekPart part, Aim aim, MatrixStack pose, DalekEntity entity, VertexConsumerProvider vcp, float tick, int light, int overlay) {
        BakedGeoModel model = getGeoModel().getBakedModel(part.model());
        applyPoint(pose, part, aim);
        RenderLayer type = RenderLayer.getEntityCutoutNoCull(part.texture());
        VertexConsumer vc = vcp.getBuffer(type);

        for (GeoBone top: model.topLevelBones()) {
            getRenderer().renderRecursively(pose, entity, top, type, vcp, vc, true, tick, light, overlay, 0xFFFFFFFF);
        }

        for (DalekPart child: part.parts()) {
            model.getBone(child.point()).ifPresent(point -> {
                pose.push();
                RenderUtil.translateToPivotPoint(pose, point);
                draw(child, aim, pose, entity, vcp, tick, light, overlay);
                pose.pop();
            });
        }
    }

    private void applyPoint(MatrixStack pose, DalekPart part, Aim aim) {
        switch (part.point()) {
            case "torso_point" -> turn(pose, aim.torso(), TORSO_MAX, 0, 0);
            case "head_point" -> turn(pose, aim.rest(), HEAD_MAX, 0, 0);
            case "eye_point" -> turn(pose, 0, 0, aim.pitch(), EYE_MAX);
            case "left_arm_point", "right_arm_point" -> {
                if (part.aims()) turn(pose, aim.rest(), ARM_MAX, aim.pitch(), ARM_MAX);
            }
            default -> { }
        }
    }

    private static Aim aimOf(DalekEntity e, float tick) {
        float total = relativeYaw(e, tick);
        float torso = MathHelper.clamp(total * TORSO, -TORSO_MAX, TORSO_MAX);
        return new Aim(torso, total - torso, e.getPitch(tick));
    }

    private static void turn(MatrixStack pose, float yaw, float yawMax, float pitch, float pitchMax) {
        pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-MathHelper.clamp(yaw, -yawMax, yawMax)));
        pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-MathHelper.clamp(pitch, -pitchMax, pitchMax)));
    }

    private static float relativeYaw(DalekEntity e, float tick) {
        return MathHelper.wrapDegrees(MathHelper.lerpAngleDegrees(tick, e.prevHeadYaw, e.headYaw) - MathHelper.lerpAngleDegrees(tick, e.prevBodyYaw, e.bodyYaw));
    }
}