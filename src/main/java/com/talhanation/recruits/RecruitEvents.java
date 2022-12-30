package com.talhanation.recruits;

import com.talhanation.recruits.config.RecruitsModConfig;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.RecruitEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.scoreboard.Team;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RecruitEvents {

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        Entity entity = event.getEntity();
        RayTraceResult rayTrace = event.getRayTraceResult();
        if (entity instanceof ProjectileEntity) {
            ProjectileEntity projectile = (ProjectileEntity)entity;
            Entity owner = projectile.getOwner();

            if (rayTrace.getType() == RayTraceResult.Type.ENTITY) {
                if (((EntityRayTraceResult) rayTrace).getEntity() instanceof LivingEntity) {
                    LivingEntity impactEntity = (LivingEntity) ((EntityRayTraceResult) rayTrace).getEntity();
                    if (owner instanceof AbstractRecruitEntity) {
                        AbstractRecruitEntity recruit = (AbstractRecruitEntity) owner;

                        if (!AbstractRecruitEntity.canDamageTarget(recruit, impactEntity)) {
                            event.setCanceled(true);
                        }

                        if (recruit.getOwner() == impactEntity) {
                            event.setCanceled(true);
                        } else
                            recruit.addXp(2);
                    }

                    if (owner instanceof AbstractIllagerEntity) {
                        AbstractIllagerEntity illager = (AbstractIllagerEntity) owner;

                        if (illager.isAlliedTo(impactEntity)) {
                            event.setCanceled(true);
                        }
                    }

                }
            }
        }
    }

    public static void onAttackButton(AbstractRecruitEntity recruit, LivingEntity owner, UUID target, int group) {
        if (recruit.getGroup() == group || group == 0) {
            List<LivingEntity> list = recruit.level.getEntitiesOfClass(LivingEntity.class, recruit.getBoundingBox().inflate(64.0D));
            for (LivingEntity potTargets : list) {
                recruit.getOwner().sendMessage(new StringTextComponent("FOR"), recruit.getOwner().getUUID());
                if (potTargets.getUUID() == target) {
                    if (recruit.getOwner() == owner && recruit.wantsToAttack(potTargets, owner))
                        recruit.getOwner().sendMessage(new StringTextComponent("TARGET"), recruit.getOwner().getUUID());
                        recruit.setTarget(potTargets);
                }
            }
        }
    }


    public static void onStopButton(AbstractRecruitEntity recruit, UUID owner, int group) {
        if (recruit.isTame() &&(recruit.getListen()) && Objects.equals(recruit.getOwnerUUID(), owner) && (recruit.getGroup() == group || group == 0)) {
            recruit.setTarget(null);
        }
    }

    int timestamp;
    public static final TranslationTextComponent TEXT_BLOCK_WARN = new TranslationTextComponent("chat.recruits.text.block_placing_warn");


    public static boolean canHarmTeam(LivingEntity attacker, LivingEntity target) {
        Team team = attacker.getTeam();
        Team team1 = target.getTeam();
        if (team == null) {
            return true;
        } else {
            return !team.isAlliedTo(team1) || team.isAllowFriendlyFire();
            //attacker can Harm target when attacker has no team
            //or attacker and target are not allied
            //or team friendlyfire is true
        }
    }
}
/*
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        Entity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();

        if(target instanceof AbstractRecruitEntity recruit && sourceEntity instanceof PlayerEntity player){

            if (!canHarmTeam(player, recruit)){
                event.setCanceled(true);
            }

        }
    }

    //TODO POTENTIAL BUG
    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        if(RecruitsModConfig.AggroRecruitsBlockEvents.get()) {
            PlayerEntity blockBreaker = event.getPlayer();
            timestamp = 0;
            List<AbstractRecruitEntity> list = Objects.requireNonNull(blockBreaker.level.getEntitiesOfClass(AbstractRecruitEntity.class, blockBreaker.getBoundingBox().inflate(32.0D)));
            for (AbstractRecruitEntity recruits : list) {
                if (canDamageTarget(recruits, blockBreaker) && recruits.getState() == 1) {
                    recruits.setTarget(blockBreaker);
                    if (timestamp < 1) {
                        blockBreaker.sendMessage(new StringTextComponent(list.get(0).getName().getString() + ": " + TEXT_BLOCK_WARN.getString()), blockBreaker.getUUID());
                        timestamp++;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.EntityPlaceEvent event) {
        if(RecruitsModConfig.AggroRecruitsBlockEvents.get()) {
            Entity blockPlacer = event.getEntity();
            timestamp = 0;
            if (blockPlacer instanceof LivingEntity livingBlockPlacer) {
                List<AbstractRecruitEntity> list = Objects.requireNonNull(livingBlockPlacer.level.getEntitiesOfClass(AbstractRecruitEntity.class, livingBlockPlacer.getBoundingBox().inflate(32.0D)));
                for (AbstractRecruitEntity recruits : list) {
                    if (canDamageTarget(recruits, livingBlockPlacer) && recruits.getState() == 1) {
                        recruits.setTarget(livingBlockPlacer);
                        if (timestamp < 1) {
                            livingBlockPlacer.sendMessage(new StringTextComponent(list.get(0).getName().getString() + ": " + TEXT_BLOCK_WARN.getString()), livingBlockPlacer.getUUID());
                            timestamp++;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeaveTeam(){

    }

    public boolean canDamageTarget(AbstractRecruitEntity recruit, LivingEntity target) {
        if (recruit.isOwned() && target instanceof AbstractRecruitEntity recruitEntityTarget) {
            if (recruit.getOwnerUUID().equals(recruitEntityTarget.getOwnerUUID())){
                return false;
            }
            //extra for safety
            else if (recruit.getTeam() != null && recruitEntityTarget.getTeam() != null && recruit.getTeam().equals(recruitEntityTarget.getTeam())){
                return false;
            }
        }
        return RecruitEvents.canHarmTeam(recruit, target);

    }
}*/
