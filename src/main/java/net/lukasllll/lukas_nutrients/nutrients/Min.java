package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Min extends Operator implements ICalcElement, IDisplayElement {
    public Min(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] summandIDs) {
        super(id, displayname, pointRanges, basePoint, score, summandIDs);
    }

    public Min(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public int getCurrentAmount(Iterator<Integer> inputAmounts, Iterator<Integer> inputScores) {
        AtomicInteger out = new AtomicInteger(inputAmounts.next());
        inputAmounts.forEachRemaining((inputAmount) -> {
            out.set(Math.min(inputAmount, out.get()));
        });
        return out.get();
    }

    @Override
    public void calcMaxAmount() {
        maxAmount = 0;
        for(ICalcElement input : inputs) {
            int inputMaxAmount = input.getMaxAmount();
            if(inputMaxAmount == -1 && input instanceof Operator) {
                ((Operator) input).calcMaxAmount();
                inputMaxAmount = input.getMaxAmount();
            }
            if(maxAmount == 0) maxAmount = inputMaxAmount;
            else maxAmount = Math.min(maxAmount, inputMaxAmount);
        }
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.NUTRIENT;
    }

    @Override
    public int getTextureStartX() {
        return 16;
    }
}
