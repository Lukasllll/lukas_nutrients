package net.lukasllll.lukas_nutrients.integration.farmersdelight;

import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.world.item.Item;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.HashSet;

public class FarmersDelightFoodNutrientProvider {

    public static void assignUniqueItems() {
        HashSet<FeastBlock> feasts = new HashSet<>();
        feasts.add((FeastBlock) ModBlocks.ROAST_CHICKEN_BLOCK.get());
        feasts.add((FeastBlock) ModBlocks.STUFFED_PUMPKIN_BLOCK.get());
        feasts.add((FeastBlock) ModBlocks.HONEY_GLAZED_HAM_BLOCK.get());
        feasts.add((FeastBlock) ModBlocks.SHEPHERDS_PIE_BLOCK.get());
        feasts.add((FeastBlock) ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get());

        for(FeastBlock feast : feasts) {
            Item feastItem = feast.asItem();
            FoodNutrientProvider.assignItemNutrientsThroughRecipes(feastItem);

            int numberOfServings = feast.getMaxServings();
            Item servingItem = feast.getServingItem(feast.defaultBlockState()).getItem();

            if(((INutrientPropertiesHaver) servingItem).hasFoodNutrientProperties()) continue;

            NutrientProperties feastNutrients = ((INutrientPropertiesHaver) feastItem).getFoodNutrientProperties();
            double[] feastNutrientAmounts = feastNutrients.getNutrientAmounts();
            double[] servingItemNutrientAmounts = feastNutrientAmounts.clone();

            FoodNutrientProvider.scaleArray(servingItemNutrientAmounts, 1.0/numberOfServings);

            ((INutrientPropertiesHaver) servingItem).setFoodNutrientProperties(new NutrientProperties(servingItemNutrientAmounts));
        }
    }
}
