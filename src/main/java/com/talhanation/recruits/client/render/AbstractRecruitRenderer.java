package com.talhanation.recruits.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.talhanation.recruits.entities.AbstractInventoryEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.Minecraft;
public abstract class AbstractRecruitRenderer<Type extends AbstractInventoryEntity> extends MobRenderer<Type, PlayerModel<Type>> {

    public AbstractRecruitRenderer(EntityRendererProvider.Context mgr) {
        super(mgr, new PlayerModel<>((mgr.bakeLayer(EntityType.PLAYER)), false), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new BipedModel(mgr.bakeLayer(EntityType.PLAYER_INNER_ARMOR)), new BipedModel(mgr.bakeLayer(EntityType.PLAYER_OUTER_ARMOR))));
        this.addLayer(new ArrowLayer<>(mgr, this));
        this.addLayer(new BeeStingerLayer<>(this));
        this.addLayer(new CustomHeadLayer<>(this, mgr.getModelSet()));
        this.addLayer(new ItemInHandLayer<>(this));
    }

    public void render(AbstractInventoryEntity recruit, float p_117789_, float p_117790_, MatrixStack p_117791_, IRenderTypeBuffer p_117792_, int p_117793_) {
        this.setModelProperties(recruit);
        super.render((Type) recruit, p_117789_, p_117790_, p_117791_, p_117792_, p_117793_);
    }

    private void setModelProperties(AbstractInventoryEntity recruit) {
        PlayerModel<AbstractInventoryEntity> model = (PlayerModel<AbstractInventoryEntity>) this.getModel();

        model.setAllVisible(true);
        model.hat.visible = true;
        model.jacket.visible = false;
        model.leftPants.visible = false;
        model.rightPants.visible = false;
        model.leftSleeve.visible = false;
        model.rightSleeve.visible = false;
        model.crouching = recruit.isCrouching();
        BipedModel.ArmPose humanoidmodel$armpose = getArmPose(recruit, InteractionHand.MAIN_HAND);
        BipedModel.ArmPose humanoidmodel$armpose1 = getArmPose(recruit, InteractionHand.OFF_HAND);
        if (humanoidmodel$armpose.isTwoHanded()) {
            humanoidmodel$armpose1 = recruit.getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
        }
        if (recruit.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = humanoidmodel$armpose;
            model.leftArmPose = humanoidmodel$armpose1;
        } else {
            model.rightArmPose = humanoidmodel$armpose1;
            model.leftArmPose = humanoidmodel$armpose;
        }
    }

    private static BipedModel.ArmPose getArmPose(AbstractInventoryEntity recruit, InteractionHand hand) {
        ItemStack itemstack = recruit.getItemInHand(hand);
        if (itemstack.isEmpty()) {
            return BipedModel.ArmPose.EMPTY;
        } else {
            if (recruit.getUsedItemHand() == hand && recruit.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK)
                    return BipedModel.ArmPose.BLOCK;

                if (useanim == UseAnim.BOW) {
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == UseAnim.SPEAR) {
                    return BipedModel.ArmPose.THROW_SPEAR;
                }

                if (useanim == UseAnim.CROSSBOW && hand == recruit.getUsedItemHand()) {
                    return BipedModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == UseAnim.SPYGLASS) {
                    return BipedModel.ArmPose.SPYGLASS;
                }

                if (recruit.getUsedItemHand() == hand && recruit.getItemInHand(hand) == Items.SHIELD.getDefaultInstance()){
                    return BipedModel.ArmPose.ITEM;
                }
            } else if (!recruit.swinging && itemstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemstack)) {
                return BipedModel.ArmPose.CROSSBOW_HOLD;
            }

            return BipedModel.ArmPose.ITEM;
        }
    }
}
