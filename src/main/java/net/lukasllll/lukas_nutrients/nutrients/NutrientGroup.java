package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.config.NutrientGroupsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NutrientGroup {

    public static NutrientGroup[] nutrientGroups = null;

    private final String id;
    private String displayname;
    private int[] pointRanges;
    private ItemStack DisplayItemStack;
    private double defaultAmount;

    public NutrientGroup(String id, String displayname, String item, int[] ranges, double defaultAmount) {
        this.id=id;
        this.displayname=displayname;
        this.pointRanges=ranges;
        this.defaultAmount = defaultAmount;

        RegistryObject<Item> DisplayItemRegistry = RegistryObject.create(new ResourceLocation(item), ForgeRegistries.ITEMS);

        DisplayItemStack = new ItemStack(DisplayItemRegistry.get());
    }

    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public ItemStack getDisplayItemStack() {return this.DisplayItemStack;}
    public int[] getPointRanges() {return this.pointRanges;}
    public double getDefaultAmount() {return this.defaultAmount;}

    public static NutrientGroup[] getNutrientGroups() {
        if(nutrientGroups !=null) return nutrientGroups;

        getFromConfig();
        return nutrientGroups;
    }

    public static void getFromConfig() {
        nutrientGroups = NutrientGroupsConfig.getNutrientGroups();
    }

    //gets the array index from a nutrientID. Returns -1 if no such ID exists.
    public static int getArrayIndex(String nutrientID) {
        for(int i = 0; i< nutrientGroups.length; i++) {
            if(nutrientGroups[i].getID().equals(nutrientID)) {
                return i;
            }
        }
        return -1;
    }
}
