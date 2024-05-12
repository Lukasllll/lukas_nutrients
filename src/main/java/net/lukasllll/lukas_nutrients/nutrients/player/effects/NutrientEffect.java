package net.lukasllll.lukas_nutrients.nutrients.player.effects;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.Charset;
import java.util.UUID;

public class NutrientEffect {
    public static final String EFFECT_NAME = LukasNutrients.MOD_ID + ":nutrient_effect";

    private final String targetID;
    private final int minDietScore, maxDietScore;
    private final AssignedAttributeModifier attributeModifier;

    public NutrientEffect(String targetID, int minDietScore, int maxDietScore, AssignedAttributeModifier attributeModifier) {
        this.targetID = targetID;
        this.minDietScore = minDietScore;
        this.maxDietScore = maxDietScore;
        this.attributeModifier = attributeModifier;
    }

    public NutrientEffect(String targetID, int minDietScore, int maxDietScore, String attributeString, double amount, String operationString) {
        this.targetID = targetID;
        this.minDietScore = minDietScore;
        this.maxDietScore = maxDietScore;
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeString));
        this.attributeModifier = new AssignedAttributeModifier(UUID.randomUUID(), EFFECT_NAME, attribute, amount, operationString);
    }

    public NutrientEffect(FriendlyByteBuf buf) {
        int tempLength = buf.readInt();
        this.targetID = (String) buf.readCharSequence(tempLength, Charset.defaultCharset());
        this.minDietScore = buf.readInt();
        this.maxDietScore = buf.readInt();

        tempLength = buf.readInt();
        String attributeString = (String) buf.readCharSequence(tempLength, Charset.defaultCharset());

        double amount = buf.readDouble();

        tempLength = buf.readInt();
        String operationString = (String) buf.readCharSequence(tempLength, Charset.defaultCharset());

        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeString));
        this.attributeModifier = new AssignedAttributeModifier(UUID.randomUUID(), EFFECT_NAME, attribute, amount, operationString);
    }


    /*
    Write the objects relevant data to a ByteBuffer to be sent from the server to the client.
     */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(targetID.length());
        buf.writeCharSequence(targetID, Charset.defaultCharset());
        buf.writeInt(minDietScore);
        buf.writeInt(maxDietScore);

        String attributeString = ForgeRegistries.ATTRIBUTES.getKey(getAttributeModifier().getAttribute()).toString();
        buf.writeInt(attributeString.length());
        buf.writeCharSequence(attributeString, Charset.defaultCharset());

        buf.writeDouble(attributeModifier.getAmount());

        String operationString = this.attributeModifier.getOperationString();
        buf.writeInt(operationString.length());
        buf.writeCharSequence(operationString, Charset.defaultCharset());
    }

    public void apply(ServerPlayer player, int totalDietScore) {
        remove(player);
        if(isActive(totalDietScore)) {
            player.getAttribute(attributeModifier.getAttribute()).addPermanentModifier(attributeModifier);
        }
    }

    public void apply(ServerPlayer player) {
        remove(player);
        player.getAttribute(attributeModifier.getAttribute()).addPermanentModifier(attributeModifier);
    }

    public void remove(ServerPlayer player) {
        player.getAttribute(attributeModifier.getAttribute()).removeModifier(attributeModifier.getId());
    }

    public boolean isActive(int totalDietScore) {
        return totalDietScore <= maxDietScore && totalDietScore >= minDietScore;
    }

    public String getTargetID() { return targetID; }

    public int getMinDietScore() { return minDietScore; }
    public int getMaxDietScore() { return maxDietScore; }

    public AssignedAttributeModifier getAttributeModifier() { return attributeModifier; }

    public String getAttributeString() {
        return ForgeRegistries.ATTRIBUTES.getKey(getAttributeModifier().getAttribute()).toString();
    }

    public double getAttributeModifierAmount() {
        return getAttributeModifier().getAmount();
    }

    public AttributeModifier.Operation getAttributeModifierOperation() {
        return getAttributeModifier().getOperation();
    }

    public boolean canCombineWith(NutrientEffect other) {
        return (this.attributeModifier.getAttribute() == other.attributeModifier.getAttribute()
                && this.attributeModifier.getOperation() == other.attributeModifier.getOperation()
                && this.getTargetID().equals(other.getTargetID())
                && this.minDietScore <= other.maxDietScore
                && other.minDietScore <= this.maxDietScore);
    }

    /*
    returns a new NutrientEffect object. Only works, if the modified attribute and operation are identical.
    The NutrientEffect.attributeModifier of the returned NutrientEffect object has a new amount based on the NutrientEffect
    this object is combined with. The new amount is equivalent to the total change of applying both attributeModifiers
    at the same time.
    The returned objects min- and maxDietScore are both set to -1, since they're not used.
     */
    public NutrientEffect combineWith(NutrientEffect other) {
        if(!canCombineWith(other)) return null;
        double amount = this.attributeModifier.getAmount();
        switch(this.attributeModifier.getOperation()) {
            case ADDITION, MULTIPLY_BASE:
                //for these two operations, getting the amount resulting of applying both modifiers at the same time
                //is pretty easy.
                amount += other.attributeModifier.getAmount();
                break;
            case MULTIPLY_TOTAL:
                //for MULTIPLY_TOTAL, it's a bit more tricky.
                //d_1*(1 + amount_1)*(1 + amount_2) = d_1*(1 + amount_1 + amount_2 + amount_1*amount_2)
                amount = this.attributeModifier.getAmount() + other.attributeModifier.getAmount() + this.attributeModifier.getAmount()*other.attributeModifier.getAmount();
                break;
        }

        return new NutrientEffect(this.getTargetID(), Math.max(this.minDietScore, other.minDietScore), Math.min(this.maxDietScore, other.maxDietScore),
                new AssignedAttributeModifier(this.attributeModifier.getName(), this.attributeModifier.getAttribute(), amount, this.attributeModifier.getOperation()));
    }
}
