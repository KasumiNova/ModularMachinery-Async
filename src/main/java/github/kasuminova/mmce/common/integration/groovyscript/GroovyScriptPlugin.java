package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.api.INamed;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.event.EventBusType;
import com.cleanroommc.groovyscript.event.GroovyEventManager;
import com.cleanroommc.groovyscript.event.GroovyReloadEvent;
import com.cleanroommc.groovyscript.event.ScriptRunEvent;
import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import com.cleanroommc.groovyscript.sandbox.LoadStage;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.resource.GeoModelExternalLoader;
import github.kasuminova.mmce.common.concurrent.RecipeCraftingContextPool;
import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.machine.MachineEvent;
import github.kasuminova.mmce.common.upgrade.registry.RegistryUpgrade;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineBuilder;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineModifier;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.RecipeModifierBuilder;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.event.MMEvents;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.upgrade.MachineUpgradeBuilder;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.BlockArrayCache;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GroovyScriptPlugin implements GroovyPlugin {

    private static GroovyContainer<Container> container;

    public static GroovyContainer<?> getContainer() {
        return container;
    }

    @Override
    public @NotNull String getModId() {
        return ModularMachinery.MODID;
    }

    @Override
    public @NotNull String getContainerName() {
        return ModularMachinery.NAME;
    }

    @Override
    public @Nullable GroovyPropertyContainer createGroovyPropertyContainer() {
        return new Container();
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> container) {
        MinecraftForge.EVENT_BUS.register(container.get());
        MinecraftForge.EVENT_BUS.register(GroovyScriptPlugin.class);
        GroovyScriptPlugin.container = (GroovyContainer<Container>) container;
    }

    @Override
    public @NotNull Collection<String> getAliases() {
        return Arrays.asList("modmach", "modular_machinery");
    }

    @SubscribeEvent
    public static void onReload(GroovyReloadEvent event) {
        MachineBuilder.WAIT_FOR_LOAD.clear();

        RegistryUpgrade.clearAll();
        RecipeRegistry.getRegistry().clearAllRecipes();
        // Reset RecipeAdapterIncId
        RegistriesMM.ADAPTER_REGISTRY.getValuesCollection().forEach(RecipeAdapter::resetIncId);

        if (FMLCommonHandler.instance().getSide().isClient() && Mods.GECKOLIB.isPresent()) {
            DynamicMachineModelRegistry.INSTANCE.onReload();
        }

        for (DynamicMachine loadedMachine : MachineRegistry.getLoadedMachines()) {
            loadedMachine.getMachineEventHandlers().clear();
            loadedMachine.getSmartInterfaceTypes().clear();
            loadedMachine.getCoreThreadPreset().clear();
            loadedMachine.getModifiers().clear();
            loadedMachine.getMultiBlockModifiers().clear();
        }
        // Reload JSON Machine
        MachineRegistry.preloadMachines();
        // Reload All Machine
        MachineRegistry.reloadMachine(MachineRegistry.loadMachines(null));
    }

    @SubscribeEvent
    public static void afterScriptRun(ScriptRunEvent.Post event) {
        if (event.getLoadStage() != LoadStage.POST_INIT) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        boolean isServer = server != null && server.isDedicatedServer();

        MachineRegistry.reloadMachine(MachineBuilder.WAIT_FOR_LOAD);

        if (FMLCommonHandler.instance().getSide().isClient() && Mods.GECKOLIB.isPresent()) {
            ClientProxy.clientScheduler.addRunnable(GeoModelExternalLoader.INSTANCE::onReload, 0);
        }

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> BlockArrayCache.buildCache(MachineRegistry.getLoadedMachines()));

        MachineModifier.loadAll();
        MMEvents.registryAll();

        RecipeCraftingContextPool.onReload();
        RecipeRegistry.getRegistry().loadRecipeRegistry(null, true);
        // TODO
        /*for (Action action : FactoryRecipeThread.WAIT_FOR_ADD) {
            action.doAction();
        }
        FactoryRecipeThread.WAIT_FOR_ADD.clear();*/

        if (!isServer) {
            ModIntegrationJEI.reloadRecipeWrappers();
        }

        future.join();

        // Flush the context to preview the changed structure.
        if (!isServer) {
            ModIntegrationJEI.reloadPreviewWrappers();
        }
    }

    private static class Container extends GroovyPropertyContainer {

        private final Map<ResourceLocation, GroovyMachineRecipes> machines = new Object2ObjectOpenHashMap<>();
        public final MachineUpgrades machineUpgrades = new MachineUpgrades();

        @GroovyBlacklist
        @SubscribeEvent
        public void onScriptRun(ScriptRunEvent.Pre event) {
            for (DynamicMachine machine : MachineRegistry.getRegistry()) {
                if (machine.getRegistryName().getNamespace().equals(ModularMachinery.MODID)) {
                    if (!getProperties().containsKey(machine.getRegistryName().getPath())) {
                        addProperty(new GroovyMachineRecipes(machine.getRegistryName()));
                    }
                }
            }
        }

        private GroovyMachineRecipes findMachine(String name) {
            INamed named = getProperties().get(name);
            if (named instanceof GroovyMachineRecipes machine) {
                return machine;
            }
            GroovyLog.get().error("Could not find modular machine with name '{}'!", name);
            return null;
        }

        public GroovyMachineRecipes machine(ResourceLocation rl) {
            if (rl.getNamespace().equals(ModularMachinery.MODID)) {
                return findMachine(rl.getPath());
            }

            GroovyMachineRecipes machine = this.machines.get(rl);
            if (machine == null) {
                DynamicMachine dynamicMachine = MachineRegistry.getRegistry().getMachine(rl);
                if (dynamicMachine != null) {
                    machine = new GroovyMachineRecipes(rl);
                    this.machines.put(rl, machine);
                }
            } else if (MachineRegistry.getRegistry().getMachine(rl) == null) {
                this.machines.remove(rl);
                machine = null;
            }
            if (machine == null) {
                GroovyLog.get().error("Could not find modular machine with name '{}'!", rl);
                return null;
            }
            return machine;
        }

        public GroovyMachineRecipes machine(String mod, String name) {
            return machine(new ResourceLocation(mod, name));
        }

        public GroovyMachineRecipes machine(String name) {
            String mod;
            int i = name.indexOf(":");
            if (i > 0) {
                mod = name.substring(0, i);
                name = name.substring(i + 1);
                return machine(new ResourceLocation(mod, name));
            }
            if (i == 0) {
                return findMachine(name.substring(1));
            }
            return findMachine(name);
        }

        public GroovyMachineRecipes getAt(String name) {
            return machine(name);
        }

        /*public void registerMachine(String registryName) {
            registerMachine(registryName, null);
        }*/

        public void registerMachine(String registryName, @DelegatesTo(MachineBuilderHelper.class) Closure<?> buildFunction) {
            ResourceLocation rl = new ResourceLocation(ModularMachinery.MODID, registryName);
            if (GroovyMachine.PRE_LOAD_MACHINES.containsKey(rl)) {
                throw new IllegalStateException("Machine with name '" + registryName + "' already exists!");
            }
            new GroovyMachine(new DynamicMachine(registryName), buildFunction);
        }

        public void machineEvent(String machineRegistryName, Class<? extends MachineEvent> clazz, Closure<?> listener) {
            GroovyEventManager.INSTANCE.listen(EventPriority.NORMAL, EventBusType.MAIN, clazz, event -> {
                DynamicMachine machine = MachineRegistry.getRegistry()
                                                        .getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
                if (machine != null) {
                    machine.addMachineEventHandler(clazz, IEventHandler.of(listener));
                } else {
                    GroovyLog.get().error("Could not find machine `" + machineRegistryName + "`!");
                }
            });
        }

        public void machineUpgrade(String name, String localizedName, float level, int maxStack, Closure<?> builder) {
            MachineUpgradeBuilder machineUpgradeBuilder = MachineUpgradeBuilder.newBuilder(name, localizedName, level, maxStack);
            if (machineUpgradeBuilder == null) return;
            ClosureHelper.withEnvironment(builder, machineUpgradeBuilder, true);
            builder.call();
            machineUpgradeBuilder.buildAndRegister();
        }

        public RecipeModifierBuilder recipeModifierBuilder() {
            return new RecipeModifierBuilder();
        }

        public RecipeModifierBuilder recipeModifierBuilder(String type, String ioTypeStr, float value, int operation,
                                                           boolean affectChance) {
            return RecipeModifierBuilder.create(type, ioTypeStr, value, operation, affectChance);
        }

        public RecipeModifier recipeModifier(String type, String ioTypeStr, float value, int operation, boolean affectChance) {
            return recipeModifierBuilder(type, ioTypeStr, value, operation, affectChance).build();
        }
    }
}
