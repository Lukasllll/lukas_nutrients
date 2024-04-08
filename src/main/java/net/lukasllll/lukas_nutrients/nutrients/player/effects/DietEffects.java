package net.lukasllll.lukas_nutrients.nutrients.player.effects;


import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DietEffects {

    private static final int[] POINT_RANGES = {
            4,
            6
    };

    public static final int BASE_POINT = 5;

    private static List<DietEffect> dietEffects = getDietEffects();
    private static List<DietEffect> baseEffects = getBaseEffects();

    public static int[] getPointRanges() {
        return POINT_RANGES;
    }

    public static int getBasePoint() {
        return BASE_POINT;
    }

    private static List<DietEffect> getDietEffects() {
        if(dietEffects == null) {
            dietEffects = new ArrayList<>();
            //negative effect
            dietEffects.add(new DietEffect(0, 0,
                    new AssignedAttributeModifier(UUID.fromString("9cd9e6c3-108a-4c56-8d80-60bff6b74156"),"health reduction", Attributes.MAX_HEALTH, -2.0d, AttributeModifier.Operation.ADDITION)));
            dietEffects.add(new DietEffect(0, 1,
                    new AssignedAttributeModifier(UUID.fromString("b6dd60cf-b60a-4f2d-ae73-b2c693449b29"),"health reduction", Attributes.MAX_HEALTH, -2.0d, AttributeModifier.Operation.ADDITION)));
            dietEffects.add(new DietEffect(0, 2,
                    new AssignedAttributeModifier(UUID.fromString("294b5ae6-7bab-4d70-a675-c6f9367229b2"),"health reduction", Attributes.MAX_HEALTH, -2.0d, AttributeModifier.Operation.ADDITION)));
            dietEffects.add(new DietEffect(0, 3,
                    new AssignedAttributeModifier(UUID.fromString("b4633d39-6942-420c-9d23-f16fd254ceac"),"health reduction", Attributes.MAX_HEALTH, -2.0d, AttributeModifier.Operation.ADDITION)));
            //positive effects
            dietEffects.add(new DietEffect(7, 10,
                    new AssignedAttributeModifier(UUID.fromString("b15b0388-f0a1-4cbe-bd3d-67cdff89cd7d"),"health increase", Attributes.MAX_HEALTH, 2.0d, AttributeModifier.Operation.ADDITION)));
            dietEffects.add(new DietEffect(8, 10,
                    new AssignedAttributeModifier(UUID.fromString("5a1d652d-b538-4dc1-a89c-148cfdeb6528"), "mining speed increase", Attributes.ATTACK_SPEED, 0.1d, AttributeModifier.Operation.MULTIPLY_TOTAL)));
            dietEffects.add(new DietEffect(9, 10,
                    new AssignedAttributeModifier(UUID.fromString("5c623220-a0c9-4877-a2d3-3e2d113e572e"), "health increase", Attributes.MAX_HEALTH, 2.0d, AttributeModifier.Operation.ADDITION)));
            dietEffects.add(new DietEffect(10, 10,
                    new AssignedAttributeModifier(UUID.fromString("33fb9a3f-cd76-4e33-8325-77604fec98b7"), "speed increase", Attributes.MOVEMENT_SPEED, 0.1d, AttributeModifier.Operation.MULTIPLY_TOTAL)));
        }
        return dietEffects;
    }

    private static List<DietEffect> getBaseEffects() {
        if(baseEffects == null) {
            baseEffects = new ArrayList<>();

            baseEffects.add(new DietEffect(0, 10,
                    new AssignedAttributeModifier(UUID.fromString("9d4527a1-50c1-4729-884b-7812fc274d55"),"base health reduction", Attributes.MAX_HEALTH, -4.0d, AttributeModifier.Operation.ADDITION)));
        }
        return baseEffects;
    }

    public static void apply(ServerPlayer player) {

        int previousMaxHealth = (int) player.getAttribute(Attributes.MAX_HEALTH).getValue();

        player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
            int totalDietScore = nutrients.getTotalScore();
            for(DietEffect baseEffect : getBaseEffects()) {
                baseEffect.apply(player);
            }
            for(DietEffect dietEffect : getDietEffects()) {
                dietEffect.apply(player, totalDietScore);
            }
        });

        int currentMaxHealth = (int) player.getAttribute(Attributes.MAX_HEALTH).getValue();

        if(currentMaxHealth > previousMaxHealth) {
            player.heal(currentMaxHealth - previousMaxHealth);
        } else if(previousMaxHealth > currentMaxHealth) {
            player.setHealth(Math.min(player.getHealth(), currentMaxHealth));
        }
    }

    public static void remove(ServerPlayer player) {
        for(int i=0; i<dietEffects.size(); i++) {
            dietEffects.get(i).remove(player);
        }
    }

    /*
    returns a list with all important information about active attributeModifiers. Similar modifiers are combined.
    baseEffects are ignored for this list.
     */
    public static List< Triple<String, AttributeModifier.Operation, Double> > getSimplifiedList(ServerPlayer player) {
        ArrayList<DietEffect> activeEffects = new ArrayList<>();

        player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
            for(DietEffect effect : dietEffects) {
                if(effect.isActive(nutrients.getTotalScore())) {
                    activeEffects.add(effect);
                }
            }
        });

        //merges entries that match in attribute and operation
        //actually it doesn't technically merge them, it creates a new DietEffects object, that has the merged properties,
        //then it removes the two old entries.
        for(int i0 = 0; i0 < activeEffects.size(); i0++) {
            for(int i1 = i0+1; i1 < activeEffects.size(); i1++) {
                DietEffect effect = activeEffects.get(i0);
                DietEffect otherEffect = activeEffects.get(i1);

                if(effect.canCombineWith(otherEffect)) {
                    DietEffect combined = effect.combineWith(otherEffect);
                    if(combined.attributeModifier.getAmount() != 0 ) activeEffects.add(combined);
                    activeEffects.remove(effect);
                    activeEffects.remove(otherEffect);
                    i0--;
                    break;
                }
            }
        }

        ArrayList< Triple<String, AttributeModifier.Operation, Double> > out = new ArrayList<>();
        for(DietEffect effect : activeEffects) {
            out.add(Triple.of(effect.attributeModifier.getAttribute().getDescriptionId(),
                    effect.attributeModifier.getOperation(),
                    effect.attributeModifier.getAmount()));
        }

        return out;
    }

    protected static class DietEffect {
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

}
