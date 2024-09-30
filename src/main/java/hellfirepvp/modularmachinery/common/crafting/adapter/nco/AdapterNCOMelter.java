package hellfirepvp.modularmachinery.common.crafting.adapter.nco;

import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import nc.recipe.BasicRecipe;
import nc.recipe.NCRecipes;
import nc.recipe.ingredient.IFluidIngredient;
import nc.recipe.ingredient.IItemIngredient;
import nc.recipe.ingredient.ItemArrayIngredient;
import nc.recipe.ingredient.OreIngredient;
import nc.recipe.processor.MelterRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AdapterNCOMelter extends AdapterNCOMachine {
    public static final int WORK_TIME = 800;
    public static final int BASE_ENERGY_USAGE = 40;

    public AdapterNCOMelter() {
        super(new ResourceLocation("nuclearcraft", "melter"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName, List<RecipeModifier> modifiers, List<ComponentRequirement<?, ?>> additionalRequirements, Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers, List<String> recipeTooltips) {
        MelterRecipes melterRecipes = NCRecipes.melter;

        List<BasicRecipe> recipeList = melterRecipes.getRecipeList();
        List<MachineRecipe> machineRecipeList = new ArrayList<>(recipeList.size());

        for (BasicRecipe basicRecipe : recipeList) {
            MachineRecipe recipe = createRecipeShell(new ResourceLocation("nuclearcraft", "melter_" + incId),
                    owningMachineName, (int) basicRecipe.getBaseProcessTime(Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, WORK_TIME, false))),
                    incId, false
            );

            // Item Input
            for (IItemIngredient iItemIngredient : basicRecipe.getItemIngredients()) {
                ItemStack stack = iItemIngredient.getStack();

                int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, stack.getCount(), false));
                if (inAmount <= 0) {
                    continue;
                }

                if (iItemIngredient instanceof OreIngredient) {
                    OreIngredient oreIngredient = (OreIngredient) iItemIngredient;
                    recipe.addRequirement(new RequirementItem(IOType.INPUT, oreIngredient.oreName, inAmount));
                    continue;
                }

                if (iItemIngredient instanceof final ItemArrayIngredient arrayIngredient) {
                    List<IItemIngredient> ingredientList = arrayIngredient.ingredientList;
                    List<ChancedIngredientStack> ingredientStackList = new ArrayList<>(ingredientList.size());
                    for (IItemIngredient itemIngredient : ingredientList) {

                        if (itemIngredient instanceof final OreIngredient oreIngredient) {
                            int subInAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, oreIngredient.stackSize, false));

                            ingredientStackList.add(new ChancedIngredientStack(oreIngredient.oreName, subInAmount));
                        } else {
                            ItemStack ingredientStack = itemIngredient.getStack();
                            int subInAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, ingredientStack.getCount(), false));

                            ingredientStackList.add(new ChancedIngredientStack(ItemUtils.copyStackWithSize(ingredientStack, subInAmount)));
                        }
                    }

                    recipe.addRequirement(new RequirementIngredientArray(ingredientStackList));
                    continue;
                }

                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(stack, inAmount)));
            }

            // Fluid Output
            for (IFluidIngredient fluidIngredient : basicRecipe.getFluidProducts()) {
                FluidStack stack = fluidIngredient.getStack().copy();
                int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, stack.amount, false));
                if (inAmount > 0) {
                    stack.amount = inAmount;
                    recipe.addRequirement(new RequirementFluid(IOType.OUTPUT, stack));
                }
            }

            // Energy Input
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, Math.round(RecipeModifier.applyModifiers(
                    modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, BASE_ENERGY_USAGE, false)))
            );

            machineRecipeList.add(recipe);
            incId++;
        }

        return machineRecipeList;
    }
}
