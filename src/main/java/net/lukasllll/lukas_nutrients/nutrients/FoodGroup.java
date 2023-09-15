package net.lukasllll.lukas_nutrients.nutrients;

public class FoodGroup {
    public static final int MAX_NUTRIENT_AMOUNT=12;
    public static final int MIN_NUTRIENT_AMOUNT=0;

    public static FoodGroup[] FoodGroups=null;

    private final String id;
    private String displayname;
    private int[] pointRanges;
    //TODO display Item
    //TODO decay

    public FoodGroup(String id, String displayname, int r1, int r2, int r3, int r4) {
        this.id=id;
        this.displayname="displayname";
        int[] tPointRanges={r1,r2,r3,r4};
        this.pointRanges=tPointRanges;
    }

    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public int[] getPointRanges() {return this.pointRanges;}

    public static  FoodGroup[] getFoodGroups() {
        if(FoodGroups!=null) return FoodGroups;
        FoodGroups= new FoodGroup[5];
        FoodGroups[0] = new FoodGroup("fruits", "Fruits", 4, 6, 9, 11);
        FoodGroups[0] = new FoodGroup("grains", "Grains", 4, 5, 9, 10);
        FoodGroups[0] = new FoodGroup("proteins", "Proteins", 3, 5, 9, 11);
        FoodGroups[0] = new FoodGroup("vegetables", "Vegetables", 4, 8, 11, 12);
        FoodGroups[0] = new FoodGroup("sugars", "Sugars", 0, 1, 3, 7);

        return FoodGroups;
    }
}
