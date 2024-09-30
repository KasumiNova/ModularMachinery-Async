package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.integration.Logger;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.modularmachinery.RecipeModifierBuilder")
public class RecipeModifierBuilder {
    private String type = "";
    private IOType ioType = null;
    private float value = 0.0f;
    private int operation = 0;
    private boolean affectChance = false;

    @ZenMethod
    public static RecipeModifierBuilder newBuilder() {
        return new RecipeModifierBuilder();
    }

    @ZenMethod
    public static RecipeModifierBuilder create(String type, String ioTypeStr, float value, int operation, boolean affectChance) {
        RecipeModifierBuilder builder = new RecipeModifierBuilder();
        builder.type = type;
        builder.ioType = IOType.getByString(ioTypeStr);
        builder.value = value;
        builder.operation = operation;
        builder.affectChance = affectChance;
        return builder;
    }

    @ZenMethod
    public RecipeModifierBuilder setRequirementType(String type) {
        this.type = type;
        return this;
    }

    public RecipeModifierBuilder requirementType(String type) {
        return setRequirementType(type);
    }

    @ZenMethod
    public RecipeModifierBuilder setIOType(String ioTypeStr) {
        this.ioType = IOType.getByString(ioTypeStr);
        return this;
    }

    public RecipeModifierBuilder input() {
        return ioType(IOType.INPUT);
    }

    public RecipeModifierBuilder output() {
        return ioType(IOType.OUTPUT);
    }

    public RecipeModifierBuilder ioType(IOType ioType) {
        this.ioType = ioType;
        return this;
    }

    @ZenMethod
    public RecipeModifierBuilder setValue(float value) {
        this.value = value;
        return this;
    }

    public RecipeModifierBuilder value(float value) {
        return setValue(value);
    }

    @ZenMethod
    public RecipeModifierBuilder setOperation(int operation) {
        this.operation = operation;
        return this;
    }

    @ZenMethod
    public RecipeModifierBuilder add() {
        return setOperation(RecipeModifier.OPERATION_ADD);
    }

    @ZenMethod
    public RecipeModifierBuilder multiply() {
        return setOperation(RecipeModifier.OPERATION_MULTIPLY);
    }

    @ZenMethod
    public RecipeModifierBuilder isAffectChance(boolean affectChance) {
        this.affectChance = affectChance;
        return this;
    }

    @ZenMethod
    public RecipeModifier build() {
        RequirementType<?, ?> target = RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(new ResourceLocation(type));
        if (target == null) {
            Logger.error("Could not find requirementType " + type + "!");
            return null;
        }
        if (this.ioType == null) {
            Logger.error("IOType was not set or was invalid. Valid values are: ['input', 'output'].");
            return null;
        }
        if (operation > 1 || operation < 0) {
            Logger.error("Invalid operation " + operation + "!");
            return null;
        }

        return new RecipeModifier(target, ioType, value, operation, affectChance);
    }
}
