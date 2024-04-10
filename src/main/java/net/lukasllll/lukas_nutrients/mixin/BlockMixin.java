package net.lukasllll.lukas_nutrients.mixin;

import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockMixin implements INutrientPropertiesHaver {
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
