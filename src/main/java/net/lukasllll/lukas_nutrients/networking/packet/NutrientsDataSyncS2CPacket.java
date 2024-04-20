package net.lukasllll.lukas_nutrients.networking.packet;

import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NutrientsDataSyncS2CPacket {

    private final NutrientGroup[] nutrientGroups;

    private final double[] nutrientAmounts;
    private final double[] exhaustionLevels;
    private final int[] nutrientRanges;
    private final int[] nutrientScores;
    private int totalScore;
    List<Triple<String, AttributeModifier.Operation, Double>> activeDietEffects;

    public NutrientsDataSyncS2CPacket(NutrientGroup[] nutrientGroups, double[] amounts, double[] exhaustionLevels, int[] ranges, int[] scores, int totalScore,
                                      List<Triple<String, AttributeModifier.Operation, Double>> activeDietEffects) {
        this.nutrientGroups = nutrientGroups;
        this.nutrientAmounts = amounts;
        this.exhaustionLevels = exhaustionLevels;
        this.nutrientRanges = ranges;
        this.nutrientScores = scores;
        this.totalScore = totalScore;
        this.activeDietEffects = activeDietEffects;
    }

    public NutrientsDataSyncS2CPacket(FriendlyByteBuf buf) {
        int tempArrayLength = buf.readInt();
        this.nutrientGroups = new NutrientGroup[tempArrayLength];
        for(int i=0; i < tempArrayLength; i++) {
            this.nutrientGroups[i] = new NutrientGroup(buf);
        }
        this.nutrientAmounts = new double[nutrientGroups.length];
        this.exhaustionLevels = new double[nutrientGroups.length];
        this.nutrientRanges = new int[nutrientGroups.length];
        this.nutrientScores = new int[nutrientGroups.length];
        this.activeDietEffects = new ArrayList<>();
        for(int i=0; i< nutrientAmounts.length; i++) {
            this.nutrientAmounts[i] = buf.readDouble();
            this.exhaustionLevels[i] = buf.readDouble();
            this.nutrientRanges[i] = buf.readInt();
            this.nutrientScores[i] = buf.readInt();
        }
        this.totalScore = buf.readInt();

        int listLength = buf.readInt();
        for(int i=0; i < listLength; i++) {
            int stringLength = buf.readInt();
            String attributeDescriptionId = buf.readCharSequence(stringLength, Charset.defaultCharset()).toString();
            AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(buf.readInt());
            Double amount = buf.readDouble();
            activeDietEffects.add(Triple.of(attributeDescriptionId, operation, amount));
        }

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(nutrientGroups.length);
        for(int i=0; i < nutrientGroups.length; i++) {
            nutrientGroups[i].toBytes(buf);
        }
        for(int i=0; i< nutrientAmounts.length; i++) {
            buf.writeDouble(nutrientAmounts[i]);
            buf.writeDouble(exhaustionLevels[i]);
            buf.writeInt(nutrientRanges[i]);
            buf.writeInt(nutrientScores[i]);
        }
        buf.writeInt(totalScore);

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
            ClientNutrientData.set(nutrientGroups, nutrientAmounts, exhaustionLevels, nutrientRanges, nutrientScores, totalScore, activeDietEffects);
        });

        return true;
    }

}
