package net.lukasllll.lukas_nutrients.api.event;

import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;

public abstract class NutrientEffectEvent extends PlayerEvent {
    protected NutrientEffect effect;

    public NutrientEffect getEffect() { return effect; }

    public NutrientEffectEvent(NutrientEffect effect, Player player) {
        super(player);
        this.effect = effect;
    }

    public static class Removed extends NutrientEffectEvent {
        public Removed(NutrientEffect effect, Player player) {
            super(effect, player);
        }
    }

    public static class Added extends NutrientEffectEvent {

        private boolean setup;

        public Added(NutrientEffect effect, Player player, boolean setup) {
            super(effect, player);
            this.setup = setup;
        }

        public boolean isSetup() {
            return setup;
        }
    }

}
