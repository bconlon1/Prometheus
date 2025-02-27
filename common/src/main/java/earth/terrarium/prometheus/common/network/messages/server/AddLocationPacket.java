package earth.terrarium.prometheus.common.network.messages.server;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.prometheus.Prometheus;
import earth.terrarium.prometheus.common.handlers.locations.HomeHandler;
import earth.terrarium.prometheus.common.handlers.locations.WarpHandler;
import earth.terrarium.prometheus.common.menus.content.location.LocationType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record AddLocationPacket(LocationType type, String name) implements Packet<AddLocationPacket> {

    public static final PacketHandler<AddLocationPacket> HANDLER = new Handler();
    public static final ResourceLocation ID = new ResourceLocation(Prometheus.MOD_ID, "add_location");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<AddLocationPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<AddLocationPacket> {

        @Override
        public void encode(AddLocationPacket message, FriendlyByteBuf buffer) {
            buffer.writeEnum(message.type());
            buffer.writeUtf(message.name());
        }

        @Override
        public AddLocationPacket decode(FriendlyByteBuf buffer) {
            return new AddLocationPacket(buffer.readEnum(LocationType.class), buffer.readUtf());
        }

        @Override
        public PacketContext handle(AddLocationPacket message) {
            return (player, level) -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    switch (message.type) {
                        case HOME -> {
                            if (HomeHandler.add(serverPlayer, message.name)) {
                                OpenLocationPacket.openHomes(serverPlayer);
                            }
                        }
                        case WARP -> {
                            if (WarpHandler.add(serverPlayer, message.name)) {
                                OpenLocationPacket.openWarps(serverPlayer);
                            }
                        }
                    }
                }
            };
        }
    }
}
