/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import com.google.common.collect.Lists;
import com.google.gson.*;
import github.kasuminova.mmce.common.concurrent.RecipeCraftingContextPool;
import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.machine.MachineEvent;
import github.kasuminova.mmce.common.util.BlockPos2ValueMap;
import github.kasuminova.mmce.common.util.DynamicPattern;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.modifier.MultiBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicMachine
 * Created by HellFirePvP
 * Date: 27.06.2017 / 13:57
 */
public class DynamicMachine extends AbstractMachine {
    private final Map<BlockPos, List<SingleBlockModifierReplacement>> modifiers = new BlockPos2ValueMap<>();
    private final List<MultiBlockModifierReplacement> multiBlockModifiers = new ArrayList<>();

    private final Map<Class<?>, List<IEventHandler<MachineEvent>>> machineEventHandlers = new HashMap<>();

    private final TaggedPositionBlockArray pattern = new TaggedPositionBlockArray();
    private final Map<String, DynamicPattern> dynamicPatterns = new HashMap<>();

    // TODO: Remove this
    private final Map<String, SmartInterfaceType> smartInterfaces = new HashMap<>();

    private final Map<String, FactoryRecipeThread> coreThreadPreset = new LinkedHashMap<>();

    private boolean hideComponentsWhenFormed = false;

    private AxisAlignedBB controllerBoundingBox = Block.FULL_BLOCK_AABB;

    public DynamicMachine(String registryName) {
        super(registryName);
    }

    public Map<String, DynamicPattern> getDynamicPatterns() {
        return dynamicPatterns;
    }

    public DynamicPattern getDynamicPatternByName(String name) {
        return dynamicPatterns.get(name);
    }

    public void addDynamicPattern(String name, DynamicPattern pattern) {
        dynamicPatterns.put(name, pattern);
    }

    public void addCoreThread(FactoryRecipeThread thread) {
        coreThreadPreset.put(thread.getThreadName(), thread);
    }

    public Map<String, FactoryRecipeThread> getCoreThreadPreset() {
        return coreThreadPreset;
    }

    public boolean hasSmartInterfaceType(String type) {
        return smartInterfaces.containsKey(type);
    }

    public Map<String, SmartInterfaceType> getSmartInterfaceTypes() {
        return smartInterfaces;
    }

    public SmartInterfaceType getSmartInterfaceType(String type) {
        return smartInterfaces.get(type);
    }

    public void addSmartInterfaceType(SmartInterfaceType type) {
        smartInterfaces.put(type.getType(), type);
    }

    public Optional<SmartInterfaceType> getFirstSmartInterfaceType() {
        return smartInterfaces.values().stream().sorted().findFirst();
    }

    public boolean smartInterfaceTypesIsEmpty() {
        return smartInterfaces.isEmpty();
    }

    public Map<String, SmartInterfaceType> getFilteredType(Collection<String> ignoredTypes) {
        Map<String, SmartInterfaceType> filtered = new HashMap<>();
        smartInterfaces.forEach((type, data) -> {
            if (!ignoredTypes.contains(type)) {
                filtered.put(type, data);
            }
        });

        return filtered;
    }

    @SuppressWarnings("unchecked")
    public <H extends MachineEvent> void addMachineEventHandler(Class<H> hClass, IEventHandler<H> handler) {
        machineEventHandlers.putIfAbsent(hClass, new ArrayList<>());
        machineEventHandlers.get(hClass).add((IEventHandler<MachineEvent>) handler);
    }

    @Nullable
    public List<IEventHandler<MachineEvent>> getMachineEventHandlers(Class<?> handlerClass) {
        return machineEventHandlers.get(handlerClass);
    }

    public Map<Class<?>, List<IEventHandler<MachineEvent>>> getMachineEventHandlers() {
        return machineEventHandlers;
    }

    public TaggedPositionBlockArray getPattern() {
        return pattern;
    }

    public Map<BlockPos, List<SingleBlockModifierReplacement>> getModifiers() {
        return modifiers;
    }

    public List<MultiBlockModifierReplacement> getMultiBlockModifiers() {
        return multiBlockModifiers;
    }

