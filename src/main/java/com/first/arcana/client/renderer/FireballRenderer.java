package com.first.arcana.client.renderer;

import com.first.arcana.client.model.FireballModel;
import com.first.arcana.entity.custom.FireballProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** 블록벤치에서 만든 FireballModel 을 화염구 엔티티에 그려준다. */
public class FireballRenderer extends EntityRenderer<FireballProjectile> {
    /**
     * 모델의 bone 원점이 y=21 (모델 단위)에 있다. 모델 좌표는 1/16 스케일이므로
     * 21/16 만큼 내려야 원점이 0 으로 온다.
     */
    private static final float BONE_PIVOT_Y = 21.0F / 16.0F;

    private final FireballModel<FireballProjectile> model;

    public FireballRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FireballModel<>(context.bakeLayer(FireballModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(FireballProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1) 히트박스 중앙으로 옮긴다.
        poseStack.translate(0.0F, entity.getBbHeight() / 2.0F, 0.0F);

        // 2) 카메라 방향으로 정렬한다(빌보드).
        //    어느 각도에서 봐도 한 면이 시선과 직각(90도)이 되므로 화면에는 정사각형으로 보인다.
        //    파티클이 쓰는 방식과 같고, 날아가는 방향과는 무관해진다.
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // 3) 엔티티 좌표계(Y 위)에서 모델 좌표계(Y 아래)로 뒤집는다.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -BONE_PIVOT_Y, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FireballProjectile entity) {
        return FireballModel.TEXTURE;
    }
}
