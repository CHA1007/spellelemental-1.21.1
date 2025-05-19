package com.chadate.spellelemental.network.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ElementData implements CustomPacketPayload {
    static int entityId;
    static String element;
    static int duration;

    public int entityIdMessage;
    public String elementMessage;
    public int durationMessage;

    public static String getElement() {
        return element;
    }

    public static final CustomPacketPayload.Type<ElementData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("spellelemental", "element_data"));

    public static final StreamCodec<FriendlyByteBuf,ElementData> STREAM_CODEC = CustomPacketPayload.codec(ElementData::write,ElementData::new);

    private ElementData(FriendlyByteBuf buf) {
        this.entityIdMessage  = buf.readInt();
        this.elementMessage = buf.readUtf();
        this.durationMessage = buf.readInt();

    }

    public void write(FriendlyByteBuf buf) {
            buf.writeInt(this.entityIdMessage);
            buf.writeUtf(this.elementMessage);
            buf.writeInt(durationMessage);

    }

    public ElementData(int entityId,String element,int duration) {
        this.entityIdMessage = entityId;
        this.elementMessage = element;
        this.durationMessage = duration;

    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}