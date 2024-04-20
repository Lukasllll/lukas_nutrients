package net.lukasllll.lukas_nutrients.nutrients.player.effects;


import net.lukasllll.lukas_nutrients.config.NutrientEffectsConfig;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DietEffects {

    private static final int[] POINT_RANGES = {
            4,
            6
    };

    public static final int BASE_POINT = 5;

    private static List<DietEffect> baseEffects = null;
    private static List<DietEffect> dietEffects = null;

    public static int[] getPointRanges() {
        return POINT_RANGES;
    }

    public static int getBasePoint() {
        return BASE_POINT;
    }

    public static void getFromConfig() {
        baseEffects = NutrientEffectsConfig.DATA.getBaseEffects();
        dietEffects = NutrientEffectsConfig.DATA.getNutrientEffects();
    }

    /*
    The apply method is split into two, so that you can manually specify, what the previousMaxHealth was. This is
    useful, when all effects have previously been removed e.g. using removeAll. If the previousMaxHealth is not manually
    specified in such a case, just calling apply(player) may lead to the player gaining health.
    Otherwise, just call apply(player) and it will probably be fine.
     */
    public static void apply(ServerPlayer player) {
        int previousMaxHealth = (int) player.getAttribute(Attributes.MAX_HEALTH).getValue();
        apply(player, previousMaxHealth);
    }

    public static void apply(ServerPlayer player, int previousMaxHealth) {

        player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
            int totalDietScore = nutrients.getTotalScore();
            for(DietEffect baseEffect : baseEffects) {
                baseEffect.apply(player);
            }
            for(DietEffect dietEffect : dietEffects) {
                dietEffect.apply(player, totalDietScore);
            }
        });

        int currentMaxHealth = (int) player.getAttribute(Attributes.MAX_HEALTH).getValue();
        //heals the player if they gain max_health or caps the current health at max_health if the player lost max_health
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
    This function removes all diet effect attribute modifiers from a player.
    Diet effect modifiers are identified via their name.
    This function is needed to remove attribute modifiers that might not have been removed at ModEvents:onPlayerLeaveServer.
    This can happen, because the event isn't fired, when the game closes unexpectedly (e.g. using alt+f4).
     */
    public static void removeAll(ServerPlayer player) {
        for(Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            if(player.getAttributes().hasAttribute(attribute)) {
                AttributeInstance attributeInstance = player.getAttributes().getInstance(attribute);
                Set<AttributeModifier> modifiers = attributeInstance.getModifiers();

                if(modifiers == null) continue;

                for(AttributeModifier modifier : modifiers) {
                    if(modifier.getName().equals(DietEffect.EFFECT_NAME)) {
                        attributeInstance.removeModifier(modifier);
                    }
                }
            }
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
                    if(combined.getAttributeModifier().getAmount() != 0 ) activeEffects.add(combined);
                    activeEffects.remove(effect);
                    activeEffects.remove(otherEffect);
                    i0--;
                    break;
                }
            }
        }

        ArrayList< Triple<String, AttributeModifier.Operation, Double> > out = new ArrayList<>();
        for(DietEffect effect : activeEffects) {
            out.add(Triple.of(ForgeRegistries.ATTRIBUTES.getKey(effect.getAttributeModifier().getAttribute()).toString(),
                    effect.getAttributeModifier().getOperation(),
                    effect.getAttributeModifier().getAmount()));
        }

        return out;
    }

}
