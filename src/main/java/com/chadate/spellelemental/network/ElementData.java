package com.chadate.spellelemental.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ElementData implements CustomPacketPayload {
    static int entityId;
    static String element;
    static int duration;

    public final int entityIdMessage;
    public final String elementMessage;
    public final int durationMessage;

    public static int getEntityId() {
        return entityId;
    }

    public static String getElement() {
        return element;
    }

    public static int getDuration() {
        return duration;
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
            buf.writeInt(this.durationMessage);

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

    // 批量同步（实体元素快照）
    public static class ElementSnapshot implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ElementSnapshot> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("spellelemental", "element_snapshot"));
        public static final StreamCodec<FriendlyByteBuf, ElementSnapshot> STREAM_CODEC = CustomPacketPayload.codec(ElementSnapshot::write, ElementSnapshot::new);

        public final int entityId;
        public final String[] keys;
        public final int[] values;

        public ElementSnapshot(int entityId, String[] keys, int[] values) {
            this.entityId = entityId;
            this.keys = keys;
            this.values = values;
        }

        private ElementSnapshot(FriendlyByteBuf buf) {
            this.entityId = buf.readInt();
            int n = buf.readVarInt();
            this.keys = new String[n];
            this.values = new int[n];
            for (int i = 0; i < n; i++) {
                this.keys[i] = buf.readUtf();
                this.values[i] = buf.readVarInt();
            }
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeInt(entityId);
            buf.writeVarInt(keys.length);
            for (int i = 0; i < keys.length; i++) {
                buf.writeUtf(keys[i]);
                buf.writeVarInt(values[i]);
            }
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // 开关调试显示
    public static class ElementDebugToggle implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ElementDebugToggle> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("spellelemental", "element_debug_toggle"));
        public static final StreamCodec<FriendlyByteBuf, ElementDebugToggle> STREAM_CODEC = CustomPacketPayload.codec(ElementDebugToggle::write, ElementDebugToggle::new);

        public final boolean enabled;

        public ElementDebugToggle(boolean enabled) { this.enabled = enabled; }
        private ElementDebugToggle(FriendlyByteBuf buf) { this.enabled = buf.readBoolean(); }
        private void write(FriendlyByteBuf buf) { buf.writeBoolean(enabled); }
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 客户端 -> 服务端：检查某实体的元素快照
    public static class ElementInspectRequest implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ElementInspectRequest> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("spellelemental", "element_inspect_req"));
        public static final StreamCodec<FriendlyByteBuf, ElementInspectRequest> STREAM_CODEC = CustomPacketPayload.codec(ElementInspectRequest::write, ElementInspectRequest::new);
        public final int entityId;
        public ElementInspectRequest(int entityId) { this.entityId = entityId; }
        private ElementInspectRequest(FriendlyByteBuf buf) { this.entityId = buf.readInt(); }
        private void write(FriendlyByteBuf buf) { buf.writeInt(entityId); }
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 服务端 -> 客户端：检查回复
    public static class ElementInspectResponse implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ElementInspectResponse> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("spellelemental", "element_inspect_resp"));
        public static final StreamCodec<FriendlyByteBuf, ElementInspectResponse> STREAM_CODEC = CustomPacketPayload.codec(ElementInspectResponse::write, ElementInspectResponse::new);
        public final int entityId;
        public final String[] keys;
        public final int[] values;
        public ElementInspectResponse(int entityId, String[] keys, int[] values) { this.entityId = entityId; this.keys = keys; this.values = values; }
        private ElementInspectResponse(FriendlyByteBuf buf) {
            this.entityId = buf.readInt();
            int n = buf.readVarInt();
            this.keys = new String[n];
            this.values = new int[n];
            for (int i = 0; i < n; i++) {
                this.keys[i] = buf.readUtf();
                this.values[i] = buf.readVarInt();
            }
        }
        private void write(FriendlyByteBuf buf) {
            buf.writeInt(entityId);
            buf.writeVarInt(keys.length);
            for (int i = 0; i < keys.length; i++) {
                buf.writeUtf(keys[i]);
                buf.writeVarInt(values[i]);
            }
        }
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
