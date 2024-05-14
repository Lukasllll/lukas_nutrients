package net.lukasllll.lukas_nutrients.client.graphics.gui;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.config.EffectIconsConfig;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class NutrientToast implements Toast {

    private static final ResourceLocation ICONS = new ResourceLocation(LukasNutrients.MOD_ID, "textures/gui/icons.png");
    private static final double BASE_LIFETIME = 4000D;

    private long lastChanged;
    private boolean changed;
    private NutrientEffect effect;
    private boolean positive;

    public NutrientToast(NutrientEffect effect, boolean gained) {
        override(effect, gained);
    }

    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toastGui, long timeSinceLastVisible) {
        if (this.changed) {
            this.lastChanged = timeSinceLastVisible;
            this.changed = false;
        }

        graphics.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());

        graphics.drawString(toastGui.getMinecraft().font, "Nutrient Effects", 30, 7, ChatFormatting.GOLD.getColor(), false);
        MutableComponent line2 = Component.literal("");
        if(positive) line2.append("Gained ");
        else line2.append("Lost ");
        line2.append(Component.translatable(effect.getAttributeModifier().getAttribute().getDescriptionId()));
        graphics.drawString(toastGui.getMinecraft().font, line2, 30, 18, ChatFormatting.WHITE.getColor(), false);

        ResourceLocation effectLocation = new ResourceLocation(
                "minecraft","textures/mob_effect/" +
                EffectIconsConfig.getEffectIcon(effect.getAttributeString(), positive) + ".png");

        graphics.blit(effectLocation, 7, 7, 0, (float) 0.0, (float) 0.0, 18, 18, 18, 18);

        graphics.blit(ICONS, 2, 2, 0, 64, 9, 9);

        return (double)(timeSinceLastVisible - this.lastChanged) >= BASE_LIFETIME * toastGui.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;

    }

    public void override(NutrientEffect effect, boolean gained) {
        this.effect = effect;
        this.positive = gained == (effect.getAttributeModifierAmount() >= 0);
        this.changed = true;
    }

    public static void addOrUpdate(ToastComponent toastGui, NutrientEffect effect, boolean gained) {
        NutrientToast nutrientToast = toastGui.getToast(NutrientToast.class, NO_TOKEN);
        if (nutrientToast == null) {
            toastGui.addToast(new NutrientToast(effect, gained));
        } else {
            nutrientToast.override(effect, gained);
        }

    }
}
