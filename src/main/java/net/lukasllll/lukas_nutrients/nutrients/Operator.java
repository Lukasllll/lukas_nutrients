package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Operator implements ICalcElement, IDisplayElement {
    protected final String id;
    protected String displayname;
    protected int[] pointRanges;
    protected int basePoint;
    private boolean score;
    protected int maxAmount;

    protected String[] inputIds;
    protected ICalcElement[] inputs;

    public Operator(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] inputIDs) {
        this.id = id;
        this.displayname = displayname;
        this.pointRanges = pointRanges;
        this.basePoint = basePoint;
        this.score = score;
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
        this.score = buf.readBoolean();
        this.maxAmount = buf.readInt();
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
        buf.writeBoolean(score);
        buf.writeInt(maxAmount);
        buf.writeInt(inputIds.length);
        for(int i = 0; i< inputIds.length; i++) {
            buf.writeInt(inputIds[i].length());
            buf.writeCharSequence(inputIds[i], Charset.defaultCharset());
        }
    }
    public void fetchInputs(boolean onClient) {
        ArrayList<ICalcElement> tempInputs = new ArrayList<>();
        if(onClient) {
            //TODO ???
        } else {
            for(String id : inputIds) {
                Nutrient nutrient = NutrientManager.getNutrientFromID(id);
                Operator operator = NutrientManager.getOperatorFromID(id);
                if(nutrient != null) {
                    tempInputs.add(nutrient);
                } else if(operator != null) {
                    tempInputs.add(operator);
                }
            }
        }
        inputs = tempInputs.toArray(new ICalcElement[0]);
    }

    public ICalcElement[] getInputs() { return inputs; }

    public abstract int getCurrentAmount(Iterator<Integer> inputAmounts, Iterator<Integer> inputScores);
    public int getCurrentScore(int amount) {
        if(!score) return amount;
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

    public String[] getInputIds() { return inputIds; }
    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public int[] getPointRanges() {return this.pointRanges;}
    public int getBasePoint() {return this.basePoint; }

    public int getMaxScore() {
        if(!score) return maxAmount;
        return 2;
    }
    public int getMaxAmount() {
        return maxAmount;
    }

    public abstract DisplayBarStyle getDisplayBarStyle();

    public enum DisplayBarStyle {
        NUTRIENT,
        SUM
    }

    public static int getOperatorType(Operator operator) {
        if(operator instanceof Sum) return 0;
        if(operator instanceof Min) return 1;
        if(operator instanceof Max) return 2;
        return -1;
    }

    public static Operator createOperator(int operatorType, String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] inputIDs) {
        switch(operatorType) {
            case 0 -> { return new Sum(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 1 -> { return new Min(id, displayname, pointRanges, basePoint, score,inputIDs); }
            case 2 -> { return new Max(id, displayname, pointRanges, basePoint, score,inputIDs); }
            default -> { return null; }
        }
    }

    public static Operator createOperator(FriendlyByteBuf buf) {
        int operatorType = buf.readInt();
        switch(operatorType) {
            case 0 -> { return new Sum(buf); }
            case 1 -> { return new Min(buf); }
            case 2 -> { return new Max(buf); }
            default -> { return null; }
        }
    }

    public int getTextureStartY() {return 48;}
    public abstract int getTextureStartX();
}
