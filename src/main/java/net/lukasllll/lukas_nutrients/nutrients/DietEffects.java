package net.lukasllll.lukas_nutrients.nutrients;

public class DietEffects {
    private static final int[] POINT_RANGES = {
            4,
            6
    };

    public static final int BASE_POINT = 5;

    public static int[] getPointRanges() {
        return POINT_RANGES;
    }

    public static int getBasePoint() {
        return BASE_POINT;
    }
}
