package net.lukasllll.lukas_nutrients.nutrients.operators;

import com.google.common.util.concurrent.AtomicDouble;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Max extends Operator implements ICalcElement, IDisplayElement {
    public Max(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] summandIDs) {
        super(id, displayname, pointRanges, basePoint, score, summandIDs);
    }

    public Max(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public double getCurrentAmount(Iterator<Double> inputValues) {
        AtomicDouble out = new AtomicDouble(inputValues.next());
        inputValues.forEachRemaining((inputAmount) -> {
            out.set(Math.max(inputAmount, out.get()));
        });
        return out.get();
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
            maxAmount = Math.max(maxAmount, inputMaxValue);
        }
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.NUTRIENT;
    }

    @Override
    public int getTextureStartX() {
        return 48;
    }
}
