package com.talhanation.recruits.entities;
//ezgi&talha kantar

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.config.RecruitsModConfig;
import com.talhanation.recruits.entities.ai.*;
import com.talhanation.recruits.inventory.RecruitHireContainer;
import com.talhanation.recruits.inventory.RecruitInventoryContainer;
import com.talhanation.recruits.network.MessageHireGui;
import com.talhanation.recruits.network.MessageRecruitGui;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.potion.Effects;
import net.minecraft.potion.EffectInstance;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ActionResultType;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractRecruitEntity extends AbstractInventoryEntity{
    private static final DataParameter<Integer> DATA_REMAINING_ANGER_TIME = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> STATE = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> FOLLOW_STATE = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.INT);
    private static final DataParameter<Boolean> SHOULD_FOLLOW = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SHOULD_MOUNT = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SHOULD_ESCORT = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SHOULD_HOLD_POS = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> HOLD_POS = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Optional<BlockPos>> MOVE_POS = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Boolean> LISTEN = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> isFollowing = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<UUID>> MOUNT_ID = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<Optional<UUID>> ESCORT_ID = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<Boolean> MOVE = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> GROUP = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> XP = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> LEVEL = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> KILLS = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.INT);
    private static final DataParameter<Boolean> isEating = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FLEEING = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Float> HUNGER = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> MORAL = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.FLOAT);

    private static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<Boolean> OWNED = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.BOOLEAN);

    //private static final DataParameter<ItemStack> OFFHAND_ITEM_SAVE = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.ITEM_STACK);

    public ItemStack beforeFoodItem;
    public int blockCoolDown;
    public int eatCoolDown;

    public AbstractRecruitEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.setOwned(false);
        this.xpReward = 6;
    }

    ///////////////////////////////////TICK/////////////////////////////////////////

    public double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateMoral();
        updateShield();
    }

    private void resetItemInHand() {
        this.setItemInHand(Hand.OFF_HAND, this.beforeFoodItem);
        this.setSlot(10, this.beforeFoodItem);
        this.beforeFoodItem = null;
    }

    public void tick() {
        super.tick();
        updateSwingTime();
        updateSwimming();
        updateHunger();
        updateTeam();

        /*
        if (getOwner() != null) {
            this.getOwner().sendMessage(new StringTextComponent("Health: " + getAttribute(Attributes.ATTACK_DAMAGE).getValue()), getOwner().getUUID());
            this.getOwner().sendMessage(new StringTextComponent("Attack: " + getAttribute(Attributes.ATTACK_DAMAGE).getValue()), getOwner().getUUID());
            this.getOwner().sendMessage(new StringTextComponent("Speed: " + getAttribute(Attributes.ATTACK_DAMAGE).getValue()), getOwner().getUUID());
        }
        */
        if (this.getIsEating() && !this.isUsingItem()) {
            if (beforeFoodItem != null) resetItemInHand();
            setIsEating(false);
        }
        /*
        if (this.isBlocking()) {
            this.blockCooldown++;
        }

        if (blockCooldown >= 100){
            blockCooldown = 0;
        }

        if (blockCooldown < 50){
            canBlock = true;
        }else
            canBlock = false;


        if (getOwner() != null)
        this.getOwner().sendMessage(new StringTextComponent("Block Timer: " + blockCooldown), getOwner().getUUID());
        */

    }

    public void rideTick() {
        super.rideTick();
        /*
        if (this.getVehicle() instanceof CreatureEntity) {
            CreatureEntity creatureentity = (CreatureEntity)this.getVehicle();
            this.yBodyRot = creatureentity.yBodyRot;
        }
        */

    }

    @Nullable
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance diff, SpawnReason reason, @Nullable ILivingEntityData spawnData, @Nullable CompoundNBT nbt) {
        setRandomSpawnBonus();
        return spawnData;
    }

    public void setRandomSpawnBonus(){
        getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus", this.random.nextDouble() * 1.5, AttributeModifier.Operation.MULTIPLY_BASE));
        getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("attack_bonus", this.random.nextDouble(), AttributeModifier.Operation.MULTIPLY_BASE));
        getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("knockback_bonus", this.random.nextDouble() * 0.1D, AttributeModifier.Operation.MULTIPLY_BASE));
        getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("speed_bonus", this.random.nextDouble() * 0.1D, AttributeModifier.Operation.MULTIPLY_BASE));

    }

    public void setDropEquipment(){
        this.dropEquipment();
    }

    ////////////////////////////////////REGISTER////////////////////////////////////
    //TODO GOALLARI DÜZENLE
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RecruitQuaffGoal(this));
        this.goalSelector.addGoal(0, new FleeTNT(this));
        this.goalSelector.addGoal(0, new FleeFire(this));
        this.goalSelector.addGoal(1, new RecruitEscortEntityGoal(this));
        //this.goalSelector.addGoal(0, new (this));
        //TODO this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RecruitEatGoal(this));
        this.goalSelector.addGoal(2, new RecruitMountEntity(this));
        //this.goalSelector.addGoal(2, new RecruitMountGoal(this, 1.2D, 32.0F));
        this.goalSelector.addGoal(3, new RecruitMoveToPosGoal(this, 1.2D, 32.0F));
        this.goalSelector.addGoal(4, new RecruitFollowOwnerGoal(this, 1.2D, this.getFollowStartDistance(), 3.0F));
        this.goalSelector.addGoal(5, new RecruitMeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(6, new RecruitHoldPosGoal(this, 1.0D, 32.0F));
        this.goalSelector.addGoal(7, new RecruitMoveTowardsTargetGoal(this, 1.15D, 32.0F));
        this.goalSelector.addGoal(8, new RecruitPickupWantedItemGoal(this));
        this.goalSelector.addGoal(9, new ReturnToVillageGoal(this, 0.6D, false));
        this.goalSelector.addGoal(10, new PatrolVillageGoal(this, 0.6D));
        //TODO this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0F));
        this.goalSelector.addGoal(11, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(12, new LookRandomlyGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (target) -> {
            return (this.getState() == 2 && this.canAttack(target));
        }));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, (target) -> {
            return (this.getState() == 1 && this.canAttack(target));
        }));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractRecruitEntity.class, 10, true, false, (target) -> {
            return (this.getState() == 1 && this.canAttack(target));
        }));

        this.targetSelector.addGoal(0, new RecruitOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(0, new RecruitEscortHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, (new RecruitHurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(3, new RecruitOwnerHurtTargetGoal(this));

        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractIllagerEntity.class, 10, true, false, (target) -> {
            return (this.getState() != 3);
        }));

        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, MonsterEntity.class, 10, true, false, (target) -> {
            return this.canAttack(target) && !(target instanceof CreeperEntity) && (this.getState() != 3);
        }));
        this.targetSelector.addGoal(10, new RecruitDefendVillageGoal(this));
    }

    protected double getFollowStartDistance(){
        return RecruitsModConfig.RecruitFollowStartDistance.get();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
        this.entityData.define(GROUP, 0);
        this.entityData.define(SHOULD_FOLLOW, false);
        this.entityData.define(SHOULD_MOUNT, false);
        this.entityData.define(SHOULD_ESCORT, false);
        this.entityData.define(SHOULD_HOLD_POS, false);
        this.entityData.define(FLEEING, false);
        this.entityData.define(STATE, 0);
        this.entityData.define(XP, 0);
        this.entityData.define(KILLS, 0);
        this.entityData.define(LEVEL, 1);
        this.entityData.define(FOLLOW_STATE, 0);
        this.entityData.define(HOLD_POS, Optional.empty());
        this.entityData.define(MOVE_POS, Optional.empty());
        this.entityData.define(LISTEN, true);
        this.entityData.define(MOUNT_ID, Optional.empty());
        this.entityData.define(ESCORT_ID, Optional.empty());
        this.entityData.define(isFollowing, false);
        this.entityData.define(isEating, false);
        this.entityData.define(HUNGER, 50F);
        this.entityData.define(MORAL, 50F);
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(OWNED, false);
        //STATE
        // 0 = NEUTRAL
        // 1 = AGGRESSIVE
        // 2 = RAID

        //FOLLOW
        //0 = wander
        //1 = follow
        //2 = hold position
        //3 = back to position
        //4 = hold my position

    }
    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("AggroState", this.getState());
        nbt.putBoolean("ShouldFollow", this.getShouldFollow());
        nbt.putBoolean("ShouldMount", this.getShouldMount());
        nbt.putBoolean("ShouldEscort", this.getShouldEscort());
        nbt.putInt("Group", this.getGroup());
        nbt.putBoolean("Listen", this.getListen());
        nbt.putBoolean("Fleeing", this.getFleeing());
        nbt.putBoolean("isFollowing", this.isFollowing());
        nbt.putBoolean("isEating", this.getIsEating());
        nbt.putInt("Xp", this.getXp());
        nbt.putInt("Level", this.getXpLevel());
        nbt.putInt("Kills", this.getKills());
        nbt.putFloat("Hunger", this.getHunger());
        nbt.putFloat("Moral", this.getMoral());
        nbt.putBoolean("isOwned", this.getIsOwned());

        if(this.getHoldPos() != null){
            nbt.putInt("HoldPosX", this.getHoldPos().getX());
            nbt.putInt("HoldPosY", this.getHoldPos().getY());
            nbt.putInt("HoldPosZ", this.getHoldPos().getZ());
            nbt.putBoolean("ShouldHoldPos", this.getShouldHoldPos());
        }

        if(this.getOwnerUUID() != null){
            nbt.putUUID("OwnerUUID", this.getOwnerUUID());
        }

        if(this.getMountUUID() != null){
            nbt.putUUID("MountUUID", this.getMountUUID());
        }

        if(this.getEscortUUID() != null){
            nbt.putUUID("EscortUUID", this.getEscortUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        this.setXpLevel(nbt.getInt("Level"));
        this.setState(nbt.getInt("AggroState"));
        this.setShouldFollow(nbt.getBoolean("ShouldFollow"));
        this.setShouldMount(nbt.getBoolean("ShouldMount"));
        this.setShouldEscort(nbt.getBoolean("ShouldEscort"));
        this.setFleeing(nbt.getBoolean("Fleeing"));
        this.setGroup(nbt.getInt("Group"));
        this.setListen(nbt.getBoolean("Listen"));
        this.setIsFollowing(nbt.getBoolean("isFollowing"));
        this.setIsEating(nbt.getBoolean("isEating"));
        this.setXp(nbt.getInt("Xp"));
        this.setKills(nbt.getInt("Kills"));
        this.setHunger(nbt.getFloat("Hunger"));
        this.setMoral(nbt.getFloat("Moral"));
        this.setIsOwned(nbt.getBoolean("isOwned"));

        if (nbt.contains("HoldPosX") && nbt.contains("HoldPosY") && nbt.contains("HoldPosZ")) {
            this.setShouldHoldPos(nbt.getBoolean("ShouldHoldPos"));
            this.setHoldPos(new BlockPos (
                    nbt.getInt("HoldPosX"),
                    nbt.getInt("HoldPosY"),
                    nbt.getInt("HoldPosZ")));
        }

        if (nbt.contains("OwnerUUID")){
            Optional<UUID> uuid = Optional.of(nbt.getUUID("OwnerUUID"));
            this.setOwnerUUID(uuid);
        }

        if (nbt.contains("EscortUUID")){
            Optional<UUID> uuid = Optional.of(nbt.getUUID("EscortUUID"));
            this.setEscortUUID(uuid);
        }

        if (nbt.contains("MountUUID")){
            Optional<UUID> uuid = Optional.of(nbt.getUUID("MountUUID"));
            this.setMountUUID(uuid);
        }
    }


    ////////////////////////////////////GET////////////////////////////////////
    public PlayerEntity getOwner(){
        if (this.isOwned() && this.getOwnerUUID() != null){
            UUID ownerID = this.getOwnerUUID();
            return level.getPlayerByUUID(ownerID);
        }
        else
            return null;
    }

    public UUID getOwnerUUID(){
        return  this.entityData.get(OWNER_ID).orElse(null);
    }

    public UUID getEscortUUID(){
        return  this.entityData.get(ESCORT_ID).orElse(null);
    }

    public UUID getMountUUID(){
        return  this.entityData.get(MOUNT_ID).orElse(null);
    }

    public boolean getIsOwned() {
        return entityData.get(OWNED);
    }

    public float getMoral() {
        return this.entityData.get(MORAL);
    }

    public float getHunger() {
        return this.entityData.get(HUNGER);
    }

    public float getAttackDamage(){
        return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public BlockPos getRecruitOnPos(){
        return getOnPos();
    }

    public boolean getFleeing() {
        return entityData.get(FLEEING);
    }

    public int getKills() {
        return entityData.get(KILLS);
    }

    public int getXpLevel() {
        return entityData.get(LEVEL);
    }

    public int getXp() {
        return entityData.get(XP);
    }

    public boolean getIsEating() {
        return entityData.get(isEating);
    }

    public boolean getShouldHoldPos() {
        return entityData.get(SHOULD_HOLD_POS);
    }

    public boolean getShouldMount() {
        return entityData.get(SHOULD_MOUNT);
    }

    public boolean getShouldFollow() {
        return entityData.get(SHOULD_FOLLOW);
    }

    public boolean getShouldEscort() {
        return entityData.get(SHOULD_ESCORT);
    }

    public boolean isFollowing(){
        return entityData.get(isFollowing);
    }

    public int getState() {
        return entityData.get(STATE);
    }

    public int getGroup() {
        return entityData.get(GROUP);
    }

    public int getFollowState(){
        return entityData.get(FOLLOW_STATE);
    }

    public SoundEvent getHurtSound(DamageSource ds) {
        if (this.isBlocking())
            return SoundEvents.SHIELD_BLOCK;
        return SoundEvents.VILLAGER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    protected float getStandingEyeHeight(Pose pos, EntitySize size) {
        return size.height * 0.9F;
    }

    public int getMaxHeadXRot() {
        return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
    }

    public int getMaxSpawnClusterSize() {
        return 8;
    }

    public BlockPos getHoldPos(){
        return entityData.get(HOLD_POS).orElse(null);
    }

    @Nullable
    public BlockPos getMovePos(){
        return entityData.get(MOVE_POS).orElse(null);
    }

    public boolean getMove() {
        return entityData.get(MOVE);
    }

    public boolean getListen() {
        return entityData.get(LISTEN);
    }

    @Nullable
    public UUID getMount() {
        return entityData.get(MOUNT_ID).orElse(null);
    }

    /*
    public ItemStack getOffHandItemSave(){
        return  entityData.get(OFFHAND_ITEM_SAVE);
    }

     */

    ////////////////////////////////////SET////////////////////////////////////

    public void setIsOwned(boolean bool){
        entityData.set(OWNED, bool);
    }

    public void setOwnerUUID(Optional<UUID> id) {
        this.entityData.set(OWNER_ID,id);
    }

    public void setEscortUUID(Optional<UUID> id) {
        this.entityData.set(ESCORT_ID, id);
    }

    public void setMountUUID(Optional<UUID> id) {
        this.entityData.set(MOUNT_ID, id);
    }

    public void setMoral(float value) {
        this.entityData.set(MORAL, value);
    }

    public void setHunger(float value) {
        this.entityData.set(HUNGER, value);
    }

    public void setFleeing(boolean bool){
        entityData.set(FLEEING, bool);
    }

    public void disband(PlayerEntity player){
        player.sendMessage(new StringTextComponent(this.getName().getString() + ": " +"Then this is where we part ways."), player.getUUID());
        this.setTame(false);
        this.setTarget(null);
        this.setOwned(false);
        this.setOwnerUUID((Optional.empty()));
        CommandEvents.saveRecruitCount(player, CommandEvents.getSavedRecruitCount(player) - 1);
    }

    public void addXpLevel(int level){
        int currentLevel = this.getXpLevel();
        int newLevel = currentLevel + level;
        makelevelUpSound();
        this.entityData.set(LEVEL, newLevel);
    }

    public void setKills(int kills){
        this.entityData.set(KILLS, kills);
    }

    public void setXpLevel(int XpLevel){
        this.entityData.set(LEVEL, XpLevel);
    }

    public void setXp(int xp){
        this. entityData.set(XP, xp);
    }

    public void addXp(int xp){
        int currentXp = this.getXp();
        int newXp = currentXp + xp;

        this. entityData.set(XP, newXp);
    }

    public void setIsEating(boolean bool){
        entityData.set(isEating, bool);
    }

    public void setShouldHoldPos(boolean bool){
        entityData.set(SHOULD_HOLD_POS, bool);
    }

    public void setShouldEscort(boolean bool){
        entityData.set(SHOULD_ESCORT, bool);
    }

    public void setShouldMount(boolean bool){
        entityData.set(SHOULD_MOUNT, bool);
    }

    public void setShouldFollow(boolean bool){
            entityData.set(SHOULD_FOLLOW, bool);
    }

    public void setIsFollowing(boolean bool){
        entityData.set(isFollowing, bool);
    }

    public void setGroup(int group){
        entityData.set(GROUP, group);
    }

    public void setState(int state) {
        switch (state){
            case 0:
                setTarget(null);//wird nur 1x aufgerufen
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                setTarget(null);//wird nur 1x aufgerufen
                break;
        }
        entityData.set(STATE, state);
    }

    public void setFollowState(int state){
        switch (state){
            case 0:
                setShouldFollow(false);
                setShouldHoldPos(false);
                break;

            case 1:
                setShouldFollow(true);
                setShouldHoldPos(false);
                break;

            case 2:
                setShouldFollow(false);
                setShouldHoldPos(true);
                clearHoldPos();
                setHoldPos(getOnPos());
                break;

            case 3:
                setShouldFollow(false);
                setShouldHoldPos(true);
                break;

            case 4:
                setShouldFollow(false);
                setShouldHoldPos(true);
                clearHoldPos();
                setHoldPos(this.getOwner().blockPosition());
                state = 3;
                break;
        }
        entityData.set(FOLLOW_STATE, state);
    }

    public void setHoldPos(BlockPos holdPos){
        this.entityData.set(HOLD_POS, Optional.of(holdPos));
    }

    public void clearHoldPos(){
        this.entityData.set(HOLD_POS, Optional.empty());
    }

    public void setMovePos(BlockPos holdPos){
        this.entityData.set(MOVE_POS, Optional.of(holdPos));
    }

    public void clearMovePos(){
        this.entityData.set(MOVE_POS, Optional.empty());
    }

    public void setMove(boolean bool) {
        entityData.set(MOVE, bool);
    }

    public void setListen(boolean bool) {
        entityData.set(LISTEN, bool);
    }

    public void setOwned(boolean owned) {
        super.setTame(owned);
    }

    public void setEquipment(){}

    public void setMount(UUID uuid){
        entityData.set(MOUNT_ID, Optional.of(uuid));
    }

    public abstract void initSpawn();
    ////////////////////////////////////is FUNCTIONS////////////////////////////////////

    public boolean isOwned(){
        return getIsOwned();
    }

    public boolean isOwnedBy(PlayerEntity player){
        return player.getUUID() == this.getOwnerUUID() || player == this.getOwner();
    }

    ////////////////////////////////////ON FUNCTIONS////////////////////////////////////

    public ActionResultType mobInteract( PlayerEntity player, Hand hand) {
        String name = this.getName().getString() + ": ";
        boolean isPlayerTarget = this.getTarget() != null && getTarget().equals(player);

        if (this.level.isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isOwned() || !this.isOwned();
            return flag ? ActionResultType.CONSUME : ActionResultType.PASS;
        } else {
            if ((this.isOwned() && player.getUUID().equals(this.getOwnerUUID()))) {
                if (player.isCrouching()) {
                    checkItemsInInv();
                    openGUI(player);
                    return ActionResultType.SUCCESS;
                }
                if(!player.isCrouching()) {
                    int state = this.getFollowState();
                    switch (state) {
                        default:
                        case 0:
                            setFollowState(1);
                            String follow = TEXT_FOLLOW.getString();
                            player.sendMessage(new StringTextComponent(name + follow), player.getUUID());
                            break;
                        case 1:
                            setFollowState(2);
                            String holdyourpos = TEXT_HOLD_YOUR_POS.getString();
                            player.sendMessage(new StringTextComponent(name + holdyourpos), player.getUUID());
                            break;
                        case 2:
                            setFollowState(0);
                            String wander = TEXT_WANDER.getString();
                            player.sendMessage(new StringTextComponent(name + wander), player.getUUID());
                            break;
                    }
                    return ActionResultType.SUCCESS;
                }
            }

            else if (!this.isOwned() && CommandEvents.playerCanRecruit(player) && !isPlayerTarget) {

                this.openHireGUI(player);
                this.dialogue(name, player);
                this.navigation.stop();
                return ActionResultType.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
    }


    public boolean hire(PlayerEntity player) {
        String name = this.getName().getString() + ": ";
        if (!CommandEvents.playerCanRecruit(player)) {

            String info_max = INFO_RECRUITING_MAX.getString();
            player.sendMessage(new StringTextComponent(name + info_max), player.getUUID());
            return false;
        }
        else
            this.makeHireSound();

        this.setOwnerUUID(Optional.of(player.getUUID()));
        this.setIsOwned(true);
        this.navigation.stop();
        this.setTarget(null);
        this.setFollowState(2);
        this.setState(0);

        int i = this.random.nextInt(4);
        switch (i) {
            case 1 : {
                String recruited1 = TEXT_RECRUITED1.getString();
                player.sendMessage(new StringTextComponent(name + recruited1), player.getUUID());
            }
            case 2 : {
                String recruited2 = TEXT_RECRUITED2.getString();
                player.sendMessage(new StringTextComponent(name + recruited2), player.getUUID());
            }
            case 3 : {
                String recruited3 = TEXT_RECRUITED3.getString();
                player.sendMessage(new StringTextComponent(name + recruited3), player.getUUID());
            }
        }

        int currentRecruits = CommandEvents.getSavedRecruitCount(player);
        CommandEvents.saveRecruitCount(player,  currentRecruits + 1);
        return true;
    }

    public void dialogue(String name, PlayerEntity player) {
        int i = this.random.nextInt(4);
        switch (i) {
            case 1 : {
                String hello1 = TEXT_HELLO_1.getString();
                player.sendMessage(new StringTextComponent(name + hello1), player.getUUID());
            }
            case 2 : {
                String hello2 = TEXT_HELLO_2.getString();
                player.sendMessage(new StringTextComponent(name + hello2), player.getUUID());
            }
            case 3 : {
                String hello3 = TEXT_HELLO_3.getString();
                player.sendMessage(new StringTextComponent(name + hello3), player.getUUID());
            }
        }
    }

    ////////////////////////////////////ATTACK FUNCTIONS////////////////////////////////////

    public boolean hurt(DamageSource dmg, float amt) {
        if (this.isInvulnerableTo(dmg)) {
            return false;
        } else {
            Entity entity = dmg.getEntity();
            this.setOrderedToSit(false);
            if (entity != null && !(entity instanceof PlayerEntity) && !(entity instanceof AbstractArrowEntity)) {
                amt = (amt + 1.0F) / 2.0F;
            }
            //this.addXp(1);
            //this.checkLevel();

            return super.hurt(dmg, amt);
        }
    }

    public boolean doHurtTarget(Entity entity) {
        boolean flag = entity.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (flag) {
            this.doEnchantDamageEffects(this, entity);
        }
        this.addXp(2);
        this.checkLevel();
        this.damageMainHandItem();
        return flag;
    }

    public void addLevelBuffs(){
        int level = getXpLevel();
        if(level <= 10){
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus_level", 3D, AttributeModifier.Operation.ADDITION));
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("attack_bonus_level", 0.15D, AttributeModifier.Operation.ADDITION));
            getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("knockback_bonus_level", 0.01D, AttributeModifier.Operation.ADDITION));
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("speed_bonus_level", 0.01D, AttributeModifier.Operation.ADDITION));
        }
        if(level > 10){
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus_level", 2D, AttributeModifier.Operation.ADDITION));
        }
    }
    /*
           .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    */

    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof CreeperEntity) && !(target instanceof GhastEntity)) {
            if (target instanceof AbstractRecruitEntity) {

                AbstractRecruitEntity otherRecruit = (AbstractRecruitEntity)target;
                // || otherRecruit.getOwner().getTeam() != owner.getTeam() fix
                return otherRecruit.getOwner() != owner ;
            } else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canHarmPlayer((PlayerEntity)target)) {
                return false;
            } else if (target instanceof AbstractHorseEntity && ((AbstractHorseEntity)target).isTamed()) {
                return false;
            } else if (target instanceof AbstractOrderAbleEntity && ((AbstractOrderAbleEntity)target).getIsInOrder() && ((AbstractOrderAbleEntity)target).getOwner() != owner) {
                return true;
            } else if (target instanceof RecruitHorseEntity) {
                return false;
            } else {
                return !(target instanceof TameableEntity) || !((TameableEntity)target).isTame();
            }
        } else {
            return false;
        }
    }

    public void die(DamageSource dmg) {
        super.die(dmg);
        LivingEntity owner = this.getOwner();
        if (owner instanceof PlayerEntity){
            PlayerEntity player = (PlayerEntity) owner;
            CommandEvents.saveRecruitCount(player, CommandEvents.getSavedRecruitCount(player) - 1);
        }
    }


    ////////////////////////////////////OTHER FUNCTIONS////////////////////////////////////

    public void updateMoral(){
        boolean confused =  10 <= getMoral() && getMoral() < 30;
        boolean lowMoral =  30 <= getMoral() && getMoral() < 45;
        boolean highMoral =  80 <= getMoral() && getMoral() < 95;
        boolean strong =  95 <= getMoral();

        if(this.getIsEating()){
            if(getMoral() < 100) setMoral((getMoral() + 0.001F));
        }

        if (isStarving() && this.isOwned()){
            if(getMoral() > 0) setMoral((getMoral() - 0.01F));
        }

        if (this.isOwned() && !isSaturated()){
            if(getMoral() > 35) setMoral((getMoral() - 0.0001F));
        }

        if (confused) {
            if (!this.hasEffect(Effects.WEAKNESS))
                this.addEffect(new EffectInstance(Effects.WEAKNESS, 200, 3, false, true, true));
            if (!this.hasEffect(Effects.MOVEMENT_SLOWDOWN))
                this.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200, 2, false, true, true));
            if (!this.hasEffect(Effects.CONFUSION))
                this.addEffect(new EffectInstance(Effects.CONFUSION, 200, 1, false, true, true));
        }

        if (lowMoral) {
            if (!this.hasEffect(Effects.WEAKNESS))
                this.addEffect(new EffectInstance(Effects.WEAKNESS, 200, 1, false, true, true));
            if (!this.hasEffect(Effects.MOVEMENT_SLOWDOWN))
                this.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200, 1, false, true, true));
        }

        if (highMoral) {
            if (!this.hasEffect(Effects.DAMAGE_BOOST))
                this.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 200, 0, false, false, true));
            if (!this.hasEffect(Effects.DAMAGE_RESISTANCE))
                this.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 200, 0, false, false, true));
        }

        if (strong) {
            if (!this.hasEffect(Effects.DAMAGE_BOOST))
                this.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 200, 1, false, false, true));
            if (!this.hasEffect(Effects.DAMAGE_RESISTANCE))
                this.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 200, 1, false, false, true));
        }
    }

    public void updateHunger(){
        if(getHunger() > 0) {
            setHunger((getHunger() - 0.0001F));
        }
        if(eatCoolDown > 0){
            eatCoolDown--;
        }
    }

    public boolean needsToEat(){
        if (getHunger() <= 50F){
            return true;
        }
        else if(getHealth() <= (getMaxHealth() * 0.40) && eatCoolDown == 0) {
            return true;
        }
        else if(getHealth() <= (getMaxHealth() * 0.90) && this.getTarget() == null){
            return true;
        }else
            return false;
    }

    public boolean isStarving(){
        return (getHunger() <= 1F );
    }

    public boolean isSaturated(){
        return (getHunger() >= 90F);
    }

    @Override
    public void checkItemsInInv(){

    }

    public void checkLevel(){
        int currentXp = this.getXp();
        if (currentXp >= RecruitsModConfig.RecruitsMaxXpForLevelUp.get()){
            this.addXpLevel(1);
            this.setXp(0);
            this.addLevelBuffs();
            this.heal(10F);
        }
    }

    public void makelevelUpSound() {
        this.level.playSound(null, this.getX(), this.getY() + 4 , this.getZ(), SoundEvents.VILLAGER_YES, this.getSoundSource(), 15.0F, 0.8F + 0.4F * this.random.nextFloat());
        this.level.playSound(null, this.getX(), this.getY() + 4 , this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), 15.0F, 0.8F + 0.4F * this.random.nextFloat());
    }

    public void makeHireSound() {
        this.level.playSound(null, this.getX(), this.getY() + 4 , this.getZ(), SoundEvents.VILLAGER_AMBIENT, this.getSoundSource(), 15.0F, 0.8F + 0.4F * this.random.nextFloat());
    }

    public boolean isOwnedByThisPlayer(AbstractRecruitEntity recruit, PlayerEntity player){
        return  (recruit.getOwnerUUID() == player.getUUID());
    }

    @Override
    public boolean canBeLeashed(PlayerEntity player) {
        return false;
    }

    public abstract int recruitCosts();

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void spawnTamingParticles(boolean p_70908_1_) {
        IParticleData iparticledata = ParticleTypes.HAPPY_VILLAGER;
        if (!p_70908_1_) {
            iparticledata = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(iparticledata, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    protected void hurtArmor(DamageSource damageSource, float damage) {
        if (damage >= 0.0F) {
            damage = damage / 4.0F;
            if (damage < 1.0F) {
                damage = 1.0F;
            }
            for (int i = 11; i < 15; ++i) {//11,12,13,14 = armor
                ItemStack itemstack = this.inventory.getItem(i);
                if ((!damageSource.isFire() || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem) {
                    itemstack.setDamageValue((int) damage);
                }
            }
        }
    }

    protected void blockUsingShield(LivingEntity living) {
        super.blockUsingShield(living);
        if (living.getMainHandItem().canDisableShield(this.useItem, this, living))
            this.disableShield();
    }

    public void disableShield() {
        this.blockCoolDown = 100;
        this.stopUsingItem();
        this.level.broadcastEntityEvent(this, (byte) 30);
    }

    public boolean canBlock(){
        return this.blockCoolDown == 0;
    }

    public void updateShield(){
        if(this.blockCoolDown > 0){
            this.blockCoolDown--;
        }
    }

    protected void damageMainHandItem() {
        ItemStack itemstack = this.inventory.getItem(9);// 10 = hoffhand slot
        if (itemstack.getItem().isDamageable(itemstack)) {
            itemstack.setDamageValue(1);
        }
    }

    public boolean isValidTargetPlayer(PlayerEntity player){
        if(player.getUUID() == this.getOwnerUUID()) {
            return false;
        }
        else
            return RecruitEvents.canHarmTeam(this, player);
    }

    @Override
    public void killed(ServerWorld p_241847_1_, LivingEntity p_241847_2_) {
        super.killed(p_241847_1_, p_241847_2_);
        this.addXp(5);
        this.setKills(this.getKills() + 1);
    }

    @Override
    protected void hurtCurrentlyUsedShield(float damage) {
        if (this.useItem.isShield(this)) {
            int i = 1 + MathHelper.floor(damage);
            Hand hand = this.getUsedItemHand();
            this.useItem.hurtAndBreak(i, this, (entity) -> entity.broadcastBreakEvent(hand));
            if (this.useItem.isEmpty()) {
                if (hand == Hand.MAIN_HAND) {
                    this.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
                    this.setSlot(9, ItemStack.EMPTY);
                } else {
                    this.setItemSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
                    this.setSlot(10, ItemStack.EMPTY);
                }
                this.useItem = ItemStack.EMPTY;
                this.setItemSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
                this.setSlot(10, ItemStack.EMPTY);
                this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
            }

            ItemStack itemstack = this.inventory.getItem(10);// 10 = hoffhand slot
            if (itemstack.getItem() instanceof ShieldItem) {
                itemstack.setDamageValue((int) damage);
            }
        }
    }

    public static boolean canDamageTarget(AbstractRecruitEntity recruit, LivingEntity target) {
        if (recruit.isTame() && target instanceof AbstractRecruitEntity) {
            return !Objects.equals(recruit.getOwnerUUID(), ((AbstractRecruitEntity) target).getOwnerUUID());
        } else
            return true;
    }

    @Override
    public void openGUI(PlayerEntity player) {
        this.navigation.stop();

        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return getName();
                }

                @Nullable
                @Override
                public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    return new RecruitInventoryContainer(i, AbstractRecruitEntity.this, playerInventory);
                }
            }, packetBuffer -> {packetBuffer.writeUUID(getUUID());});
        } else {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageRecruitGui(player, this.getUUID()));
        }
    }
