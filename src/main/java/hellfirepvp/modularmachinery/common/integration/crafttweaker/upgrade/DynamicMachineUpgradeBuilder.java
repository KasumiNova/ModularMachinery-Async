package hellfirepvp.modularmachinery.common.integration.crafttweaker.upgrade;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import github.kasuminova.mmce.common.event.machine.*;
import github.kasuminova.mmce.common.event.recipe.*;
import github.kasuminova.mmce.common.integration.Logger;
import github.kasuminova.mmce.common.upgrade.SimpleDynamicMachineUpgrade;
import github.kasuminova.mmce.common.upgrade.UpgradeType;
import github.kasuminova.mmce.common.upgrade.registry.RegistryUpgrade;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineModifier;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.IFunction;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.UpgradeEventHandlerCT;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.modularmachinery.DynamicMachineUpgradeBuilder")
public class DynamicMachineUpgradeBuilder {
    private final SimpleDynamicMachineUpgrade machineUpgrade;

    public DynamicMachineUpgradeBuilder(final SimpleDynamicMachineUpgrade machineUpgrade) {
        this.machineUpgrade = machineUpgrade;
    }

    /**
     * 创建一个新的机械升级构建器。
     * 动态升级和普通升级的区别是，物品也可以添加自定义 NBT（实验性）。
     *
     * @param name          升级名称，必须是唯一的
     * @param localizedName 译名，可以是翻译键
     * @param level         等级（暂无作用）
     * @param maxStack      最大堆叠数
     * @return 构建器
     */
    @ZenMethod
    public static DynamicMachineUpgradeBuilder newBuilder(String name, String localizedName, float level, int maxStack) {
        if (RegistryUpgrade.getUpgrade(name) != null) {
            Logger.error("[ModularMachinery] Already registered SimpleMachineUpgrade " + name + '!');
            return null;
        }
        return new DynamicMachineUpgradeBuilder(new SimpleDynamicMachineUpgrade(new UpgradeType(name, localizedName, level, maxStack)));
    }

    /**
     * 为升级添加动态工具提示，会在添加了升级的物品上显示。
     *
     * @param handler 动态参数回调，相当于事件
     */
    @ZenMethod
    public DynamicMachineUpgradeBuilder setDescriptionHandler(IFunction<SimpleDynamicMachineUpgrade, String[]> handler) {
        machineUpgrade.setDescriptionHandler(handler);
        return this;
    }

    /**
     * 为升级添加升级总线提示，会在升级总线的右方显示。
     *
     * @param handler 动态参数回调，相当于事件
     */
    @ZenMethod
    public DynamicMachineUpgradeBuilder setBusGUIDescriptionHandler(IFunction<SimpleDynamicMachineUpgrade, String[]> handler) {
        machineUpgrade.setBusGUIDescriptionHandler(handler);
        return this;
    }

    /**
     * 为升级添加白名单机械，与 addIncompatibleMachines 冲突。
     *
     * @param machineNames 机械名称
     */
    @ZenMethod
    public DynamicMachineUpgradeBuilder addCompatibleMachines(String... machineNames) {
        MachineModifier.WAIT_FOR_MODIFY.add(() -> {
            for (final String machineName : machineNames) {
                DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
                if (machine == null) {
                    Logger.error("Cloud not found machine " + machineName);
                    continue;
                }
                machineUpgrade.getType().addCompatibleMachine(machine);
            }
        });
        return this;
    }

    /**
     * 为升级添加黑名单机械，与 addCompatibleMachines 冲突
     *
     * @param machineNames 机械名称
     */
    @ZenMethod
    public DynamicMachineUpgradeBuilder addIncompatibleMachines(String... machineNames) {
        MachineModifier.WAIT_FOR_MODIFY.add(() -> {
            for (final String machineName : machineNames) {
                DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
                if (machine == null) {
                    Logger.error("Cloud not found machine " + machineName);
                    continue;
                }
                machineUpgrade.getType().addIncompatibleMachine(machine);
            }
        });
        return this;
    }

    //--------------------------------------------------------
    // 以下方法均为监听机械的事件。
    // 它们将会在配方中添加的事件执行前执行。
    // 注意：MachineEvent 和 MachineUpgrade 需要使用特殊方法才能转换为特定事件，
    // 详情请参考 MMEvents 和 MachineUpgradeHelper。
    //--------------------------------------------------------

    @Deprecated
    @ZenMethod
    public DynamicMachineUpgradeBuilder addRecipeCheckHandler(UpgradeEventHandlerCT handler) {
        Logger.warn("Deprecated method addRecipeCheckHandler()! Consider using addPostRecipeCheckHandler()");
        addEventHandler(RecipeCheckEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addPreRecipeCheckHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(RecipeCheckEvent.class, (event, upgrade) -> {
            if (((RecipeCheckEvent) event).phase != Phase.START) return;
            handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addPostRecipeCheckHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(RecipeCheckEvent.class, (event, upgrade) -> {
            if (((RecipeCheckEvent) event).phase != Phase.END) return;
            handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addRecipeStartHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(RecipeStartEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addRecipePreTickHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(RecipeTickEvent.class, (event, upgrade) -> {
            if (((RecipeTickEvent) event).phase == Phase.START) handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addRecipePostTickHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(RecipeTickEvent.class, (event, upgrade) -> {
            if (((RecipeTickEvent) event).phase == Phase.END) handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    @Deprecated
    public DynamicMachineUpgradeBuilder addRecipeTickHandler(UpgradeEventHandlerCT handler) {
        Logger.warn("Deprecated method addTickHandler()! Consider using addPostTickHandler()");
        return addRecipePostTickHandler(handler);
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addRecipeFailureHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(RecipeFailureEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addRecipeFinishHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(RecipeFinishEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addFactoryRecipeStartHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(FactoryRecipeStartEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addFactoryRecipePreTickHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(FactoryRecipeTickEvent.class, (event, upgrade) -> {
            if (((FactoryRecipeTickEvent) event).phase == Phase.START) handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addFactoryRecipePostTickHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(FactoryRecipeTickEvent.class, (event, upgrade) -> {
            if (((FactoryRecipeTickEvent) event).phase == Phase.END) handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addFactoryRecipeFailureHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(FactoryRecipeFailureEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addFactoryRecipeFinishHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(FactoryRecipeFinishEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addMachinePreTickHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(MachineTickEvent.class, (event, upgrade) -> {
            if (((MachineTickEvent) event).phase == Phase.START) handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addMachinePostTickHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(MachineTickEvent.class, (event, upgrade) -> {
            if (((MachineTickEvent) event).phase == Phase.END) handler.handle(event, upgrade);
        });
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addStructureFormedHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(MachineStructureFormedEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addStructureUpdateHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(MachineStructureUpdateEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addControllerGUIRenderHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(ControllerGUIRenderEvent.class, handler);
        return this;
    }

    @ZenMethod
    public DynamicMachineUpgradeBuilder addSmartInterfaceUpdateHandler(UpgradeEventHandlerCT handler) {
        addEventHandler(SmartInterfaceUpdateEvent.class, handler);
        return this;
    }

    @ZenMethod
    public void buildAndRegister() {
        RegistryUpgrade.registerUpgrade(machineUpgrade.getType().getName(), machineUpgrade);
    }

    private <E extends MachineEvent> void addEventHandler(Class<E> eventClass, UpgradeEventHandlerCT handler) {
        machineUpgrade.addEventHandler(eventClass, handler);
    }
}
