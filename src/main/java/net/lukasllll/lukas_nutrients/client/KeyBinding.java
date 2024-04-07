package net.lukasllll.lukas_nutrients.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_NUTRIENTS = "key.category."+ LukasNutrients.MOD_ID;
    public static final String KEY_OPEN_GUI = "key."+LukasNutrients.MOD_ID+".open_gui";

    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(KEY_OPEN_GUI, KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N,KEY_CATEGORY_NUTRIENTS);


}
