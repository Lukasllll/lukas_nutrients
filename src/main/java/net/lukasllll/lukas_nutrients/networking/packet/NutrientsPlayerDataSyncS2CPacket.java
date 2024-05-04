package net.lukasllll.lukas_nutrients.networking.packet;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NutrientsPlayerDataSyncS2CPacket {

    private final double[] nutrientAmounts;
    private final double[] exhaustionLevels;
    private final int[] nutrientScores;                   //the score of the given range
    private final int[] ranges;
    private final int[] sumScores;
    List<Triple<String, AttributeModifier.Operation, Double>> activeDietEffects;

    public NutrientsPlayerDataSyncS2CPacket(double[] amounts, double[] exhaustionLevels, int[] nutrientScores, int[] ranges, int[] sumScores,
                                            List<Triple<String, AttributeModifier.Operation, Double>> activeDietEffects) {
        this.nutrientAmounts = amounts;
        this.exhaustionLevels = exhaustionLevels;
        this.nutrientScores = nutrientScores;
        this.ranges = ranges;
        this.sumScores = sumScores;
        this.activeDietEffects = activeDietEffects;
    }

    public NutrientsPlayerDataSyncS2CPacket(FriendlyByteBuf buf) {
        int tempArrayLength = buf.readInt();
        nutrientAmounts = new double[tempArrayLength];
        exhaustionLevels = new double[tempArrayLength];
        nutrientScores = new int[tempArrayLength];
        ranges = new int[tempArrayLength];
        for(int i=0; i<tempArrayLength; i++) {
            nutrientAmounts[i] = buf.readDouble();
            exhaustionLevels[i] = buf.readDouble();
            nutrientScores[i] = buf.readInt();
            ranges[i] = buf.readInt();
        }
        tempArrayLength = buf.readInt();
        sumScores = new int[tempArrayLength];
        for(int i=0; i<tempArrayLength; i++) {
            sumScores[i] = buf.readInt();
        }

        int listLength = buf.readInt();
        activeDietEffects = new ArrayList<>();
        for(int i=0; i < listLength; i++) {
            int stringLength = buf.readInt();
            String attributeDescriptionId = buf.readCharSequence(stringLength, Charset.defaultCharset()).toString();
            AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(buf.readInt());
            Double amount = buf.readDouble();
            activeDietEffects.add(Triple.of(attributeDescriptionId, operation, amount));
        }

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(nutrientAmounts.length);
        for(int i=0; i< nutrientAmounts.length; i++) {
            buf.writeDouble(nutrientAmounts[i]);
            buf.writeDouble(exhaustionLevels[i]);
            buf.writeInt(nutrientScores[i]);
            buf.writeInt(ranges[i]);
        }
        buf.writeInt(sumScores.length);
        for(int i=0; i<sumScores.length; i++) {
            buf.writeInt(sumScores[i]);
        }

        buf.writeInt(activeDietEffects.size());
        for(int i=0; i < activeDietEffects.size(); i++) {
            buf.writeInt(activeDietEffects.get(i).getLeft().length());
            buf.writeCharSequence(activeDietEffects.get(i).getLeft(), Charset.defaultCharset());
            buf.writeInt(activeDietEffects.get(i).getMiddle().toValue());
            buf.writeDouble(activeDietEffects.get(i).getRight());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();;
        context.enqueueWork(() -> {
            //CLIENT CODE
            ClientNutrientData.setPlayerData(nutrientAmounts, exhaustionLevels, nutrientScores, ranges, sumScores, activeDietEffects);
        });

        return true;
    }

}
