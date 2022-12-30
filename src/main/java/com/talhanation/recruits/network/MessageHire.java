package com.talhanation.recruits.network;

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageHire implements Message<MessageHire> {

    private UUID player;
    private UUID recruit;

    public MessageHire() {}

    public MessageHire(UUID player, UUID recruit) {
        this.player = player;
        this.recruit = recruit;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){

        ServerPlayerEntity player = context.getSender();
        player.level.getEntitiesOfClass(AbstractRecruitEntity.class, player.getBoundingBox()
                .inflate(16.0D), v -> v
                .getUUID()
                .equals(this.recruit))
                .stream()
                .filter(AbstractRecruitEntity::isAlive)
                .findAny()
                .ifPresent(abstractRecruitEntity -> CommandEvents.handleRecruiting(player, abstractRecruitEntity));

    }

    public MessageHire fromBytes(PacketBuffer buf) {
        this.player = buf.readUUID();
        this.recruit = buf.readUUID();
        return this;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(this.player);
        buf.writeUUID(this.recruit);
    }

}