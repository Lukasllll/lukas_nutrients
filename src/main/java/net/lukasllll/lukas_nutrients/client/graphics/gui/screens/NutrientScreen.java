package net.lukasllll.lukas_nutrients.client.graphics.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.client.KeyBinding;
import net.lukasllll.lukas_nutrients.client.graphics.CustomTexture;
import net.lukasllll.lukas_nutrients.client.graphics.NativeImageLoader;
import net.lukasllll.lukas_nutrients.config.EffectIconsConfig;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.DietEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.commons.lang3.tuple.Triple;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NutrientScreen extends Screen {

    private static final Component TITLE = Component.translatable("gui." + LukasNutrients.MOD_ID +".nutrient_screen_title");
    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft", "textures/gui/demo_background.png");
    public static final ResourceLocation INVENTORY_LOCATION = new ResourceLocation("minecraft","textures/gui/container/inventory.png");
    private static final ResourceLocation ICONS = new ResourceLocation(LukasNutrients.MOD_ID, "textures/gui/icons.png");

    //the horizontal spacing between the different elements. In order:
    //icon distance from left side, text distance from icons, bar distance from text, number distance from bars, right side distance from numbers
    private static final int[] horizontalSpacing = {
            11, 4, 16, 7, 8
    };

    //the horizontal spacing between the different elements. In order:
    //icon distance from top, icon distance to next icon, last icon distance to separator, separator distance to summary icon, summary icon distance to bottom
    private static final int[] verticalSpacing = {
            10, 4, 7, 5, 10
    };

    private static final int TEXT_COLOR = new Color(63, 63, 63).getRGB();
    private static final int SEPARATOR_COLOR = new Color(139, 139, 139).getRGB();

    private static NutrientGroup[] Groups;

    private static NativeImage ICONS_BASE_IMAGE = null;
    private static ResourceLocation[] GroupBarLocations;
    private static ResourceLocation DietEffectBarLocation;
    private static ResourceLocation[] BarArrowsLocation;


    private int leftModuleWidth, middleModuleWidth, rightModuleWidth;
    private int totalWidth;
    private int mainSectionHeight, bottomSectionHeight;
    private int totalHeight;

    private int startX;
    private int startY;

    private List<Component> tooltip;

    public NutrientScreen() {
        super(TITLE);
        Groups = ClientNutrientData.getFoodGroups();
    }

    @Override
    protected void init() {
        super.init();
    }
    /*
    Method is needed to close the screen on pressing 'n' again.
    I don't now, what all these parameters do, so I didn't change their names.
     */
    public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_){
        InputConstants.Key key = InputConstants.getKey(p_97765_, p_97766_);
        if(super.keyPressed(p_97765_, p_97766_, p_97767_)) {
            return true;
        } else if (KeyBinding.OPEN_GUI_KEY.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        tooltip = null;
        if(startX == 0) calculateDimensions();
        renderBackground(pose);
        renderDietEffects(pose, mouseX, mouseY);
        renderLeftModule(pose);
        renderMiddleModule(pose);
        renderRightModule(pose);
        if(tooltip != null)
            renderComponentTooltip(pose, tooltip, mouseX, mouseY);
        super.render(pose, mouseX, mouseY, partialTick);
    }

    public void renderLeftModule(PoseStack pose) {
        int currentY = startY + verticalSpacing[0];
        for(int i = 0; i < Groups.length; i++) {
            //render Item
            this.itemRenderer.renderGuiItem(Groups[i].getDisplayItemStack(), startX + horizontalSpacing[0], currentY);
            //render Name
            this.font.draw(pose, Groups[i].getDisplayname(), startX + horizontalSpacing[0] + 16 + horizontalSpacing[1], currentY + 5, TEXT_COLOR);
            currentY += 16 + verticalSpacing[1];
        }
        currentY -= verticalSpacing[1];
        currentY += verticalSpacing[2] + 2 + verticalSpacing[3];

        //render sum symbol
        RenderSystem.setShaderTexture(0, ICONS);
        GuiComponent.blit(pose, startX + horizontalSpacing[0], currentY, 16, 16, 0, 0, 16, 16, 256, 256);
        //render Diet
        this.font.draw(pose, "Diet", startX + horizontalSpacing[0] + 16 + horizontalSpacing[1], currentY + 5, TEXT_COLOR);
    }

    public void renderMiddleModule(PoseStack pose) {
        ResourceLocation[] nutrientBars = getGroupBarLocations();
        ResourceLocation effectBar = getDietEffectBarLocation();
        ResourceLocation[] arrows = getBarArrowsLocation();

        int currentY = startY + verticalSpacing[0];
        int currentX =  startX + horizontalSpacing[0] + leftModuleWidth + horizontalSpacing[2];
        //draw nutrient amount bars
        for(int i = 0; i < Groups.length; i++) {
            //render empty bar
            RenderSystem.setShaderTexture(0, nutrientBars[i]);
            GuiComponent.blit(pose, currentX, currentY + 7, 101, 5, 0, 5, 101, 5, 256, 256);

            int amount = (int) ClientNutrientData.getPlayerNutrientAmounts()[i];
            int range = ClientNutrientData.getPlayerNutrientRanges()[i];

            int barLength = 1 + range + amount * 4;
            //render full bar
            RenderSystem.setShaderTexture(0, nutrientBars[i]);
            GuiComponent.blit(pose, currentX, currentY + 7, barLength, 5, 0, 0, barLength, 5, 256, 256);
            //render arrow
            if(barLength == 1) {
                barLength += 1;
            } else if(barLength == 101) {
                barLength -= 1;
            }
            RenderSystem.setShaderTexture(0, arrows[ClientNutrientData.getPlayerNutrientScores()[i]]);
            GuiComponent.blit(pose, currentX + barLength - 3, currentY + 1, 5, 5, 0, 0, 5, 5, 256, 256);
            //render exhaustion bar
            double exhaustionLevel = ClientNutrientData.getPlayerExhaustionLevels()[i];
            barLength = (int) ((4.0 - exhaustionLevel)/4.0 * 98.0);
            RenderSystem.setShaderTexture(0, ICONS);
            GuiComponent.blit(pose, currentX, currentY + 12, barLength, 2, 16, 10, barLength, 2, 256, 256);

            currentY += 16 + verticalSpacing[1];
        }
        currentY -= verticalSpacing[1];
        currentY += verticalSpacing[2] + 2 + verticalSpacing[3];
        //draw diet effects bar
        RenderSystem.setShaderTexture(0, effectBar);
        GuiComponent.blit(pose, currentX, currentY + 7, 101, 5, 0, 5, 101, 5, 256, 256);

        int totalScore=ClientNutrientData.getTotalScore();

        int effectsBarStartX, effectsBarEndX;
        boolean toTheRight = false;
        if(totalScore > DietEffects.BASE_POINT) {
            effectsBarStartX = 10 * DietEffects.BASE_POINT;
            effectsBarEndX = totalScore * 10 + 1;
            toTheRight = true;
        } else if(totalScore < DietEffects.BASE_POINT) {
            effectsBarEndX = 10 * DietEffects.BASE_POINT + 1;
            effectsBarStartX = 10 * totalScore;
        } else {
            effectsBarStartX = 10 * DietEffects.BASE_POINT;
            effectsBarEndX = 10 * DietEffects.BASE_POINT + 1;
        }

        RenderSystem.setShaderTexture(0, effectBar);
        GuiComponent.blit(pose, currentX + effectsBarStartX, currentY + 7, effectsBarEndX - effectsBarStartX, 5, effectsBarStartX, 0, effectsBarEndX - effectsBarStartX, 5, 256, 256);

        //draw diet effects bar arrow;
        int arrowArrayIndex = 1;
        if(totalScore < DietEffects.getPointRanges()[0]) {
            arrowArrayIndex = 0;
        } else if(totalScore > DietEffects.getPointRanges()[1]) {
            arrowArrayIndex = 2;
        }

        int arrowX = (toTheRight ? effectsBarEndX - 3 : effectsBarStartX - 2);
        if(totalScore == 0) arrowX += 1;
        else if(totalScore == 10) arrowX -= 1;

        RenderSystem.setShaderTexture(0, arrows[arrowArrayIndex]);
        GuiComponent.blit(pose, currentX + arrowX, currentY + 1, 5, 5, 0, 0, 5, 5, 256, 256);
    }

    public void renderRightModule(PoseStack pose) {

        int currentY = startY + verticalSpacing[0];
        int currentX =  startX + horizontalSpacing[0] + leftModuleWidth + horizontalSpacing[2] + middleModuleWidth + horizontalSpacing[3];

        int totalScore = ClientNutrientData.getTotalScore();

        for(int i = 0; i < Groups.length; i++) {
            //render box
            RenderSystem.setShaderTexture(0, ICONS);
            GuiComponent.blit(pose, currentX + 6, currentY + 2, 9, 11, 0, 16, 9, 11, 256, 256);
            //render number
            this.font.draw(pose, ""+ClientNutrientData.getPlayerNutrientScores()[i], currentX + 8, currentY + 4, TEXT_COLOR);
            this.font.draw(pose, "/2", currentX + 17, currentY + 4, TEXT_COLOR);
            currentY += 16 + verticalSpacing[1];
        }
        currentY -= verticalSpacing[1];
        currentY += verticalSpacing[2] + 2 + verticalSpacing[3];
        //render box
        RenderSystem.setShaderTexture(0, ICONS);
        GuiComponent.blit(pose, currentX, currentY + 2, 15, 11, 0, 27, 15, 11, 256, 256);
        //render number
        if(totalScore < 10) {
            this.font.draw(pose, "" + totalScore, currentX + 5, currentY + 4, TEXT_COLOR);
        } else {
            this.font.draw(pose, "" + totalScore, currentX + 2, currentY + 4, TEXT_COLOR);
        }
        this.font.draw(pose, "/10", currentX + 17, currentY + 4, TEXT_COLOR);
    }

    /*
    Renders the icons for currently active diet effect and sets the appropriate tooltip, if moused-over
     */
    public void renderDietEffects(PoseStack pose, int mouseX, int mouseY) {
        List<Triple<String, AttributeModifier.Operation, Double>> activeDietEffects = ClientNutrientData.getActiveDietEffects();

        int x = startX + totalWidth + 2;
        int y = startY;

        int numberOfEffectsRendered = Math.min(activeDietEffects.size(), 2 * totalHeight/32);

        for(int i = 0; i < numberOfEffectsRendered; i++) {
            //background
            RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
            this.blit(pose, x, y, 0, 198, 32, 32);

            //effect image
            ResourceLocation effectLocation = new ResourceLocation(
                    "minecraft","textures/mob_effect/" +
                    EffectIconsConfig.getEffectIcon(activeDietEffects.get(i).getLeft(), activeDietEffects.get(i).getRight()) + ".png");

            RenderSystem.setShaderTexture(0, effectLocation);
            this.blit(pose, x + 7, y + 7, 0, (float) 0.0, (float) 0.0, 18, 18, 18, 18);

            /*
            The tooltip is just created here. It is rendered, once renderComponentTooltip is called
             */
            if(mouseX >= x && mouseX < x+32 && mouseY >= y && mouseY < y+32) {
                tooltip = new ArrayList<>();
                switch (activeDietEffects.get(i).getMiddle()) {
                    case ADDITION:
                        if(activeDietEffects.get(i).getRight() >= 0)
                            tooltip.add(Component.translatable(("attribute.modifier.plus.0"), Component.literal("" + activeDietEffects.get(i).getRight()), Component.translatable(Registry.ATTRIBUTE.get(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        else
                            tooltip.add(Component.translatable(("attribute.modifier.take.0"), Component.literal("" + Math.abs(activeDietEffects.get(i).getRight())), Component.translatable(Registry.ATTRIBUTE.get(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        break;
                    case MULTIPLY_TOTAL:
                        if(activeDietEffects.get(i).getRight() >= 0)
                            tooltip.add(Component.translatable(("attribute.modifier.plus.1"), Component.literal("" + activeDietEffects.get(i).getRight() * 100.0), Component.translatable(Registry.ATTRIBUTE.get(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        else
                            tooltip.add(Component.translatable(("attribute.modifier.take.1"), Component.literal("" + Math.abs(activeDietEffects.get(i).getRight()) * 100.0), Component.translatable(Registry.ATTRIBUTE.get(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        break;
                    case MULTIPLY_BASE:
                        tooltip.add(Component.translatable(("attribute.modifier.equals.0"), Component.literal("" + ((1.0 + activeDietEffects.get(i).getRight())) * 100.0), Component.translatable(Registry.ATTRIBUTE.get(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        break;
                }
            }

            y += 34;
            if(y >= startY + totalHeight - 32) {
                y = startY;
                x += 34;
            }
        }
    }


    //this function draws the background. Not very interesting.
    @Override
    public void renderBackground(PoseStack pose) {
        super.renderBackground(pose);

        RenderSystem.setShaderTexture(0, BACKGROUND);
        //top line:
        GuiComponent.blit(pose, startX, startY, 4, 4, 0, 0, 4, 4, 256, 256);  //left corner
        GuiComponent.blit(pose, startX + 4, startY, totalWidth - 8, 4, 4, 0, 240, 4, 256, 256);    //main line
        GuiComponent.blit(pose, startX + totalWidth - 4, startY, 4, 4, 244, 0, 4, 4, 256, 256);  //right corner
        //left line:
        GuiComponent.blit(pose, startX, startY + 4, 4, totalHeight - 8, 0, 4, 4, 158, 256, 256);
        //right line:
        GuiComponent.blit(pose, startX + totalWidth - 4, startY + 4, 4, totalHeight - 8, 244, 4, 4, 158, 256, 256);
        //meat:
        GuiComponent.blit(pose, startX + 4, startY + 4, totalWidth - 8, totalHeight - 8, 4, 4, 240, 158, 256, 256);
        //bottom line:
        GuiComponent.blit(pose, startX, startY + totalHeight - 4, 4, 4, 0, 162, 4, 4, 256, 256);  //left corner
        GuiComponent.blit(pose, startX + 4, startY + totalHeight - 4, totalWidth - 8, 4, 4, 162, 240, 4, 256, 256);    //main line
        GuiComponent.blit(pose, startX + totalWidth -4, startY + totalHeight - 4, 4, 4, 244, 162, 4, 4, 256, 256);  //right corner

        //separator
        GuiComponent.fill(pose, startX + 6, startY + verticalSpacing[0] + mainSectionHeight + verticalSpacing[2], startX + totalWidth - 6,  startY + verticalSpacing[0] + mainSectionHeight + verticalSpacing[2] + 2, SEPARATOR_COLOR);

    }

    //calculates how much space each of the modules and sections need.
    private void calculateDimensions() {
        //each line of the left module consists of one 16x16 item sprite, some empty space and the display name of a food group
        leftModuleWidth = 16 + horizontalSpacing[1] + findLongestTextDisplayWidth();
        //the middle module contains the bars which are always 101 pixels wide
        middleModuleWidth = 101;
        //the right module contains the numbers and is always 34 pixels wide
        rightModuleWidth = 34;
        //the total width with the spacing between modules
        totalWidth = horizontalSpacing[0] + leftModuleWidth + horizontalSpacing[2] + middleModuleWidth + horizontalSpacing[3] + rightModuleWidth + horizontalSpacing[4];

        NutrientGroup[] Groups = ClientNutrientData.getFoodGroups();
        //the main section contains the food groups and their bars, its height depends on how many food groups there are.
        mainSectionHeight = Groups.length * (16 + verticalSpacing[1]) - verticalSpacing[1];
        //the bottom section contains the sum score of all food groups. It's always 16 pixels high
        bottomSectionHeight = 16;
        //total height including spacings
        totalHeight = verticalSpacing[0] + mainSectionHeight + verticalSpacing[2] + 2 + verticalSpacing[3] + bottomSectionHeight + verticalSpacing[4];

        //where on the screen the gui is supposed to be rendered
        startX = (this.width - totalWidth) / 2;
        startY = (this.height - totalHeight) / 2;
    }


    //to calculate how much space the left module (containing the text) uses up, one needs to know how long the pixel length of
    //the longest food group name is. This function returns exactly that.
    private int findLongestTextDisplayWidth() {
        int longest=0;
        for(int i=0; i< Groups.length; i++) {
            int textDisplayWidth = this.font.width(Groups[i].getDisplayname());
            if(textDisplayWidth > longest) {
                longest = textDisplayWidth;
            }
        }
        return longest;
    }

    private static ResourceLocation[] getGroupBarLocations() {
        if(GroupBarLocations == null) {
            GroupBarLocations = new ResourceLocation[Groups.length];
            for(int i = 0; i < Groups.length; i++) {
                GroupBarLocations[i] = createNutrientBar(Groups[i]);
            }
        }

        return GroupBarLocations;
    }

    private static ResourceLocation[] getBarArrowsLocation() {
        int[] hues = {
                0,          //red
                44,         //yellow
                114         //green
        };

        if(BarArrowsLocation == null) {
            BarArrowsLocation = new ResourceLocation[hues.length];
            for(int i = 0; i < hues.length; i++) {
                BarArrowsLocation[i] = createBarArrow(hues[i]);
            }
        }

        return BarArrowsLocation;
    }

    private static ResourceLocation getDietEffectBarLocation() {
        if(DietEffectBarLocation == null) {
            DietEffectBarLocation = createDietEffectsBar();
        }

        return DietEffectBarLocation;
    }

    private static ResourceLocation createDietEffectsBar() {
        int[] hues = {
                0,          //red
                44,         //yellow
                114         //green
        };

        int saturation = 87;

        String locationPath = "textures/gui/generated/diet_effects_bar";
        ResourceLocation out =  new ResourceLocation(LukasNutrients.MOD_ID, locationPath);

        NativeImage baseImage = getIconsBaseImage();

        //creates a new empty NativeImage with width = height = 256. I don't really know what that boolean does.
        NativeImage barImage = new NativeImage(256, 256, true);

        int[] pointRanges = DietEffects.getPointRanges();
        int base_point = DietEffects.getBasePoint();

        //draw left side
        for(int i = base_point; i > 0; i--) {
            int hue = hues[1];
            if(i <= pointRanges[0]) hue = hues[0];
            switch (i) {
                case 1:
                    copyRectangleOverAndShiftHue(baseImage, barImage, 16, 16, 10, 5, (i - 1) * 10, 5, hue, saturation);
                    copyRectangleOverAndShiftHue(baseImage, barImage, 16, 21, 10, 5, (i - 1) * 10, 0, hue, saturation);
                    break;
                default:
                    copyRectangleOverAndShiftHue(baseImage, barImage, 26, 16, 10, 5, (i - 1) * 10, 5, hue, saturation);
                    copyRectangleOverAndShiftHue(baseImage, barImage, 26, 21, 10, 5, (i - 1) * 10, 0, hue, saturation);
                    break;
            }
        }

        //draw right side
        for(int i = base_point; i < 10; i++) {
            int hue = hues[1];
            if(i >= pointRanges[1]) hue = hues[2];
            switch (i) {
                case 9:
                    copyRectangleOverAndShiftHue(baseImage, barImage, 37, 16, 10, 5, 1 + i * 10, 5, hue, saturation);
                    copyRectangleOverAndShiftHue(baseImage, barImage, 37, 21, 10, 5, 1 + i * 10, 0, hue, saturation);
                    break;
                default:
                    copyRectangleOverAndShiftHue(baseImage, barImage, 27, 16, 10, 5, 1 + i * 10, 5, hue, saturation);
                    copyRectangleOverAndShiftHue(baseImage, barImage, 27, 21, 10, 5, 1 + i * 10, 0, hue, saturation);
                    break;
            }
        }

        //draw middle
        copyRectangleOverAndShiftHue(baseImage, barImage, 26, 21, 1, 5, 50, 0, hues[1], saturation);

        //creates a new custom texture containing the new image
        AbstractTexture generatedTexture = new CustomTexture(barImage);
        //registers that Texture
        Minecraft.getInstance().getTextureManager().register(out, generatedTexture);
        //then closes the new image, since it's not used again. idk if this has to be done, but I'm guessing, they didn't create
        //a close()-method for no reason.
        barImage.close();

        //returns the ResourceLocation of the newly generated image
        return out;
    }

    private static ResourceLocation createNutrientBar(NutrientGroup group) {
        int[] hues = {
                0,          //red
                44,         //yellow
                114,         //green
                44,         //yellow
                0          //red
        };

        int saturation = 87;

        String locationPath = "textures/gui/generated/" + group.getID() + "_bar";
        ResourceLocation out =  new ResourceLocation(LukasNutrients.MOD_ID, locationPath);

        NativeImage baseImage = getIconsBaseImage();

        //creates a new empty NativeImage with width = height = 256. I don't really know what that boolean does.
        NativeImage barImage = new NativeImage(256, 256, true);
        //draw the bars
        int[] pointRanges = group.getPointRanges();
        int currentSegment = 0;
        for(int n = 0; n < pointRanges.length; n++) {
            for(currentSegment = currentSegment; currentSegment < pointRanges[n]/2; currentSegment++) {
                switch (currentSegment) {
                    case 0:
                        for(int i=0; i <= n; i++) {
                            copyRectangleOverAndShiftHue(baseImage, barImage, 16, 5, 9, 5, i, 5, hues[n], saturation);
                            copyRectangleOverAndShiftHue(baseImage, barImage, 16, 0, 9, 5, i, 0, hues[n], saturation);
                        }
                        break;
                    case 11:
                        for(int i=4; i >= n; i--) {
                            copyRectangleOverAndShiftHue(baseImage, barImage, 32, 5, 9, 5, currentSegment * 8 + i, 5, hues[n], saturation);
                            copyRectangleOverAndShiftHue(baseImage, barImage, 32, 0, 9, 5, currentSegment * 8 + i, 0, hues[n], saturation);
                        }
                        break;
                    default:
                        copyRectangleOverAndShiftHue(baseImage, barImage, 24, 5, 9, 5, currentSegment * 8 + n, 5, hues[n], saturation);
                        copyRectangleOverAndShiftHue(baseImage, barImage, 24, 0, 9, 5, currentSegment * 8 + n, 0, hues[n], saturation);
                        break;
                }
            }
        }
        for(currentSegment = currentSegment; currentSegment < 12; currentSegment ++) {
            int n = 4;
            switch (currentSegment) {
                case 0:
                    for(int i=0; i <= n; i++) {
                        copyRectangleOverAndShiftHue(baseImage, barImage, 16, 5, 9, 5, i, 5, hues[n], saturation);
                        copyRectangleOverAndShiftHue(baseImage, barImage, 16, 0, 9, 5, i, 0, hues[n], saturation);
                    }
                    break;
                case 11:
                    copyRectangleOverAndShiftHue(baseImage, barImage, 32, 5, 9, 5, currentSegment * 8 + n, 5, hues[n], saturation);
                    copyRectangleOverAndShiftHue(baseImage, barImage, 32, 0, 9, 5, currentSegment * 8 + n, 0, hues[n], saturation);
                    break;
                default:
                    copyRectangleOverAndShiftHue(baseImage, barImage, 24, 5, 9, 5, currentSegment * 8 + n, 5, hues[n], saturation);
                    copyRectangleOverAndShiftHue(baseImage, barImage, 24, 0, 9, 5, currentSegment * 8 + n, 0, hues[n], saturation);
                    break;
            }
        }

        //creates a new custom texture containing the new image
        AbstractTexture generatedTexture = new CustomTexture(barImage);
        //registers that Texture
        Minecraft.getInstance().getTextureManager().register(out, generatedTexture);
        //then closes the new image, since it's not used again. idk if this has to be done, but I'm guessing, they didn't create
        //a close()-method for no reason.
        barImage.close();

        //returns the ResourceLocation of the newly generated image
        return out;
    }

    private static ResourceLocation createBarArrow(int hue) {
        int saturation = 87;

        String locationPath = "textures/gui/generated/bar_arrow_" + hue;
        ResourceLocation out =  new ResourceLocation(LukasNutrients.MOD_ID, locationPath);

        NativeImage baseImage = getIconsBaseImage();

        //creates a new empty NativeImage with width = height = 256. I don't really know what that boolean does.
        NativeImage arrowImage = new NativeImage(256, 256, true);

        copyRectangleOverAndShiftHue(baseImage, arrowImage, 42, 0, 5, 5, 0, 0, hue, saturation);

        //creates a new custom texture containing the new image
        AbstractTexture generatedTexture = new CustomTexture(arrowImage);
        //registers that Texture
        Minecraft.getInstance().getTextureManager().register(out, generatedTexture);
        //then closes the new image, since it's not used again. idk if this has to be done, but I'm guessing, they didn't create
        //a close()-method for no reason.
        arrowImage.close();

        //returns the ResourceLocation of the newly generated image
        return out;
    }

    //this function draws a rectangle from the source image to the target image. It also sets the hue of all gray scale pixels to a specified value
    private static void copyRectangleOverAndShiftHue(NativeImage source, NativeImage target, int sourceStartX, int sourceStartY, int width, int height, int targetStartX, int targetStartY, int hue, int saturation) {
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int sourcePixelColor = source.getPixelRGBA(sourceStartX + x, sourceStartY + y);
                //gets the alpha
                int alpha = (sourcePixelColor & 0xff000000) >>> 24;
                //only non transparent pixels are drawn
                if(alpha == 255) {
                    target.setPixelRGBA(targetStartX + x, targetStartY + y, shiftHue(sourcePixelColor, hue, saturation));
                }
            }
        }
    }


    //this method sets the hue and saturation of a color to a specified value while leaving brightness intact. If the saturation of the
    //input pixel is not 0 it returns without doing anything. This is intended behaviour. I only want to color in gray pixels.
    private static int shiftHue(int sourcePixelColor, int targetHue, int targetSaturation) {
        //covert hue and saturation to floats
        float saturation = (float) targetSaturation / 100f;
        float hue = (float) targetHue / 360f;

        //gets the rgb values
        float red = (float) ((sourcePixelColor & 0xff0000) >> 16) / 255f;
        float green = (float) ((sourcePixelColor & 0xff00) >> 8) / 255f;
        float blue = (float) (sourcePixelColor & 0xff) / 255f;

        //checks if they are all the same (grey)
        if(red != green || red != blue) return sourcePixelColor;

        //for a color without saturation, the luminance is equal to the red, green and blue value (which are all the same)
        float luminance = red;

        //uses the Color class to convert HSB to RGB
        Color temp = Color.getHSBColor(hue, saturation, luminance);
        //for some godless reason I have to switch blue and red, otherwise it doesn't work... I don't get why, but it works this way.
        return new Color(temp.getBlue(), temp.getGreen(), temp.getRed(), 255).getRGB();
    }

    //when first called, loads a NativeImage of the .png containing the icons and bars
    //it then returns that NativeImage
    private static NativeImage getIconsBaseImage() {
        if(ICONS_BASE_IMAGE == null) {
            ICONS_BASE_IMAGE = NativeImageLoader.loadFromPath(ICONS);
        }

        return ICONS_BASE_IMAGE;
    }
}
