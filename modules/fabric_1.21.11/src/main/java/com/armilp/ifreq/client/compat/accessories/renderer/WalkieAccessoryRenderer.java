package com.armilp.ifreq.client.compat.accessories.renderer;

//import io.wispforest.accessories.api.client.AccessoryRenderer;
//import io.wispforest.accessories.api.slot.SlotReference;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.render.OverlayTexture;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.model.BipedEntityModel;
//import net.minecraft.client.render.entity.model.EntityModel;
//import net.minecraft.client.render.model.json.ModelTransformationMode;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.math.RotationAxis;
//
//public class WalkieAccessoryRenderer implements AccessoryRenderer {
//
//
//    @Override
//    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, MatrixStack matrices, EntityModel<M> model, VertexConsumerProvider multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//        if (!(model instanceof BipedEntityModel<M> bipedModel)) return;
//
//        matrices.push();
//
//        AccessoryRenderer.transformToModelPart(matrices, bipedModel.body);
//
//        matrices.translate(0.25, -0.20, 0.25);
//
//        matrices.scale(0.5f, 0.5f, 0.5f);
//
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
//        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(-95));
//
//        MinecraftClient.getInstance().getItemRenderer().renderItem(
//                stack,
//                ModelTransformationMode.FIXED,
//                light,
//                OverlayTexture.DEFAULT_UV,
//                matrices,
//                multiBufferSource,
//                reference.entity().getWorld(),
//                0
//        );
//
//        matrices.pop();
//    }
//}
