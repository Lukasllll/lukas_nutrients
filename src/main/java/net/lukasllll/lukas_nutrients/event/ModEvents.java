package net.lukasllll.lukas_nutrients.event;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsDataSyncS2CPacket;
import net.lukasllll.lukas_nutrients.nutrients.PlayerNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.PlayerNutrients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

@Mod.EventBusSubscriber(modid = LukasNutrients.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).isPresent()) {
                event.addCapability(new ResourceLocation(LukasNutrients.MOD_ID, "properties"), new PlayerNutrientProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerNutrients.class);
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide()) {
            if(event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                    ModMessages.sendToPlayer(new NutrientsDataSyncS2CPacket(nutrients.getNutrientAmounts()), player);
                });
            }
        }
    }

}
