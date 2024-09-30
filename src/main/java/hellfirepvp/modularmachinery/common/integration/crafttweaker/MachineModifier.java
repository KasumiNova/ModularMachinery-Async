package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.common.integration.Logger;
import github.kasuminova.mmce.common.util.concurrent.Action;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedList;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineModifier")
public class MachineModifier {
    public static final List<Action> WAIT_FOR_MODIFY = new LinkedList<>();

    @ZenMethod
    public static void addSmartInterfaceType(String machineName, SmartInterfaceType type) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                Logger.error("Could not find machine `" + machineName + "`!");
                return;
            }
            if (!machine.hasSmartInterfaceType(type.getType())) {
                machine.addSmartInterfaceType(type);
            } else {
                Logger.warn("DynamicMachine `" + machine.getRegistryName() + "` is already has SmartInterfaceType `" + type.getType() + "`!");
            }
        });
    }

    @ZenMethod
    public static void setMaxParallelism(String machineName, int maxParallelism) {
        if (maxParallelism < 1) {
            Logger.error("Max Parallelism must larger than 1!");
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                Logger.error("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setMaxParallelism(maxParallelism);
        });
    }

    @ZenMethod
    public static void setInternalParallelism(String machineName, int parallelism) {
        if (parallelism < 0) {
            Logger.error("Max Parallelism must larger than 0!");
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                Logger.error("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setInternalParallelism(parallelism);
        });
    }

    @ZenMethod
    public static void setMaxThreads(String machineName, int maxThreads) {
        // Maybe the author only wanted to use the core thread?
        if (maxThreads < 0) {
            Logger.error("Max Threads must larger than or equal 0!");
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                Logger.error("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setMaxThreads(maxThreads);
        });
    }

    @ZenMethod
    public static void addCoreThread(String machineName, FactoryRecipeThread thread) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                Logger.error("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.addCoreThread(thread);
        });
    }

    @ZenMethod
    public static void setMachineGeoModel(String machineName, String modelName) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }

        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                Logger.error("Could not find machine `" + machineName + "`!");
                return;
            }

            MachineControllerModel model = DynamicMachineModelRegistry.INSTANCE.getMachineModel(modelName);
            if (model == null) {
                Logger.error("Could not find geo model `" + modelName + "`!");
                return;
            }

            DynamicMachineModelRegistry.INSTANCE.registerMachineDefaultModel(machine, model);
        });
    }

    @ZenMethod
    public static void setMachinePrefix(String machineName, String prefixName) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }

        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                Logger.error("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setPrefix(prefixName);
        });
    }

    public static void loadAll() {
        for (Action waitForRegister : WAIT_FOR_MODIFY) {
            waitForRegister.doAction();
        }
        WAIT_FOR_MODIFY.clear();
    }
}
