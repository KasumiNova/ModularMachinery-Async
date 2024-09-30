package github.kasuminova.mmce.common.event.recipe;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.machine.MachineEvent;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.RecipeThread;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.RecipeEvent")
public class RecipeEvent extends MachineEvent {
    private final ActiveMachineRecipe activeRecipe;
    private final RecipeCraftingContext context;
    private final RecipeThread recipeThread;

    public RecipeEvent(TileMultiblockMachineController controller, RecipeThread recipeThread, RecipeCraftingContext context) {
        super(controller);
        this.activeRecipe = context.getActiveRecipe();
        this.context = context;
        this.recipeThread = recipeThread;
    }

    @ZenGetter("activeRecipe")
    public ActiveMachineRecipe getActiveRecipe() {
        return activeRecipe;
    }

    public RecipeCraftingContext getContext() {
        return context;
    }

    public RecipeThread getRecipeThread() {
        return recipeThread;
    }

    @Override
    public void postCrTEvent() {
        if (activeRecipe == null || activeRecipe.getRecipe() == null) {
            return;
        }

        super.postCrTEvent();
        if (isCanceled()) {
            return;
        }
        List<IEventHandler<RecipeEvent>> handlers = activeRecipe.getRecipe().getRecipeEventHandlers().get(getClass());
        if (handlers == null) {
            return;
        }
        for (IEventHandler<RecipeEvent> handler : handlers) {
            handler.invoke(this);
            if (isCanceled()) {
                break;
            }
        }
    }
}
