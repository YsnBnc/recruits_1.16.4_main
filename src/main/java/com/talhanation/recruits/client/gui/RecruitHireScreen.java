package com.talhanation.recruits.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.inventory.RecruitHireContainer;
import com.talhanation.recruits.network.MessageHire;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.text.DecimalFormat;

@OnlyIn(Dist.CLIENT)
public class RecruitHireScreen extends ScreenBase<RecruitHireContainer> {
    private static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(Main.MOD_ID,"textures/gui/hire_gui.png" );

    private static final TranslationTextComponent TEXT_HIRE = new TranslationTextComponent("gui.recruits.hire_gui.text.hire");

    private static final int fontColor = 4210752;

    private final AbstractRecruitEntity recruit;
    private final PlayerEntity player;

    public RecruitHireScreen(RecruitHireContainer recruitContainer, PlayerInventory playerInventory, ITextComponent title) {
        super(RESOURCE_LOCATION, recruitContainer, playerInventory, new StringTextComponent(""));
        this.recruit = recruitContainer.getRecruitEntity();
        this.player = playerInventory.player;
        imageWidth = 176;
        imageHeight = 218;
    }

    @Override
    protected void init() {
        super.init();
        int zeroLeftPos = leftPos + 180;
        int zeroTopPos = topPos + 10;


        int mirror = 240 - 60;

        addButton(new Button(zeroLeftPos - mirror + 40, zeroTopPos + 85, 100, 20, TEXT_HIRE, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageHire(player.getUUID(), recruit.getUUID()));
            this.onClose();
        }));
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        int health = MathHelper.ceil(recruit.getHealth());
        int maxHealth = MathHelper.ceil(recruit.getMaxHealth());
        int moral = MathHelper.ceil(recruit.getMoral());

        double A_damage = MathHelper.ceil(recruit.getAttackDamage());
        double speed = recruit.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) / 0.3;
        DecimalFormat decimalformat = new DecimalFormat("##.##");
        double armor = recruit.getArmorValue();
        int costs = recruit.recruitCosts();


        int k = 79;//rechst links
        int l = 19;//höhe

        //Titles
        font.draw(matrixStack, recruit.getDisplayName().getVisualOrderText(), 8, 5, fontColor);
        font.draw(matrixStack, player.getDisplayName().getVisualOrderText(), 8, this.imageHeight - 96 + 2, fontColor);

        //Info
        font.draw(matrixStack, "Hp:", k, l, fontColor);
        font.draw(matrixStack, "" + health, k + 25, l , fontColor);

        font.draw(matrixStack, "Lvl:", k , l  + 10, fontColor);
        font.draw(matrixStack, "" + recruit.getXpLevel(), k + 25 , l + 10, fontColor);

        font.draw(matrixStack, "Exp:", k, l + 20, fontColor);
        font.draw(matrixStack, "" + recruit.getXp(), k + 25, l + 20, fontColor);

        font.draw(matrixStack, "Kills:", k, l + 30, fontColor);
        font.draw(matrixStack, ""+ recruit.getKills(), k + 25, l + 30, fontColor);

        font.draw(matrixStack, "Moral:", k, l + 40, fontColor);
        font.draw(matrixStack, ""+ moral, k + 30, l + 40, fontColor);

        font.draw(matrixStack, "MaxHp:", k + 43, l, fontColor);
        font.draw(matrixStack, ""+ maxHealth, k + 77, l, fontColor);

        font.draw(matrixStack, "Attack:", k + 43, l + 10, fontColor);
        font.draw(matrixStack, ""+ A_damage, k + 77, l + 10, fontColor);

        font.draw(matrixStack, "Speed:", k +43, l + 20, fontColor);
        font.draw(matrixStack, ""+ decimalformat.format(speed), k + 77, l + 20, fontColor);

        font.draw(matrixStack, "Armor:", k + 43, l + 30, fontColor);
        font.draw(matrixStack, ""+ armor, k + 77, l + 30, fontColor);

        font.draw(matrixStack, "Costs:", k + 43, l + 40, fontColor);
        font.draw(matrixStack, ""+ costs, k + 77, l + 40, fontColor);

    }

    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        InventoryScreen.renderEntityInInventory(i + 40, j + 72, 20, (float)(i + 50) - mouseX, (float)(j + 75 - 50) - mouseY, this.recruit);
    }
}
