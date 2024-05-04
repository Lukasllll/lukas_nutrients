package net.lukasllll.lukas_nutrients.networking;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsGlobalDataSyncS2CPacket;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsPlayerDataSyncS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int generateID() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(LukasNutrients.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(NutrientsPlayerDataSyncS2CPacket.class, generateID(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NutrientsPlayerDataSyncS2CPacket::new)
                .encoder(NutrientsPlayerDataSyncS2CPacket::toBytes)
                .consumerMainThread(NutrientsPlayerDataSyncS2CPacket::handle)
                .add();
        net.messageBuilder(NutrientsGlobalDataSyncS2CPacket.class, generateID(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NutrientsGlobalDataSyncS2CPacket::new)
                .encoder(NutrientsGlobalDataSyncS2CPacket::toBytes)
                .consumerMainThread(NutrientsGlobalDataSyncS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}