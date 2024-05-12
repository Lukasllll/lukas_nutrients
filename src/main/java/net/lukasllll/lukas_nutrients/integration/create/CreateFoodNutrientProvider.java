package net.lukasllll.lukas_nutrients.integration.create;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.integration.IntegrationHelper;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class CreateFoodNutrientProvider {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static Map<Fluid, List<ProcessingRecipe<?>>> processingRecipesByFluidResult;

    private static final Set<Fluid> fluidsCurrentlyWorkingOn = new HashSet<>();

    public static void init() {
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.CONVERSION.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.CRUSHING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.CUTTING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.MILLING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.BASIN.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.MIXING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.COMPACTING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.PRESSING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.SANDPAPER_POLISHING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.SPLASHING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.HAUNTING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.DEPLOYING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.FILLING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.EMPTYING.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
        IntegrationHelper.addOutsourcedRecipeNutrientProvider(AllRecipeTypes.ITEM_APPLICATION.getType(), CreateFoodNutrientProvider::getProcessingRecipeNutrients);
    }

    /**
     * Implementation of IRecipeNutrientProvider for the create mod's ProcessingRecipes.
     * @param recipe a create mod ProcessingRecipe (will throw Exception if recipe is not an instance of ProcessingRecipe)
     * @return Returns a double array of length equal to the amount of different nutrients.
     *   The contents of the returned array represent the amounts of the different nutrients, that the ingredients
     *   of the given recipe provide.
     *   Returned nutrients are normalized to the count of the resulting item stack. Might be awkward for recipes, where
     *   the resulting item stack's count is not relevant, but resulting fluid amount is.
     */
    public static double[] getProcessingRecipeNutrients(Recipe<?> recipe) {

        ProcessingRecipe<?> processingRecipe = (ProcessingRecipe<?>) recipe;

        boolean isHeated = processingRecipe.getRequiredHeat() == HeatCondition.HEATED || processingRecipe.getRequiredHeat() == HeatCondition.SUPERHEATED;

        double[] recipeNutrientAmounts = FoodNutrientProvider.getRecipeNutrients(processingRecipe, isHeated);

        double[] recipeFluidNutrientAmounts = new double[NutrientManager.getNutrients().length];
        //for each fluid ingredient
        for(FluidIngredient fluidIngredient : processingRecipe.getFluidIngredients()) {
            //find the largest amount of nutrients of that ingredient
            double[] largestFluidNutrientAmounts = new double[NutrientManager.getNutrients().length];
            double largestTotalFluidNutrientAmount = 0;
            List<FluidStack> ingredientFluidStacks= fluidIngredient.getMatchingFluidStacks();
            //loop through all fluids that can be used for this ingredient to find the one, that provides the most nutrients
            for(FluidStack fluidStack : ingredientFluidStacks) {
                Fluid currentFluid = fluidStack.getFluid();
                int currentFluidAmount = fluidStack.getAmount();
                //if the ingredient fluid doesn't have nutrient properties assign them new properties through their crafting
                //recipes, but skip fluids that are currently being assigned recipes to avoid infinite loops.
                if(!((INutrientPropertiesHaver) currentFluid).hasFoodNutrientProperties()) {
                    if(isFluidCurrentlyBeingWorkedOn(currentFluid)) {
                        continue;
                    }
                    assignFluidNutrientsThroughRecipes(currentFluid);
                }
                //clone the nutrient amount array, so that it can be modified, without modifying the original
                double[] currentFluidNutrientAmounts = ((INutrientPropertiesHaver) currentFluid).getFoodNutrientProperties().getNutrientAmounts().clone();
                double currentFluidTotalNutrientAmount = 0;

                //calculate how many nutrients this item would contribute to the new item
                FoodNutrientProvider.scaleArray(currentFluidNutrientAmounts, currentFluidAmount);

                for(double currentFluidNutrientAmount : currentFluidNutrientAmounts) {
                    currentFluidTotalNutrientAmount += currentFluidNutrientAmount;
                }

                //save these nutrients if they are larger than any previous nutrients
                if(currentFluidTotalNutrientAmount > largestTotalFluidNutrientAmount) {
                    largestFluidNutrientAmounts = currentFluidNutrientAmounts;
                    largestTotalFluidNutrientAmount = currentFluidTotalNutrientAmount;
                }
            }
            //the largest amount of nutrients for that ingredient has been determined and is now added to the nutrient
            //array for that recipe
            FoodNutrientProvider.addArrays(recipeFluidNutrientAmounts, largestFluidNutrientAmounts);
        }
        //after the largest nutrient amounts from all ingredients have been added together, the nutrient amount is
        //divided by the resultStackCount
        int resultStackCount = recipe.getResultItem(Minecraft.getInstance().level.registryAccess()).getCount();
        if(resultStackCount != 0) {
            for (int j = 0; j < recipeFluidNutrientAmounts.length; j++) {
                recipeFluidNutrientAmounts[j] /= resultStackCount;
            }
        }

        FoodNutrientProvider.addArrays(recipeNutrientAmounts, recipeFluidNutrientAmounts);

        return recipeNutrientAmounts;
    }

    /**
     * Assigns NutrientProperties to a given item based on any registered or automatically generated ProcessingRecipes,
     * that have the given item as a result. If there is no such crafting recipe, an empty nutrient array will be
     * assigned instead.
     * Note that this will overwrite any nutrients the item might currently have.
     * @param fluid the fluid to be assigned nutrients.
     */
    private static void assignFluidNutrientsThroughRecipes(Fluid fluid) {
        addFluidToCurrentlyWorkingOn(fluid);

        List<ProcessingRecipe<?>> processingRecipes = getProcessingRecipesByFluidResult().get(fluid);
        if(processingRecipes == null) {
            FoodNutrientProvider.assignNoNutrients((INutrientPropertiesHaver) fluid);
            return;
        }
        double[] largestNutrientAmounts = null; //this saves the largest amount of nutrients provided by any recipe. If there are no recipes this stays null
        double largestTotalNutrientAmounts = 0;
        //we now loop through each recipe to find the largest amount of nutrients they can provide
        for(ProcessingRecipe<?> currentProcessingRecipe: processingRecipes) {

            double[] currentRecipeNutrientAmounts = getProcessingRecipeNutrients(currentProcessingRecipe);

            int resultFluidAmount = currentProcessingRecipe.getFluidResults().get(0).getAmount();

            FoodNutrientProvider.scaleArray(currentRecipeNutrientAmounts, 1.0/((double) resultFluidAmount));

            double currentRecipeTotalNutrientAmounts = 0;
            for(double currentRecipeNutrientAmount : currentRecipeNutrientAmounts) {
                currentRecipeTotalNutrientAmounts += currentRecipeNutrientAmount;
            }

            //then it is determined, if the nutrientAmounts of this recipe is larger than the nutrientAmounts of any previous
            //recipe and are saved if that's the case.
            if(currentRecipeTotalNutrientAmounts > largestTotalNutrientAmounts) {
                largestNutrientAmounts = currentRecipeNutrientAmounts;
                largestTotalNutrientAmounts = currentRecipeTotalNutrientAmounts;
            }
        }
        //if no recipe could produce any nutrientAmounts, no nutrients are assigned to this fluid
        if(largestNutrientAmounts == null) {
            FoodNutrientProvider.assignNoNutrients((INutrientPropertiesHaver) fluid);
            CreateFoodNutrientProvider.LOGGER.info("Considered, but assigned no nutrients to fluid: " + ForgeRegistries.FLUIDS.getKey(fluid));
        } else {
            //otherwise the largest amount of nutrients provided by any recipe is added to the fluid
            ((INutrientPropertiesHaver) fluid).setFoodNutrientProperties(new NutrientProperties(largestNutrientAmounts));
            CreateFoodNutrientProvider.LOGGER.info("Added nutrient values to fluid: " + ForgeRegistries.FLUIDS.getKey(fluid));
        }

        //the fluid is now no longer being worked on. It now definitely has NutrientProperties.
        removeFluidFromCurrentlyWorkingOn(fluid);
    }

    public static Map<Fluid, List<ProcessingRecipe<?>>> getProcessingRecipesByFluidResult() {
        if(processingRecipesByFluidResult == null) processingRecipesByFluidResult = findAllProcessingRecipesByResult();
        return processingRecipesByFluidResult;
    }

    private static Map<Fluid, List<ProcessingRecipe<?>>> findAllProcessingRecipesByResult() {

        Map<Fluid, List<ProcessingRecipe<?>>> out = new HashMap<>();

        RecipeManager recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();

        for(Recipe<?> currentRecipe: recipeManager.getRecipes()) {
            if(!(currentRecipe instanceof ProcessingRecipe<?> currentProcessingRecipe)) continue;

            List<FluidStack> resultFluidsStacks = currentProcessingRecipe.getFluidResults();
            Fluid resultFluid;
            if(resultFluidsStacks.size() == 1 && (resultFluid = resultFluidsStacks.get(0).getFluid()) != Fluids.EMPTY) {
                out.computeIfAbsent(resultFluid, k -> new ArrayList<>());
                out.get(resultFluid).add(currentProcessingRecipe);
            }
        }

        Collection<Item> items = ForgeRegistries.ITEMS.getValues();
        for(Item item : items) {
            ItemStack stack = new ItemStack(item);
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluidHandlerItem -> {
                if(fluidHandlerItem.getTanks() != 1) return;
                Fluid fluid = fluidHandlerItem.getFluidInTank(0).getFluid();
                ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(item);
                ResourceLocation helperRecipeLocation = new ResourceLocation(LukasNutrients.MOD_ID, "generated/emptying_" + itemLocation.getNamespace() + "_" + itemLocation.getPath() + "_helper");
                ProcessingRecipe<?> helperEmptyingRecipe = new ProcessingRecipeBuilder<>(EmptyingRecipe::new, helperRecipeLocation)
                        .require(item).output(fluid, fluidHandlerItem.getFluidInTank(0).getAmount()).build();
                out.computeIfAbsent(fluid, k -> new ArrayList<>());
                out.get(fluid).add(helperEmptyingRecipe);

                CreateFoodNutrientProvider.LOGGER.debug("Generated helper recipe: " + helperRecipeLocation);
            });
        }

        return out;
    }

    public static void addFluidToCurrentlyWorkingOn(Fluid fluid) {
        fluidsCurrentlyWorkingOn.add(fluid);
    }

    public static boolean isFluidCurrentlyBeingWorkedOn(Fluid fluid) {
        return fluidsCurrentlyWorkingOn.contains(fluid);
    }

    public static void removeFluidFromCurrentlyWorkingOn(Fluid fluid) {
        fluidsCurrentlyWorkingOn.remove(fluid);
    }
}
