package net.lukasllll.lukas_nutrients.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = LukasNutrients.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBinding {
    public static final String KEY_CATEGORY_NUTRIENTS = LukasNutrients.MOD_ID + ".key.category";
    public static final String KEY_OPEN_GUI = LukasNutrients.MOD_ID + ".key.open_gui";

    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(KEY_OPEN_GUI, KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N,KEY_CATEGORY_NUTRIENTS);

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.OPEN_GUI_KEY);
    }
}
