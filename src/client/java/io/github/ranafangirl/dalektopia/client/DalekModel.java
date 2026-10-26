package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.DalekEntity;
import io.github.ranafangirl.dalektopia.Dalektopia;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DalekModel extends GeoModel<DalekEntity> {
    private static final Identifier MODEL = Identifier.of(Dalektopia.MOD_ID, "geo/entity/dalek/default_dalek_skirt.geo.json");
    private static final Identifier TEXTURE = Identifier.of(Dalektopia.MOD_ID, "textures/entity/dalek/default_dalek_skirt.png");
    private static final Identifier ANIMATION = Identifier.of(Dalektopia.MOD_ID, ""); // Animation is code only

    @Override
    public Identifier getModelResource(DalekEntity entity) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(DalekEntity entity) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DalekEntity entity) {
        return ANIMATION;
    }
}
