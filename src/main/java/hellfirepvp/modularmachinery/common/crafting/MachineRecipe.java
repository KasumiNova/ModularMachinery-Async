/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.*;
import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.command.RecipeCommandContainer;
import hellfirepvp.modularmachinery.common.crafting.command.RecipeRunnableCommand;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.RecipeAdapterBuilder;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineRecipe
 * Created by HellFirePvP
 * Date: 27.06.2017 / 00:24
 */
public class MachineRecipe implements Comparable<MachineRecipe> {

    protected static int counter = 0;

    protected final int sortId;
    protected final String recipeFilePath;
    protected final ResourceLocation owningMachine, registryName;
    protected final int tickTime;
    protected final List<ComponentRequirement<?, ?>> recipeRequirements = Lists.newArrayList();
    protected final RecipeCommandContainer commandContainer = new RecipeCommandContainer();
    protected final int configuredPriority;
    protected final boolean voidPerTickFailure;
    protected final Map<Class<?>, List<IEventHandler<RecipeEvent>>> recipeEventHandlers;
    protected final List<String> tooltipList;

    protected boolean parallelized;
    protected String threadName;
    protected int maxThreads;

    public MachineRecipe(String path, ResourceLocation registryName, ResourceLocation owningMachine,
                         int tickTime, int configuredPriority, boolean voidPerTickFailure, boolean parallelized) {
        this.sortId = counter;
        counter++;
        this.recipeFilePath = path;
        this.registryName = registryName;
        this.owningMachine = owningMachine;
        this.tickTime = tickTime;
        this.configuredPriority = configuredPriority;
        this.voidPerTickFailure = voidPerTickFailure;
        this.parallelized = parallelized;
        this.recipeEventHandlers = new HashMap<>();
        this.tooltipList = new ArrayList<>();
        this.threadName = "";
        this.maxThreads = -1;
    }

    public MachineRecipe(String path, ResourceLocation registryName, ResourceLocation owningMachine,
                         int tickTime, int configuredPriority, boolean voidPerTickFailure, boolean parallelized,
                         Map<Class<?>, List<IEventHandler<RecipeEvent>>> recipeEventHandlers, List<String> tooltipList,
                         String threadName, int maxThreads) {
        this.sortId = counter;
        counter++;
        this.recipeFilePath = path;
        this.registryName = registryName;
        this.owningMachine = owningMachine;
        this.tickTime = tickTime;
        this.configuredPriority = configuredPriority;
        this.voidPerTickFailure = voidPerTickFailure;
        this.parallelized = parallelized;
        this.recipeEventHandlers = recipeEventHandlers;
        this.tooltipList = tooltipList;
        this.threadName = threadName;
        this.maxThreads = maxThreads;
    }

    public MachineRecipe(PreparedRecipe preparedRecipe) {
        this.sortId = counter;
        counter++;
        this.recipeFilePath = preparedRecipe.getFilePath();
        this.registryName = preparedRecipe.getRecipeRegistryName();
        this.owningMachine = preparedRecipe.getAssociatedMachineName();
        this.tickTime = preparedRecipe.getTotalProcessingTickTime();
        this.configuredPriority = preparedRecipe.getPriority();
        this.voidPerTickFailure = preparedRecipe.voidPerTickFailure();
        this.parallelized = preparedRecipe.isParallelized();
        this.recipeEventHandlers = preparedRecipe.getRecipeEventHandlers();
        this.tooltipList = preparedRecipe.getTooltipList();
        this.threadName = preparedRecipe.getThreadName();
        this.maxThreads = preparedRecipe.getMaxThreads();
    }

    public void mergeAdapter(final RecipeAdapterBuilder adapterBuilder) {
        this.parallelized = adapterBuilder.isParallelized();
        this.tooltipList.addAll(adapterBuilder.getTooltipList());
        if (!adapterBuilder.getThreadName().isEmpty()) {
            this.threadName = adapterBuilder.getThreadName();
        }
        if (adapterBuilder.getMaxThreads() != -1) {
            this.maxThreads = adapterBuilder.getMaxThreads();
        }
        for (final ComponentRequirement<?, ?> requirement : adapterBuilder.getComponents()) {
            addRequirement(requirement);
        }
        adapterBuilder.getRecipeEventHandlers().forEach((clazz, actions) -> actions.forEach(action -> addRecipeEventHandler(clazz, action)));
    }

