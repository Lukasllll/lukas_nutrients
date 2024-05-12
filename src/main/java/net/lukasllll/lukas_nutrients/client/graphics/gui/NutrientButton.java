package net.lukasllll.lukas_nutrients.client.graphics.gui;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.client.graphics.gui.screens.NutrientScreen;
import net.lukasllll.lukas_nutrients.event.ClientEvents;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class NutrientButton extends ImageButton {
    /*
    This button is added to the InventoryScreen and opens the NutrientScreen when clicked.
     */
    private static final ResourceLocation NUTRIENTS_BUTTON_LOCATION = new ResourceLocation(LukasNutrients.MOD_ID, "textures/gui/nutrients_button.png");
    private static final int buttonXOffset = 104 + 44;
    private static final int buttonYOffset = 22;

    private final InventoryScreen screen;

    public NutrientButton(InventoryScreen screen) {
        super(screen.getGuiLeft() + buttonXOffset, screen.height/2 - buttonYOffset, 20, 18, 0, 0, 19, NUTRIENTS_BUTTON_LOCATION, (button) -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new NutrientScreen());
        });
        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //The position of the inventory changes, if the recipe book is opened, so it has to be updated to keep the same relative position.
        this.setPosition(screen.getGuiLeft() + buttonXOffset, screen.height/2 - buttonYOffset);

        this.renderTexture(graphics, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);

        if(mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY < this.getY() + this.getHeight()) {
            List<Component> tooltip = new LinkedList<>();
            tooltip.add(Component.literal("Active Nutrient Effects:").withStyle(ChatFormatting.GRAY));
            for(NutrientEffect effect : ClientNutrientData.getActiveDietEffects()) {
                tooltip.add(effect.getSmallEffectTooltip());
            }
            graphics.renderComponentTooltip(screen.getMinecraft().font, tooltip, mouseX, mouseY);
        }
    }
}