    public boolean isHideComponentsWhenFormed() {
        return hideComponentsWhenFormed;
    }

    public void setHideComponentsWhenFormed(final boolean hideComponentsWhenFormed) {
        this.hideComponentsWhenFormed = hideComponentsWhenFormed;
    }

    @Nonnull
    public ModifierReplacementMap getModifiersAsMatchingReplacements() {
        ModifierReplacementMap infoMap = new ModifierReplacementMap();
        for (BlockPos pos : modifiers.keySet()) {
            List<BlockArray.BlockInformation> infoList = infoMap.computeIfAbsent(pos, v -> new ArrayList<>());
            List<SingleBlockModifierReplacement> replacements = modifiers.get(pos);
            for (SingleBlockModifierReplacement replacement : replacements) {
                infoList.add(replacement.getBlockInformation());
            }
        }
        return infoMap;
    }

    @Nonnull
    public Iterable<MachineRecipe> getAvailableRecipes() {
        return RecipeRegistry.getRecipesFor(this);
    }

    public RecipeCraftingContext createContext(ActiveMachineRecipe recipe,
            TileMultiblockMachineController ctrl) {
        if (!recipe.getRecipe().getOwningMachineIdentifier().equals(registryName)) {
            throw new IllegalArgumentException("Tried to create context for a recipe that doesn't belong to the referenced machine!");
        }

        return RecipeCraftingContextPool.borrowCtx(recipe, ctrl);
    }

    public AxisAlignedBB getControllerBoundingBox() {
        return controllerBoundingBox;
    }

    public void setControllerBoundingBox(AxisAlignedBB controllerBoundingBox) {
        this.controllerBoundingBox = controllerBoundingBox;
    }

    public void mergeFrom(DynamicMachine another) {
        smartInterfaces.clear();
        smartInterfaces.putAll(another.smartInterfaces);

        modifiers.clear();
        modifiers.putAll(another.modifiers);

        pattern.overwrite(another.pattern);

        machineEventHandlers.clear();
        machineEventHandlers.putAll(another.machineEventHandlers);

        dynamicPatterns.clear();
        dynamicPatterns.putAll(another.dynamicPatterns);

        hideComponentsWhenFormed = another.hideComponentsWhenFormed;
        controllerBoundingBox = another.controllerBoundingBox;
    }

    public static class ModifierReplacementMap extends HashMap<BlockPos, List<BlockArray.BlockInformation>> {

        public ModifierReplacementMap rotateYCCW() {
            ModifierReplacementMap map = new ModifierReplacementMap();

            for (BlockPos pos : keySet()) {
                List<BlockArray.BlockInformation> infoList = this.get(pos);
                List<BlockArray.BlockInformation> copyRotated = new ArrayList<>(infoList.size());
                for (BlockArray.BlockInformation info : infoList) {
                    copyRotated.add(info.copyRotateYCCW());
                }
                map.put(new BlockPos(pos.getZ(), pos.getY(), -pos.getX()), copyRotated);
            }

            return map;
        }

    }

    public static class MachineDeserializer implements JsonDeserializer<DynamicMachine> {

        private static List<BlockPos> buildPermutations(List<Integer> avX, List<Integer> avY, List<Integer> avZ) {
            List<BlockPos> out = new ArrayList<>(avX.size() * avY.size() * avZ.size());
            for (int x : avX) {
                for (int y : avY) {
                    for (int z : avZ) {
                        out.add(new BlockPos(x, y, z));
                    }
                }
            }
            return out;
        }

        private static void addModifierWithPattern(DynamicMachine machine, SingleBlockModifierReplacement mod, JsonObject part) throws JsonParseException {
            List<Integer> avX = new ArrayList<>();
            List<Integer> avY = new ArrayList<>();
            List<Integer> avZ = new ArrayList<>();
            addCoordinates("x", part, avX);
            addCoordinates("y", part, avY);
            addCoordinates("z", part, avZ);

            for (BlockPos permutation : buildPermutations(avX, avY, avZ)) {
                if (permutation.getX() == 0 && permutation.getY() == 0 && permutation.getZ() == 0) {
                    continue; //We're not going to overwrite the controller.
                }
                machine.modifiers.putIfAbsent(permutation, Lists.newArrayList());
                machine.modifiers.get(permutation).add(mod.setPos(permutation));
            }
        }