    public void addTooltip(String tooltip) {
        tooltipList.add(tooltip);
    }

    public List<String> getTooltipList() {
        return tooltipList;
    }

    @SideOnly(Side.CLIENT)
    public List<String> getFormattedTooltip() {
        return tooltipList.stream()
                .map(tip -> I18n.hasKey(tip) ? I18n.format(tip) : tip)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <H extends RecipeEvent> void addRecipeEventHandler(Class<?> hClass, IEventHandler<H> handler) {
        recipeEventHandlers.putIfAbsent(hClass, new ArrayList<>());
        recipeEventHandlers.get(hClass).add((IEventHandler<RecipeEvent>) handler);
    }

    public boolean isParallelized() {
        return parallelized;
    }

    @Nullable
    public List<IEventHandler<RecipeEvent>> getRecipeEventHandlers(Class<?> handlerClass) {
        return recipeEventHandlers.get(handlerClass);
    }

    public Map<Class<?>, List<IEventHandler<RecipeEvent>>> getRecipeEventHandlers() {
        return recipeEventHandlers;
    }

    public String getRecipeFilePath() {
        return recipeFilePath;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public ResourceLocation getOwningMachineIdentifier() {
        return owningMachine;
    }

    public List<ComponentRequirement<?, ?>> getCraftingRequirements() {
        return Collections.unmodifiableList(recipeRequirements);
    }

    public RecipeCommandContainer getCommandContainer() {
        return commandContainer;
    }

    public void addRequirement(ComponentRequirement<?, ?> requirement) {
        if (requirement instanceof RequirementEnergy) {
            for (ComponentRequirement<?, ?> req : this.recipeRequirements) {
                if (req instanceof RequirementEnergy && req.getActionType() == requirement.getActionType()) {
                    throw new IllegalStateException("Tried to add multiple energy requirements for the same ioType! Please only add one for each ioType!");
                }
            }
        }
        this.recipeRequirements.add(requirement);
    }

    public int getRecipeTotalTickTime() {
        return this.tickTime;
    }

    public int getConfiguredPriority() {
        return this.configuredPriority;
    }

    public boolean doesCancelRecipeOnPerTickFailure() {
        return this.voidPerTickFailure;
    }

    @Nullable
    public DynamicMachine getOwningMachine() {
        return MachineRegistry.getRegistry().getMachine(owningMachine);
    }

    public String getThreadName() {
        return threadName;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public MachineRecipe copy(Function<ResourceLocation, ResourceLocation> registryNameChange,
                              ResourceLocation newOwningMachineIdentifier,
                              List<RecipeModifier> modifiers) {
        MachineRecipe copy = new MachineRecipe(this.recipeFilePath,
                registryNameChange.apply(this.registryName),
                newOwningMachineIdentifier,
                Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_DURATION, null, this.tickTime, false)),
                this.configuredPriority,
                this.doesCancelRecipeOnPerTickFailure(),
                this.parallelized,
                this.recipeEventHandlers,
                this.tooltipList,
                this.threadName,
                this.maxThreads);

        for (ComponentRequirement<?, ?> requirement : this.getCraftingRequirements()) {
            copy.addRequirement(requirement.deepCopyModified(modifiers).postDeepCopy(requirement));
        }
        return copy;
    }

    @Override
    public int compareTo(MachineRecipe o) {
        return Integer.compare(buildWeight(), o.buildWeight());
    }

    private int buildWeight() {
        int weightOut = sortId;

        for (ComponentRequirement<?, ?> req : this.recipeRequirements) {
            if (req.getActionType() == IOType.OUTPUT) {
                continue;
            }
            weightOut -= req.getSortingWeight();
        }
        return weightOut;
    }

    public static class MachineRecipeContainer {

        private final MachineRecipe parent;
        private final List<ResourceLocation> recipeOwnerList = Lists.newLinkedList();

        private MachineRecipeContainer(MachineRecipe copyParent) {
            this.parent = copyParent;
        }

        public List<MachineRecipe> getRecipes() {
            List<MachineRecipe> out = Lists.newArrayListWithCapacity(recipeOwnerList.size());
            for (int i = 0; i < recipeOwnerList.size(); i++) {
                ResourceLocation location = recipeOwnerList.get(i);
                MachineRecipe rec = new MachineRecipe(parent.recipeFilePath + "_sub_" + i,
                        new ResourceLocation(parent.registryName.getNamespace(), parent.registryName.getPath() + "_sub_" + i),
                        location, parent.tickTime, parent.configuredPriority, parent.voidPerTickFailure, parent.parallelized);
                for (ComponentRequirement<?, ?> req : parent.recipeRequirements) {
                    rec.recipeRequirements.add(req.deepCopy().postDeepCopy(req));
                }
                out.add(rec);
            }
            return out;
        }

    }

    public static class Deserializer implements JsonDeserializer<MachineRecipeContainer> {

        private static void loadCommands(JsonObject root, JsonDeserializationContext context,
                                         String arrayTag, Consumer<RecipeRunnableCommand> addFunction) {
            if (root.has(arrayTag)) {
                JsonElement elementStartCommands = root.get(arrayTag);
                if (!elementStartCommands.isJsonArray()) {
                    throw new JsonParseException(arrayTag + " should be an array of commands!");
                }

                for (JsonElement je : elementStartCommands.getAsJsonArray()) {
                    addFunction.accept(context.deserialize(je, RecipeRunnableCommand.class));
                }
            }
        }

        @Override
        public MachineRecipeContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject root = json.getAsJsonObject();
            if (!root.has("machine")) {
                throw new JsonParseException("No 'machine'-entry specified!");
            }
            if (!root.has("registryName") && !root.has("registryname")) {
                throw new JsonParseException("No 'registryName'-entry specified!");
            }
            if (!root.has("recipeTime")) {
                throw new JsonParseException("No 'recipeTime'-entry specified!");
            }
            JsonElement elementMachine = root.get("machine");
            List<ResourceLocation> qualifiedMachineNames = Lists.newLinkedList();
            if (elementMachine.isJsonArray()) {
                if (elementMachine.getAsJsonArray().size() <= 0) {
                    throw new JsonParseException("'machine' is an array, but it's empty! Provide at least 1 owning-machine name!");
                }
                JsonArray jar = elementMachine.getAsJsonArray();
                for (JsonElement je : jar) {
                    if (je.isJsonPrimitive() && je.getAsJsonPrimitive().isString()) {
                        qualifiedMachineNames.add(new ResourceLocation(ModularMachinery.MODID, je.getAsJsonPrimitive().getAsString()));
                        continue;
                    }
                    throw new JsonParseException("Found an element in the array specified in 'machine' that is not a string! " + je);
                }
                if (qualifiedMachineNames.isEmpty()) {
                    //We capture this before already, but just to be safe...
                    throw new JsonParseException("'machine' is an array, but it's empty! Provide at least 1 owning-machine name!");
                }
            } else if (elementMachine.isJsonPrimitive() && elementMachine.getAsJsonPrimitive().isString()) {
                qualifiedMachineNames.add(new ResourceLocation(ModularMachinery.MODID, elementMachine.getAsJsonPrimitive().getAsString()));
            } else {
                throw new JsonParseException("'machine' has to be either an array of strings or just a string! - Found " + elementMachine + " instead!");
            }
            JsonElement elementRegistryName = root.get("registryName");
            if (elementRegistryName == null) {
                elementRegistryName = root.get("registryname");
            }
            if (!elementRegistryName.isJsonPrimitive() || !elementRegistryName.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'registryName' has to have as value only a String that defines its unique registry name!");
            }
            JsonElement elementTime = root.get("recipeTime");
            if (!elementTime.isJsonPrimitive() || !elementTime.getAsJsonPrimitive().isNumber()) {
                throw new JsonParseException("'recipeTime' has to be a number!");
            }
            int priority = 0;
            if (root.has("priority")) {
                JsonElement elementPriority = root.get("priority");
                if (!elementPriority.isJsonPrimitive() || !elementPriority.getAsJsonPrimitive().isNumber()) {
                    throw new JsonParseException("'priority' has to be a number! (if specified)");
                }
                priority = elementPriority.getAsInt();
            }

            boolean voidPerTickFailure = false;
            if (root.has("cancelIfPerTickFails")) {
                JsonElement elementDeletePerTick = root.get("cancelIfPerTickFails");
                if (!elementDeletePerTick.isJsonPrimitive() || !elementDeletePerTick.getAsJsonPrimitive().isBoolean()) {
                    throw new JsonParseException("'cancelIfPerTickFails' has to be a boolean! (if specified)");
                }
                voidPerTickFailure = elementDeletePerTick.getAsBoolean();
            }

            ResourceLocation parentName = Iterables.getFirst(qualifiedMachineNames, null);
            if (parentName == null) {
                //This actually never happens. Never. But just to be sure and prevent weird issues down the line.
                throw new IllegalStateException("Couldn't find machine name from qualified-names list: " + Arrays.toString(qualifiedMachineNames.toArray()));
            }

            String registryName = elementRegistryName.getAsJsonPrimitive().getAsString();
            int recipeTime = elementTime.getAsJsonPrimitive().getAsInt();
            MachineRecipe recipe = new MachineRecipe(RecipeLoader.CURRENTLY_READING_PATH.get(),
                    new ResourceLocation(ModularMachinery.MODID, registryName),
                    parentName, recipeTime, priority, voidPerTickFailure, Config.recipeParallelizeEnabledByDefault);

            MachineRecipeContainer outContainer = new MachineRecipeContainer(recipe);
            outContainer.recipeOwnerList.addAll(qualifiedMachineNames);

            if (!root.has("requirements")) {
                throw new JsonParseException("No 'requirements'-entry specified!");
            }
            JsonElement elementRequirements = root.get("requirements");
            if (!elementRequirements.isJsonArray()) {
                throw new JsonParseException("'requirements' should be an array of recipe requirements!");
            }
            JsonArray requirementsArray = elementRequirements.getAsJsonArray();
            for (int i = 0; i < requirementsArray.size(); i++) {
                JsonElement elementRequirement = requirementsArray.get(i);
                if (!elementRequirement.isJsonObject()) {
                    throw new JsonParseException("Each element in the 'requirements' array needs to be a fully defined requirement-object!");
                }
                recipe.recipeRequirements.add(context.deserialize(elementRequirement, ComponentRequirement.class));
            }
            if (recipe.recipeRequirements.isEmpty()) {
                throw new JsonParseException("A recipe needs to have at least 1 requirement!");
            }

            loadCommands(root, context, "startCommands", (cmd) -> recipe.getCommandContainer().addStartCommand(cmd));
            loadCommands(root, context, "processingCommands", (cmd) -> recipe.getCommandContainer().addProcessingCommand(cmd));
            loadCommands(root, context, "finishCommands", (cmd) -> recipe.getCommandContainer().addFinishCommand(cmd));
            return outContainer;
        }

    }

