package net.lukasllll.lukas_nutrients.mixin;

import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Fluid.class)
public abstract class FluidMixin implements INutrientPropertiesHaver {
    private NutrientProperties foodNutrientProperties;

    public NutrientProperties getFoodNutrientProperties() {
        return foodNutrientProperties;
    }

    public void setFoodNutrientProperties(NutrientProperties foodNutrientProperties) {
        this.foodNutrientProperties = foodNutrientProperties;
    }

    @Override
    public boolean hasFoodNutrientProperties() {
        return foodNutrientProperties != null;
    }

}
