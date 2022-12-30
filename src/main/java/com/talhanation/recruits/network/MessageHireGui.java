package com.talhanation.recruits.network;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageHireGui implements Message<MessageHireGui> {

    private UUID uuid;
    private UUID recruit;


    public MessageHireGui(ServerPlayerEntity player, UUID uuid) {
        this.uuid = new UUID(0, 0);
    }

    public MessageHireGui(PlayerEntity player, UUID recruit) {
        this.uuid = player.getUUID();
        this.recruit = recruit;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (!context.getSender().getUUID().equals(uuid)) {
            return;
        }

        ServerPlayerEntity player = context.getSender();
        player.level.getEntitiesOfClass(AbstractRecruitEntity.class, player.getBoundingBox()
                .inflate(16.0D), v -> v
                .getUUID()
                .equals(this.recruit))
                .stream()
                .filter(AbstractRecruitEntity::isAlive)
                .findAny()
                .ifPresent(recruit -> recruit.openHireGUI(player));
    }

    @Override
    public MessageHireGui fromBytes(PacketBuffer buf) {
        this.uuid = buf.readUUID();
        this.recruit = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(recruit);
    }

}