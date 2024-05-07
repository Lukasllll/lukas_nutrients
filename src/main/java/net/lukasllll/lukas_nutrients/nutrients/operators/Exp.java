package net.lukasllll.lukas_nutrients.nutrients.operators;

import com.google.common.util.concurrent.AtomicDouble;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Iterator;

public class Exp extends Operator implements IDisplayElement, ICalcElement {

    public Exp(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] summandIDs) {
        super(id, displayname, pointRanges, basePoint, score, summandIDs);
    }

    public Exp(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public double getCurrentAmount(Iterator<Double> inputValues) {
        double base = inputValues.next();
        AtomicDouble exponent = new AtomicDouble();
        inputValues.forEachRemaining(exponent::addAndGet);
        return Math.pow(base, exponent.get());
    }

    @Override
    public void calcMaxAmount() {
        double base = 1;
        double exponent = 0;
        for(int i=0; i< inputs.length; i++) {
            double inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            if(inputMaxValue == -1 && inputs[i] instanceof Operator) {
                ((Operator) inputs[i]).calcMaxAmount();
                inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            }
            if(i == 0) base = inputMaxValue;
            else exponent += inputMaxValue;
        }
        maxAmount = Math.pow(base, exponent);
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.SUM;
    }

    @Override
    public int getTextureStartX() {
        return 80;
    }
}
