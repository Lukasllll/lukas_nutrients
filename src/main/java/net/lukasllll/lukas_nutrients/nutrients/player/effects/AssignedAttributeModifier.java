package net.lukasllll.lukas_nutrients.nutrients.player.effects;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.UUID;

public class AssignedAttributeModifier extends AttributeModifier {

    private final Attribute attribute;

    public AssignedAttributeModifier(UUID uuid, String name, Attribute attribute, double amount, Operation operation) {
        super(uuid, name, amount, operation);
        this.attribute = attribute;

    }

    public AssignedAttributeModifier(String name, Attribute attribute, double amount, Operation operation) {
        super(name, amount, operation);
        this.attribute = attribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }
}
