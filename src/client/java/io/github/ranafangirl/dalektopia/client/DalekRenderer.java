package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.DalekEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DalekRenderer extends GeoEntityRenderer<DalekEntity> {
    public DalekRenderer(EntityRendererFactory.Context context) {
        super(context, new DalekModel());
        addRenderLayer(new DalekLayer(this));
        this.shadowRadius = 0.4f;
    }
}
