package net.lukasllll.lukas_nutrients.networking.packet;

import net.lukasllll.lukas_nutrients.client.graphics.gui.NutrientToast;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This packet gets send to the client to get it to display a NutrientToast.
 */
public class NutrientsAddToastS2CPacket {

    private final NutrientEffect effect;
    private final boolean gained;

    /**
     * Send this packet to get the client to display a NutrientToast
     * @param effect the effect that should be displayed
     * @param gained true if the effect was added, false if it was removed
     */
    public NutrientsAddToastS2CPacket(NutrientEffect effect, boolean gained) {
        this.effect = effect;
        this.gained = gained;
    }

    public NutrientsAddToastS2CPacket(FriendlyByteBuf buf) {
        effect = new NutrientEffect(buf);
        gained = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        effect.toBytes(buf);
        buf.writeBoolean(gained);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();;
        context.enqueueWork(() -> {
            //CLIENT CODE
            ToastComponent toastGui = Minecraft.getInstance().getToasts();
            //add the toast
            NutrientToast.addOrUpdate(toastGui, effect, gained);
        });

        return true;
    }

}
