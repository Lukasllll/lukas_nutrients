package net.lukasllll.lukas_nutrients.networking.packet;

import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.operators.Operator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;
import java.util.function.Supplier;

public class NutrientsGlobalDataSyncS2CPacket {

    private final Nutrient[] nutrients;
    private final Operator[] operators;

    private final String[] displayOrder;

    public NutrientsGlobalDataSyncS2CPacket(Nutrient[] nutrients, Operator[] operators, String[] displayOrder) {
        this.nutrients = nutrients;
        this.operators = operators;
        this.displayOrder = displayOrder;
    }

    public NutrientsGlobalDataSyncS2CPacket(FriendlyByteBuf buf) {
        int tempArrayLength = buf.readInt();
        nutrients = new Nutrient[tempArrayLength];
        for(int i=0; i<tempArrayLength; i++) {
            nutrients[i] = new Nutrient(buf);
        }
        tempArrayLength = buf.readInt();
        operators = new Operator[tempArrayLength];
        for(int i=0; i<tempArrayLength; i++) {
            operators[i] = Operator.createOperator(buf);
        }
        tempArrayLength = buf.readInt();
        displayOrder = new String[tempArrayLength];
        for(int i=0; i<tempArrayLength; i++) {
            int stringLength = buf.readInt();
            displayOrder[i] = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(nutrients.length);
        for(int i=0; i<nutrients.length; i++) {
            nutrients[i].toBytes(buf);
        }
        buf.writeInt(operators.length);
        for(int i = 0; i< operators.length; i++) {
            operators[i].toBytes(buf);
        }
        buf.writeInt(displayOrder.length);
        for(int i=0; i<displayOrder.length; i++) {
            buf.writeInt(displayOrder[i].length());
            buf.writeCharSequence(displayOrder[i], Charset.defaultCharset());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();;
        context.enqueueWork(() -> {
            //CLIENT CODE
            ClientNutrientData.setGlobalData(nutrients, operators, displayOrder);
        });

        return true;
    }

}
