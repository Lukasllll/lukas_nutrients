package net.lukasllll.lukas_nutrients.nutrients;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class NutrientGroup {
    public static final int MAX_NUTRIENT_AMOUNT=12;
    public static final int MIN_NUTRIENT_AMOUNT=0;

    public static NutrientGroup[] nutrientGroups = null;

    private final String id;
    private String displayname;
    private int[] pointRanges;
    private ItemStack DisplayItemStack;
    private double defaultAmount;
    //TODO decay

    public NutrientGroup(String id, String displayname, String item, int r1, int r2, int r3, int r4, double defaultAmount) {
        this.id=id;
        this.displayname=displayname;
        int[] tPointRanges={r1,r2,r3,r4};
        this.pointRanges=tPointRanges;
        this.defaultAmount = defaultAmount;

        RegistryObject<Item> DisplayItemRegistry = RegistryObject.create(new ResourceLocation(item), ForgeRegistries.ITEMS);

        DisplayItemStack = new ItemStack(DisplayItemRegistry.get());
    }

    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public ItemStack getDisplayItemStack() {return this.DisplayItemStack;}
    public int[] getPointRanges() {return this.pointRanges;}
    public double getDefaultAmount() {return this.defaultAmount;}

    public static  NutrientGroup[] getNutrientGroups() {
        if(nutrientGroups !=null) return nutrientGroups;
        nutrientGroups = new NutrientGroup[5];
        nutrientGroups[0] = new NutrientGroup("fruits", "Fruits", "minecraft:apple", 8, 12, 18, 22, 12);
        nutrientGroups[1] = new NutrientGroup("grains", "Grains", "minecraft:bread", 8, 10, 18, 20, 10);
        nutrientGroups[2] = new NutrientGroup("proteins", "Proteins", "minecraft:cooked_beef", 6, 10, 18, 22, 10);
        nutrientGroups[3] = new NutrientGroup("vegetables", "Vegetables", "minecraft:carrot", 8, 16, 22, 24, 16);
        nutrientGroups[4] = new NutrientGroup("sugars", "Sugars", "minecraft:honey_bottle", 0, 2, 6, 14, 2);

        return nutrientGroups;
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
