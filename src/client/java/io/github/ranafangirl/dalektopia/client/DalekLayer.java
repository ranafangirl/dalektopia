package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.DalekEntity;
import io.github.ranafangirl.dalektopia.DalekPart;
import io.github.ranafangirl.dalektopia.Dalektopia;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Map;

public class DalekLayer extends GeoRenderLayer<DalekEntity> {
    private static final int MAX_DEPTH = 8;

    public DalekLayer(GeoRenderer<DalekEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(MatrixStack pose, DalekEntity entity, GeoBone bone, RenderLayer type, VertexConsumerProvider vcp, VertexConsumer vc, float tick, int light, int overlay) {
        DalekPart part = entity.getBuild().parts().get(bone.getName());
        if (part == null) return;
        pose.push();
        RenderUtil.translateToPivotPoint(pose, bone);
        draw(part, 1, DalekParts.Aim.of(entity, tick), pose, entity, vcp, tick, light, overlay);
        pose.pop();
    }

    private void draw(DalekPart part, int depth, DalekParts.Aim aim, MatrixStack pose, DalekEntity entity, VertexConsumerProvider vcp, float tick, int light, int overlay) {
        if (depth > MAX_DEPTH) {
            Dalektopia.LOGGER.warn("[part] stopped at depth {}: {} mounts on itself?", MAX_DEPTH, part.model());
            return;
        }

        BakedGeoModel model = getGeoModel().getBakedModel(part.model());
        DalekParts.check(part.model(), model);
        DalekParts.motionOf(DalekParts.kindOf(model)).apply(pose, aim);

        RenderLayer type = RenderLayer.getEntityCutoutNoCull(part.texture());
        VertexConsumer vc = vcp.getBuffer(type);
        for (GeoBone top : model.topLevelBones()) {
            getRenderer().renderRecursively(pose, entity, top, type, vcp, vc, true, tick, light, overlay, 0xFFFFFFFF);
        }

        for (GeoBone point : DalekParts.pointsOf(model)) {
            DalekPart child = entity.getBuild().parts().get(point.getName());
            if (child == null) continue;
            pose.push();
            RenderUtil.translateToPivotPoint(pose, point);
            draw(child, depth + 1, aim, pose, entity, vcp, tick, light, overlay);
            pose.pop();
        }
    }
}