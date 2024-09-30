/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.adapter;

import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicMachineRecipeAdapter
 * Created by HellFirePvP
 * Date: 02.03.2019 / 15:02
 */
public class DynamicMachineRecipeAdapter extends RecipeAdapter {
    private final DynamicMachine originalMachine;

    public DynamicMachineRecipeAdapter(@Nonnull ResourceLocation registryName, DynamicMachine originalMachine) {
        super(registryName);
        this.originalMachine = originalMachine;
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        String newIdentifier = owningMachineName.getNamespace() + "." + owningMachineName.getPath();

        List<MachineRecipe> newRecipeList = new ArrayList<>();
        for (MachineRecipe recipe : RecipeRegistry.getRecipesFor(this.originalMachine)) {
            MachineRecipe newRecipe = recipe.copy(
                    (res) -> new ResourceLocation(ModularMachinery.MODID, res.getPath() + ".copy." + newIdentifier + "_" + incId),
                    owningMachineName,
                    modifiers);
            newRecipeList.add(newRecipe);
        }

        incId++;
        return newRecipeList;
    }

}
