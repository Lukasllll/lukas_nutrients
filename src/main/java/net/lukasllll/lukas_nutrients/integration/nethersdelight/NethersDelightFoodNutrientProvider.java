package net.lukasllll.lukas_nutrients.integration.nethersdelight;

import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import umpaz.nethersdelight.common.registry.NDItems;

import java.util.HashMap;

public class NethersDelightFoodNutrientProvider {

    public static void assignUniqueItems() {

        Item stuffedHoglin = NDItems.STUFFED_HOGLIN.get();

        FoodNutrientProvider.assignNutrientsThroughRecipe(stuffedHoglin);

        HashMap<Item, Integer> stuffedHoglinBlockServings = new HashMap<>();
        stuffedHoglinBlockServings.put(NDItems.PLATE_OF_STUFFED_HOGLIN_SNOUT.get(), 1);
        stuffedHoglinBlockServings.put(NDItems.PLATE_OF_STUFFED_HOGLIN_HAM.get(), 4);
        stuffedHoglinBlockServings.put(NDItems.PLATE_OF_STUFFED_HOGLIN_ROAST.get(), 4);

        double totalFoodValue = 0;
        for(Item item : stuffedHoglinBlockServings.keySet()) {
            FoodProperties currentItemFoodProperties = item.getFoodProperties();
            totalFoodValue += currentItemFoodProperties.getNutrition() * (1.0 + currentItemFoodProperties.getSaturationModifier()) * stuffedHoglinBlockServings.get(item);
        }

        double[] stuffedHoglinTotalNutrients = ((INutrientPropertiesHaver) stuffedHoglin).getFoodNutrientProperties().getNutrientAmounts();

        for(Item item : stuffedHoglinBlockServings.keySet()) {
            FoodProperties currentItemFoodProperties = item.getFoodProperties();
            double currentFoodValue = currentItemFoodProperties.getNutrition() * (1.0 + currentItemFoodProperties.getSaturationModifier());

            double[] servingNutrients = stuffedHoglinTotalNutrients.clone();
            FoodNutrientProvider.scaleArray(servingNutrients, currentFoodValue / totalFoodValue);

            ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(servingNutrients));
        }

    }
}
