package net.lukasllll.lukas_nutrients.networking.packet;

import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NutrientsDataSyncS2CPacket {

    private static NutrientGroup[] Groups= NutrientGroup.getFoodGroups();

    private final double[] nutrientAmounts;
    private final double[] exhaustionLevels;
    private final int[] nutrientRanges;
    private final int[] nutrientScores;
    private int totalScore;

    public NutrientsDataSyncS2CPacket(double[] amounts, double[] exhaustionLevels, int[] ranges, int[] scores, int totalScore) {
        this.nutrientAmounts = amounts;
        this.exhaustionLevels = exhaustionLevels;
        this.nutrientRanges = ranges;
        this.nutrientScores = scores;
        this.totalScore = totalScore;
    }

    public NutrientsDataSyncS2CPacket(FriendlyByteBuf buf) {
        this.nutrientAmounts = new double[Groups.length];
        this.exhaustionLevels = new double[Groups.length];
        this.nutrientRanges = new int[Groups.length];
        this.nutrientScores = new int[Groups.length];
        for(int i=0; i< nutrientAmounts.length; i++) {
            this.nutrientAmounts[i] = buf.readDouble();
            this.exhaustionLevels[i] = buf.readDouble();
            this.nutrientRanges[i] = buf.readInt();
            this.nutrientScores[i] = buf.readInt();
        }
        this.totalScore = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        for(int i=0; i< nutrientAmounts.length; i++) {
            buf.writeDouble(nutrientAmounts[i]);
            buf.writeDouble(exhaustionLevels[i]);
            buf.writeInt(nutrientRanges[i]);
            buf.writeInt(nutrientScores[i]);
        }
        buf.writeInt(totalScore);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();;
        context.enqueueWork(() -> {
            //CLIENT CODE
            ClientNutrientData.set(nutrientAmounts, exhaustionLevels, nutrientRanges, nutrientScores, totalScore);
        });

        return true;
    }

}
