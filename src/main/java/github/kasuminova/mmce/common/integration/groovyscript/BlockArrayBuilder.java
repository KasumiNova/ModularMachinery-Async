package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.helper.ingredient.NbtHelper;
import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import groovy.lang.Closure;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public class BlockArrayBuilder {

    protected final DynamicMachine machine;
    private final TaggedPositionBlockArray blockArray;
    private final List<List<String>> tensor = new ArrayList<>();
    private final Char2ObjectMap<BlockArray.BlockInformation> charMap = new Char2ObjectOpenHashMap<>();
    private final Char2ObjectMap<ComponentSelectorTag> selectorTagMap = new Char2ObjectOpenHashMap<>();
    private final Map<BlockPos, List<SingleBlockModifierReplacement>> blockModifierMap = new Object2ObjectOpenHashMap<>();
    private final Char2ObjectMap<List<SingleBlockModifierReplacement>> blockModifierCharMap = new Char2ObjectOpenHashMap<>();
    protected BlockArray.BlockInformation lastInformation = null;
    protected char lastChar = Character.MIN_VALUE;
    private char controllerChar = Character.MIN_VALUE;

    private static final BlockArray.BlockInformation CONTROLLER = new BlockArray.BlockInformation(Collections.emptyList());

    public BlockArrayBuilder(DynamicMachine machine) {
        this.machine = machine;
        this.blockArray = machine.getPattern();
        this.charMap.put(' ', null);
    }

    public BlockArrayBuilder layer(String... row) {
        this.tensor.add(Arrays.asList(row));
        return this;
    }

    public BlockArrayBuilder layer(List<String> row) {
        this.tensor.add(row);
        return this;
    }

    @GroovyBlacklist
    public BlockArrayBuilder where(String c, Runnable func) {
        // groovy doesn't have char literals
        if (c.length() != 1) {
            throw new IllegalArgumentException("Argument in `where()` must have exactly one character!");
        }
        func.run();
        return this;
    }

    public BlockArrayBuilder whereAny(String c) {
        return where(c, () -> {
            this.lastInformation = null;
            this.lastChar = c.charAt(0);
            this.charMap.put(this.lastChar, null);
        });
    }

    public BlockArrayBuilder whereAir(String c) {
        return where(c, Blocks.AIR.getDefaultState());
    }

    public BlockArrayBuilder whereController(String c) {
        if (this.controllerChar != Character.MIN_VALUE) {
            GroovyLog.get().exception(new IllegalStateException("Controller is already assigned!"));
            return this;
        }
        return where(c, () -> {
            this.lastInformation = CONTROLLER;
            this.lastChar = c.charAt(0);
            this.controllerChar = this.lastChar;
            this.charMap.put(this.lastChar, CONTROLLER);
        });
    }

    public BlockArrayBuilder where(String c, IBlockState blockStates) {
        return where(c, blockStates, null, null, null);
    }

    public BlockArrayBuilder where(String c, IBlockState blockStates, @Nullable Map<String, Object> nbt) {
        return where(c, blockStates, nbt, nbt, null);
    }

    public BlockArrayBuilder where(String c, IBlockState blockStates, @Nullable Map<String, Object> nbt,
                                   @Nullable Map<String, Object> previewNBT) {
        return where(c, blockStates, nbt, previewNBT, null);
    }

    public BlockArrayBuilder where(String c, IBlockState blockStates, @Nullable Map<String, Object> nbt,
                                   Closure<Boolean> checker) {
        return where(c, blockStates, nbt, nbt, checker);
    }

    public BlockArrayBuilder where(String c, IBlockState blockStates, @Nullable Map<String, Object> nbt,
                                   @Nullable Map<String, Object> previewNBT, Closure<Boolean> checker) {
        return where(c, Collections.singletonList(blockStates), nbt, previewNBT, checker);
    }

    public BlockArrayBuilder where(String c, Iterable<IBlockState> blockStates) {
        return where(c, blockStates, null, null, null);
    }

    public BlockArrayBuilder where(String c, Iterable<IBlockState> blockStates, @Nullable Map<String, Object> nbt) {
        return where(c, blockStates, nbt, nbt, null);
    }

    public BlockArrayBuilder where(String c, Iterable<IBlockState> blockStates, @Nullable Map<String, Object> nbt,
                                   @Nullable Map<String, Object> previewNBT) {
        return where(c, blockStates, nbt, previewNBT, null);
    }

    public BlockArrayBuilder where(String c, Iterable<IBlockState> blockStates, @Nullable Map<String, Object> nbt,
                                   Closure<Boolean> checker) {
        return where(c, blockStates, nbt, nbt, checker);
    }

    public BlockArrayBuilder where(String c, Iterable<IBlockState> blockStates, @Nullable Map<String, Object> nbt,
                                   @Nullable Map<String, Object> previewNBT, Closure<Boolean> checker) {
        return where(c, () -> {
            List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
            for (IBlockState blockState : blockStates) {
                stateDescriptorList.add(new IBlockStateDescriptor(blockState));
            }
            this.lastInformation = new BlockArray.BlockInformation(stateDescriptorList);
            this.lastChar = c.charAt(0);
            this.charMap.put(this.lastChar, this.lastInformation);
            if (nbt != null) nbt(nbt);
            if (previewNBT != null) previewNbt(previewNBT);
            if (checker != null) blockChecker(checker);
        });
    }

    public BlockArrayBuilder blockModifier(int x, int y, int z, IBlockState blockStates, String description, RecipeModifier... modifiers) {
        return blockModifier(x, y, z, Collections.singletonList(blockStates), description, modifiers);
    }

    /**
     * 添加单方块配方修改器。
     *
     * @param x           X
     * @param y           Y
     * @param z           Z
     * @param blockStates BlockState
     * @param description 描述
     * @param modifiers   修改器列表
     */
    public BlockArrayBuilder blockModifier(int x, int y, int z, Iterable<IBlockState> blockStates, String description, RecipeModifier... modifiers) {
        List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
        for (IBlockState blockState : blockStates) {
            stateDescriptorList.add(new IBlockStateDescriptor(blockState));
        }
        singleBlockModifier(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList), description, modifiers);
        return this;
    }

    public BlockArrayBuilder whereBlockModifier(String c, IBlockState blockStates, String description, RecipeModifier... modifiers) {
        return whereBlockModifier(c, Collections.singletonList(blockStates), description, modifiers);
    }

    public BlockArrayBuilder whereBlockModifier(String c, Iterable<IBlockState> blockStates, String description, RecipeModifier... modifiers) {
        where(c, () -> {
            List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
            for (IBlockState blockState : blockStates) {
                stateDescriptorList.add(new IBlockStateDescriptor(blockState));
            }
            singleBlockModifier(c.charAt(0), new BlockArray.BlockInformation(stateDescriptorList), description, modifiers);
        });
        return this;
    }

    private void singleBlockModifier(BlockPos pos, BlockArray.BlockInformation information, String description, RecipeModifier... modifiers) {
        this.lastInformation = information;
        this.lastChar = Character.MIN_VALUE;
        this.blockModifierMap.computeIfAbsent(pos, k -> new ArrayList<>())
                .add(new SingleBlockModifierReplacement(information, Arrays.asList(modifiers), description));
    }

    private void singleBlockModifier(char c, BlockArray.BlockInformation information, String description, RecipeModifier... modifiers) {
        this.lastInformation = information;
        this.lastChar = c;
        this.blockModifierCharMap.computeIfAbsent(c, k -> new ArrayList<>())
                .add(new SingleBlockModifierReplacement(information, Arrays.asList(modifiers), description));
    }

    public BlockArrayBuilder nbt(Map<String, Object> tag) {
        return nbt(NbtHelper.ofMap(tag));
    }

    public BlockArrayBuilder nbt(NBTTagCompound tag) {
        if (lastInformation != null) {
            lastInformation.setMatchingTag(tag);
            if (lastInformation.getPreviewTag() == null) {
                lastInformation.setPreviewTag(tag);
            }
        }
        return this;
    }

    public BlockArrayBuilder previewNbt(Map<String, Object> data) {
        return previewNbt(NbtHelper.ofMap(data));
    }

    public BlockArrayBuilder previewNbt(NBTTagCompound data) {
        if (lastInformation != null) {
            lastInformation.setPreviewTag(data);
        }
        return this;
    }

    public BlockArrayBuilder blockChecker(Closure<Boolean> checker) {
        if (lastInformation != null) {
            lastInformation.nbtChecker = (world, pos, blockState, nbt) ->
                    ClosureHelper.call(true, checker, world, pos, blockState, nbt);
        }
        return this;
    }

    public BlockArrayBuilder tag(String tag) {
        if (lastInformation != null && this.lastChar != Character.MIN_VALUE) {
            this.selectorTagMap.put(this.lastChar, new ComponentSelectorTag(tag));
        }
        return this;
    }

    private BlockPos validate() {
        if (this.controllerChar == Character.MIN_VALUE) {
            GroovyLog.get().exception(new IllegalStateException("Controller location must be defined!"));
            return null;
        }
        GroovyLog.Msg msg = GroovyLog.msg("Error creating BlockArray").error();
        if (this.tensor.isEmpty()) {
            msg.add("no block matrix defined").post();
            return null;
        }
        CharSet checkedChars = new CharArraySet();
        int foundController = 0;
        int cx = 0, cy = 0, cz = 0;
        int layerSize = this.tensor.get(0).size();
        for (int x = 0; x < this.tensor.size(); x++) {
            List<String> xLayer = this.tensor.get(x);
            if (xLayer.isEmpty()) {
                msg.add("Layer {} is empty. This is not right", x + 1);
            } else if (xLayer.size() != layerSize) {
                msg.add("Invalid x-layer size. Expected {}, but got {} at layer {}", layerSize, xLayer.size(), x + 1);
            }
            int rowSize = xLayer.get(0).length();
            for (int y = 0; y < xLayer.size(); y++) {
                String yRow = xLayer.get(y);
                if (yRow.isEmpty()) {
                    msg.add("Row {} in layer {} is empty. This is not right", y + 1, x + 1);
                } else if (yRow.length() != rowSize) {
                    msg.add("Invalid x-layer size. Expected {}, but got {} at row {} in layer {}", layerSize, xLayer.size(), y + 1, x + 1);
                }
                for (int z = 0; z < yRow.length(); z++) {
                    char zChar = yRow.charAt(z);
                    if (!checkedChars.contains(zChar)) {
                        if (!this.charMap.containsKey(zChar)) {
                            msg.add("Found char '{}' at char {} in row {} in layer {}, but character was not found in map!", zChar, z + 1, y + 1, x + 1);
                        }
                        checkedChars.add(zChar);
                    }
                    if (zChar == this.controllerChar) {
                        cx = x;
                        cy = y;
                        cz = z;
                        foundController++;
                    }
                }
            }
        }
        if (foundController == 0) {
            msg.add("No controller was found, but exactly 1 is required");
        } else if (foundController > 1) {
            msg.add("{} controller were found, but exactly 1 is required");
        }
        if (msg.postIfNotEmpty()) {
            return null;
        }
        return new BlockPos(cx, cy, cz);
    }

    public void build() {
        BlockPos controller = validate();
        if (controller == null) return;

        for (int x = 0; x < this.tensor.size(); x++) {
            List<String> xLayer = this.tensor.get(x);
            for (int y = 0; y < xLayer.size(); y++) {
                String yRow = xLayer.get(y);
                for (int z = 0; z < yRow.length(); z++) {
                    char zChar = yRow.charAt(z);
                    BlockArray.BlockInformation info = this.charMap.get(zChar);
                    if (info == null || info == CONTROLLER) continue; // null -> any allowed -> don't need to check; CONTROLLER -> don't need to check
                    ComponentSelectorTag tag = this.selectorTagMap.get(zChar);
                    BlockPos pos = new BlockPos(x - controller.getX(), y - controller.getY(), z - controller.getZ());
                    blockArray.addBlock(pos, info);
                    if (tag != null) {
                        blockArray.setTag(pos, tag);
                    }
                    List<SingleBlockModifierReplacement> modifiers = this.blockModifierMap.get(pos);
                    if (modifiers != null) {
                        modifiers.forEach(modifier -> modifier.setPos(pos));
                        this.machine.getModifiers().computeIfAbsent(pos, k -> new ArrayList<>()).addAll(modifiers);
                    }
                    modifiers = this.blockModifierCharMap.get(zChar);
                    if (modifiers != null) {
                        modifiers.forEach(modifier -> modifier.setPos(pos));
                        this.machine.getModifiers().computeIfAbsent(pos, k -> new ArrayList<>()).addAll(modifiers);
                    }
                }
            }
        }
    }
}
