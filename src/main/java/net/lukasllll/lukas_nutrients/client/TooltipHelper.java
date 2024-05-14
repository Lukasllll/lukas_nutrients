package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TooltipHelper {
    public static Component getHoldShiftComponent() {
        return Component.translatable(LukasNutrients.MOD_ID + ".tooltip.holdForInfo", Component.translatable(LukasNutrients.MOD_ID + ".tooltip.keyShift").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY);
    }

    public static double round(double in) {
        return (double) Math.round(in * 100) / 100.0;
    }
}
