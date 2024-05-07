package net.lukasllll.lukas_nutrients.nutrients.operators;

import com.google.common.util.concurrent.AtomicDouble;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Iterator;

public class Invert extends Operator implements IDisplayElement, ICalcElement {

    public Invert(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] summandIDs) {
        super(id, displayname, pointRanges, basePoint, score, summandIDs);
    }

    public Invert(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public double getCurrentAmount(Iterator<Double> inputValues) {
        AtomicDouble out = new AtomicDouble();
        inputValues.forEachRemaining(out::addAndGet);
        return maxAmount - out.get();
    }

    @Override
    public void calcMaxAmount() {
        maxAmount = 0;
        for(int i=0; i< inputs.length; i++) {
            double inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            if(inputMaxValue == -1 && inputs[i] instanceof Operator) {
                ((Operator) inputs[i]).calcMaxAmount();
                inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            }
            maxAmount += inputMaxValue;
        }
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.SUM;
    }

    @Override
    public int getTextureStartX() {
        return 64;
    }
}
