package net.lukasllll.lukas_nutrients.nutrients.operators;

import com.google.common.primitives.Booleans;
import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Operators implement different calculations, taking Nutrient objects or other Operator objects as inputs and
 * calculating a value (amount) and a score (score). Operators can also be rendered on the NutrientScreen and can
 * be the basis for calculating NutrientEffects.
 */
public abstract class Operator implements ICalcElement, IDisplayElement {

    /**
     * The id is a String used by various classes (e.g. NutrientManager, PlayerNutrients, ClientNutrientData, NutrientScreen)
     * to uniquely identify an Operator object among various different ICalcElement objects.
     * The id should ideally (but doesn't have to) only consist of lowercase letters and underscores. Including most
     * other characters should not break anything, but full stops ('.') and equal signs ('=') might cause issues.
     */
    protected final String id;

    /**
     * The displayname can be any String. There are no special restrictions on length or allowed characters.
     * The displayname is purely cosmetic and will sometimes be displayed instead of the id, e.g. in some command
     * outputs, though its primary purpose is to be rendered on the NutrientScreen. Note that depending on screen and
     * gui scale, very long displaynames might be shortened when rendered; In that case "..." will be added to the
     * shortened String.
     */
    protected String displayname;

    /**
     * pointRanges contain up to four integers, that mark the beginnings of scoring zones. (see Operator::getCurrentScore)
     */
    protected int[] pointRanges;

    /**
     * The basePoint is mostly cosmetic (though it minimally effects scoring (see Operator::getCurrentScore)).
     * When rendering in NutrientScreen, the basePoint determines where the bar originates.
     */
    protected int basePoint;

    /**
     * calculateScore determines whether how the output of Operator::getCurrentScore is calculated.
     * If false, it will just output the respective amount unchanged. If true, it will score the amount based on the
     * pointRanges.
     */
    private boolean calculateScore;

    /**
     * maxAmount is calculated in Operator::calcMaxAmount. It is the maximum value amount can reach given the inputs.
     */
    protected double maxAmount;

    protected String[] inputIds;
    protected ICalcElement[] inputs;
    protected boolean[] takeInputScore;

