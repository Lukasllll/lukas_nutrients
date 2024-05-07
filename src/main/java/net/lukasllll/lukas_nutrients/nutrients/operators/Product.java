package net.lukasllll.lukas_nutrients.nutrients.operators;

import com.google.common.util.concurrent.AtomicDouble;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Iterator;

public class Product extends Operator implements IDisplayElement, ICalcElement {

    public Product(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] summandIDs) {
        super(id, displayname, pointRanges, basePoint, score, summandIDs);
    }

    public Product(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public double getCurrentAmount(Iterator<Double> inputValues) {
        AtomicDouble out = new AtomicDouble(1.0);
        inputValues.forEachRemaining((inputAmount) -> {
            out.updateAndGet(v -> v * inputAmount);
        });
        return out.get();
    }

    @Override
    public void calcMaxAmount() {
        maxAmount = 1.0;
        for(int i=0; i< inputs.length; i++) {
            double inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            if(inputMaxValue == -1 && inputs[i] instanceof Operator) {
                ((Operator) inputs[i]).calcMaxAmount();
                inputMaxValue = takeInputScore[i] ? inputs[i].getMaxScore() : inputs[i].getMaxAmount();
            }
            maxAmount *= inputMaxValue;
        }
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.SUM;
    }

    @Override
    public int getTextureStartX() {
        return 16;
    }
}