//TODO CRACKED
   /* public boolean isValidTarget(LivingEntity living){
        boolean notAllowed = living instanceof AbstractFishEntity || living instanceof AbstractHorseEntity || living instanceof CreeperEntity || living instanceof RecruitHorseEntity || living instanceof GhastEntity;


         if (living instanceof AbstractRecruitEntity otherRecruit) {
            if (otherRecruit.isOwned() && this.isOwned()){
                UUID recruitOwnerUuid = this.getOwnerUUID();
                UUID otherRecruitOwnerUuid = otherRecruit.getOwnerUUID();

                if(otherRecruit.getTeam() != null && this.getTeam() != null){
                    return !otherRecruit.getTeam().isAlliedTo(this.getTeam());
                }
                else if(recruitOwnerUuid != null && otherRecruitOwnerUuid != null){
                    return !recruitOwnerUuid.equals(otherRecruitOwnerUuid);
                }
            }
            else
                return RecruitEvents.canHarmTeam(this, living);
            return false;
        }
        return !notAllowed && !RecruitsModConfig.TargetBlackList.get().contains(living.getEncodeId());
    }*/


    /*public boolean canAttack(@Nonnull LivingEntity target) {
        if (canBeSeenAsEnemy()){
            if (target instanceof PlayerEntity player){
                return this.isValidTargetPlayer(player);
            }
            else
                return isValidTarget(target);
        }
        return false;
    }*/

    public void updateTeam(){
        if(this.isOwned() && getOwner() != null){
            Team team = this.getTeam();
            Team ownerTeam = this.getOwner().getTeam();
            if (team == ownerTeam) {
                return;
            }
            else if(ownerTeam == null){
                String teamName = team.getName();
                ScorePlayerTeam recruitTeam = this.level.getScoreboard().getPlayerTeam(teamName);
                this.level.getScoreboard().removePlayerFromTeam(this.getStringUUID(), recruitTeam);
            }
            else{
                String ownerTeamName = ownerTeam.getName();
                ScorePlayerTeam playerteam = this.level.getScoreboard().getPlayerTeam(ownerTeamName);

                boolean flag = playerteam != null && this.level.getScoreboard().addPlayerToTeam(this.getStringUUID(), playerteam);
                if (!flag) {
                    Main.LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", ownerTeamName);
                }else
                    this.setTarget(null);// fix "if owner was other team and now same team und was target"
            }
        }
    }

    public void openHireGUI(PlayerEntity player) {//TODO something aint right
        this.navigation.stop();

        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return getName();
                }

                @Nullable
                @Override
                public RecruitHireContainer createMenu(int i,  PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    return new RecruitHireContainer(i, playerInventory.player, AbstractRecruitEntity.this, playerInventory);
                }
            }, packetBuffer -> {packetBuffer.writeUUID(getUUID());});
        } else {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageHireGui((ServerPlayerEntity) player, this.getUUID()));
        }
    }

    private static final TranslationTextComponent TEXT_DISBAND = new TranslationTextComponent("chat.recruits.text.disband");
    private static final TranslationTextComponent TEXT_HELLO_1 = new TranslationTextComponent("chat.recruits.text.hello_1");
    private static final TranslationTextComponent TEXT_HELLO_2 = new TranslationTextComponent("chat.recruits.text.hello_2");
    private static final TranslationTextComponent TEXT_HELLO_3 = new TranslationTextComponent("chat.recruits.text.hello_3");
    public static final TranslationTextComponent TEXT_RECRUITED1 = new TranslationTextComponent("chat.recruits.text.recruited1");
    public static final TranslationTextComponent TEXT_RECRUITED2 = new TranslationTextComponent("chat.recruits.text.recruited2");
    public static final TranslationTextComponent TEXT_RECRUITED3 = new TranslationTextComponent("chat.recruits.text.recruited3");
    private static final TranslationTextComponent INFO_RECRUITING_MAX = new TranslationTextComponent("chat.recruits.info.reached_max");
    private static final TranslationTextComponent TEXT_FOLLOW = new TranslationTextComponent("chat.recruits.text.follow");
    private static final TranslationTextComponent TEXT_HOLD_YOUR_POS = new TranslationTextComponent("chat.recruits.text.hold_your_pos");
    private static final TranslationTextComponent TEXT_WANDER = new TranslationTextComponent("chat.recruits.text.wander");

    public void shouldMount(boolean should, UUID mount_uuid) {
        Main.LOGGER.debug("MOUNT: Abstract Recruit");
        if (!this.isPassenger()){
            this.setShouldMount(should);
            if(mount_uuid != null) {
                this.setMountUUID(Optional.of(mount_uuid));
                Main.LOGGER.debug("MOUNT: Abstract Recruit setMountUUID done");
            }
            else this.setMountUUID(Optional.empty());
        }
    }

    public void shouldEscort(boolean should, UUID escort_uuid) {
        this.setShouldEscort(should);
        if(escort_uuid != null) this.setEscortUUID(Optional.of(escort_uuid));
        else this.setEscortUUID(Optional.empty());
    }

/////TODO POSSIBLE RECRUIT BUG REASONS
    public boolean canBeSeenByAnyone() {
        return !this.isSpectator() && this.isAlive();
    }
    public boolean canBeSeenAsEnemy() {
        return isInvulnerable() && canBeSeenByAnyone();
    }

}
