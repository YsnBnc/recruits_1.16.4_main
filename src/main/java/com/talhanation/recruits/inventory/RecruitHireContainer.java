package com.talhanation.recruits.inventory;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;

public class RecruitHireContainer extends ContainerBase {


    private final PlayerEntity playerEntity;
    private final AbstractRecruitEntity recruit;

    public RecruitHireContainer(int id, PlayerEntity playerEntity, AbstractRecruitEntity recruit, PlayerInventory playerInventory) {
        super(Main.HIRE_CONTAINER_TYPE, id, null, new Inventory(0));
        this.playerEntity = playerEntity;
        this.recruit = recruit;
        this.playerInventory = playerInventory;

        addPlayerInventorySlots();
    }

    @Override
    public int getInvOffset() {
        return 56;
    }

    public AbstractRecruitEntity getRecruitEntity() {
        return recruit;
    }
}