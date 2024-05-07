package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.config.NutrientsConfig;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsGlobalDataSyncS2CPacket;
import net.lukasllll.lukas_nutrients.nutrients.operators.Operator;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;

public class NutrientManager {

    public static final String DIVIDER_ID = "divider";

    private static Nutrient[] nutrients;
    private static Operator[] operators;
    private static HashMap<String, Integer> operatorArrayIndexMap;

    private static String[] displayOrder;

    public static Nutrient[] getNutrients() {
        if(nutrients == null) getFromConfig();
        return nutrients;
    }

    public static Operator[] getOperators() {
        if(operators == null) getFromConfig();  //TODO!!!
        return operators;
    }

    public static void getFromConfig() {
        nutrients = NutrientsConfig.getNutrients();
        operators = NutrientsConfig.getOperators();
        displayOrder = NutrientsConfig.getDisplayOrder();

        operatorArrayIndexMap = new HashMap<>();


        for(int i=0; i<operators.length; i++) {
            operatorArrayIndexMap.put(operators[i].getID(), i);
            operators[i].fetchInputs(false);
            operators[i].calcMaxAmount();
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

    public static int getOperatorArrayIndex(String operatorID) {
        return operatorArrayIndexMap.getOrDefault(operatorID, -1);
    }

    public static Nutrient getNutrientFromID(String nutrientID) {
        for(Nutrient nutrient : nutrients) {
            if(nutrient.getID().equals(nutrientID)) {
                return  nutrient;
            }
        }
        return null;
    }

    public static Operator getOperatorFromID(String operatorID) {
        int arrayIndex = operatorArrayIndexMap.getOrDefault(operatorID, -1);
        if(arrayIndex == -1) return null;
        return operators[arrayIndex];
    }


    public static void updateClient(ServerPlayer player) {
        ModMessages.sendToPlayer(new NutrientsGlobalDataSyncS2CPacket(nutrients, operators, displayOrder), player);
    }
}
