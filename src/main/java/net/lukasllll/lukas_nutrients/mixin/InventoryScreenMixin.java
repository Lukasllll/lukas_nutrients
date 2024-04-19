package net.lukasllll.lukas_nutrients.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.graphics.gui.screens.NutrientScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
This class injects the button to open the nutrient screen into the player inventory.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen {
    private static final ResourceLocation NUTRIENTS_BUTTON_LOCATION = new ResourceLocation(LukasNutrients.MOD_ID, "textures/gui/nutrients_button.png");
    private static final int buttonXOffset = 104 + 44;

    private ImageButton nutrientButton;

    public InventoryScreenMixin(AbstractContainerMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }

    @Inject(method = "init", at = @At("TAIL"))
    protected void onInit(CallbackInfo ci) {
        if (!this.getMinecraft().gameMode.hasInfiniteItems()) {

            nutrientButton = new ImageButton(this.leftPos + buttonXOffset, this.height / 2 - 22, 20, 18, 0, 0, 19, NUTRIENTS_BUTTON_LOCATION, (button) -> {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreen(new NutrientScreen());
            });

            this.addRenderableWidget(nutrientButton);
        }
    }

    /*
    The position of the actual inventory changes, if the recipe book is opened. When the recipe book button is clicked,
    it changes its own position accordingly but not the position of the new nutrientButton. This is why the nutrientButton
    position is manually updated here. Updating it every time the screen is rendered isn't actually necessary. I originally
    wanted to inject into the mouseClicked() function, but it seems to be called before the recipe book button is triggered
    so this.left isn't yet updated and I haven't found another suitable function to inject to.
     */
    @Inject(method = "render", at = @At("HEAD"))
    protected void onRender(GuiGraphics p_283246_, int p_98876_, int p_98877_, float p_98878_, CallbackInfo ci) {
        nutrientButton.setPosition(this.leftPos + buttonXOffset, this.height / 2 - 22);
    }
}
