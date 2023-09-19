package net.lukasllll.lukas_nutrients.nutrients;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FoodGroup {
    public static final int MAX_NUTRIENT_AMOUNT=12;
    public static final int MIN_NUTRIENT_AMOUNT=0;

    public static FoodGroup[] FoodGroups=null;

    private final String id;
    private String displayname;
    private int[] pointRanges;
    private ItemStack DisplayItemStack;
    private double defaultAmount;
    //TODO decay

    public FoodGroup(String id, String displayname, String item, int r1, int r2, int r3, int r4, double defaultAmount) {
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

    public static  FoodGroup[] getFoodGroups() {
        if(FoodGroups!=null) return FoodGroups;
        FoodGroups= new FoodGroup[5];
        FoodGroups[0] = new FoodGroup("fruits", "Fruits", "minecraft:apple", 8, 12, 18, 22, 16);
        FoodGroups[1] = new FoodGroup("grains", "Grains", "minecraft:bread", 8, 10, 18, 20, 16);
        FoodGroups[2] = new FoodGroup("proteins", "Proteins", "minecraft:cooked_beef", 6, 10, 18, 22, 16);
        FoodGroups[3] = new FoodGroup("vegetables", "Vegetables", "minecraft:carrot", 8, 16, 22, 24, 20);
        FoodGroups[4] = new FoodGroup("sugars", "Sugars", "minecraft:honey_bottle", 0, 2, 6, 14, 0);

        return FoodGroups;
    }
}
