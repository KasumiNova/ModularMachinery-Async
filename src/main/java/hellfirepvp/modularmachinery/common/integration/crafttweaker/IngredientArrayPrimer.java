package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import github.kasuminova.mmce.common.integration.Logger;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.AdvancedItemCheckerCT;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.AdvancedItemModifierCT;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;

@ZenRegister
@ZenClass("mods.modularmachinery.IngredientArrayPrimer")
public class IngredientArrayPrimer {
    private final ArrayList<ChancedIngredientStack> ingredientStackList = new ArrayList<>();
    private ChancedIngredientStack lastIngredientStack = null;

    @ZenMethod
    public IngredientArrayPrimer addIngredient(IIngredient input) {
        if (input instanceof final IItemStack stackCT) {
            ItemStack stackMC = CraftTweakerMC.getItemStack(input);
            if (stackMC.isEmpty()) {
                Logger.error("ItemStack not found/unknown item: " + stackMC);
                return this;
            }
            ChancedIngredientStack ingredientStack = new ChancedIngredientStack(stackMC);
            if (stackCT.getTag().length() > 0) {
                ingredientStack.tag = CraftTweakerMC.getNBTCompound(stackCT.getTag());
            }
            addIngredient(ingredientStack);
        } else if (input instanceof IOreDictEntry) {
            addIngredient(new ChancedIngredientStack(((IOreDictEntry) input).getName(), 1));
        } else if (input instanceof IngredientStack && input.getInternal() instanceof IOreDictEntry) {
            addIngredient(new ChancedIngredientStack(
                    ((IOreDictEntry) input.getInternal()).getName(), input.getAmount()));
        }
        return this;
    }

    @ZenMethod
    public IngredientArrayPrimer addIngredients(IIngredient... inputs) {
        for (IIngredient input : inputs) {
            addIngredient(input);
        }
        return this;
    }

    @ZenMethod
    public IngredientArrayPrimer addChancedIngredient(IIngredient input, float chance) {
        addIngredient(input);
        setChance(chance);
        return this;
    }

    @ZenMethod
    public IngredientArrayPrimer setMinMaxAmount(int min, int max) {
        if (lastIngredientStack != null) {
            if (min < max) {
                lastIngredientStack.minCount = min;
                lastIngredientStack.maxCount = max;
            } else {
                Logger.warn("`min` cannot larger than `max`!");
            }
        }
        return this;
    }

    @ZenMethod
    public IngredientArrayPrimer setChance(float chance) {
        if (lastIngredientStack != null) {
            lastIngredientStack.chance = chance;
        }
        return this;
    }

    @ZenMethod
    public IngredientArrayPrimer setChecker(AdvancedItemCheckerCT checker) {
        if (lastIngredientStack != null) {
            lastIngredientStack.itemChecker = (controller, stack) ->
                    checker.isMatch(controller, CraftTweakerMC.getIItemStack(stack));
        }
        return this;
    }

    @ZenMethod
    public IngredientArrayPrimer addItemModifier(AdvancedItemModifierCT modifier) {
        if (lastIngredientStack != null) {
            lastIngredientStack.itemModifierList.add((controller, stack) -> CraftTweakerMC.getItemStack(
                    modifier.apply(controller, CraftTweakerMC.getIItemStackMutable(stack))));
        }
        return this;
    }

    @ZenMethod
    @Deprecated
    public IngredientArrayPrimer build() {
        Logger.warn("IngredientArrayPrimer#build is deprecated, it will be removed in future version.");
        return this;
    }

    public ArrayList<ChancedIngredientStack> getIngredientStackList() {
        return ingredientStackList;
    }

    private void addIngredient(ChancedIngredientStack input) {
        ingredientStackList.add(input);
        lastIngredientStack = input;
    }
}
