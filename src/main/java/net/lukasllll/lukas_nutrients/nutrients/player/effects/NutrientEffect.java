package net.lukasllll.lukas_nutrients.nutrients.player.effects;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

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

    public NutrientEffect(String targetID,int minDietScore, int maxDietScore, String attributeString, double amount, String operationString) {
        this.targetID = targetID;
        this.minDietScore = minDietScore;
        this.maxDietScore = maxDietScore;
        AttributeModifier.Operation operation = null;
        switch(operationString) {
            case "ADDITION":
                operation = AttributeModifier.Operation.ADDITION;
                break;
            case "MULTIPLY_BASE":
                operation = AttributeModifier.Operation.MULTIPLY_BASE;
                break;
            case "MULTIPLY_TOTAL":
                operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
                break;
        }
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeString));
        this.attributeModifier = new AssignedAttributeModifier(UUID.randomUUID(), EFFECT_NAME, attribute, amount, operation);
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

    public AssignedAttributeModifier getAttributeModifier() { return attributeModifier; }

    public boolean canCombineWith(NutrientEffect other) {
        return (this.attributeModifier.getAttribute() == other.attributeModifier.getAttribute()
                && this.attributeModifier.getOperation() == other.attributeModifier.getOperation()
                && this.getTargetID().equals(other.getTargetID()));
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

        return new NutrientEffect(this.getTargetID(), -1, -1,
                new AssignedAttributeModifier(this.attributeModifier.getName(), this.attributeModifier.getAttribute(), amount, this.attributeModifier.getOperation()));
    }
}
