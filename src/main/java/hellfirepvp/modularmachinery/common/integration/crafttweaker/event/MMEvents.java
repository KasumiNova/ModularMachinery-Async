package hellfirepvp.modularmachinery.common.integration.crafttweaker.event;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelAnimationEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelGetEvent;
import github.kasuminova.mmce.common.event.machine.*;
import github.kasuminova.mmce.common.event.recipe.*;
import github.kasuminova.mmce.common.integration.Logger;
import github.kasuminova.mmce.common.util.concurrent.Action;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedList;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.MMEvents")
public class MMEvents {
    public static final List<Action> WAIT_FOR_MODIFY = new LinkedList<>();

    @ZenMethod
    public static void onStructureFormed(String machineRegistryName, IEventHandler<MachineStructureFormedEvent> function) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(MachineStructureFormedEvent.class, function::handle);
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    @ZenMethod
    public static void onStructureUpdate(String machineRegistryName, IEventHandler<MachineStructureUpdateEvent> function) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(MachineStructureUpdateEvent.class, function::handle);
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    @ZenMethod
    public static void onMachinePreTick(String machineRegistryName, IEventHandler<MachineTickEvent> function) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(MachineTickEvent.class, event -> {
                    if (event.phase != Phase.START) {
                        return;
                    }
                    function.handle(event);
                });
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    @ZenMethod
    public static void onMachinePostTick(String machineRegistryName, IEventHandler<MachineTickEvent> function) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(MachineTickEvent.class, event -> {
                    if (event.phase != Phase.END) {
                        return;
                    }
                    function.handle(event);
                });
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    @ZenMethod
    @Deprecated
    public static void onMachineTick(String machineRegistryName, IEventHandler<MachineTickEvent> function) {
        Logger.warn("Deprecated method onMachineTick()! Consider using onMachinePostTick()");
        onMachinePostTick(machineRegistryName, function);
    }

    @ZenMethod
    public static void onControllerGUIRender(String machineRegistryName, IEventHandler<ControllerGUIRenderEvent> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(ControllerGUIRenderEvent.class, function::handle);
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    @ZenMethod
    @Optional.Method(modid = "geckolib3")
    public static void onControllerModelAnimation(String machineRegistryName, IEventHandler<ControllerModelAnimationEvent> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(ControllerModelAnimationEvent.class, function::handle);
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    @ZenMethod
    @Optional.Method(modid = "geckolib3")
    public static void onControllerModelGet(String machineRegistryName, IEventHandler<ControllerModelGetEvent> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(ControllerModelGetEvent.class, function::handle);
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    @ZenMethod
    public static void onSmartInterfaceUpdate(String machineRegistryName, IEventHandler<SmartInterfaceUpdateEvent> function) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
            if (machine != null) {
                machine.addMachineEventHandler(SmartInterfaceUpdateEvent.class, function::handle);
            } else {
                Logger.error("Could not find machine `" + machineRegistryName + "`!");
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    // 让我们谈谈这段转换的问题...
    // ZenScript 的强制转换存在问题，即时目标类能够转换，它依然会抛出错误。
    // 我把这个问题归咎于 CraftTweaker，让事件处理变得可怕。
    // 我曾尝试过使用泛型函数接口，可惜它只支持一个泛型参数。
    // 我不能指望 CraftTweaker 做更多事情，这些事件系统已经变得颇有难度，如果有能力，我会建议你使用模组来实现这些功能。
    //
    // Let's talk about this conversion problem...
    // ZenScript has a problem with forced conversions. Even if the target class is able to convert,
    // it still throws an error.
    // I blame this problem on CraftTweaker, which makes event handling horrible.
    // I have tried using the generic function interface, but unfortunately it only supports one generic parameter.
    // I can't expect CraftTweaker to do much more than that, these event systems have become quite challenging,
    // and I would recommend using a mod for these functions if you are able to.
    //----------------------------------------------------------------------------------------------

    @ZenMethod
    public static RecipeCheckEvent castToRecipeCheckEvent(MachineEvent event) {
        return event instanceof RecipeCheckEvent ? (RecipeCheckEvent) event : null;
    }

    @ZenMethod
    public static RecipeStartEvent castToRecipeStartEvent(MachineEvent event) {
        return event instanceof RecipeStartEvent ? (RecipeStartEvent) event : null;
    }

    @ZenMethod
    public static RecipeTickEvent castToRecipeTickEvent(MachineEvent event) {
        return event instanceof RecipeTickEvent ? (RecipeTickEvent) event : null;
    }

    @ZenMethod
    public static RecipeFailureEvent castToRecipeFailureEvent(MachineEvent event) {
        return event instanceof RecipeFailureEvent ? (RecipeFailureEvent) event : null;
    }

    @ZenMethod
    public static RecipeFinishEvent castToRecipeFinishEvent(MachineEvent event) {
        return event instanceof RecipeFinishEvent ? (RecipeFinishEvent) event : null;
    }

    @ZenMethod
    public static MachineStructureFormedEvent castToMachineStructureFormedEvent(MachineEvent event) {
        return event instanceof MachineStructureFormedEvent ? (MachineStructureFormedEvent) event : null;
    }

    @ZenMethod
    public static MachineStructureUpdateEvent castToMachineStructureUpdateEvent(MachineEvent event) {
        return event instanceof MachineStructureUpdateEvent ? (MachineStructureUpdateEvent) event : null;
    }

    @ZenMethod
    public static MachineTickEvent castToMachineTickEvent(MachineEvent event) {
        return event instanceof MachineTickEvent ? (MachineTickEvent) event : null;
    }

    @ZenMethod
    public static SmartInterfaceUpdateEvent castToSmartInterfaceUpdateEvent(MachineEvent event) {
        return event instanceof SmartInterfaceUpdateEvent ? (SmartInterfaceUpdateEvent) event : null;
    }

    @ZenMethod
    public static FactoryRecipeStartEvent castToFactoryRecipeStartEvent(MachineEvent event) {
        return event instanceof FactoryRecipeStartEvent ? (FactoryRecipeStartEvent) event : null;
    }

    @ZenMethod
    public static FactoryRecipeTickEvent castToFactoryRecipeTickEvent(MachineEvent event) {
        return event instanceof FactoryRecipeTickEvent ? (FactoryRecipeTickEvent) event : null;
    }

    @ZenMethod
    public static FactoryRecipeFailureEvent castToFactoryRecipeFailureEvent(MachineEvent event) {
        return event instanceof FactoryRecipeFailureEvent ? (FactoryRecipeFailureEvent) event : null;
    }

    @ZenMethod
    public static FactoryRecipeFinishEvent castToFactoryRecipeFinishEvent(MachineEvent event) {
        return event instanceof FactoryRecipeFinishEvent ? (FactoryRecipeFinishEvent) event : null;
    }

    public static void registryAll() {
        for (Action waitForRegister : WAIT_FOR_MODIFY) {
            waitForRegister.doAction();
        }
        WAIT_FOR_MODIFY.clear();
    }
}
