package net.lukasllll.lukas_nutrients.event;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.KeyBinding;
import net.lukasllll.lukas_nutrients.client.textures.gui.screens.NutrientScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = LukasNutrients.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.OPEN_GUI_KEY);
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if(KeyBinding.OPEN_GUI_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreen(new NutrientScreen());
            }
        }
    }
}
