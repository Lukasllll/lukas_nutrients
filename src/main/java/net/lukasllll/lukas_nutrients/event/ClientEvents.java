package net.lukasllll.lukas_nutrients.event;

import com.mojang.datafixers.util.Either;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.KeyBinding;
import net.lukasllll.lukas_nutrients.client.graphics.gui.screens.NutrientScreen;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = LukasNutrients.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.OPEN_GUI_KEY);
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if(KeyBinding.OPEN_GUI_KEY.consumeClick() && mc.screen == null) {
                mc.setScreen(new NutrientScreen());
            }
        }

        @SubscribeEvent
        public static void onRenderTooltip(RenderTooltipEvent.GatherComponents event) {
            Item item = event.getItemStack().getItem();
            LukasNutrients.LOGGER.debug("Item: " + event.getItemStack().isEdible());
            //I only want the tooltip to be rendered on edible items that have nutrients assigned to them or items that can be placed to create edible blocks like cake
            if(item == null || !((INutrientPropertiesHaver) item).hasFoodNutrientProperties() ||
                    ( !item.isEdible() && !((INutrientPropertiesHaver) item).getFoodNutrientProperties().getPlaceableEdible()) ) {
                return;
            }
            LukasNutrients.LOGGER.debug("1. check");
            double[] nutrientAmounts = ((INutrientPropertiesHaver) item).getFoodNutrientProperties().getNutrientAmounts();
            int servings = ((INutrientPropertiesHaver) item).getFoodNutrientProperties().getServings();
            NutrientGroup[] groups = NutrientGroup.getNutrientGroups();
            //The Either.class is weird and I don't like it
            List<Either<FormattedText, TooltipComponent>> toolTipElements = event.getTooltipElements();     //tooltips added to this list will be rendered

            //I first loop through all nutrient groups and check whether any nutrients of that type are present. If not, nothing is added
            //to the nutrientTooltipElements list. If the list turns out empty at the end, nothing is rendered at all.
            List<Either<FormattedText, TooltipComponent>> nutrientTooltipElements = new ArrayList<>();
            for(int i=0; i< groups.length; i++) {
                LukasNutrients.LOGGER.debug("2. check");
                if(nutrientAmounts[i] == 0) continue;
                String text = "+" + round(nutrientAmounts[i] / servings) + " " + groups[i].getDisplayname();
                nutrientTooltipElements.add(Either.left(Component.literal(text).withStyle(ChatFormatting.GOLD)));
            }
            if(nutrientTooltipElements.isEmpty()) {
                return;
            }
            LukasNutrients.LOGGER.debug("3. check");
            //If it's not empty, for edible items first add the "when eaten" tooltip
            if(item.isEdible()) {
                toolTipElements.add(Either.left(Component.literal("When eaten:").withStyle(ChatFormatting.GRAY)));

            }
            //if it's placeable like cake add the "when placed" tooltip
            if(((INutrientPropertiesHaver) item).getFoodNutrientProperties().getPlaceableEdible()) {
                toolTipElements.add(Either.left(Component.literal("When placed:").withStyle(ChatFormatting.GRAY)));
            }
            //add servings (only if there are more than one)
            if(servings > 1) {
                toolTipElements.add(Either.left(Component.literal(servings + " servings of").withStyle(ChatFormatting.GRAY)));
            }
            //add the nutrients
            toolTipElements.addAll(nutrientTooltipElements);
            LukasNutrients.LOGGER.debug("4. check");
        }

        private static double round(double in) {
            return (double) Math.round(in * 100) / 100.0;
        }
    }
}
