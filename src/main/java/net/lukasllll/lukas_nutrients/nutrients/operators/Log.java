package net.lukasllll.lukas_nutrients.nutrients.operators;

import com.google.common.util.concurrent.AtomicDouble;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Iterator;

public class Log extends Operator implements IDisplayElement, ICalcElement {

    public Log(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] summandIDs) {
        super(id, displayname, pointRanges, basePoint, score, summandIDs);
    }

    public Log(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public double getCurrentAmount(Iterator<Double> inputValues) {
        double base = inputValues.next();
        AtomicDouble antiLog = new AtomicDouble();
        inputValues.forEachRemaining(antiLog::addAndGet);
        return Math.log(antiLog.get() + 1.0) / Math.log(base);
    }

    @Override
    public void calcMaxAmount() {
        double antiLog = 0;
        double base = 2;
        for(int i=0; i< inputs.length; i++) {
            double inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            if(inputMaxValue == -1 && inputs[i] instanceof Operator) {
                ((Operator) inputs[i]).calcMaxAmount();
                inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            }
            if(i == 0) base = inputMaxValue;
            else antiLog += inputMaxValue;
        }
        maxAmount = Math.log(antiLog + 1.0) / Math.log(base);
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.SUM;
    }

    @Override
    public int getTextureStartX() {
        return 96;
    }
}
