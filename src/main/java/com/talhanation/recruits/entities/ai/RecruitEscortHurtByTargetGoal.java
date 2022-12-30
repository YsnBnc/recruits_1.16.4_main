package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.EntityPredicate;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class RecruitEscortHurtByTargetGoal extends TargetGoal {
    private final AbstractRecruitEntity recruit;
    private LivingEntity escortLastHurtBy;
    private int timestamp;

    public RecruitEscortHurtByTargetGoal(AbstractRecruitEntity recruit) {
        super(recruit, false);
        this.recruit = recruit;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        if (this.recruit.isOwned()) {
            LivingEntity escort = this.getEscort();
            if (escort == null) {
                return false;
            } else {
                this.escortLastHurtBy = escort.getLastHurtByMob();
                int i = escort.getLastHurtByMobTimestamp();
                return i != this.timestamp
                        && this.canAttack(this.escortLastHurtBy, EntityPredicate.DEFAULT)
                        && this.recruit.wantsToAttack(this.escortLastHurtBy, escort)
                        && recruit.getState() != 3;
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.escortLastHurtBy);
        this.mob.setLastHurtMob(this.escortLastHurtBy);
        LivingEntity livingentity = this.getEscort();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }
        super.start();
    }

    @Override
    public void tick() {
        super.tick();
    }
    @Nullable
    public LivingEntity getEscort(){
        List<LivingEntity> list = recruit.level.getEntitiesOfClass(LivingEntity.class, recruit.getBoundingBox().inflate(32D));
        for(LivingEntity livings : list){
            if (recruit.getEscortUUID() != null && livings.getUUID().equals(recruit.getEscortUUID()) && livings.isAlive()){
                return livings;
            }
        }
        return null;
    }

}