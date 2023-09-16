package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.nutrients.FoodGroup;

public class ClientNutrientData {
    private static FoodGroup[] Groups=FoodGroup.getFoodGroups();
    private static double[] amounts;

    public static void set(double[] pamounts) {
        ClientNutrientData.amounts=pamounts;
    }

    public static double[] getPlayerNutrients() {
        return ClientNutrientData.amounts;
    }

    private int getArrayIndex(String nutrientID) {
        for(int i=0; i< Groups.length; i++) {
            if(Groups[i].getID().equals(nutrientID)) {
                return i;
            }
        }

        return -1;
    }
}
