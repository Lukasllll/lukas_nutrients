package net.lukasllll.lukas_nutrients.gamerule;

import net.minecraft.world.level.GameRules;

public class ModGameRules{
    public static final GameRules.Key<GameRules.BooleanValue> RULE_KEEPNUTRIENTS = GameRules.register("keepNutrients", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
}
