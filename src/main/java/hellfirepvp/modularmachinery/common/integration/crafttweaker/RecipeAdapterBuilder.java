package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.integration.Logger;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedList;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.RecipeAdapterBuilder")
public class RecipeAdapterBuilder extends RecipePrimer {
    protected final List<RecipeModifier> modifiers = new LinkedList<>();
    protected final ResourceLocation parentMachineName;

    private RecipeAdapterBuilder(ResourceLocation machineName, ResourceLocation parentMachineName) {
        super(null, machineName, 0, 0, false);
        this.parentMachineName = parentMachineName;
    }

    @ZenMethod
    public static RecipeAdapterBuilder create(String machineName, String parentMachineName) {
        ResourceLocation machineLoc = new ResourceLocation(machineName);
        if (machineLoc.getNamespace().equals("minecraft")) {
            machineLoc = new ResourceLocation(ModularMachinery.MODID, machineLoc.getPath());
        }
        return new RecipeAdapterBuilder(machineLoc, new ResourceLocation(parentMachineName));
    }

    //----------------------------------------------------------------------------------------------
    // Adapter
    //----------------------------------------------------------------------------------------------

    /**
     * 为配方适配器添加一个 RecipeModifier，RecipeModifier 的数值会在复制父机械的配方的时候被应用。
     * 如果使用链式调用此方法，必须第一个调用！
     * Adds a RecipeModifier to the recipe adapter. The value of the RecipeModifier will be applied when copying the recipe of the parent machine.
     * To call this method, it must be the first one called!
     *
     * @param modifier RecipeModifier
     */
    @ZenMethod
    public RecipeAdapterBuilder addModifier(RecipeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public List<RecipeModifier> getModifiers() {
        return modifiers;
    }

    public ResourceLocation getAdapterParentMachineName() {
        return parentMachineName;
    }

    @Override
    public void build() {
        RecipeRegistry.getRegistry().registerRecipeAdapterEarly(this);
    }

    //----------------------------------------------------------------------------------------------
    // Unavailable features
    //----------------------------------------------------------------------------------------------
    @Override
    public RecipePrimer setParallelized(boolean isParallelized) {
        Logger.warn("RecipeAdapterBuilder cannot set parallelized, it depends on the parent machine recipe!");
        return this;
    }
}
