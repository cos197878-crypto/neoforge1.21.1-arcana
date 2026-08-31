package com.first.arcana.client.model;

import com.first.arcana.Arcana;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Blockbench 5.1.6 에서 modded_entity 포맷으로 내보낸 화염구 모델을 1.21.1 API 로 옮긴 것.
 *
 * 내보낸 코드에서 고친 부분:
 *   - new ResourceLocation(...)          -> Arcana.id(...)  (1.21 에서 생성자가 막혔다)
 *   - renderToBuffer(... float r,g,b,a)  -> (... int color) (1.21.1 에서 색이 int 하나로 합쳐졌다)
 *   - class fireball                     -> class FireballModel
 * 큐브 치수와 texOffs 는 손대지 않았다.
 *
 * 텍스처: assets/arcana/textures/entity/fireball.png (32x32)
 */
public class FireballModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Arcana.id("fireball"), "main");

    public static final ResourceLocation TEXTURE = Arcana.id("textures/entity/fireball.png");

    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 32;

    private final ModelPart bone;

    public FireballModel(ModelPart root) {
        // EntityModel 의 기본값인 entityCutoutNoCull 은 알파를 0/1 로만 처리한다(컷아웃).
        // 그러면 텍스처의 반투명 빨강(A=150)이 완전 불투명으로 그려져 안쪽 주황이 가려진다.
        // entityTranslucent 는 실제로 알파 블렌딩을 하고 쿼드 정렬도 해준다.
        super(RenderType::entityTranslucent);
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 12)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // 화염구는 관절이 없다. 회전은 나중에 렌더러에서 PoseStack 으로 준다.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
        bone.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
