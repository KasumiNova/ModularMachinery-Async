package hellfirepvp.modularmachinery.common.crafting.adapter.tconstruct;

import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeCheckEvent;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AdapterSmelteryMeltingRecipe extends RecipeAdapter {

    public AdapterSmelteryMeltingRecipe() {
        super(new ResourceLocation("tconstruct", "smeltery_melting"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName, List<RecipeModifier> modifiers, List<ComponentRequirement<?, ?>> additionalRequirements, Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers, List<String> recipeTooltips) {
        List<MeltingRecipe> meltingRecipes = TinkerRegistry.getAllMeltingRecipies();
        List<MachineRecipe> machineRecipeList = new ArrayList<>(meltingRecipes.size());

        for (MeltingRecipe meltingRecipe : meltingRecipes) {
            MachineRecipe recipe = createRecipeShell(new ResourceLocation("tconstruct", "smeltery_melting_" + incId),
                    owningMachineName, Math.max(Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, (float) meltingRecipe.temperature / 10, false)), 1),
                    incId, false
            );
            recipe.addRecipeEventHandler(RecipeCheckEvent.class, (IEventHandler<RecipeCheckEvent>) event -> {
                if (event.phase == Phase.START) {
                    event.getActiveRecipe().getDataCompound().setFloat("temperatureRequired", meltingRecipe.temperature);
                }
            });

            // Item Input
            List<ItemStack> inputs = meltingRecipe.input.getInputs();
            List<ChancedIngredientStack> ingredientStackList = new ArrayList<>(inputs.size());
            for (ItemStack stack : inputs) {
                int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, stack.getCount(), false));
                if (inAmount > 0) {
                    ItemStack input = ItemUtils.copyStackWithSize(stack, inAmount);
                    ingredientStackList.add(new ChancedIngredientStack(input));
                }
            }
            recipe.addRequirement(new RequirementIngredientArray(ingredientStackList));

            // Fluid Output
            FluidStack output = meltingRecipe.getResult().copy();
            int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, output.amount, false));
            if (inAmount > 0) {
                output.amount = inAmount;
                recipe.addRequirement(new RequirementFluid(IOType.OUTPUT, output));
            }

            machineRecipeList.add(recipe);
            incId++;
        }

        return machineRecipeList;
    }
}
