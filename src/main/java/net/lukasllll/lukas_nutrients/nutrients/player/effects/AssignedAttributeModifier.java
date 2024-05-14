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

    public AssignedAttributeModifier(UUID uuid, String name, Attribute attribute, double amount, String operationString) {
        super(uuid, name, amount, getOperationFromString(operationString));
        this.attribute = attribute;
    }

    public AssignedAttributeModifier(String name, Attribute attribute, double amount, Operation operation) {
        super(name, amount, operation);
        this.attribute = attribute;
    }


    public Attribute getAttribute() {
        return attribute;
    }

    public String getOperationString() {
        return getStringFromOperation(getOperation());
    }

    private static Operation getOperationFromString(String s) {
        return switch (s) {
            case "ADDITION" -> Operation.ADDITION;
            case "MULTIPLY_BASE" -> Operation.MULTIPLY_BASE;
            case "MULTIPLY_TOTAL" -> Operation.MULTIPLY_TOTAL;
            default -> null;
        };
    }

    private static String getStringFromOperation(Operation o) {
        return switch (o) {
            case ADDITION -> "ADDITION";
            case MULTIPLY_BASE -> "MULTIPLY_BASE";
            case MULTIPLY_TOTAL -> "MULTIPLY_TOTAL";
            default -> null;
        };
    }
}
