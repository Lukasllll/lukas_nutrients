package net.lukasllll.lukas_nutrients.client.graphics.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.client.KeyBinding;
import net.lukasllll.lukas_nutrients.client.graphics.CustomTexture;
import net.lukasllll.lukas_nutrients.client.graphics.NativeImageLoader;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.lukasllll.lukas_nutrients.config.EffectIconsConfig;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.Operator;
import net.lukasllll.lukas_nutrients.nutrients.Sum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Triple;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
            10, 4, 3, 5, 6
    };

    private static final int TEXT_COLOR = new Color(63, 63, 63).getRGB();
    private static final int SEPARATOR_COLOR = new Color(139, 139, 139).getRGB();

    private final Nutrient[] nutrients;
    private final Operator[] operators;
    private final String[] displayOrder;
    private final HashMap<String, String> shortenedNameById;

    private NativeImage iconsBaseImage = null;
    private ResourceLocation[] nutrientBarLocations;
    private ResourceLocation[] operatorBarLocations;
    private ResourceLocation[] barArrowsLocation;


    //the middle module contains the bars which are always 101 pixels wide
    private final int middleModuleWidth = 101;
    //the right module contains the numbers and is always 34 pixels wide
    private final int rightModuleWidth = 34;
    private int leftModuleWidth;
    private int totalWidth;
    private int totalHeight;

    private int startX;
    private int startY;

    private List<Component> tooltip;

    public NutrientScreen() {
        super(TITLE);
        nutrients = ClientNutrientData.getNutrients();
        operators = ClientNutrientData.getOperators();
        displayOrder = ClientNutrientData.getDisplayOrder();
        shortenedNameById = new HashMap<>();
    }

    @Override
    protected void init() {
        super.init();

        int maxDisplayNameWidth = super.width - (horizontalSpacing[0] + 16 + horizontalSpacing[1] + horizontalSpacing[2] + middleModuleWidth + horizontalSpacing[3] + rightModuleWidth + horizontalSpacing[4]);
        for(Nutrient nutrient : nutrients) {
            shortenedNameById.put(nutrient.getID(), shortenStringIfNecessary(nutrient.getDisplayname(), maxDisplayNameWidth));
        }
        for(Operator operator : operators) {
            shortenedNameById.put(operator.getID(), shortenStringIfNecessary(operator.getDisplayname(), maxDisplayNameWidth));
        }
    }
    /*
    Method is needed to close the screen on pressing 'n' again.
    I don't know what all these parameters do, so I didn't change their names.
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tooltip = null;
        if(startX == 0) calculateDimensions();
        renderBackground(graphics);
        renderDividers(graphics);
        renderDietEffects(graphics, mouseX, mouseY);
        renderLeftModule(graphics);
        renderMiddleModule(graphics);
        renderRightModule(graphics);
        if(tooltip != null)
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public void renderDividers(GuiGraphics graphics) {
        int currentY = startY + verticalSpacing[0];

        for(int i=0; i<displayOrder.length; i++) {
            IDisplayElement element = getDisplayElementFromID(displayOrder[i]);
            if (element == null) continue;
            if (Objects.equals(element.getID(), NutrientManager.DIVIDER_ID)) {
                currentY += verticalSpacing[2];
                graphics.fill(startX + 6, currentY, startX + totalWidth - 6,  currentY + 2, SEPARATOR_COLOR);
                currentY += 2 + verticalSpacing[3];
            } else {
                currentY += 16 + verticalSpacing[1];
            }
        }
    }

    public void renderLeftModule(GuiGraphics graphics) {
        int currentY = startY + verticalSpacing[0];

        for(int i=0; i<displayOrder.length; i++) {
            IDisplayElement element = getDisplayElementFromID(displayOrder[i]);
            if (element == null) continue;
            if (Objects.equals(element.getID(), NutrientManager.DIVIDER_ID)) {
                currentY += verticalSpacing[2] + 2 + verticalSpacing[3]; //divider
            } else if (element instanceof Nutrient) {
                //render display item
                graphics.renderItem(((Nutrient)element).getDisplayItemStack(), startX + horizontalSpacing[0], currentY);
                //render Name
                graphics.drawString(this.font, shortenedNameById.get(element.getID()), startX + horizontalSpacing[0] + 16 + horizontalSpacing[1], currentY + 5, TEXT_COLOR, false);
                currentY += 16 + verticalSpacing[1];
            } else if(element instanceof Operator) {
                //render symbol
                graphics.blit(ICONS, startX + horizontalSpacing[0], currentY, 16, 16, ((Operator) element).getTextureStartX(), ((Operator) element).getTextureStartY(), 16, 16, 256, 256);
                //render Name
                graphics.drawString(this.font, shortenedNameById.get(element.getID()), startX + horizontalSpacing[0] + 16 + horizontalSpacing[1], currentY + 5, TEXT_COLOR, false);
                currentY += 16 + verticalSpacing[1];
            }
        }
    }

    public void renderMiddleModule(GuiGraphics graphics) {
        ResourceLocation[] nutrientBars = getNutrientBarLocations();
        ResourceLocation[] sumBars = getOperatorBarLocations();
        ResourceLocation[] arrows = getBarArrowsLocation();

        int currentY = startY + verticalSpacing[0];
        int currentX =  startX + horizontalSpacing[0] + leftModuleWidth + horizontalSpacing[2];

        for(int i=0; i<displayOrder.length; i++) {
            IDisplayElement element = getDisplayElementFromID(displayOrder[i]);
            if(element == null) continue;
            if(Objects.equals(element.getID(), NutrientManager.DIVIDER_ID)) {
                currentY += verticalSpacing[2] + 2 + verticalSpacing[3]; //divider
            } else if(element instanceof Nutrient) {
                //render empty bar
                graphics.blit(nutrientBars[ClientNutrientData.getNutrientArrayIndex(element.getID())], currentX, currentY + 7, 101, 5, 0, 5, 101, 5, 256, 256);

                int amount = (int) ClientNutrientData.getNutrientAmount(element.getID());

                int barLength = 1 + ClientNutrientData.getNutrientRange(element.getID()) + amount * 4;
                //render full bar
                graphics.blit(nutrientBars[ClientNutrientData.getNutrientArrayIndex(element.getID())], currentX, currentY + 7, barLength, 5, 0, 0, barLength, 5, 256, 256);
                //render arrow
                if(barLength == 1) {
                    barLength += 1;
                } else if(barLength == 101) {
                    barLength -= 1;
                }
                graphics.blit(arrows[ClientNutrientData.getNutrientScore(element.getID())], currentX + barLength - 3, currentY + 1, 5, 5, 0, 0, 5, 5, 256, 256);
                //render exhaustion bar
                double exhaustionLevel = ClientNutrientData.getPlayerExhaustionLevels()[ClientNutrientData.getNutrientArrayIndex(element.getID())];
                barLength = (int) ((4.0 - exhaustionLevel)/4.0 * 98.0);
                graphics.blit(ICONS, currentX, currentY + 12, barLength, 2, 16, 10, barLength, 2, 256, 256);

                currentY += 16 + verticalSpacing[1];
            } else if(element instanceof Operator) {
                //draw diet effects bar
                graphics.blit(sumBars[ClientNutrientData.getOperatorArrayIndex(element.getID())], currentX, currentY + 7, 101, 5, 0, 5, 101, 5, 256, 256);

                int operatorAmount = ClientNutrientData.getOperatorAmount(element.getID());

                int effectsBarStartX, effectsBarEndX;
                boolean toTheRight = false;
                int basePoint = ((Operator) element).getBasePoint();
                int divisions = ((Operator) element).getMaxAmount();
                if(operatorAmount > basePoint) {
                    effectsBarStartX = getPieceStartX(basePoint, divisions);
                    effectsBarEndX = getPieceStartX(operatorAmount, divisions) + 1;
                    toTheRight = true;
                } else if(operatorAmount < basePoint) {
                    effectsBarStartX = getPieceStartX(operatorAmount, divisions);
                    effectsBarEndX = getPieceStartX(basePoint, divisions) + 1;
                } else {
                    effectsBarStartX = getPieceStartX(basePoint, divisions);
                    effectsBarEndX = effectsBarStartX + 1;
                }

                //LukasNutrients.LOGGER.debug("start = " + effectsBarStartX + ", end = " + effectsBarEndX + ", totalScore = " + totalScore + ", basePoint = " + basePoint +", divisons = " + divisions + ", toTheRight = " + toTheRight);

                graphics.blit(sumBars[ClientNutrientData.getOperatorArrayIndex(element.getID())], currentX + effectsBarStartX, currentY + 7, effectsBarEndX - effectsBarStartX, 5, effectsBarStartX, 0, effectsBarEndX - effectsBarStartX, 5, 256, 256);

                //draw diet effects bar arrow;
                int arrowArrayIndex = 1;
                if(operatorAmount < ((Operator) element).getPointRanges()[0]) {
                    arrowArrayIndex = 0;
                } else if(operatorAmount > ((Operator) element).getPointRanges()[1]) {
                    arrowArrayIndex = 2;
                }

                int arrowX = (toTheRight ? effectsBarEndX - 3 : effectsBarStartX - 2);
                if(operatorAmount == 0) arrowX += 1;
                else if(operatorAmount == divisions) arrowX -= 2;

                graphics.blit(arrows[arrowArrayIndex], currentX + arrowX, currentY + 1, 5, 5, 0, 0, 5, 5, 256, 256);

                currentY += 16 + verticalSpacing[1];
            }
        }
    }

    public void renderRightModule(GuiGraphics graphics) {

        int currentY = startY + verticalSpacing[0];
        int currentX = startX + horizontalSpacing[0] + leftModuleWidth + horizontalSpacing[2] + middleModuleWidth + horizontalSpacing[3];

        for(int i=0; i<displayOrder.length; i++) {
            IDisplayElement element = getDisplayElementFromID(displayOrder[i]);
            if(element == null) continue;
            if(Objects.equals(element.getID(), NutrientManager.DIVIDER_ID)) {
                currentY += verticalSpacing[2] + 2 + verticalSpacing[3]; //divider
            } else if(element instanceof Nutrient) {
                //render box
                graphics.blit(ICONS, currentX + 6, currentY + 2, 9, 11, 0, 16, 9, 11, 256, 256);
                //render number
                graphics.drawString(this.font, ""+ClientNutrientData.getNutrientScore(element.getID()), currentX + 8, currentY + 4, TEXT_COLOR, false);
                graphics.drawString(this.font, "/" + ClientNutrientData.getNutrient(element.getID()).getMaxScore(), currentX + 17, currentY + 4, TEXT_COLOR, false);
                currentY += 16 + verticalSpacing[1];
            } else if(element instanceof Operator) {
                //render box
                graphics.blit(ICONS, currentX, currentY + 2, 15, 11, 0, 27, 15, 11, 256, 256);
                //render number
                int operatorScore = ClientNutrientData.getOperatorScore(element.getID());
                if(operatorScore < 10) {
                    graphics.drawString(this.font, "" + operatorScore, currentX + 5, currentY + 4, TEXT_COLOR, false);
                } else {
                    graphics.drawString(this.font, "" + operatorScore, currentX + 2, currentY + 4, TEXT_COLOR, false);
                }
                graphics.drawString(this.font, "/" + ((Operator) element).getMaxScore(), currentX + 17, currentY + 4, TEXT_COLOR, false);
                currentY += 16 + verticalSpacing[1];
            }
        }
    }

    /*
    Renders the icons for currently active diet effect and sets the appropriate tooltip, if moused-over
     */
    public void renderDietEffects(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Triple<String, AttributeModifier.Operation, Double>> activeDietEffects = ClientNutrientData.getActiveDietEffects();

        int x = startX + totalWidth + 2;
        int y = startY;

        int numberOfEffectsRendered = Math.min(activeDietEffects.size(), 2 * totalHeight/32);

        for(int i = 0; i < numberOfEffectsRendered; i++) {
            //background
            graphics.blit(INVENTORY_LOCATION, x, y, 0, 198, 32, 32);

            //effect image
            ResourceLocation effectLocation = new ResourceLocation(
                    "minecraft","textures/mob_effect/" +
                    EffectIconsConfig.getEffectIcon(activeDietEffects.get(i).getLeft(), activeDietEffects.get(i).getRight()) + ".png");

            graphics.blit(effectLocation, x + 7, y + 7, 0, (float) 0.0, (float) 0.0, 18, 18, 18, 18);

            /*
            The tooltip is just created here. It is rendered, once renderComponentTooltip is called
             */
            if(mouseX >= x && mouseX < x+32 && mouseY >= y && mouseY < y+32) {
                tooltip = new ArrayList<>();
                switch (activeDietEffects.get(i).getMiddle()) {
                    case ADDITION:
                        if(activeDietEffects.get(i).getRight() >= 0)
                            tooltip.add(Component.translatable(("attribute.modifier.plus.0"), Component.literal("" + activeDietEffects.get(i).getRight()), Component.translatable(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        else
                            tooltip.add(Component.translatable(("attribute.modifier.take.0"), Component.literal("" + Math.abs(activeDietEffects.get(i).getRight())), Component.translatable(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        break;
                    case MULTIPLY_TOTAL:
                        if(activeDietEffects.get(i).getRight() >= 0)
                            tooltip.add(Component.translatable(("attribute.modifier.plus.1"), Component.literal("" + activeDietEffects.get(i).getRight() * 100.0), Component.translatable(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        else
                            tooltip.add(Component.translatable(("attribute.modifier.take.1"), Component.literal("" + Math.abs(activeDietEffects.get(i).getRight()) * 100.0), Component.translatable(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
                        break;
                    case MULTIPLY_BASE:
                        tooltip.add(Component.translatable(("attribute.modifier.equals.0"), Component.literal("" + ((1.0 + activeDietEffects.get(i).getRight())) * 100.0), Component.translatable(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(activeDietEffects.get(i).getLeft())).getDescriptionId())));
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
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);

        //top line:
        graphics.blit(BACKGROUND, startX, startY, 4, 4, 0, 0, 4, 4, 256, 256);  //left corner
        graphics.blit(BACKGROUND, startX + 4, startY, totalWidth - 8, 4, 4, 0, 240, 4, 256, 256);    //main line
        graphics.blit(BACKGROUND, startX + totalWidth - 4, startY, 4, 4, 244, 0, 4, 4, 256, 256);  //right corner
        //left line:
        graphics.blit(BACKGROUND, startX, startY + 4, 4, totalHeight - 8, 0, 4, 4, 158, 256, 256);
        //right line:
        graphics.blit(BACKGROUND, startX + totalWidth - 4, startY + 4, 4, totalHeight - 8, 244, 4, 4, 158, 256, 256);
        //meat:
        graphics.blit(BACKGROUND, startX + 4, startY + 4, totalWidth - 8, totalHeight - 8, 4, 4, 240, 158, 256, 256);
        //bottom line:
        graphics.blit(BACKGROUND, startX, startY + totalHeight - 4, 4, 4, 0, 162, 4, 4, 256, 256);  //left corner
        graphics.blit(BACKGROUND, startX + 4, startY + totalHeight - 4, totalWidth - 8, 4, 4, 162, 240, 4, 256, 256);    //main line
        graphics.blit(BACKGROUND, startX + totalWidth -4, startY + totalHeight - 4, 4, 4, 244, 162, 4, 4, 256, 256);  //right corner
    }

    //calculates how much space each of the modules and sections need.
    private void calculateDimensions() {
        //each line of the left module consists of one 16x16 item sprite, some empty space and the display name of a food group
        leftModuleWidth = 16 + horizontalSpacing[1] + findLongestTextDisplayWidth();
        //the total width with the spacing between modules
        totalWidth = horizontalSpacing[0] + leftModuleWidth + horizontalSpacing[2] + middleModuleWidth + horizontalSpacing[3] + rightModuleWidth + horizontalSpacing[4];

        totalHeight = verticalSpacing[0];
        //add the height of all display elements (nutrients, sums, dividers)
        for(int i=0; i<displayOrder.length; i++) {
            IDisplayElement element = getDisplayElementFromID(displayOrder[i]);
            if(element == null) continue;
            if(Objects.equals(element.getID(), NutrientManager.DIVIDER_ID)) {
                totalHeight += verticalSpacing[2] + 2 + verticalSpacing[3]; //divider
            } else {
                totalHeight += 16 + verticalSpacing[1];                     //nutrient or sum
            }
        }
        totalHeight += verticalSpacing[4];

        //where on the screen the gui is supposed to be rendered
        startX = (this.width - totalWidth) / 2;
        startY = (this.height - totalHeight) / 2;
    }


    //to calculate how much space the left module (containing the text) uses up, one needs to know how long the pixel length of
    //the longest food group name is. This function returns exactly that.
    private int findLongestTextDisplayWidth() {
        int longest=0;
        for(int i = 0; i< displayOrder.length; i++) {
            int textDisplayWidth = getTextDisplayWidthFromID(displayOrder[i]);
            if(textDisplayWidth > longest) {
                longest = textDisplayWidth;
            }
        }
        return longest;
    }

    private int getTextDisplayWidthFromID(String id) {
        String shortenedName = shortenedNameById.getOrDefault(id, "");
        return this.font.width(shortenedName);
    }

    private IDisplayElement getDisplayElementFromID(String id) {
        if(id.equals(NutrientManager.DIVIDER_ID)) return () -> {return id; };
        for(Operator operator : operators) {
            if(operator.getID().equals(id)) return operator;
        }
        for(Nutrient nutrient : nutrients) {
            if(nutrient.getID().equals(id)) return nutrient;
        }
        return null;
    }

    private String shortenStringIfNecessary(String s, int maxWidth) {
        int stringWidth = this.font.width(s);
        if(stringWidth <= maxWidth) return s;
        int dotdotdotWidth = font.width("...");
        maxWidth -= dotdotdotWidth;
        return font.plainSubstrByWidth(s, maxWidth) + "...";
    }

    private ResourceLocation[] getOperatorBarLocations() {
        if(operatorBarLocations == null) {
            operatorBarLocations = new ResourceLocation[operators.length];
            for(int i = 0; i < operators.length; i++) {
                switch (operators[i].getDisplayBarStyle()) {
                    case NUTRIENT -> operatorBarLocations[i] = createSumBar(operators[i]); //TODO !!!
                    case SUM -> operatorBarLocations[i] = createSumBar(operators[i]);
                }
            }
        }

        return operatorBarLocations;
    }

    private ResourceLocation[] getNutrientBarLocations() {
        if(nutrientBarLocations == null) {
            nutrientBarLocations = new ResourceLocation[nutrients.length];
            for(int i = 0; i < nutrients.length; i++) {
                nutrientBarLocations[i] = createNutrientBar(nutrients[i]);
            }
        }

        return nutrientBarLocations;
    }

    private ResourceLocation[] getBarArrowsLocation() {
        int[] hues = {
                0,          //red
                44,         //yellow
                114         //green
        };

        if(barArrowsLocation == null) {
            barArrowsLocation = new ResourceLocation[hues.length];
            for(int i = 0; i < hues.length; i++) {
                barArrowsLocation[i] = createBarArrow(hues[i]);
            }
        }

        return barArrowsLocation;
    }

    private ResourceLocation createSumBar(Operator operator) {
        int[] hues = {
                0,          //red
                44,         //yellow
                114         //green
        };

        int saturation = 87;

        String locationPath = "textures/gui/generated/" + operator.getID() + "_bar";
        ResourceLocation out =  new ResourceLocation(LukasNutrients.MOD_ID, locationPath);

        NativeImage baseImage = getIconsBaseImage();

        //creates a new empty NativeImage with width = height = 256. I don't really know what that boolean does.
        NativeImage barImage = new NativeImage(256, 256, true);

        int divisions = operator.getMaxAmount();
        int[] pointRanges = operator.getPointRanges();
        int basePoint = operator.getBasePoint();

        //save the pointRange for each specific piece of the bar, so that the hue can be assigned more easily later
        int[] pieceHue = new int[divisions];
        int range = 0;
        for(int i=0; i<divisions; i++) {
            while(range < pointRanges.length && i >= pointRanges[range]) range++;
            int hueIndex;
            switch(range) {
                case 0,4 -> hueIndex = 0;
                case 1,3 -> hueIndex = 1;
                default -> hueIndex = 2;
            }
            pieceHue[i] = hues[hueIndex];
        }

        //draw bar
        for(int i = 0; i < divisions; i++) {
            int hue = pieceHue[i];

            //where the piece of the bar should be drawn. pieceStartX gets the "+ 1" to account for the "middle bit" being drawn separately further down
            //this also results in pieceLength being shorter by 1 than it otherwise would be, again accounting for the "middle bit" being drawn separately
            int pieceStartX = getPieceStartX(i, divisions) + 1;
            int pieceNextX = getPieceStartX(i + 1, divisions);
            int pieceLength = pieceNextX - pieceStartX;

            //the first and last piece are the end caps and have their own draw methods
            if(i == 0) {
                drawSumBarLeftPiece(baseImage, barImage, pieceStartX - 1, pieceLength + 1, hue, saturation);             //left end cap
            } else if( i == divisions-1) {
                drawSumBarRightPiece(baseImage, barImage, pieceStartX, pieceLength, hue, saturation);                   //right end cap
            } else {
                drawSumBarMiddlePiece(baseImage, barImage, pieceStartX, pieceLength, hue, saturation);                  //middle pieces
            }
            //drawing the "middle bit"
            if(i != divisions-1) {                                                                                      //"middle bit" doesn't need to be drawn for the last iteration
                if(i < basePoint) hue = pieceHue[i+1];
                drawSumBarMiddleBit(baseImage, barImage, pieceNextX, hue, saturation);
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

    private int getPieceStartX(int i, int divisions) {
        return 101/divisions * i + Math.min(i, 101%divisions) - ((i == 0 || i == divisions) ? 0 : 1);
    }

    //You know... the middle bit... idk what else to call it :/
    private void drawSumBarMiddleBit(NativeImage baseImage, NativeImage barImage, int x, int hue, int saturation) {
        copyRectangleOverAndShiftHue(baseImage, barImage, 26, 16, 1, 5, x, 5, hue, saturation);
        copyRectangleOverAndShiftHue(baseImage, barImage, 26, 21, 1, 5, x, 0, hue, saturation);
    }

    private void drawSumBarLeftPiece(NativeImage baseImage, NativeImage barImage, int x, int length, int hue, int saturation) {
        int rx=0;
        //draw the left tip
        copyRectangleOverAndShiftHue(baseImage, barImage, 16, 16, 3, 5, x + rx, 5, hue, saturation);
        copyRectangleOverAndShiftHue(baseImage, barImage, 16, 21, 3, 5, x + rx, 0, hue, saturation);
        rx += 3;
        //draw the body
        for(; rx <= length - 7; rx += 5) {
            copyRectangleOverAndShiftHue(baseImage, barImage, 19, 16, 5, 5, x + rx, 5, hue, saturation);
            copyRectangleOverAndShiftHue(baseImage, barImage, 19, 21, 5, 5, x + rx, 0, hue, saturation);
        }
        //draw the remainder
        copyRectangleOverAndShiftHue(baseImage, barImage, 26 - length + rx, 16, length - rx, 5, x + rx, 5, hue, saturation);
        copyRectangleOverAndShiftHue(baseImage, barImage, 26 - length + rx, 21, length - rx, 5, x + rx, 0, hue, saturation);
    }

    private void drawSumBarMiddlePiece(NativeImage baseImage, NativeImage barImage, int x, int length, int hue, int saturation) {
        int rx = 0;
        //draw the left tip
        copyRectangleOverAndShiftHue(baseImage, barImage, 27, 16, 3, 5, x + rx, 5, hue, saturation);
        copyRectangleOverAndShiftHue(baseImage, barImage, 27, 21, 3, 5, x + rx, 0, hue, saturation);
        rx += 2;
        //draw the body
        for(; rx <= length - 6; rx += 5) {
            copyRectangleOverAndShiftHue(baseImage, barImage, 29, 16, 5, 5, x + rx, 5, hue, saturation);
            copyRectangleOverAndShiftHue(baseImage, barImage, 29, 21, 5, 5, x + rx, 0, hue, saturation);
        }
        //draw the remainder
        copyRectangleOverAndShiftHue(baseImage, barImage, 36 - length + rx, 16, length - rx, 5, x + rx, 5, hue, saturation);
        copyRectangleOverAndShiftHue(baseImage, barImage, 36 - length + rx, 21, length - rx, 5, x + rx, 0, hue, saturation);
    }

    private void drawSumBarRightPiece(NativeImage baseImage, NativeImage barImage, int x, int length, int hue, int saturation) {
        int rx = 0;
        //draw the left tip
        copyRectangleOverAndShiftHue(baseImage, barImage, 37, 16, 3, 5, x + rx, 5, hue, saturation);
        copyRectangleOverAndShiftHue(baseImage, barImage, 37, 21, 3, 5, x + rx, 0, hue, saturation);
        rx += 2;
        //draw the body
        for(; rx <= length - 6; rx += 5) {
            copyRectangleOverAndShiftHue(baseImage, barImage, 39, 16, 5, 5, x + rx, 5, hue, saturation);
            copyRectangleOverAndShiftHue(baseImage, barImage, 39, 21, 5, 5, x + rx, 0, hue, saturation);
        }
        //draw the remainder
        copyRectangleOverAndShiftHue(baseImage, barImage, 47 - length + rx, 16, length - rx, 5, x + rx, 5, hue, saturation);
        copyRectangleOverAndShiftHue(baseImage, barImage, 47 - length + rx, 21, length - rx, 5, x + rx, 0, hue, saturation);
    }

    private ResourceLocation createNutrientBar(Nutrient nutrient) {
        int[] hues = {
                0,          //red
                44,         //yellow
                114,         //green
                44,         //yellow
                0          //red
        };

        int saturation = 87;

        String locationPath = "textures/gui/generated/" + nutrient.getID() + "_bar";
        ResourceLocation out =  new ResourceLocation(LukasNutrients.MOD_ID, locationPath);

        NativeImage baseImage = getIconsBaseImage();

        //creates a new empty NativeImage with width = height = 256. I don't really know what that boolean does.
        NativeImage barImage = new NativeImage(256, 256, true);
        //draw the bars
        int[] pointRanges = nutrient.getPointRanges();
        int currentSegment = 0;
        for(int n = 0; n < pointRanges.length; n++) {
            for(; currentSegment < pointRanges[n]/2; currentSegment++) {
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
        for(; currentSegment < 12; currentSegment ++) {
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

    private ResourceLocation createBarArrow(int hue) {
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
    private NativeImage getIconsBaseImage() {
        if(iconsBaseImage == null) {
            iconsBaseImage = NativeImageLoader.loadFromPath(ICONS);
        }

        return iconsBaseImage;
    }
}
