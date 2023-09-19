package net.lukasllll.lukas_nutrients.event;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.commands.NutrientsCommand;
import net.lukasllll.lukas_nutrients.nutrients.PlayerNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.PlayerNutrients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = LukasNutrients.MOD_ID)
public class ModEvents {


    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).isPresent()) {
                event.addCapability(new ResourceLocation(LukasNutrients.MOD_ID, "nutrient_data"), new PlayerNutrientProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerNutrients.class);
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new NutrientsCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide()) {
            if(event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                    nutrients.recalculateAll();
                    nutrients.updateClient(player);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.getEntity() instanceof ServerPlayer player && event.isWasDeath()) {
            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                nutrients.setToDefault();
                nutrients.updateClient(player);
            });
        }
    }

}
