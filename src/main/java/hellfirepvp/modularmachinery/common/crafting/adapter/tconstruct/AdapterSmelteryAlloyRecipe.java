package hellfirepvp.modularmachinery.common.crafting.adapter.tconstruct;

import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.AlloyRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AdapterSmelteryAlloyRecipe extends RecipeAdapter {
    public static final int WORK_TIME = 4;

    public AdapterSmelteryAlloyRecipe() {
        super(new ResourceLocation("tconstruct", "smeltery_alloy"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName, List<RecipeModifier> modifiers, List<ComponentRequirement<?, ?>> additionalRequirements, Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers, List<String> recipeTooltips) {
        List<AlloyRecipe> alloyRecipes = TinkerRegistry.getAlloys();
        List<MachineRecipe> machineRecipeList = new ArrayList<>(alloyRecipes.size());

        for (AlloyRecipe alloyRecipe : alloyRecipes) {
            MachineRecipe recipe = createRecipeShell(new ResourceLocation("tconstruct", "smeltery_alloy_" + incId),
                    owningMachineName, Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, WORK_TIME, false)),
                    incId, false
            );

            // Fluid Input
            List<FluidStack> inputs = alloyRecipe.getFluids();
            for (FluidStack fluid : inputs) {
                FluidStack input = fluid.copy();
                int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, input.amount, false));
                if (inAmount > 0) {
                    input.amount = inAmount;
                    recipe.addRequirement(new RequirementFluid(IOType.INPUT, input));
                }
            }

            // Fluid Output
            FluidStack output = alloyRecipe.getResult().copy();
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
