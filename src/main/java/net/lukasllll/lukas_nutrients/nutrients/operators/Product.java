package net.lukasllll.lukas_nutrients.nutrients.operators;

import com.google.common.util.concurrent.AtomicDouble;
import net.lukasllll.lukas_nutrients.client.TooltipHelper;
import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Iterator;
import java.util.List;

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

    @Override
    public String getOperatorName() {
        return "Product";
    }

    @Override
    public List<Component> getTooltip(boolean moreInfo) {
        List<Component> out = super.getTooltip(moreInfo);
        if(moreInfo) {
            out.add(Component.literal("Factors: ").withStyle(ChatFormatting.DARK_GRAY));
            for(int i=0; i<inputs.length; i++) {
                MutableComponent line = Component.literal(inputs[i].getDisplayname()).withStyle(ChatFormatting.GRAY);
                if(inputs[i] instanceof Constant)
                    line.append(Component.literal(" (constant)").withStyle(ChatFormatting.DARK_GRAY));
                else
                    line.append(Component.literal(takeInputScore[i] ? " (score)" : " (amount)").withStyle(ChatFormatting.DARK_GRAY));
                out.add(line);
            }
        } else {
            out.add(TooltipHelper.getHoldShiftComponent());
        }

        return out;
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.SUM;
    }

    @Override
    public int getTextureStartX() {
        return 16;
    }
}
