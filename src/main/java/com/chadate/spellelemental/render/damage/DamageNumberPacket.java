package com.chadate.spellelemental.render.damage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DamageNumberPacket implements CustomPacketPayload {
    private  int entityId;
    private  int damage;

    public int entityIdMessage;
    public int damageMessage;

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public static final CustomPacketPayload.Type<DamageNumberPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("spellelemental", "damage_number_packet"));

    public static final StreamCodec<FriendlyByteBuf,DamageNumberPacket> STREAM_CODEC = CustomPacketPayload.codec(DamageNumberPacket::write,DamageNumberPacket::new);

    private DamageNumberPacket(FriendlyByteBuf buf) {
        this.entityIdMessage  = buf.readInt();
        this.damageMessage = buf.readInt();

    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityIdMessage);
        buf.writeInt(this.damageMessage);

    }

    public DamageNumberPacket(int entityId, int damage) {
        this.entityIdMessage = entityId;
        this.damageMessage = damage;

    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