    public Operator(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] inputIDs) {
        this.id = id;
        this.displayname = displayname;
        this.pointRanges = pointRanges;
        this.basePoint = basePoint;
        this.calculateScore = score;
        this.inputIds = inputIDs;
        this.maxAmount = -1;
    }

    /*
    read relevant data from a ByteBuffer. This constructor is called when reading a packet send from the server to the client
    containing the servers Nutrients and Sums for rendering
     */
    public Operator(FriendlyByteBuf buf) {
        int stringLength = buf.readInt();
        this.id = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        stringLength = buf.readInt();
        this.displayname = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        this.pointRanges = buf.readVarIntArray();
        this.basePoint = buf.readInt();
        this.calculateScore = buf.readBoolean();
        this.maxAmount = buf.readDouble();
        int tempArrayLength = buf.readInt();
        inputIds = new String[tempArrayLength];
        for(int i=0; i<tempArrayLength; i++) {
            stringLength = buf.readInt();
            inputIds[i] = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        }
    }

    /*
    Write the objects relevant data to a ByteBuffer to be sent from the server to the client.
     */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(Operator.getOperatorType(this));

        buf.writeInt(id.length());
        buf.writeCharSequence(id, Charset.defaultCharset());
        buf.writeInt(displayname.length());
        buf.writeCharSequence(displayname, Charset.defaultCharset());
        buf.writeVarIntArray(pointRanges);
        buf.writeInt(basePoint);
        buf.writeBoolean(calculateScore);
        buf.writeDouble(maxAmount);
        buf.writeInt(inputIds.length);
        for(int i = 0; i< inputIds.length; i++) {
            buf.writeInt(inputIds[i].length());
            buf.writeCharSequence(inputIds[i], Charset.defaultCharset());
        }
    }
    public void fetchInputs(boolean onClient) {
        ArrayList<ICalcElement> tempInputs = new ArrayList<>();
        ArrayList<Boolean> tempTakeInputScore = new ArrayList<>();
        for(String inputID : inputIds) {
            if(inputID.contains("=")) {
                tempInputs.add(new Constant(inputID));
                tempTakeInputScore.add(false);
            } else {
                String[] splitID = inputID.split("\\.");
                if (splitID.length > 2) continue;
                Nutrient nutrient;
                Operator operator;
                if(onClient) {
                    nutrient = ClientNutrientData.getNutrient(splitID[0]);
                    operator = ClientNutrientData.getOperator(splitID[0]);
                } else {
                    nutrient = NutrientManager.getNutrientFromID(splitID[0]);
                    operator = NutrientManager.getOperatorFromID(splitID[0]);
                }
                if (nutrient != null) {
                    tempInputs.add(nutrient);
                } else if (operator != null) {
                    tempInputs.add(operator);
                }
                String takeInputScoreSuffix = (splitID.length == 2) ? splitID[1] : "empty";
                tempTakeInputScore.add(!takeInputScoreSuffix.equals("amount"));
            }
        }

        inputs = tempInputs.toArray(new ICalcElement[0]);
        takeInputScore = Booleans.toArray(tempTakeInputScore);
    }

    public ICalcElement[] getInputs() { return inputs; }
    public boolean[] getTakeInputScore() { return takeInputScore; }


    public abstract double getCurrentAmount(Iterator<Double> inputValues);

    //gets either the score or just returns the amount, depending, on whether calculateScore is true
    public int getCurrentScore(double amount) {
        if(!calculateScore) return (int) amount;
        return getCurrentForcedScore(amount);
    }

    //just calculates and gets the current score regardless of whether calculateScore is true or false
    public int getCurrentForcedScore(double amount) {
        if(amount >= basePoint) amount--;
        int range = 0;
        while(range < pointRanges.length && amount >= pointRanges[range]) range++;
        int score;
        switch(range) {
            case 0,4 -> score = 0;
            case 1,3 -> score = 1;
            default -> score = 2;
        }
        return score;
    }

    public abstract void calcMaxAmount();

    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public abstract String getOperatorName();
    public int[] getPointRanges() {return this.pointRanges;}
    public int getBasePoint() {return this.basePoint; }

    public int getMaxScore() {
        if(!calculateScore) return (int) maxAmount;
        return 2;
    }
    public double getMaxAmount() {
        return maxAmount;
    }

    public abstract DisplayBarStyle getDisplayBarStyle();

    public List<Component> getTooltip(boolean moreInfo) {
        LinkedList<Component> out = new LinkedList<>();
        out.add(Component.literal(getDisplayname()).append(Component.literal(" (" + getOperatorName() + ")").withStyle(ChatFormatting.GRAY)));
        return out;
    }

    public enum DisplayBarStyle {
        NUTRIENT,
        SUM
    }

    public static int getOperatorType(Operator operator) {
        if(operator instanceof Sum) return 0;
        if(operator instanceof Product) return 1;
        if(operator instanceof Min) return 2;
        if(operator instanceof Max) return 3;
        if(operator instanceof Invert) return 4;
        if(operator instanceof Exp) return 5;
        if(operator instanceof Log) return 6;
        return -1;
    }

    public static Operator createOperator(int operatorType, String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] inputIDs) {
        switch(operatorType) {
            case 0 -> { return new Sum(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 1 -> { return new Product(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 2 -> { return new Min(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 3 -> { return new Max(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 4 -> { return new Invert(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 5 -> { return new Exp(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 6 -> { return new Log(id, displayname, pointRanges, basePoint, score,inputIDs); }
            default -> { return null; }
        }
    }

    public static Operator createOperator(FriendlyByteBuf buf) {
        int operatorType = buf.readInt();
        switch(operatorType) {
            case 0 -> { return new Sum(buf); }
            case 1 -> { return new Product(buf); }
            case 2 -> { return new Min(buf); }
            case 3 -> { return new Max(buf); }
            case 4 -> { return new Invert(buf); }
            case 5 -> { return new Exp(buf); }
            case 6 -> { return new Log(buf); }
            default -> { return null; }
        }
    }

    public int getTextureStartY() {return 48;}
    public abstract int getTextureStartX();
}
