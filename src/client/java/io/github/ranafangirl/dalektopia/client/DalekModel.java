package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.DalekEntity;
import io.github.ranafangirl.dalektopia.Dalektopia;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DalekModel extends GeoModel<DalekEntity> {
    private static final Identifier NO_ANIMATION = Identifier.of(Dalektopia.MOD_ID, "animations/entity/dalek/none.animation.json");

    @Override
    public Identifier getModelResource(DalekEntity entity) {
        return entity.getBuild().base().model();
    }

    @Override
    public Identifier getTextureResource(DalekEntity entity) {
        return entity.getBuild().base().texture();
    }

    @Override
    public Identifier getAnimationResource(DalekEntity entity) {
        return NO_ANIMATION;
    }
}