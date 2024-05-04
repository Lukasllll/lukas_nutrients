package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.config.NutrientsConfig;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsGlobalDataSyncS2CPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NutrientManager {

    public static final String DIVIDER_ID = "divider";

    private static Nutrient[] nutrients;
    private static Sum[] sums;
    private static HashMap<String, List<Integer>> nutrientSumHashMap;

    private static String[] displayOrder;

    public static Nutrient[] getNutrients() {
        if(nutrients == null) getFromConfig();
        return nutrients;
    }

    public static Sum[] getSums() {
        if(sums == null) getFromConfig();
        return sums;
    }

    public static HashMap<String, List<Integer>> getNutrientSumHashMap() {
        if(nutrientSumHashMap == null) getFromConfig();
        return nutrientSumHashMap;
    }

    public static void getFromConfig() {
        nutrients = NutrientsConfig.getNutrients();
        sums = NutrientsConfig.getSums();
        displayOrder = NutrientsConfig.getDisplayOrder();

        nutrientSumHashMap = new HashMap<>();
        for(Nutrient nutrient : nutrients) {
            nutrientSumHashMap.put(nutrient.getID(), new ArrayList<>());
        }
        for(Sum sum : sums) {
            for(String summandID : sum.getSummandIDs()) {
                Nutrient summand = getFromID(summandID);
                if(summand != null) {
                    nutrientSumHashMap.get(summandID).add(getSumArrayIndex(sum.getID()));
                }
            }
        }
    }

    //gets the array index from a nutrientID. Returns -1 if no such ID exists.
    public static int getNutrientArrayIndex(String nutrientID) {
        for(int i = 0; i< nutrients.length; i++) {
            if(nutrients[i].getID().equals(nutrientID)) {
                return i;
            }
        }
        return -1;
    }

    public static int getSumArrayIndex(String nutrientID) {
        for(int i = 0; i< sums.length; i++) {
            if(sums[i].getID().equals(nutrientID)) {
                return i;
            }
        }
        return -1;
    }

    public static Nutrient getFromID(String nutrientID) {
        for(Nutrient nutrient : nutrients) {
            if(nutrient.getID().equals(nutrientID)) {
                return  nutrient;
            }
        }
        return null;
    }

    public static void updateClient(ServerPlayer player) {
        ModMessages.sendToPlayer(new NutrientsGlobalDataSyncS2CPacket(nutrients, sums, displayOrder), player);
    }
}
