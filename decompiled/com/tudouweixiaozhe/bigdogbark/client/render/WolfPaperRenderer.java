package com.tudouweixiaozhe.bigdogbark.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.tudouweixiaozhe.bigdogbark.BigDogBarkMod;
import com.tudouweixiaozhe.bigdogbark.client.WolfVisualStateManager;
import com.tudouweixiaozhe.bigdogbark.wolf.WolfVisualStateAccess;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.AABB;

public final class WolfPaperRenderer extends EntityRenderer<Wolf, WolfPaperRenderState> {
   private static final float ADULT_QUAD_SIZE = 1.48F;
   private static final float BABY_QUAD_SIZE = 1.3F;
   private static final float ADULT_BOTTOM = -0.13296875F;
   private static final float BABY_BOTTOM = -0.1828125F;
   private static final Identifier ADULT_NORMAL = texture("big_dog_normal.png");
   private static final Identifier ADULT_BARK = texture("big_dog_bark.png");
   private static final Identifier ADULT_MUSIC = texture("big_dog_music.png");
   private static final Identifier BABY_NORMAL = texture("puppy_normal.png");
   private static final Identifier BABY_BARK = texture("puppy_bark.png");
   private static final Identifier BABY_MUSIC = texture("puppy_music.png");

   public WolfPaperRenderer(Context context) {
      super(context);
      this.shadowRadius = 0.52F;
      this.shadowStrength = 0.8F;
   }

   public WolfPaperRenderState createRenderState() {
      return new WolfPaperRenderState();
   }

   public void extractRenderState(Wolf wolf, WolfPaperRenderState state, float partialTick) {
      super.extractRenderState(wolf, state, partialTick);
      WolfVisualStateManager.Effects effects = WolfVisualStateManager.effects(wolf, partialTick);
      state.baby = wolf.isBaby();
      state.sitting = wolf.isInSittingPose();
      state.barking = effects.barking();
      state.hurt = wolf.hurtTime > 0;
      state.musicMode = state.barking ? 0 : ((WolfVisualStateAccess)wolf).bigDogBark$getMusicMode();
      state.verticalOffset = effects.successJump();
      state.failurePitch = state.musicMode == 0 ? effects.failurePitch() : 0.0F;
      state.swayDegrees = 0.0F;
      state.spinDegrees = 0.0F;
      float phase = state.ageInTicks + Math.floorMod(wolf.getId() * 7, 20);
      if (state.musicMode == 1) {
         state.verticalOffset = state.verticalOffset + 0.15F * (1.0F - (float)Math.cos(phase * Math.PI * 2.0 / 20.0));
      } else if (state.musicMode == 2) {
         state.swayDegrees = (float)Math.sin(phase * Math.PI * 2.0 / 24.0) * 15.0F;
      } else if (state.musicMode == 3) {
         state.spinDegrees = phase * 18.0F;
      }
   }

   protected AABB getBoundingBoxForCulling(Wolf wolf) {
      return wolf.getBoundingBox().inflate(0.75, 1.0, 0.75);
   }

   public void submit(WolfPaperRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      if (!state.isInvisible) {
         poseStack.pushPose();
         poseStack.translate(0.0F, state.verticalOffset, 0.0F);
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.camera.yaw()));
         poseStack.mulPose(Axis.YP.rotationDegrees(state.spinDegrees));
         poseStack.mulPose(Axis.ZP.rotationDegrees(state.swayDegrees));
         poseStack.mulPose(Axis.XP.rotationDegrees(state.failurePitch));
         if (state.sitting && !state.barking && state.musicMode == 0) {
            poseStack.scale(1.0F, 0.88F, 1.0F);
         }

         Identifier bodyTexture = bodyTexture(state);
         int bodyRed = state.hurt ? 255 : 255;
         int bodyGreen = state.hurt ? 125 : 255;
         int bodyBlue = state.hurt ? 125 : 255;
         submitQuad(collector, poseStack, bodyTexture, state, bodyRed, bodyGreen, bodyBlue, 0.0F);
         poseStack.popPose();
      }

      super.submit(state, poseStack, collector, cameraState);
   }

   private static void submitQuad(
      SubmitNodeCollector collector, PoseStack poseStack, Identifier texture, WolfPaperRenderState state, int red, int green, int blue, float z
   ) {
      float bottom = state.baby ? -0.1828125F : -0.13296875F;
      float size = state.baby ? 1.3F : 1.48F;
      float top = bottom + size;
      float halfWidth = size / 2.0F;
      collector.submitCustomGeometry(
         poseStack,
         RenderTypes.entityCutout(texture),
         (pose, consumer) -> drawQuad(pose, consumer, state.lightCoords, halfWidth, bottom, top, red, green, blue, z)
      );
   }

   private static void drawQuad(Pose pose, VertexConsumer consumer, int light, float halfWidth, float bottom, float top, int red, int green, int blue, float z) {
      vertex(consumer, pose, -halfWidth, bottom, z, 0.0F, 1.0F, red, green, blue, light);
      vertex(consumer, pose, halfWidth, bottom, z, 1.0F, 1.0F, red, green, blue, light);
      vertex(consumer, pose, halfWidth, top, z, 1.0F, 0.0F, red, green, blue, light);
      vertex(consumer, pose, -halfWidth, top, z, 0.0F, 0.0F, red, green, blue, light);
   }

   private static void vertex(VertexConsumer consumer, Pose pose, float x, float y, float z, float u, float v, int red, int green, int blue, int light) {
      consumer.addVertex(pose, x, y, z)
         .setColor(red, green, blue, 255)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(light)
         .setNormal(pose, 0.0F, 0.0F, 1.0F);
   }

   private static Identifier bodyTexture(WolfPaperRenderState state) {
      if (state.baby) {
         if (state.barking) {
            return BABY_BARK;
         } else {
            return state.musicMode != 0 ? BABY_MUSIC : BABY_NORMAL;
         }
      } else if (state.barking) {
         return ADULT_BARK;
      } else {
         return state.musicMode != 0 ? ADULT_MUSIC : ADULT_NORMAL;
      }
   }

   private static Identifier texture(String name) {
      return BigDogBarkMod.id("textures/entity/wolf_2d/" + name);
   }
}
