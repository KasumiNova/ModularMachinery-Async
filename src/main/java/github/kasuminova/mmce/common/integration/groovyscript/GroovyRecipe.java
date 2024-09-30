package github.kasuminova.mmce.common.integration.groovyscript;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.EnumHelper;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientHelper;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.cleanroommc.groovyscript.helper.recipe.RecipeName;
import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.*;
import github.kasuminova.mmce.common.util.concurrent.Action;
import groovy.lang.Closure;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.PreparedRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.crafting.requirement.*;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.IngredientArrayPrimer;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.RecipePrimer;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kport.modularmagic.common.crafting.requirement.*;
import kport.modularmagic.common.integration.jei.ingredient.Aura;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GroovyRecipe implements PreparedRecipe {

    protected String name;
    protected final ResourceLocation machineName;
    private int tickTime = 100, priority = 0;
    private boolean doesVoidPerTick = false;

    private final List<ComponentRequirement<?, ?>> components = new ArrayList<>();
    private final List<Action> needAfterInitActions = new ArrayList<>();
    private final List<String> toolTipList = new ArrayList<>();
    private final Map<Class<?>, List<IEventHandler<RecipeEvent>>> recipeEventHandlers = new Object2ObjectOpenHashMap<>();

    private boolean parallelized = Config.recipeParallelizeEnabledByDefault;
    private int maxThreads = -1;
    private String threadName = "";
    private ComponentRequirement<?, ?> lastComponent = null;

    public GroovyRecipe(ResourceLocation owningMachine) {
        this.machineName = owningMachine;
    }

    public GroovyRecipe name(String name) {
        this.name = name;
        return this;
    }

    public GroovyRecipe time(int time) {
        this.tickTime = time;
        return this;
    }

    public GroovyRecipe priority(int priority) {
        this.priority = priority;
        return this;
    }

    public GroovyRecipe cancelIfPerTickFails(boolean b) {
        this.doesVoidPerTick = b;
        return this;
    }

    public GroovyRecipe parallelizeUnaffected(boolean unaffected) {
        if (lastComponent instanceof ComponentRequirement.Parallelizable parallelizable) {
            parallelizable.setParallelizeUnaffected(unaffected);
        } else {
            GroovyLog.get().warn("Target " + lastComponent.getClass() + " cannot be parallelized!");
        }
        return this;
    }

    public GroovyRecipe parallelized(boolean isParallelized) {
        this.parallelized = isParallelized;
        return this;
    }

    public GroovyRecipe chance(float chance) {
        if (lastComponent != null) {
            if (lastComponent instanceof ComponentRequirement.ChancedRequirement chancedReq) {
                chancedReq.setChance(chance);
            } else {
                GroovyLog.get().warn("Cannot set chance for not-chance-based Component: " + lastComponent.getClass());
            }
        }
        return this;
    }


    public GroovyRecipe tag(String selectorTag) {
        if (lastComponent != null) {
            lastComponent.setTag(new ComponentSelectorTag(selectorTag));
        }
        return this;
    }


    public GroovyRecipe preViewNBT(NBTTagCompound nbt) {
        if (lastComponent != null) {
            if (lastComponent instanceof RequirementItem reqItem) {
                reqItem.previewDisplayTag = nbt;
            } else {
                GroovyLog.get().warn("setPreViewNBT(IData nbt) only can be applied to `Item`!");
            }
        } else {
            GroovyLog.get().warn("setPreViewNBT(IData nbt) only can be applied to `Item`!");
        }
        return this;
    }


    public GroovyRecipe nbtChecker(Closure<Boolean> checker) {
        if (lastComponent != null) {
            if (lastComponent instanceof RequirementItem reqItem) {
                reqItem.setItemChecker((controller, stack) -> ClosureHelper.call(true, checker, controller, stack));
            } else {
                GroovyLog.get().warn("setNBTChecker(AdvancedItemNBTChecker checker) only can be applied to `Item`!");
            }
        } else {
            GroovyLog.get().warn("setNBTChecker(AdvancedItemNBTChecker checker) only can be applied to `Item`!");
        }
        return this;
    }


    public GroovyRecipe itemModifier(Closure<ItemStack> modifier) {
        if (lastComponent != null) {
            if (lastComponent instanceof RequirementItem reqItem) {
                reqItem.addItemModifier((controller, stack) -> ClosureHelper.call(stack, modifier, controller, stack));
            } else {
                GroovyLog.get().warn("addItemModifier(AdvancedItemModifier checker) only can be applied to `Item`!");
            }
        } else {
            GroovyLog.get().warn("addItemModifier(AdvancedItemModifier checker) only can be applied to `Item`!");
        }
        return this;
    }


    public GroovyRecipe amount(int min, int max) {
        if (lastComponent != null) {
            if (lastComponent instanceof RequirementItem reqItem) {
                if (min < max) {
                    reqItem.minAmount = min;
                    reqItem.maxAmount = max;
                } else {
                    GroovyLog.get().warn("`min` cannot larger than `max`!");
                }
            } else {
                GroovyLog.get().warn("setMinMaxOutputAmount(int min, int max) only can be applied to `Item`!");
            }
        } else {
            GroovyLog.get().warn("setMinMaxOutputAmount(int min, int max) only can be applied to `Item`!");
        }
        return this;
    }

    /**
     * <p>为某个输入或输出设置特定触发时间。</p>
     * <p>注意：如果设置了触发时间，则配方在其他时间不会触发任何消耗或产出动作。</p>
     *
     * @param tickTime 触发的配方时间，实际触发时间受到配方修改器影响
     */

    public GroovyRecipe triggerTime(int tickTime) {
        if (lastComponent != null) {
            lastComponent.setTriggerTime(tickTime);
        }
        return this;
    }

    /**
     * 使触发时间可以被重复触发。
     *
     * @param repeatable true 为可重复，默认 false。
     */

    public GroovyRecipe triggerRepeatable(boolean repeatable) {
        if (lastComponent != null) {
            lastComponent.setTriggerRepeatable(repeatable);
        }
        return this;
    }

    /**
     * <p>使一个物品/流体等需求忽略输出检测，对一些大量输出不同种类物品等需求非常有用。</p>
     * <p>警告：如果忽略输出则有时可能会导致输出吞物品行为。</p>
     *
     * @param ignoreOutputCheck true 为忽略，默认为 false 不忽略。
     */

    public GroovyRecipe ignoreOutputCheck(boolean ignoreOutputCheck) {
        if (lastComponent != null) {
            lastComponent.setIgnoreOutputCheck(ignoreOutputCheck);
        }
        return this;
    }


    public GroovyRecipe recipeTooltip(String... tooltips) {
        toolTipList.addAll(Arrays.asList(tooltips));
        return this;
    }


    public GroovyRecipe smartInterfaceDataInput(String typeStr, float minValue, float maxValue) {
        needAfterInitActions.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(machineName);
            if (machine == null) {
                GroovyLog.get().error("Could not find machine `" + machineName.toString() + "`!");
                return;
            }
            SmartInterfaceType type = machine.getSmartInterfaceType(typeStr);
            if (type == null) {
                GroovyLog.get().error("SmartInterfaceType " + typeStr + " Not Found!");
                return;
            }
            appendComponent(new RequirementInterfaceNumInput(type, minValue, maxValue));
        });
        return this;
    }


    public GroovyRecipe smartInterfaceDataInput(String typeStr, float value) {
        return smartInterfaceDataInput(typeStr, value, value);
    }

    /**
     * 设置此配方在工厂中同时运行的数量是否不超过指定数值。
     */

    public GroovyRecipe maxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    /**
     * 设置此配方只能被指定的核心线程执行。
     *
     * @param name 线程名
     */
    public GroovyRecipe threadName(String name) {
        this.threadName = name == null ? "" : name;
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // EventHandlers
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe preCheckHandler(Closure<?> handler) {
        addRecipeEventHandler(RecipeCheckEvent.class, event -> {
            if (event.phase != Phase.START) return;
            ClosureHelper.call(handler, event);
        });
        return this;
    }


    public GroovyRecipe postCheckHandler(Closure<?> handler) {
        addRecipeEventHandler(RecipeCheckEvent.class, event -> {
            if (event.phase != Phase.END) return;
            ClosureHelper.call(handler, event);
        });
        return this;
    }

    public GroovyRecipe startHandler(Closure<?> handler) {
        addRecipeEventHandler(RecipeStartEvent.class, IEventHandler.of(handler));
        return this;
    }

    public GroovyRecipe preTickHandler(Closure<?> handler) {
        addRecipeEventHandler(RecipeTickEvent.class, event -> {
            if (event.phase != Phase.START) return;
            ClosureHelper.call(handler, event);
        });
        return this;
    }

    public GroovyRecipe postTickHandler(Closure<?> handler) {
        addRecipeEventHandler(RecipeTickEvent.class, event -> {
            if (event.phase != Phase.END) return;
            ClosureHelper.call(handler, event);
        });
        return this;
    }

    public GroovyRecipe failureHandler(Closure<?> handler) {
        addRecipeEventHandler(RecipeFailureEvent.class, IEventHandler.of(handler));
        return this;
    }

    public GroovyRecipe finishHandler(Closure<?> handler) {
        addRecipeEventHandler(RecipeFinishEvent.class, IEventHandler.of(handler));
        return this;
    }

    public GroovyRecipe factoryStartHandler(Closure<?> handler) {
        addRecipeEventHandler(FactoryRecipeStartEvent.class, IEventHandler.of(handler));
        return this;
    }

    public GroovyRecipe factoryPreTickHandler(Closure<?> handler) {
        addRecipeEventHandler(FactoryRecipeTickEvent.class, event -> {
            if (event.phase != Phase.START) {
                return;
            }
            ClosureHelper.call(handler, event);
        });
        return this;
    }

    public GroovyRecipe factoryPostTickHandler(Closure<?> handler) {
        addRecipeEventHandler(FactoryRecipeTickEvent.class, event -> {
            if (event.phase != Phase.END) {
                return;
            }
            ClosureHelper.call(handler, event);
        });
        return this;
    }


    public GroovyRecipe factoryFailureHandler(Closure<?> handler) {
        return eventHandler(FactoryRecipeFailureEvent.class, handler);
    }

    public GroovyRecipe factoryFinishHandler(Closure<?> handler) {
        return eventHandler(FactoryRecipeFinishEvent.class, handler);
    }

    public <H extends RecipeEvent> GroovyRecipe eventHandler(Closure<?> handler) {
        if (handler.getParameterTypes().length > 0 && RecipeEvent.class.isAssignableFrom(handler.getParameterTypes()[0])) {
            Class<H> eventClass = (Class<H>) handler.getParameterTypes()[0];
            addRecipeEventHandler(eventClass, IEventHandler.of(handler));
        } else {
            GroovyLog.get().error("The parameter type must be explicitly declared when using `eventHandler({})`");
        }
        return this;
    }

    public <H extends RecipeEvent> GroovyRecipe eventHandler(Class<H> clazz, Closure<?> handler) {
        addRecipeEventHandler(clazz, IEventHandler.of(handler));
        return this;
    }

    @SuppressWarnings("unchecked")
    private <H extends RecipeEvent> void addRecipeEventHandler(Class<H> hClass, IEventHandler<H> handler) {
        recipeEventHandlers.computeIfAbsent(hClass, k -> new ArrayList<>()).add((IEventHandler<RecipeEvent>) handler);
    }

    //----------------------------------------------------------------------------------------------
    // General Input & Output
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe input(IIngredient input) {
        if (IngredientHelper.isItem(input) || input instanceof OreDictIngredient) {
            itemInput(input);
        } else if (input instanceof FluidStack liquidStack) {
            fluidInput(liquidStack);
        } else if (Mods.MEKANISM.isPresent() && input instanceof GasStack gasStack) {
            gasInput(gasStack);
        } else {
            GroovyLog.get().error("Invalid input type {}({})! Ignored.", input, input.getClass());
        }
        return this;
    }

    public GroovyRecipe input(IIngredient... inputs) {
        for (IIngredient input : inputs) {
            input(input);
        }
        return this;
    }

    public GroovyRecipe input(Iterable<IIngredient> inputs) {
        for (IIngredient input : inputs) {
            input(input);
        }
        return this;
    }

    public GroovyRecipe output(IIngredient output) {
        if (IngredientHelper.isItem(output) || output instanceof OreDictIngredient) {
            itemOutput(output);
        } else if (output instanceof FluidStack liquidStack) {
            fluidOutput(liquidStack);
        } else if (Mods.MEKANISM.isPresent() && output instanceof GasStack gasStack) {
            gasOutput(gasStack);
        } else {
            GroovyLog.get().error("Invalid output type {}({})! Ignored.", output, output.getClass());
        }
        return this;
    }

    public GroovyRecipe output(IIngredient... outputs) {
        for (IIngredient output : outputs) {
            output(output);
        }
        return this;
    }

    public GroovyRecipe output(Iterable<IIngredient> outputs) {
        for (IIngredient output : outputs) {
            output(output);
        }
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // Energy input & output
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe energyInput(long perTick) {
        requireEnergy(IOType.INPUT, perTick);
        return this;
    }


    public GroovyRecipe energyOutput(long perTick) {
        requireEnergy(IOType.OUTPUT, perTick);
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // FLUID input & output
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe fluidInput(FluidStack fluid, boolean perTick) {
        requireFluid(IOType.INPUT, fluid, perTick);
        return this;
    }

    public GroovyRecipe fluidInput(FluidStack fluid) {
        return fluidInput(fluid, false);
    }

    public GroovyRecipe fluidInput(FluidStack... fluids) {
        for (FluidStack fluid : fluids) {
            fluidInput(fluid);
        }
        return this;
    }

    public GroovyRecipe fluidInput(Iterable<FluidStack> fluids, boolean perTick) {
        for (FluidStack fluid : fluids) {
            fluidInput(fluid, perTick);
        }
        return this;
    }

    public GroovyRecipe fluidInput(Iterable<FluidStack> fluids) {
        return fluidInput(fluids, false);
    }

    public GroovyRecipe fluidOutput(FluidStack fluid) {
        return fluidOutput(fluid, false);
    }

    public GroovyRecipe fluidOutput(FluidStack fluid, boolean perTick) {
        requireFluid(IOType.OUTPUT, fluid, perTick);
        return this;
    }


    public GroovyRecipe fluidOutputs(FluidStack... fluids) {
        for (FluidStack fluid : fluids) {
            fluidOutput(fluid);
        }
        return this;
    }

    public GroovyRecipe fluidOutputs(Iterable<FluidStack> fluids, boolean perTick) {
        for (FluidStack fluid : fluids) {
            fluidOutput(fluid, perTick);
        }
        return this;
    }

    public GroovyRecipe fluidOutputs(Iterable<FluidStack> fluids) {
        return fluidOutputs(fluids, false);
    }

    //----------------------------------------------------------------------------------------------
    // GAS input & output
    //----------------------------------------------------------------------------------------------

    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasInput(GasStack gasStack, boolean perTick) {
        requireGas(IOType.INPUT, gasStack, perTick);
        return this;
    }


    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasOutput(GasStack gasStack, boolean perTick) {
        requireGas(IOType.OUTPUT, gasStack, perTick);
        return this;
    }

    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasInput(GasStack gasStack) {
        return gasInput(gasStack, false);
    }


    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasOutput(GasStack gasStack) {
        return gasOutput(gasStack, false);
    }


    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasInput(GasStack... gasStacks) {
        for (final GasStack gasStack : gasStacks) {
            requireGas(IOType.INPUT, gasStack, false);
        }
        return this;
    }

    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasInput(Iterable<GasStack> gasStacks) {
        for (final GasStack gasStack : gasStacks) {
            requireGas(IOType.INPUT, gasStack, false);
        }
        return this;
    }


    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasOutput(GasStack... gasStacks) {
        for (final GasStack gasStack : gasStacks) {
            requireGas(IOType.OUTPUT, gasStack, false);
        }
        return this;
    }

    @Optional.Method(modid = "mekanism")
    public GroovyRecipe gasOutput(Iterable<GasStack> gasStacks) {
        for (final GasStack gasStack : gasStacks) {
            requireGas(IOType.OUTPUT, gasStack, false);
        }
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // ITEM input
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe itemInput(IIngredient input) {
        if (IngredientHelper.isItem(input)) {
            requireFuel(IOType.INPUT, IngredientHelper.toItemStack(input));
        } else if (input instanceof OreDictIngredient oreDictIngredient) {
            requireFuel(IOType.INPUT, oreDictIngredient.getOreDict(), oreDictIngredient.getAmount());
        } else {
            GroovyLog.get().error("Invalid input type {}({})! Only ItemStack and OreDictIngredient is allowed.", input, input.getClass());
        }

        return this;
    }

    public GroovyRecipe itemInputs(IIngredient... inputs) {
        for (IIngredient input : inputs) {
            itemInput(input);
        }
        return this;
    }

    public GroovyRecipe itemInputs(Iterable<IIngredient> inputs) {
        for (IIngredient input : inputs) {
            itemInput(input);
        }
        return this;
    }

    public GroovyRecipe fuelBurnTime(int requiredTotalBurnTime) {
        requireFuel(requiredTotalBurnTime);
        return this;
    }

    // TODO
    public GroovyRecipe addIngredientArrayInput(IngredientArrayPrimer ingredientArrayPrimer) {
        appendComponent(new RequirementIngredientArray(ingredientArrayPrimer.getIngredientStackList()));
        return this;
    }

    /**
     * <p>随机选择给定的一组物品中的其中一个输出。</p>
     * <p>虽然都使用 IngredientArrayPrimer 作为参数，但是输出的工作机制要<strong>稍有不同</strong>。</p>
     * <p>每个 Ingredient 的 chance 代表整个物品列表的随机选择权重，权重越高，输出概率越大。</p>
     * <p>同样，setMinMaxAmount() 也能够起作用。</p>
     */
    // TODO
    public GroovyRecipe addRandomItemOutput(IngredientArrayPrimer ingredientArrayPrimer) {
        appendComponent(new RequirementIngredientArray(ingredientArrayPrimer.getIngredientStackList(), IOType.OUTPUT));
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // ITEM output
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe itemOutput(IIngredient output) {
        if (IngredientHelper.isItem(output)) {
            requireFuel(IOType.OUTPUT, IngredientHelper.toItemStack(output));
        } else if (output instanceof OreDictIngredient oreDictIngredient) {
            requireFuel(IOType.OUTPUT, oreDictIngredient.getOreDict(), oreDictIngredient.getAmount());
        } else {
            GroovyLog.get().error("Invalid output type {}({})! Ignored.", output, output.getClass());
        }

        return this;
    }

    public GroovyRecipe itemOutputs(IIngredient... inputs) {
        for (IIngredient input : inputs) {
            itemOutput(input);
        }
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // Catalyst
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe catalyst(IIngredient input, Iterable<String> tooltips, Iterable<RecipeModifier> modifiers) {
        if (IngredientHelper.isItem(input)) {
            requireCatalyst(IngredientHelper.toItemStack(input), tooltips, modifiers);
        } else if (input instanceof OreDictIngredient oreDictIngredient) {
            requireCatalyst(oreDictIngredient.getOreDict(), oreDictIngredient.getAmount(), tooltips, modifiers);
        } else {
            GroovyLog.get().error("Invalid input type {}({})! Ignored.", input, input.getClass());
        }

        return this;
    }


    // TODO
    public GroovyRecipe addCatalystInput(IngredientArrayPrimer input, Iterable<String> tooltips, Iterable<RecipeModifier> modifiers) {
        requireCatalyst(input, tooltips, modifiers);
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // Internals
    //----------------------------------------------------------------------------------------------
    private void requireEnergy(IOType ioType, long perTick) {
        appendComponent(new RequirementEnergy(ioType, perTick));
    }

    private void requireFluid(IOType ioType, FluidStack stack, boolean isPerTick) {
        if (IngredientHelper.isEmpty(stack) || stack.getFluid() == null) {
            GroovyLog.get().error("FluidStack must not be empty");
            return;
        }

        if (isPerTick) {
            appendComponent(new RequirementFluidPerTick(ioType, stack));
        } else {
            appendComponent(new RequirementFluid(ioType, stack));
        }
    }

    @Optional.Method(modid = "mekanism")
    private void requireGas(IOType ioType, GasStack gasStack, boolean isPerTick) {
        if (gasStack == null || gasStack.amount <= 0 || gasStack.getGas() == null) {
            GroovyLog.get().error("GasStack must not be empty");
            return;
        }
        if (isPerTick) {
            appendComponent(new RequirementGasPerTick(ioType, gasStack));
        } else {
            appendComponent(new RequirementGas(ioType, gasStack));
        }
    }

    private void requireFuel(int requiredTotalBurnTime) {
        appendComponent(new RequirementItem(IOType.INPUT, requiredTotalBurnTime));
    }

    private void requireFuel(IOType ioType, ItemStack stack) {
        if (stack.isEmpty()) {
            GroovyLog.get().error("ItemStack not found/unknown item: " + stack);
            return;
        }
        RequirementItem ri = new RequirementItem(ioType, stack);
        if (stack.hasTagCompound()) {
            ri.tag = stack.getTagCompound();
            ri.previewDisplayTag = stack.getTagCompound();
        }
        appendComponent(ri);
    }

    private void requireFuel(IOType ioType, String oreDictName, int amount) {
        appendComponent(new RequirementItem(ioType, oreDictName, amount));
    }

    private void requireCatalyst(String oreDictName, int amount, Iterable<String> tooltips, Iterable<RecipeModifier> modifiers) {
        RequirementCatalyst catalyst = new RequirementCatalyst(oreDictName, amount);
        for (String tooltip : tooltips) {
            catalyst.addTooltip(tooltip);
        }
        for (RecipeModifier modifier : modifiers) {
            if (modifier != null) {
                catalyst.addModifier(modifier);
            }
        }
        appendComponent(catalyst);
    }

    private void requireCatalyst(ItemStack stack, Iterable<String> tooltips, Iterable<RecipeModifier> modifiers) {
        if (stack.isEmpty()) {
            GroovyLog.get().error("ItemStack not found/unknown item: " + stack);
            return;
        }
        RequirementCatalyst catalyst = new RequirementCatalyst(stack);
        for (String tooltip : tooltips) {
            catalyst.addTooltip(tooltip);
        }
        for (RecipeModifier modifier : modifiers) {
            if (modifier != null) {
                catalyst.addModifier(modifier);
            }
        }
        appendComponent(catalyst);
    }

    private void requireCatalyst(IngredientArrayPrimer ingredientArrayPrimer, Iterable<String> tooltips,
                                 Iterable<RecipeModifier> modifiers) {
        RequirementCatalyst catalyst = new RequirementCatalyst(ingredientArrayPrimer.getIngredientStackList());
        for (String tooltip : tooltips) {
            catalyst.addTooltip(tooltip);
        }
        for (RecipeModifier modifier : modifiers) {
            if (modifier != null) {
                catalyst.addModifier(modifier);
            }
        }
        appendComponent(catalyst);
    }

    public void appendComponent(ComponentRequirement<?, ?> component) {
        this.components.add(component);
        this.lastComponent = component;
    }

    //----------------------------------------------------------------------------------------------
    // magic
    //----------------------------------------------------------------------------------------------

    public GroovyRecipe aspectInput(String aspectString, int amount) {
        Aspect aspect = Aspect.getAspect(aspectString);
        if (aspect != null) {
            appendComponent(new RequirementAspect(IOType.INPUT, amount, aspect));
        } else {
            GroovyLog.get().error("Invalid aspect name : " + aspectString);
        }

        return this;
    }

    public GroovyRecipe aspectOutput(String aspectString, int amount) {
        Aspect aspect = Aspect.getAspect(aspectString);
        if (aspect != null) {
            appendComponent(new RequirementAspect(IOType.OUTPUT, amount, aspect));
        } else {
            GroovyLog.get().error("Invalid aspect name : " + aspectString);
        }

        return this;
    }

    public GroovyRecipe auraInput(String auraType, int amount) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            appendComponent(new RequirementAura(IOType.INPUT, new Aura(amount, aura), Integer.MAX_VALUE, Integer.MIN_VALUE));
        } else {
            GroovyLog.get().error("Invalid aura name : " + auraType);
        }

        return this;
    }

    public GroovyRecipe auraOutput(String auraType, int amount) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            appendComponent(new RequirementAura(IOType.OUTPUT, new Aura(amount, aura), Integer.MAX_VALUE, Integer.MIN_VALUE));
        } else {
            GroovyLog.get().error("Invalid aura name : " + auraType);
        }

        return this;
    }

    public GroovyRecipe auraInput(String auraType, int amount, int max, int min) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            appendComponent(new RequirementAura(IOType.INPUT, new Aura(amount, aura), max, min));
        } else {
            GroovyLog.get().error("Invalid aura name : " + auraType);
        }

        return this;
    }

    public GroovyRecipe auraOutput(String auraType, int amount, int max, int min) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            appendComponent(new RequirementAura(IOType.OUTPUT, new Aura(amount, aura), max, min));
        } else {
            GroovyLog.get().error("Invalid aura name : " + auraType);
        }

        return this;
    }

    public GroovyRecipe constellationInput(String constellationString) {
        IConstellation constellation = ConstellationRegistry.getConstellationByName("astralsorcery.constellation." + constellationString);
        if (constellation != null) {
            appendComponent(new RequirementConstellation(IOType.INPUT, constellation));
        } else {
            GroovyLog.get().error("Invalid constellation : " + constellationString);
        }

        return this;
    }

    public GroovyRecipe gridPowerInput(int amount) {
        if (amount > 0) {
            appendComponent(new RequirementGrid(IOType.INPUT, amount));
        } else {
            GroovyLog.get().error("Invalid Grid Power amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe gridPowerOutput(int amount) {
        if (amount > 0) {
            appendComponent(new RequirementGrid(IOType.OUTPUT, amount));
        } else {
            GroovyLog.get().error("Invalid Grid Power amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe rainbowInput(RecipePrimer primer) {
        appendComponent(new RequirementRainbow());
        return this;
    }

    public GroovyRecipe lifeEssenceInput(int amount, boolean perTick) {
        if (amount > 0) {
            appendComponent(new RequirementLifeEssence(IOType.INPUT, amount, perTick));
        } else {
            GroovyLog.get().error("Invalid Life Essence amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe lifeEssenceOutput(int amount, boolean perTick) {
        if (amount > 0) {
            appendComponent(new RequirementLifeEssence(IOType.OUTPUT, amount, perTick));
        } else {
            GroovyLog.get().error("Invalid Life Essence amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe starlightInput(float amount) {
        if (amount > 0) {
            appendComponent(new RequirementStarlight(IOType.INPUT, amount));
        } else {
            GroovyLog.get().error("Invalid Starlight amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe starlightOutput(float amount) {
        if (amount > 0) {
            appendComponent(new RequirementStarlight(IOType.OUTPUT, amount));
        } else {
            GroovyLog.get().error("Invalid Starlight amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe willInput(String willTypeString, int amount) {
        EnumDemonWillType willType = EnumHelper.valueOfNullable(EnumDemonWillType.class, willTypeString, false);
        if (willType != null) {
            appendComponent(new RequirementWill(IOType.INPUT, amount, willType, Integer.MIN_VALUE, Integer.MAX_VALUE));
        } else {
            GroovyLog.get().error("Invalid demon will type : " + willTypeString);
        }

        return this;
    }

    public GroovyRecipe willOutput(String willTypeString, int amount) {
        EnumDemonWillType willType = EnumHelper.valueOfNullable(EnumDemonWillType.class, willTypeString, false);
        if (willType != null) {
            appendComponent(new RequirementWill(IOType.OUTPUT, amount, willType, Integer.MIN_VALUE, Integer.MAX_VALUE));
        } else {
            GroovyLog.get().error("Invalid demon will type : " + willTypeString);
        }

        return this;
    }

    public GroovyRecipe willInput(String willTypeString, int amount, int min, int max) {
        EnumDemonWillType willType = EnumHelper.valueOfNullable(EnumDemonWillType.class, willTypeString, false);
        if (willType != null) {
            appendComponent(new RequirementWill(IOType.INPUT, amount, willType, min, max));
        } else {
            GroovyLog.get().error("Invalid demon will type : " + willTypeString);
        }

        return this;
    }

    public GroovyRecipe willOutput(String willTypeString, int amount, int min, int max) {
        EnumDemonWillType willType = EnumHelper.valueOfNullable(EnumDemonWillType.class, willTypeString, false);
        if (willType != null) {
            appendComponent(new RequirementWill(IOType.OUTPUT, amount, willType, min, max));
        } else {
            GroovyLog.get().error("Invalid demon will type : " + willTypeString);
        }

        return this;
    }

    public GroovyRecipe manaInput(int amount, boolean perTick) {
        if (amount > 0) {
            appendComponent(new RequirementMana(IOType.INPUT, amount, perTick));
        } else {
            GroovyLog.get().error("Invalid Mana amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe manaOutput(int amount, boolean perTick) {
        if (amount > 0) {
            appendComponent(new RequirementMana(IOType.OUTPUT, amount, perTick));
        } else {
            GroovyLog.get().error("Invalid Mana amount : " + amount + " (need to be positive and not null)");
        }

        return this;
    }

    public GroovyRecipe impetusInput(int amount) {
        appendComponent(new RequirementImpetus(IOType.INPUT, amount));
        return this;
    }

    public GroovyRecipe impetusOutput(int amount) {
        appendComponent(new RequirementImpetus(IOType.OUTPUT, amount));
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // build
    //----------------------------------------------------------------------------------------------

    private boolean validate() {
        GroovyLog.Msg msg = GroovyLog.msg("Error adding {} recipe", this.name).error();
        if (this.name == null) this.name = RecipeName.generate();
        if (this.tickTime <= 0) this.tickTime = 100;

        return !msg.postIfNotEmpty();
    }

    public void register() {
        if (validate()) {
            RecipeRegistry.getRegistry().registerRecipeEarly(this);
        }
    }

    //----------------------------------------------------------------------------------------------
    // lingering stats
    //----------------------------------------------------------------------------------------------

    @Override
    public String getFilePath() {
        return "";
    }

    @Override
    public ResourceLocation getRecipeRegistryName() {
        return new ResourceLocation(ModularMachinery.MODID, this.name);
    }

    @Override
    public ResourceLocation getAssociatedMachineName() {
        return machineName;
    }

    @Override
    public ResourceLocation getParentMachineName() {
        return machineName;
    }

    @Override
    public int getTotalProcessingTickTime() {
        return tickTime;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean voidPerTickFailure() {
        return doesVoidPerTick;
    }

    @Override
    public List<ComponentRequirement<?, ?>> getComponents() {
        return components;
    }

    @Override
    public Map<Class<?>, List<IEventHandler<RecipeEvent>>> getRecipeEventHandlers() {
        return recipeEventHandlers;
    }

    @Override
    public List<String> getTooltipList() {
        return toolTipList;
    }

    @Override
    public boolean isParallelized() {
        return parallelized;
    }

    @Override
    public int getMaxThreads() {
        return maxThreads;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    public void loadNeedAfterInitActions() {
        for (Action needAfterInitAction : needAfterInitActions) {
            needAfterInitAction.doAction();
        }
    }
}

