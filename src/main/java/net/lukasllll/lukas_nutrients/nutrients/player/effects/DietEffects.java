package net.lukasllll.lukas_nutrients.nutrients.player.effects;


import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

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

    public static int[] getPointRanges() {
        return POINT_RANGES;
    }

    public static int getBasePoint() {
        return BASE_POINT;
    }

    public static List getDietEffects() {
        if(dietEffects == null) {
            dietEffects = new ArrayList<DietEffect>();
            //base effect
            dietEffects.add(new DietEffect(0, 10,
                    new AssignedAttributeModifier(UUID.fromString("9d4527a1-50c1-4729-884b-7812fc274d55"),"base health reduction", Attributes.MAX_HEALTH, -4.0d, AttributeModifier.Operation.ADDITION)));
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
        return  dietEffects;
    }

    public static void apply(ServerPlayer player) {

        int previousMaxHealth = (int) player.getAttribute(Attributes.MAX_HEALTH).getValue();

        player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
            int totalDietScore = nutrients.getTotalScore();
            for(int i=0; i<dietEffects.size(); i++) {
                dietEffects.get(i).apply(player, totalDietScore);
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
            if(totalDietScore <= maxDietScore && totalDietScore >= minDietScore) {
                player.getAttribute(attributeModifier.getAttribute()).addPermanentModifier(attributeModifier);
            }
        }

        public void remove(ServerPlayer player) {
            player.getAttribute(attributeModifier.getAttribute()).removeModifier(attributeModifier.getId());
        }

    }

}
