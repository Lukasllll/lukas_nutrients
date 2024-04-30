package net.lukasllll.lukas_nutrients.client.graphics.gui;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.graphics.gui.screens.NutrientScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;

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
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //The position of the inventory changes, if the recipe book is opened, so it has to be updated to keep the same relative position.
        this.setPosition(screen.getGuiLeft() + buttonXOffset, screen.height/2 - buttonYOffset);

        this.renderTexture(pGuiGraphics, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }
}
