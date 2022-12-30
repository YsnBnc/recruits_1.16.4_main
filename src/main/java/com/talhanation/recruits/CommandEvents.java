package com.talhanation.recruits;

import com.talhanation.recruits.config.RecruitsModConfig;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.inventory.CommandContainer;
import com.talhanation.recruits.network.MessageCommandScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.entity.Entity; //TODO POTENTIAL PROBLEM
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
//TODO (Doesnt contain createMenu()) import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class CommandEvents {

    private static int recruitsInCommand;
    public static int currentGroup;

    public static final TranslationTextComponent TEXT_HIRE_COSTS = new TranslationTextComponent("chat.recruits.text.hire_costs");
    public static final TranslationTextComponent TEXT_EVERYONE = new TranslationTextComponent("chat.recruits.text.everyone");
    public static final TranslationTextComponent TEXT_GROUP = new TranslationTextComponent("chat.recruits.text.group");

    public static final TranslationTextComponent TEXT_PASSIVE = new TranslationTextComponent("chat.recruits.command.passive");
    public static final TranslationTextComponent TEXT_NEUTRAL = new TranslationTextComponent("chat.recruits.command.neutral");
    public static final TranslationTextComponent TEXT_AGGRESSIVE = new TranslationTextComponent("chat.recruits.command.aggressive");
    public static final TranslationTextComponent TEXT_RAID = new TranslationTextComponent("chat.recruits.command.raid");

    public static final TranslationTextComponent TEXT_FOLLOW = new TranslationTextComponent("chat.recruits.command.follow");
    public static final TranslationTextComponent TEXT_WANDER = new TranslationTextComponent("chat.recruits.command.wander");
    public static final TranslationTextComponent TEXT_HOLD_POS = new TranslationTextComponent("chat.recruits.command.holdPos");
    public static final TranslationTextComponent TEXT_HOLD_MY_POS = new TranslationTextComponent("chat.recruits.command.holdMyPos");
    public static final TranslationTextComponent TEXT_BACK_TO_POS = new TranslationTextComponent("chat.recruits.command.backToPos");
    public static final TranslationTextComponent TEXT_DISMOUNT = new TranslationTextComponent("chat.recruits.command.dismount");
    public static final TranslationTextComponent TEXT_MOUNT = new TranslationTextComponent("chat.recruits.command.mount");
    public static final TranslationTextComponent TEXT_ESCORT = new TranslationTextComponent("chat.recruits.command.escort");


    public static void onRKeyPressed(UUID player_uuid, AbstractRecruitEntity recruit, int r_state, int group, boolean fromGui) {
        if (recruit.isOwned() && (recruit.getListen() || fromGui) && Objects.equals(recruit.getOwnerUUID(), player_uuid) && (recruit.getGroup() == group || group == 0)) {
            int state = recruit.getFollowState();
            switch (r_state) {

                case 0:
                    if (state != 0)
                        recruit.setFollowState(0);
                    break;

                case 1:
                    if (state != 1)
                        recruit.setFollowState(1);
                    break;

                case 2:
                    if (state != 2)
                        recruit.setFollowState(2);
                    break;

                case 3:
                    if (state != 3)
                        recruit.setFollowState(3);
                    break;

                case 4:
                    if (state != 4)
                        recruit.setFollowState(4);
                    break;

                case 5:
                    if (state != 5)
                        recruit.setFollowState(5);
                    break;
            }
        }
    }

    public static void onXKeyPressed(UUID player_uuid, AbstractRecruitEntity recruit, int x_state, int group, boolean fromGui) {
        if (recruit.isOwned() &&(recruit.getListen() || fromGui) && Objects.equals(recruit.getOwnerUUID(), player_uuid) && (recruit.getGroup() == group || group == 0)) {
            int state = recruit.getState();
            switch (x_state) {

                case 0:
                    if (state != 0)
                        recruit.setState(0);
                    break;

                case 1:
                    if (state != 1)
                        recruit.setState(1);
                    break;

                case 2:
                    if (state != 2)
                        recruit.setState(2);
                    break;

                case 3:
                    if (state != 3)
                        recruit.setState(3);
                    break;
            }
        }
    }

    public static void onCKeyPressed(UUID player_uuid, AbstractRecruitEntity recruit, int group) {
        /*
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity owner = recruit.getOwner();
        if (recruit.isTame() &&  Objects.equals(recruit.getOwnerUUID(), player_uuid)) {
            int state = recruit.getFollow();

            if (state != 2){
                RayTraceResult rayTraceResult = minecraft.RayTraceResult;
                if (rayTraceResult != null) {
                    if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
                        BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) rayTraceResult;
                        BlockPos blockpos = blockraytraceresult.getBlockPos();
                        recruit.setMovePos(blockpos);
                        recruit.setMove(true);
                    }
                    else if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY){
                        Entity crosshairEntity = minecraft.crosshairPickEntity;
                        if (crosshairEntity != null){
                            recruit.setMount(crosshairEntity.getUUID());
                        }

                    }
                }

            }
        }
*/
    }

    public static void openCommandScreen(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {

                @Override
                public ITextComponent getDisplayName() {
                    return new TranslationTextComponent("command_screen") {
                    };
                }

                @Nullable
                @Override
                public CommandContainer createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    return new CommandContainer(i, playerEntity);
                }
            }, packetBuffer -> {packetBuffer.writeUUID(player.getUUID());});
        } else {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageCommandScreen(player));
        }
    }


    public static void sendFollowCommandInChat(int state, LivingEntity owner, int group){
        String group_string = "";
        if (group == 0){
            group_string = TEXT_EVERYONE.getString() + ", ";
        }else
            group_string = TEXT_GROUP.getString() + " " + group + ", " ;

        switch (state) {
            case 0 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_WANDER.getString()), owner.getUUID());
            case 1 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_FOLLOW.getString()), owner.getUUID());
            case 2 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_HOLD_POS.getString()), owner.getUUID());
            case 3 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_BACK_TO_POS.getString()), owner.getUUID());
            case 4 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_HOLD_MY_POS.getString()), owner.getUUID());
            case 5 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_ESCORT.getString()), owner.getUUID());

            case 98 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_DISMOUNT.getString()), owner.getUUID());
            case 99 : owner.sendMessage(new StringTextComponent(group_string +  TEXT_MOUNT.getString()), owner.getUUID());
        }
    }

    public static void sendAggroCommandInChat(int state, LivingEntity owner, int group){
        String group_string = "";
        if (group == 0){
            group_string = TEXT_EVERYONE.getString() + ", ";
        }else
            group_string = TEXT_GROUP.getString() + " " + group + ", " ;


        switch (state) {
            case 0 : owner.sendMessage(new StringTextComponent(group_string + TEXT_NEUTRAL.getString()), owner.getUUID());
            case 1 : owner.sendMessage(new StringTextComponent(group_string + TEXT_AGGRESSIVE.getString()), owner.getUUID());
            case 2 : owner.sendMessage(new StringTextComponent(group_string + TEXT_RAID.getString()), owner.getUUID());
            case 3 : owner.sendMessage(new StringTextComponent(group_string + TEXT_PASSIVE.getString()), owner.getUUID());
        }
    }

    public static void setRecruitsInCommand(AbstractRecruitEntity recruit, int count) {
        LivingEntity living = recruit.getOwner();
        PlayerEntity player = (PlayerEntity) living;
        if (player != null){

            CompoundNBT playerNBT = player.getPersistentData();
            CompoundNBT nbt = playerNBT.getCompound(PlayerEntity.PERSISTED_NBT_TAG);

            nbt.putInt( "RecruitsInCommand", count);
            player.sendMessage(new StringTextComponent("EVENT int: " + count), player.getUUID());

            playerNBT.put(PlayerEntity.PERSISTED_NBT_TAG, nbt);
        }
    }

    public static int getRecruitsInCommand() {
        return recruitsInCommand;
    }

    public static void setCurrentGroup(int group) {
        currentGroup = group;
    }

    public static int getCurrentGroup() {
        return currentGroup;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        CompoundNBT playerData = event.getPlayer().getPersistentData();
        CompoundNBT data = playerData.getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        if (!data.contains("MaxRecruits")) data.putInt("MaxRecruits", RecruitsModConfig.MaxRecruitsForPlayer.get());
        if (!data.contains("CommandingGroup")) data.putInt("CommandingGroup", 0);
        if (!data.contains("TotalRecruits")) data.putInt("TotalRecruits", 0);

        playerData.put(PlayerEntity.PERSISTED_NBT_TAG, data);
    }
    /*
    @SubscribeEvent
    public void onPlayerLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        Entity entity = event.getEntityLiving();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;

            CompoundNBT playerData = player.getPersistentData();
            CompoundNBT data = playerData.getCompound(PlayerEntity.PERSISTED_NBT_TAG);

            if (player.isCrouching())
                player.sendMessage(new StringTextComponent("NBT: " + data.getInt("RecruitsInCommand")), player.getUUID());
        }
    }
     */

    public static int getSavedRecruitCount(PlayerEntity player) {
        CompoundNBT playerNBT = player.getPersistentData();
        CompoundNBT nbt = playerNBT.getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        //player.sendMessage(new StringTextComponent("getSavedCount: " + nbt.getInt("TotalRecruits")), player.getUUID());
        return nbt.getInt("TotalRecruits");
    }

    public static void saveRecruitCount(PlayerEntity player, int count) {
        CompoundNBT playerNBT = player.getPersistentData();
        CompoundNBT nbt = playerNBT.getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        //player.sendMessage(new StringTextComponent("savedCount: " + count), player.getUUID());

        nbt.putInt( "TotalRecruits", count);
        playerNBT.put(PlayerEntity.PERSISTED_NBT_TAG, nbt);
    }

    public static boolean playerCanRecruit(PlayerEntity player) {
        return  (CommandEvents.getSavedRecruitCount(player) < RecruitsModConfig.MaxRecruitsForPlayer.get());
    }

    public static void handleRecruiting(PlayerEntity player, AbstractRecruitEntity recruit){
        String name = recruit.getName().getString() + ": ";
        String hire_costs = TEXT_HIRE_COSTS.getString();
        int costs = recruit.recruitCosts();

        String recruit_info = String.format(hire_costs, costs);
        PlayerInventory playerInv = player.inventory;

        int playerEmeralds = 0;

        ItemStack emeraldItemStack = Items.EMERALD.getDefaultInstance();
        Item emerald = emeraldItemStack.getItem();//
        int sollPrice = recruit.recruitCosts();


        //checkPlayerMoney
        for (int i = 0; i < playerInv.getContainerSize(); i++){
            ItemStack itemStackInSlot = playerInv.getItem(i);
            Item itemInSlot = itemStackInSlot.getItem();
            if (itemInSlot == emerald){
                playerEmeralds = playerEmeralds + itemStackInSlot.getCount();
            }
        }

        boolean playerCanPay = playerEmeralds >= sollPrice;

        if (playerCanPay){
            if(recruit.hire(player)) {

                //give player tradeGood
                //remove playerEmeralds ->add left
                //
                playerEmeralds = playerEmeralds - sollPrice;

                //merchantEmeralds = merchantEmeralds + sollPrice;

                //remove playerEmeralds
                for (int i = 0; i < playerInv.getContainerSize(); i++) {
                    ItemStack itemStackInSlot = playerInv.getItem(i);
                    Item itemInSlot = itemStackInSlot.getItem();
                    if (itemInSlot == emerald) {
                        playerInv.removeItemNoUpdate(i);
                    }
                }

                //add leftEmeralds to playerInventory
                ItemStack emeraldsLeft = emeraldItemStack.copy();
                emeraldsLeft.setCount(playerEmeralds);
                playerInv.add(emeraldsLeft);
            }
        }
        else

            player.sendMessage(new StringTextComponent(name + recruit_info), player.getUUID());
    }



    @Nullable
    /*public static Entity getEntityByLooking() {
        //Entity pointedEntity = Minecraft.getInstance().crosshairPickEntity;// this only works for living and itemframe
        RayTraceResult hit = Minecraft.getInstance().hitResult;

        if (hit instanceof EntityRayTraceResult entityRayTraceResult){
            Entity pointedEntity = entityRayTraceResult.getEntity();

            //Main.LOGGER.debug("getEntityByLooking(): " + pointedEntity.getName().getString());
            return pointedEntity;
        }
        else {
            Main.LOGGER.debug("getEntityByLooking(): NULL");
        }
        return null;
    }*/


    public static void onMountButton(UUID player_uuid, AbstractRecruitEntity recruit, UUID mount_uuid, int group) {
        if (recruit.isOwned() && (recruit.getListen()) && Objects.equals(recruit.getOwnerUUID(), player_uuid) && (recruit.getGroup() == group || group == 0)) {
            recruit.shouldMount(true, mount_uuid);
        }
    }

    public static void onDismountButton(UUID player_uuid, AbstractRecruitEntity recruit, int group) {
        Main.LOGGER.debug("Dismount: Event start");
        Main.LOGGER.debug("Dismount: Event: player_uuid: "+ player_uuid);
        if (recruit.isOwned() && (recruit.getListen()) && Objects.equals(recruit.getOwnerUUID(), player_uuid) && (recruit.getGroup() == group || group == 0)) {
            recruit.shouldMount(false, null);
            if(recruit.isPassenger()){
                recruit.stopRiding();
                Main.LOGGER.debug("Dismount: Event done");
            }
        }
    }

    public static void onEscortButton(UUID player_uuid, AbstractRecruitEntity recruit, UUID escort_uuid, int group) {
        if (recruit.isOwned() && (recruit.getListen()) && Objects.equals(recruit.getOwnerUUID(), player_uuid) && (recruit.getGroup() == group || group == 0)) {
            recruit.shouldEscort(true, escort_uuid);
        }
    }

    public static void onStopButton(UUID player_uuid, AbstractRecruitEntity recruit, int group) {
        if (recruit.isOwned() && (recruit.getListen()) && Objects.equals(recruit.getOwnerUUID(), player_uuid) && (recruit.getGroup() == group || group == 0)) {
            recruit.setTarget(null);
        }
    }
}
