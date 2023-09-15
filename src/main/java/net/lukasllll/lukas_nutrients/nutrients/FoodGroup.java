package net.lukasllll.lukas_nutrients.nutrients;

public class FoodGroup {
    public static final int MAX_NUTRIENT_AMOUNT=12;
    public static final int MIN_NUTRIENT_AMOUNT=0;

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
}
