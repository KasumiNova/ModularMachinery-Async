package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import groovy.lang.Closure;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineBuilder;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@GroovyBlacklist
public class GroovyMachine {

    public static final Map<ResourceLocation, DynamicMachine> PRE_LOAD_MACHINES = new Object2ObjectOpenHashMap<>();
    private static final List<GroovyMachine> MACHINES = new ArrayList<>();

    public static void init() {
        MACHINES.forEach(GroovyMachine::build);
    }

    public static void setMachineBuilder(String registryName, Closure<?> buildFunction) {
        for (GroovyMachine machine : MACHINES) {
            if (machine.dynamicMachine.getRegistryName().getPath().equals(registryName)) {
                machine.buildFunction = buildFunction;
                break;
            }
        }
    }

    private final DynamicMachine dynamicMachine;
    private Closure<?> buildFunction;

    @GroovyBlacklist
    public GroovyMachine(DynamicMachine dynamicMachine, Closure<?> buildFunction) {
        this.dynamicMachine = dynamicMachine;
        this.buildFunction = buildFunction;
        MACHINES.add(this);
        PRE_LOAD_MACHINES.put(dynamicMachine.getRegistryName(), dynamicMachine);
    }

    public void build() {
        if (this.buildFunction == null) {
            GroovyLog.get().error("Machine {} has no builder function!", this.dynamicMachine.getRegistryName().getPath());
            MACHINES.remove(this);
            PRE_LOAD_MACHINES.remove(this.dynamicMachine.getRegistryName());
            return;
        }
        BlockArrayBuilder pattern = new BlockArrayBuilder(this.dynamicMachine);
        GroovyMachineBuilder settings = new GroovyMachineBuilder(this.dynamicMachine);
        ClosureHelper.withEnvironment(this.buildFunction, new MachineBuilderHelper(pattern, settings), true);
        ClosureHelper.call(this.buildFunction);
        pattern.build();
        MachineBuilder.WAIT_FOR_LOAD.add(this.dynamicMachine);
    }
}
