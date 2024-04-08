package net.lukasllll.lukas_nutrients.nutrients.player.effects;

import net.minecraft.server.level.ServerPlayer;

public class DietEffect {
    private final int minDietScore, maxDietScore;
    private final AssignedAttributeModifier attributeModifier;

    public DietEffect(int minDietScore, int maxDietScore, AssignedAttributeModifier attributeModifier) {
        this.minDietScore = minDietScore;
        this.maxDietScore = maxDietScore;
        this.attributeModifier = attributeModifier;
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

    public AssignedAttributeModifier getAttributeModifier() { return attributeModifier; }

    public boolean canCombineWith(DietEffect other) {
        return (this.attributeModifier.getAttribute() == other.attributeModifier.getAttribute()
                && this.attributeModifier.getOperation() == other.attributeModifier.getOperation());
    }

    /*
    returns a new DietEffect object. Only works, if the modified attribute and operation are identical.
    The DietEffect.attributeModifier of the returned DietEffect object has a new amount based on the DietEffect
    this object is combined with. The new amount is equivalent to the total change of applying both attributeModifiers
    at the same time.
    The returned objects min- and maxDietScore are both set to -1, since they're not used.
     */
    public DietEffect combineWith(DietEffect other) {
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

        return new DietEffect(-1, -1,
                new AssignedAttributeModifier(this.attributeModifier.getName(), this.attributeModifier.getAttribute(), amount, this.attributeModifier.getOperation()));
    }
}
