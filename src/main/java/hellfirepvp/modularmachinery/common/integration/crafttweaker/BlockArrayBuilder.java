package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.google.gson.JsonParseException;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import github.kasuminova.mmce.common.integration.Logger;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.AdvancedBlockCheckerCT;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@GroovyBlacklist
@ZenRegister
@ZenClass("mods.modularmachinery.BlockArrayBuilder")
public class BlockArrayBuilder {
    private final TaggedPositionBlockArray blockArray;
    private BlockArray.BlockInformation lastInformation = null;
    private BlockPos lastPos = null;

    private BlockArrayBuilder() {
        blockArray = new TaggedPositionBlockArray();
    }

    private BlockArrayBuilder(TaggedPositionBlockArray blockArray) {
        this.blockArray = blockArray;
    }

    @ZenMethod
    public static BlockArrayBuilder newBuilder() {
        return new BlockArrayBuilder();
    }

    @ZenMethod
    public static BlockArrayBuilder newBuilder(TaggedPositionBlockArray blockArray) {
        return new BlockArrayBuilder(blockArray);
    }

    /**
     * 添加方块至结构。
     *
     * @param x             X
     * @param y             Y
     * @param z             Z
     * @param ctBlockStates BlockState，写法请参照 <a href="https://docs.blamejared.com/1.12/en/Vanilla/Blocks/IBlockState">CT Wiki 页面</a>
     */
    @ZenMethod
    public BlockArrayBuilder addBlock(int x, int y, int z, IBlockState... ctBlockStates) {
        List<net.minecraft.block.state.IBlockState> stateList = new ArrayList<>();

        for (IBlockState ctBlockState : ctBlockStates) {
            stateList.add(CraftTweakerMC.getBlockState(ctBlockState));
        }
        List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
        for (net.minecraft.block.state.IBlockState blockState : stateList) {
            stateDescriptorList.add(IBlockStateDescriptor.of(blockState));
        }

        addBlock(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList));
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      @Nullable IData nbt, @Nullable IData previewNBT, @Nullable AdvancedBlockCheckerCT checker,
                                      IBlockState... ctBlockStates) {
        for (int x : xList) {
            for (int y : yList) {
                for (int z : zList) {
                    addBlock(x, y, z, ctBlockStates);
                    if (nbt != null) setNBT(nbt);
                    if (previewNBT != null) setPreviewNBT(previewNBT);
                    if (checker != null) setBlockChecker(checker);
                }
            }
        }
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      IData nbt, IData previewNBT,
                                      IBlockState... ctBlockStates) {
        addBlock(xList, yList, zList, nbt, previewNBT, null, ctBlockStates);
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      AdvancedBlockCheckerCT checker,
                                      IBlockState... ctBlockStates) {
        addBlock(xList, yList, zList, null, null, checker, ctBlockStates);
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList, IBlockState... ctBlockStates) {
        return addBlock(xList, yList, zList, null, null, null, ctBlockStates);
    }

    /**
     * 添加一个物品所属的方块至结构。
     *
     * @param x            X
     * @param y            Y
     * @param z            Z
     * @param ctItemStacks 物品
     */
    @SuppressWarnings("deprecation")
    @ZenMethod
    public BlockArrayBuilder addBlock(int x, int y, int z, IItemStack... ctItemStacks) {
        List<ItemStack> stackList = new ArrayList<>();
        for (IItemStack ctItemStack : ctItemStacks) {
            stackList.add(CraftTweakerMC.getItemStack(ctItemStack));
        }
        List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
        for (ItemStack stack : stackList) {
            Item item = stack.getItem();
            int meta = stack.getMetadata();
            Block block = Block.getBlockFromItem(item);
            if (block != Blocks.AIR) {
                try {
                    net.minecraft.block.state.IBlockState state = block.getStateFromMeta(meta);
                    stateDescriptorList.add(IBlockStateDescriptor.of(state));
                } catch (Exception e) {
                    Logger.error(String.format("[ModularMachinery] Failed to get BlockState from <%s>!",
                                               stack.getItem().getRegistryName() + ":" + meta
                    ));
                }
            } else {
                Logger.error("[ModularMachinery] " + stack.getDisplayName() + " cannot convert to Block!");
            }
        }

        if (!stateDescriptorList.isEmpty()) {
            addBlock(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList));
        }

        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      @Nullable IData nbt, @Nullable IData previewNBT, @Nullable AdvancedBlockCheckerCT checker,
                                      IItemStack... ctItemStacks) {
        for (int x : xList) {
            for (int y : yList) {
                for (int z : zList) {
                    addBlock(x, y, z, ctItemStacks);
                    if (nbt != null) setNBT(nbt);
                    if (previewNBT != null) setPreviewNBT(previewNBT);
                    if (checker != null) setBlockChecker(checker);
                }
            }
        }
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      IData nbt, IData previewNBT,
                                      IItemStack... ctItemStacks) {
        addBlock(xList, yList, zList, nbt, previewNBT, null, ctItemStacks);
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      AdvancedBlockCheckerCT checker,
                                      IItemStack... ctItemStacks) {
        addBlock(xList, yList, zList, null, null, checker, ctItemStacks);
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList, IItemStack... ctItemStacks) {
        return addBlock(xList, yList, zList, null, null, null, ctItemStacks);
    }

    /**
     * 添加方块至结构，方块名称写法等同于 JSON 格式的方块名。
     *
     * @param x          X
     * @param y          Y
     * @param z          Z
     * @param blockNames 名称，例如：modularmachinery:blockinputbus@1
     */
    @ZenMethod
    public BlockArrayBuilder addBlock(int x, int y, int z, String... blockNames) {
        List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
        for (String blockName : blockNames) {
            try {
                stateDescriptorList.add(BlockArray.BlockInformation.getDescriptor(blockName));
            } catch (JsonParseException e) {
                Logger.error("[ModularMachinery] " + blockName + " is invalid block!", e);
            }
        }

        if (!stateDescriptorList.isEmpty()) {
            addBlock(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList));
        }

        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      @Nullable IData nbt, @Nullable IData previewNBT, @Nullable AdvancedBlockCheckerCT checker,
                                      String... blockNames) {
        for (int x : xList) {
            for (int y : yList) {
                for (int z : zList) {
                    addBlock(x, y, z, blockNames);
                    if (nbt != null) setNBT(nbt);
                    if (previewNBT != null) setPreviewNBT(previewNBT);
                    if (checker != null) setBlockChecker(checker);
                }
            }
        }
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      IData nbt, IData previewNBT,
                                      String... blockNames) {
        addBlock(xList, yList, zList, nbt, previewNBT, null, blockNames);
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList,
                                      AdvancedBlockCheckerCT checker,
                                      String... blockNames) {
        addBlock(xList, yList, zList, null, null, checker, blockNames);
        return this;
    }

    @ZenMethod
    public BlockArrayBuilder addBlock(int[] xList, int[] yList, int[] zList, String... blockNames) {
        return addBlock(xList, yList, zList, null, null, null, blockNames);
    }

    /**
     * 设置方块的 NBT 判断，在 addBlock() 之后调用。
     *
     * @param data 要判断相同的 NBT
     */
    @ZenMethod
    public BlockArrayBuilder setNBT(IData data) {
        if (lastInformation != null) {
            NBTTagCompound tag = CraftTweakerMC.getNBTCompound(data);
            lastInformation.setMatchingTag(tag);
            if (lastInformation.getPreviewTag() == null) {
                lastInformation.setPreviewTag(tag);
            }
        }
        return this;
    }

    /**
     * 设置方块在结构预览时的 NBT 信息，在 addBlock() 之后调用。
     *
     * @param data 结构预览时的 NBT
     */
    @ZenMethod
    public BlockArrayBuilder setPreviewNBT(IData data) {
        if (lastInformation != null) {
            lastInformation.setPreviewTag(CraftTweakerMC.getNBTCompound(data));
        }
        return this;
    }

    /**
     * 设置方块高级检查器，由魔改员自行编写逻辑（非同步）。
     *
     * @param checker 函数
     */
    @ZenMethod
    public BlockArrayBuilder setBlockChecker(AdvancedBlockCheckerCT checker) {
        if (lastInformation != null) {
            lastInformation.setNBTChecker((world, pos, blockState, nbt) -> checker.isMatch(
                    CraftTweakerMC.getIWorld(world),
                    CraftTweakerMC.getIBlockPos(pos),
                    CraftTweakerMC.getBlockState(blockState),
                    CraftTweakerMC.getIData(nbt)));
        }
        return this;
    }

    /**
     * 设置 ComponentSelectorTag，用于配方寻找组件时的额外条件。
     *
     * @param tag Tag 名称
     */
    @ZenMethod
    public BlockArrayBuilder setTag(String tag) {
        if (lastInformation != null && lastPos != null) {
            blockArray.setTag(lastPos, new ComponentSelectorTag(tag));
        }
        return this;
    }

    @ZenMethod
    public TaggedPositionBlockArray getBlockArray() {
        return blockArray;
    }

    private void addBlock(BlockPos pos, BlockArray.BlockInformation information) {
        blockArray.addBlock(pos, information);
        lastInformation = information;
        lastPos = pos;
    }
}