    public static class ComponentDeserializer implements JsonDeserializer<ComponentRequirement<?, ?>> {

        @Override
        public ComponentRequirement<?, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) {
                throw new JsonParseException("Component Requirements have to be objects!");
            }
            JsonObject requirement = json.getAsJsonObject();

            if (!requirement.has("type") || !requirement.get("type").isJsonPrimitive() ||
                    !requirement.get("type").getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'type' of a requirement is missing or isn't a string!");
            }
            String type = requirement.getAsJsonPrimitive("type").getAsString();
            if (!requirement.has("io-type") || !requirement.get("io-type").isJsonPrimitive() ||
                    !requirement.get("io-type").getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'io-type' of a requirement is missing or isn't a string!");
            }
            String ioType = requirement.getAsJsonPrimitive("io-type").getAsString();

            RequirementType<?, ?> requirementType = RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(new ResourceLocation(type));
            if (requirementType == null) {
                requirementType = IntegrationTypeHelper.searchRequirementType(type);
                if (requirementType != null) {
                    ModularMachinery.log.info("[Modular Machinery]: Deprecated requirement name '"
                            + type + "'! Consider using " + requirementType.getRegistryName().toString());
                }
            }
            if (requirementType == null) {
                throw new JsonParseException("'" + type + "' is not a valid RequirementType!");
            }
            IOType machineIoType = IOType.getByString(ioType);
            if (machineIoType == null) {
                throw new JsonParseException("'" + ioType + "' is not a valid IOType!");
            }
            ComponentRequirement<?, ?> req = requirementType.createRequirement(machineIoType, requirement);

            if (requirement.has("selector-tag")) {
                JsonElement strTag = requirement.get("selector-tag");
                if (!strTag.isJsonPrimitive()) {
                    throw new JsonParseException("The 'selector-tag' in an requirement must be a string!");
                }
                if (!strTag.getAsString().isEmpty()) {
                    req.setTag(new ComponentSelectorTag(strTag.getAsString()));
                }
            }

            return req;
        }
    }

}
