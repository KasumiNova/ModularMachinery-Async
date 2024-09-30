package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;
import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelAnimationEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelGetEvent;
import github.kasuminova.mmce.common.event.machine.*;
import groovy.lang.Closure;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.RecipeFailureActions;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.modifier.MultiBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

public class GroovyMachineBuilder {

    protected final DynamicMachine machine;

    public GroovyMachineBuilder(DynamicMachine machine) {
        this.machine = machine;
        color(0xFFFFFF); // config is read after grs preInit
    }

    /**
     * 设置此机械是否受并行控制器影响。
     */
    public GroovyMachineBuilder parallelizable(boolean isParallelizable) {
        machine.setParallelizable(isParallelizable);
        return this;
    }

    /**
     * 设置此机械的最大并行数。
     *
     * @param maxParallelism 并行数
     */
    public GroovyMachineBuilder maxParallelism(int maxParallelism) {
        machine.setMaxParallelism(maxParallelism);
        return this;
    }

    /**
     * 设置此机械的内置并行数。
     *
     * @param parallelism 内置并行数
     */
    public GroovyMachineBuilder internalParallelism(int parallelism) {
        machine.setInternalParallelism(parallelism);
        return this;
    }

    /**
     * 添加多方块升级配方修改器。
     */
    public GroovyMachineBuilder multiBlockModifier(MultiBlockModifierReplacement multiBlockModifier) {
        if (multiBlockModifier != null) {
            machine.getMultiBlockModifiers().add(multiBlockModifier);
        }
        return this;
    }

    /**
     * 添加智能数据接口类型。
     *
     * @param type 类型
     */
    public GroovyMachineBuilder smartInterfaceType(SmartInterfaceType type) {
        if (!machine.hasSmartInterfaceType(type.getType())) {
            machine.addSmartInterfaceType(type);
        } else {
            GroovyLog.get().warn("DynamicMachine `" + machine.getRegistryName() + "` is already has SmartInterfaceType `" + type.getType() + "`!");
        }
        return this;
    }

    /**
     * 添加结构形成事件监听器。
     */
    public GroovyMachineBuilder structureFormedHandler(Closure<?> function) {
        machine.addMachineEventHandler(MachineStructureFormedEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加结构更新事件监听器。
     */
    public GroovyMachineBuilder structureUpdateHandler(Closure<?> function) {
        machine.addMachineEventHandler(MachineStructureUpdateEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加机器事件监听器。
     */
    public GroovyMachineBuilder tickHandler(Closure<?> function) {
        machine.addMachineEventHandler(MachineTickEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加控制器 GUI 渲染事件监听器。
     */
    public GroovyMachineBuilder guiRenderHandler(Closure<?> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerGUIRenderEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加控制器 GeckoLib 模型动画事件监听器。
     */
    @Optional.Method(modid = "geckolib3")
    public GroovyMachineBuilder controllerModelAnimationHandler(Closure<?> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerModelAnimationEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加控制器 GeckoLib 模型获取事件监听器。
     */
    @Optional.Method(modid = "geckolib3")
    public GroovyMachineBuilder controllerModelGetHandler(Closure<?> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerModelGetEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加智能数据接口更新事件监听器
     */
    public GroovyMachineBuilder smartInterfaceUpdateHandler(Closure<?> function) {
        machine.addMachineEventHandler(SmartInterfaceUpdateEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 控制器是否需要蓝图
     */
    public GroovyMachineBuilder requiresBlueprint(boolean requiresBlueprint) {
        this.machine.setRequiresBlueprint(requiresBlueprint);
        return this;
    }

    /**
     * 设置当此机器配方运行失败时的操作
     *
     * @param failureAction Action 可以通过 RecipeFailureActions.getFailureAction(String key) 获得
     */
    public GroovyMachineBuilder failureAction(RecipeFailureActions failureAction) {
        this.machine.setFailureAction(failureAction);
        return this;
    }

    /**
     * 设置机械颜色，该结构内的其他组件也将会变为此颜色。
     *
     * @param color 颜色，例如：0xFFFFFF
     */
    public GroovyMachineBuilder color(int color) {
        this.machine.setDefinedColor(color);
        return this;
    }

    /**
     * 设置此机械是否有工厂形式的控制器。
     *
     * @param hasFactory true 即为注册，false 即为不注册
     */
    public GroovyMachineBuilder hasFactory(boolean hasFactory) {
        this.machine.setHasFactory(hasFactory);
        return this;
    }

    /**
     * 设置此机械是否仅有工厂形式的控制器。
     *
     * @param factoryOnly true 即为仅工厂，false 即为普通机械和工厂
     */
    public GroovyMachineBuilder factoryOnly(boolean factoryOnly) {
        this.machine.setFactoryOnly(factoryOnly);
        return this;
    }

    /**
     * 设置此机械的工厂最大线程数。 / Sets the maximum number of threads for this machine.
     *
     * @param maxThreads 最大线程数 / Maximum number of threads
     */
    public GroovyMachineBuilder maxThreads(int maxThreads) {
        this.machine.setMaxThreads(maxThreads);
        return this;
    }

    public GroovyMachineBuilder coreThread(FactoryRecipeThread thread) {
        this.machine.addCoreThread(thread);
        return this;
    }

    public DynamicMachine getMachine() {
        return machine;
    }
}
