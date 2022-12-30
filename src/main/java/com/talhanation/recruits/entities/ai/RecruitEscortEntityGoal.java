package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.List;

public class RecruitEscortEntityGoal extends Goal {

    private final AbstractRecruitEntity recruit;
    private LivingEntity escort;

    public RecruitEscortEntityGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
    }

    @Override
    public boolean canUse() {
        return recruit.getShouldEscort();
    }

    public boolean canContinueToUse() {
        return canUse() && this.escort != null;
    }

    public void start(){
        this.getEscort();
    }

    public void stop(){
        recruit.shouldEscort(false,null);
        recruit.setFollowState(3);
        this.escort = null;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {

        if(this.escort != null) {
            boolean isClose = escort.distanceTo(this.recruit) <= 5.50D;
            if(!isClose){
                recruit.getNavigation().moveTo(escort, 1.15F);
            }
        }

        if (this.escort == null || !escort.isAlive()) {
            if(this.escort != null && !escort.isAlive()){
                if (recruit.getOwner() != null)recruit.getOwner().sendMessage(new StringTextComponent(recruit.getName().getString() + ": The Escort died."), recruit.getOwner().getUUID());
            }
            stop();
        }
    }

    public void getEscort(){
        List<LivingEntity> list = recruit.level.getEntitiesOfClass(LivingEntity.class, recruit.getBoundingBox().inflate(32D));
        for(LivingEntity livings : list){
            if (recruit.getEscortUUID() != null && livings.getUUID().equals(recruit.getEscortUUID())){
                this.escort = livings;
            }
        }
    }
}