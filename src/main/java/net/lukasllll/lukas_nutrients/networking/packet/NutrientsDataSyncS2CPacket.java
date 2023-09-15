package net.lukasllll.lukas_nutrients.networking.packet;

import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.nutrients.FoodGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NutrientsDataSyncS2CPacket {

    private static FoodGroup[] Groups=FoodGroup.getFoodGroups();

    private final double[] nutrientAmounts;

    public NutrientsDataSyncS2CPacket(double[] amounts) {
        this.nutrientAmounts = amounts;
    }

    public NutrientsDataSyncS2CPacket(FriendlyByteBuf buf) {

        this.nutrientAmounts = new double[Groups.length];
        for(int i=0; i< nutrientAmounts.length; i++) {
            this.nutrientAmounts[i] = buf.readDouble();
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        for(int i=0; i< nutrientAmounts.length; i++) {
            buf.writeDouble(nutrientAmounts[i]);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();;
        context.enqueueWork(() -> {
            //CLIENT CODE
            ClientNutrientData.set(nutrientAmounts);
        });

        return true;
    }

}