        private static void addDescriptorWithPattern(TaggedPositionBlockArray pattern, BlockArray.BlockInformation information, JsonObject part) throws JsonParseException {
            List<Integer> avX = new ArrayList<>();
            List<Integer> avY = new ArrayList<>();
            List<Integer> avZ = new ArrayList<>();
            addCoordinates("x", part, avX);
            addCoordinates("y", part, avY);
            addCoordinates("z", part, avZ);

            String tag = null;
            if (part.has("selector-tag")) {
                JsonElement strTag = part.get("selector-tag");
                if (!strTag.isJsonPrimitive()) {
                    throw new JsonParseException("The 'selector-tag' in an element must be a string!");
                }
                tag = strTag.getAsString();
            }
            ComponentSelectorTag selector = tag != null && !tag.isEmpty() ? new ComponentSelectorTag(tag) : null;

            for (BlockPos permutation : buildPermutations(avX, avY, avZ)) {
//                if (permutation.getX() == 0 && permutation.getY() == 0 && permutation.getZ() == 0) {
//                    continue; //We're not going to overwrite the controller.
//                }
                pattern.addBlock(permutation, information);

                if (tag != null && !tag.isEmpty()) {
                    pattern.setTag(permutation, selector);
                }
            }
        }

        private static void addCoordinates(String key, JsonObject part, List<Integer> out) throws JsonParseException {
            if (!part.has(key)) {
                out.add(0);
                return;
            }
            JsonElement coordinateElement = part.get(key);
            if (coordinateElement.isJsonPrimitive() && coordinateElement.getAsJsonPrimitive().isNumber()) {
                out.add(coordinateElement.getAsInt());
            } else if (coordinateElement.isJsonArray() && coordinateElement.getAsJsonArray().size() > 0) {
                for (JsonElement element : coordinateElement.getAsJsonArray()) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                        out.add(element.getAsInt());
                    } else {
                        throw new JsonParseException("Expected only numbers in JsonArray " + coordinateElement + " but found " + element);
                    }
                }
            }
        }

        @Override
        public DynamicMachine deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject root = json.getAsJsonObject();
            String registryName = JsonUtils.getString(root, "registryname", "");
            if (registryName.isEmpty()) {
                registryName = JsonUtils.getString(root, "registryName", "");
                if (registryName.isEmpty()) {
                    throw new JsonParseException("Invalid/Missing 'registryname' !");
                }
            }
            String localized = JsonUtils.getString(root, "localizedname", "");
            if (localized.isEmpty()) {
                throw new JsonParseException("Invalid/Missing 'localizedname' !");
            }
            JsonArray parts = JsonUtils.getJsonArray(root, "parts", new JsonArray());
            if (parts.size() == 0) {
                throw new JsonParseException("Empty/Missing 'parts'!");
            }
            DynamicMachine machine = new DynamicMachine(registryName);
            machine.setLocalizedName(localized);
            // Failure Action
            if (root.has("failure-action")) {
                machine.setFailureAction(DynamicMachinePreDeserializer.getFailureActions(root));
            }

            // Requires Blueprint
            if (root.has("requires-blueprint")) {
                machine.setRequiresBlueprint(DynamicMachinePreDeserializer.getRequireBlueprint(root));
            }

            // Color
            if (root.has("color")) {
                machine.setDefinedColor(DynamicMachinePreDeserializer.getColor(root));
            }

            // Has Factory
            if (root.has("has-factory")) {
                machine.setHasFactory(DynamicMachinePreDeserializer.getHasFactory(root));
            }

            // Factory Only
            if (root.has("factory-only")) {
                machine.setFactoryOnly(DynamicMachinePreDeserializer.getFactoryOnly(root));
            }

            // Hide Components When Formed
            if (root.has("hide-components-when-formed")) {
                machine.setHideComponentsWhenFormed(getHideComponentsWhenFormed(root));
            }

            // Controller Bounding Box
            if (root.has("controller-bounding-box")) {
                setControllerBoundingBox(root, machine);
            }

            // Parts
            addParts(parts, machine.pattern);
            // Remove Controller Position
            machine.pattern.getPattern().remove(new BlockPos(0, 0, 0));

            // Modifiers
            if (root.has("modifiers")) {
                addModifiers(context, root, machine);
            }

            // DynamicPatterns
            if (root.has("dynamic-patterns")) {
                addDynamicPatterns(root, machine);
            }

            return machine;
        }

        private static void addDynamicPatterns(final JsonObject root, final DynamicMachine machine) {
            JsonArray patterns = JsonUtils.getJsonArray(root, "dynamic-patterns", new JsonArray());
            if (patterns.size() == 0) {
                throw new JsonParseException("Empty 'dynamic-patterns'!");
            }

            Map<String, DynamicPattern> dynamicPatterns = new HashMap<>();

            for (int i = 0; i < patterns.size(); i++) {
                JsonElement element = patterns.get(i);

                if (!element.isJsonObject()) {
                    throw new JsonParseException("A pattern of 'dynamic-patterns' is not a compound object!");
                }

                JsonObject jsonPattern = element.getAsJsonObject();

                DynamicPattern pattern = new DynamicPattern(getName(jsonPattern));

                // faces
                setFaces(jsonPattern, pattern);

                // maxSize
                setMaxSize(jsonPattern, pattern, element);

                // parts
                addDynamicPatternParts(jsonPattern, "parts", pattern.getPattern());

                // parts-end
                if (jsonPattern.has("parts-end")) {
                    pattern.setPatternEnd(new TaggedPositionBlockArray());
                    addDynamicPatternParts(jsonPattern, "parts-end", pattern.getPatternEnd());
                }

                // minSize
                if (jsonPattern.has("minSize")) {
                    setMinSize(jsonPattern, pattern, element);
                }

                // structure-size-offset-start
                if (jsonPattern.has("structure-size-offset-start")) {
                    pattern.setStructureSizeOffsetStart(getStructureSizeOffset(jsonPattern, "structure-size-offset-start"));
                }

                // structure-size-offset
                if (jsonPattern.has("structure-size-offset")) {
                    pattern.setStructureSizeOffset(getStructureSizeOffset(jsonPattern, "structure-size-offset"));
                }

                dynamicPatterns.put(pattern.getName(), pattern);
            }

            machine.getDynamicPatterns().putAll(dynamicPatterns);
        }

        private static String getName(final JsonObject jsonPattern) {
            if (!jsonPattern.has("name")) {
                throw new JsonParseException("Pattern must have a 'name'!");
            } else {
                JsonElement element = jsonPattern.get("name");
                if (!element.isJsonPrimitive()) {
                    throw new JsonParseException("Expected only string in JsonPrimitive, but found " + element);
                }
                return element.getAsString();
            }
        }

        public static boolean getHideComponentsWhenFormed(JsonObject root) throws JsonParseException {
            JsonElement elementBlueprint = root.get("hide-components-when-formed");
            if (!elementBlueprint.isJsonPrimitive() || !elementBlueprint.getAsJsonPrimitive().isBoolean()) {
                throw new JsonParseException("'hide-components-when-formed' has to be either 'true' or 'false'!");
            }
            return elementBlueprint.getAsJsonPrimitive().getAsBoolean();
        }

        private static void addDynamicPatternParts(final JsonObject jsonPattern, final String name, final TaggedPositionBlockArray pattern) {
            if (!jsonPattern.has(name)) {
                throw new JsonParseException("Empty/Missing '" + name + "'!");
            }
            JsonArray parts = JsonUtils.getJsonArray(jsonPattern, name, new JsonArray());
            if (parts.size() == 0) {
                throw new JsonParseException("Empty/Missing 'parts'!");
            }
            addParts(parts, pattern);
        }

        private static void setFaces(final JsonObject jsonPattern, final DynamicPattern pattern) {
            if (!jsonPattern.has("faces")) {
                throw new JsonParseException("Pattern is missing string array 'faces'!");
            }
            JsonElement element = jsonPattern.get("faces");
            if (!element.isJsonArray()) {
                throw new JsonParseException("'faces' must to be string array!");
            }

            JsonArray jsonArray = element.getAsJsonArray();

            Set<EnumFacing> faces = EnumSet.noneOf(EnumFacing.class);
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement faceElement = jsonArray.get(i);
                if (!faceElement.isJsonPrimitive()) {
                    throw new JsonParseException("Expected only string in JsonPrimitive, but found " + element);
                }

                String face = faceElement.getAsString();

                try {
                    faces.add(EnumFacing.valueOf(face.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new JsonParseException(
                            "Invalid facing '" + face + "'! Expect: 'up', 'down', 'north', 'south', 'west', 'east' !");
                }
            }

            if (faces.isEmpty()) {
                throw new JsonParseException("faces is empty!");
            }

            pattern.addFaces(faces);
        }

        private static BlockPos getStructureSizeOffset(final JsonObject pattern, String name) {
            JsonElement element = pattern.get(name);
            if (!element.isJsonObject()) {
                throw new JsonParseException("Elements of '" + name + "' have to be objects!");
            }
            JsonObject structureSizeOffsetStart = element.getAsJsonObject();
            LinkedList<Integer> x = new LinkedList<>();
            LinkedList<Integer> y = new LinkedList<>();
            LinkedList<Integer> z = new LinkedList<>();

            addCoordinates("x", structureSizeOffsetStart, x);
            addCoordinates("y", structureSizeOffsetStart, y);
            addCoordinates("z", structureSizeOffsetStart, z);

            return new BlockPos(x.getFirst(), y.getFirst(), z.getFirst());
        }

        private static void setMinSize(final JsonObject jsonPattern, final DynamicPattern pattern, final JsonElement element) {
            JsonElement e = jsonPattern.get("minSize");
            if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                pattern.setMinSize(e.getAsInt());
            } else {
                throw new JsonParseException("Expected only numbers in JsonPrimitive 'minSize' but found " + element);
            }
        }

        private static void setMaxSize(final JsonObject jsonPattern, final DynamicPattern pattern, final JsonElement element) {
            if (!jsonPattern.has("maxSize")) {
                throw new JsonParseException("Pattern must be has 'maxSize'!");
            } else {
                JsonElement e = jsonPattern.get("maxSize");
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                    int maxSize = e.getAsInt();
                    if (pattern.getMinSize() > maxSize) {
                        throw new JsonParseException("The 'minSize' of the pattern is bigger than the 'maxSize'!");
                    }
                    pattern.setMaxSize(maxSize);
                } else {
                    throw new JsonParseException("Expected only numbers in JsonPrimitive 'maxSize' but found " + element);
                }
            }
        }

        private static void addModifiers(final JsonDeserializationContext context, final JsonObject root, final DynamicMachine machine) {
            JsonElement partModifiers = root.get("modifiers");
            if (!partModifiers.isJsonArray()) {
                throw new JsonParseException("'modifiers' has to be an array of modifiers!");
            }
            JsonArray modifiersArray = partModifiers.getAsJsonArray();
            for (int j = 0; j < modifiersArray.size(); j++) {
                JsonElement modifier = modifiersArray.get(j);
                if (!modifier.isJsonObject()) {
                    throw new JsonParseException("Elements of 'modifiers' have to be objects!");
                }
                addModifierWithPattern(machine, context.deserialize(modifier.getAsJsonObject(), SingleBlockModifierReplacement.class), modifier.getAsJsonObject());
            }
        }

        private static void addParts(final JsonArray parts, final TaggedPositionBlockArray pattern) {
            for (int i = 0; i < parts.size(); i++) {
                JsonElement element = parts.get(i);
                if (!element.isJsonObject()) {
                    throw new JsonParseException("A part of 'parts' is not a compound object!");
                }
                JsonObject part = element.getAsJsonObject();
                NBTTagCompound matchNBT = null;
                NBTTagCompound previewNBT = null;

                if (part.has("nbt")) {
                    JsonElement je = part.get("nbt");
                    if (!je.isJsonObject()) {
                        throw new JsonParseException("The ComponentType 'nbt' expects a json compound that defines the NBT tag to match the tileentity's nbt against!");
                    }
                    String jsonStr = je.toString();
                    try {
                        matchNBT = NBTJsonDeserializer.deserialize(jsonStr);
                    } catch (NBTException exc) {
                        throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
                    }
                }
                if (part.has("preview-nbt")) {
                    JsonElement je = part.get("preview-nbt");
                    if (!je.isJsonObject()) {
                        throw new JsonParseException("The ComponentType 'preview-nbt' expects a json compound that defines the NBT tag to preview the tileentity's nbt against!");
                    }
                    String jsonStr = je.toString();
                    try {
                        previewNBT = NBTJsonDeserializer.deserialize(jsonStr);
                    } catch (NBTException exc) {
                        throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
                    }
                }

                if (!part.has("elements")) {
                    throw new JsonParseException("Part contained empty element!");
                }
                JsonElement partElement = part.get("elements");
                if (partElement.isJsonPrimitive() && partElement.getAsJsonPrimitive().isString()) {
                    String strDesc = partElement.getAsString();
                    BlockArray.BlockInformation descr = MachineLoader.VARIABLE_CONTEXT.get(strDesc);
                    if (descr == null) {
                        descr = new BlockArray.BlockInformation(Lists.newArrayList(BlockArray.BlockInformation.getDescriptor(partElement.getAsString())));
                    } else {
                        descr = descr.copy(); //Avoid NBT-definitions bleed into variable context
                    }
                    if (matchNBT != null) {
                        descr.setMatchingTag(matchNBT);
                    }
                    if (previewNBT != null) {
                        descr.setPreviewTag(previewNBT);
                    }
                    addDescriptorWithPattern(pattern, descr, part);
                } else if (partElement.isJsonArray()) {
                    JsonArray elementArray = partElement.getAsJsonArray();
                    List<IBlockStateDescriptor> descriptors = Lists.newArrayList();
                    for (int xx = 0; xx < elementArray.size(); xx++) {
                        JsonElement p = elementArray.get(xx);
                        if (!p.isJsonPrimitive() || !p.getAsJsonPrimitive().isString()) {
                            throw new JsonParseException("Part elements of 'elements' have to be blockstate descriptions!");
                        }
                        String prim = p.getAsString();
                        BlockArray.BlockInformation descr = MachineLoader.VARIABLE_CONTEXT.get(prim);
                        if (descr != null) {
                            descriptors.addAll(descr.copy().getMatchingStates());
                        } else {
                            descriptors.add(BlockArray.BlockInformation.getDescriptor(prim));
                        }
                    }
                    if (descriptors.isEmpty()) {
                        throw new JsonParseException("'elements' array didn't contain any blockstate descriptors!");
                    }
                    BlockArray.BlockInformation bi = new BlockArray.BlockInformation(descriptors);
                    if (matchNBT != null) {
                        bi.setMatchingTag(matchNBT);
                    }
                    if (previewNBT != null) {
                        bi.setPreviewTag(previewNBT);
                    }
                    addDescriptorWithPattern(pattern, bi, part);
                } else {
                    throw new JsonParseException("'elements' has to either be a blockstate description, variable or array of blockstate descriptions!");
                }
            }
        }

        private static void setControllerBoundingBox(final JsonObject jsonPattern, DynamicMachine machine) {
            JsonArray boundingBox = JsonUtils.getJsonArray(jsonPattern, "controller-bounding-box");
            if (boundingBox.size() != 6) {
                throw new JsonParseException("Invalid 'controllerBoundingBox'!");
            }
            machine.setControllerBoundingBox(new AxisAlignedBB(
                    boundingBox.get(0).getAsDouble(),
                    boundingBox.get(1).getAsDouble(),
                    boundingBox.get(2).getAsDouble(),
                    boundingBox.get(3).getAsDouble(),
                    boundingBox.get(4).getAsDouble(),
                    boundingBox.get(5).getAsDouble()
            ));
        }
    }

}
