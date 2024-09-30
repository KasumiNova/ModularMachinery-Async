package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.google.gson.JsonParseException;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.util.IAxisAlignedBB;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelAnimationEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelGetEvent;
import github.kasuminova.mmce.common.event.machine.MachineStructureFormedEvent;
import github.kasuminova.mmce.common.event.machine.MachineStructureUpdateEvent;
import github.kasuminova.mmce.common.event.machine.MachineTickEvent;
import github.kasuminova.mmce.common.event.machine.SmartInterfaceUpdateEvent;
import github.kasuminova.mmce.common.integration.Logger;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.AdvancedBlockCheckerCT;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.RecipeFailureActions;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.modifier.MultiBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.*;

@GroovyBlacklist
@ZenRegister
@ZenClass("mods.modularmachinery.MachineBuilder")
public class MachineBuilder {
    public static final List<DynamicMachine> WAIT_FOR_LOAD = new ArrayList<>();
    public static final Map<ResourceLocation, MachineBuilder> PRE_LOAD_MACHINES = new HashMap<>();
    private final DynamicMachine machine;
    private final TaggedPositionBlockArray pattern;
    private BlockPos lastPos = null;
    private BlockArray.BlockInformation lastInformation = null;

    private MachineBuilder(String registryName, String localizedName) {
        this.machine = new DynamicMachine(registryName);
        this.pattern = this.machine.getPattern();

        this.machine.setLocalizedName(localizedName);
    }

    private MachineBuilder(
            String registryName,
            String localizedName,
            boolean hasFactory,
            boolean factoryOnly)
    {
        this.machine = new DynamicMachine(registryName);
        this.pattern = this.machine.getPattern();

        this.machine.setLocalizedName(localizedName);
        this.machine.setHasFactory(hasFactory);
        this.machine.setFactoryOnly(factoryOnly);
    }

    private MachineBuilder(
            String registryName,
            String localizedName,
            boolean requiresBlueprint,
            RecipeFailureActions failureAction,
            int color)
    {
        this.machine = new DynamicMachine(registryName);
        this.pattern = this.machine.getPattern();

        this.machine.setLocalizedName(localizedName);
        this.machine.setFailureAction(failureAction);
        this.machine.setDefinedColor(color);
        this.machine.setRequiresBlueprint(requiresBlueprint);
    }

    private MachineBuilder(
            String registryName,
            String localizedName,
            boolean requiresBlueprint,
            RecipeFailureActions failureAction,
            int color,
            boolean hasFactory,
            boolean factoryOnly)
    {
        this.machine = new DynamicMachine(registryName);
        this.pattern = this.machine.getPattern();

        this.machine.setLocalizedName(localizedName);
        this.machine.setFailureAction(failureAction);
        this.machine.setDefinedColor(color);
        this.machine.setRequiresBlueprint(requiresBlueprint);
        this.machine.setHasFactory(hasFactory);
        this.machine.setFactoryOnly(factoryOnly);
    }

    /**
     * 注册一个新的机械构建器。
     * 此方法应在 preInit 阶段调用！
     *
     * @param registryName  注册名
     * @param localizedName 译名
     * @param hasFactory 是否注册工厂
     * @param factoryOnly 是否仅注册工厂
     */
    @ZenMethod
    public static void registerMachine(String registryName, String localizedName, boolean hasFactory, boolean factoryOnly) {
        if (PRE_LOAD_MACHINES.containsKey(new ResourceLocation(ModularMachinery.MODID, registryName))) {
            return;
        }
        MachineBuilder builder = new MachineBuilder(registryName, localizedName, hasFactory, factoryOnly);
        PRE_LOAD_MACHINES.put(builder.machine.getRegistryName(), builder);
    }

    /**
     * 注册一个新的机械构建器。
     * 此方法应在 preInit 阶段调用！
     *
     * @param registryName  注册名
     * @param localizedName 译名
     */
    @ZenMethod
    public static void registerMachine(String registryName, String localizedName) {
        if (PRE_LOAD_MACHINES.containsKey(new ResourceLocation(ModularMachinery.MODID, registryName))) {
            return;
        }
        MachineBuilder builder = new MachineBuilder(registryName, localizedName);
        PRE_LOAD_MACHINES.put(builder.machine.getRegistryName(), builder);
    }

    /**
     * 注册一个新的机械构建器。
     * 此方法应在 preInit 阶段调用！
     *
     * @param registryName      注册名
     * @param localizedName     译名
     * @param requiresBlueprint 是否需要蓝图
     * @param failureAction     失败操作
     * @param color             颜色
     * @param hasFactory 是否注册工厂
     * @param factoryOnly 是否仅注册工厂
     */
    @ZenMethod
    public static void registerMachine(
            String registryName,
            String localizedName,
            boolean requiresBlueprint,
            RecipeFailureActions failureAction,
            int color,
            boolean hasFactory,
            boolean factoryOnly)
    {
        if (PRE_LOAD_MACHINES.containsKey(new ResourceLocation(ModularMachinery.MODID, registryName))) {
            return;
        }
        MachineBuilder builder = new MachineBuilder(
                registryName, localizedName, requiresBlueprint, failureAction, color, hasFactory, factoryOnly);
        PRE_LOAD_MACHINES.put(builder.machine.getRegistryName(), builder);
    }

    /**
     * 注册一个新的机械构建器。
     * 此方法应在 preInit 阶段调用！
     *
     * @param registryName      注册名
     * @param localizedName     译名
     * @param requiresBlueprint 是否需要蓝图
     * @param failureAction     失败操作
     * @param color             颜色
     */
    @ZenMethod
    public static void registerMachine(String registryName, String localizedName, boolean requiresBlueprint, RecipeFailureActions failureAction, int color) {
        if (PRE_LOAD_MACHINES.containsKey(new ResourceLocation(ModularMachinery.MODID, registryName))) {
            return;
        }
        MachineBuilder builder = new MachineBuilder(registryName, localizedName, requiresBlueprint, failureAction, color);
        PRE_LOAD_MACHINES.put(builder.machine.getRegistryName(), builder);
    }

    /**
     * 获取在 preInit 注册的机械构建器。
     *
     * @param registryName 注册名
     * @return 机械构建器，若先前未注册则返回 null。
     */
    @ZenMethod
    public static MachineBuilder getBuilder(String registryName) {
        return PRE_LOAD_MACHINES.get(new ResourceLocation(ModularMachinery.MODID, registryName));
    }

    /**
     * 获取机械结构组成。
     */
    @ZenMethod
    public TaggedPositionBlockArray getBlockArray() {
        return pattern;
    }

    /**
     * 获取机械结构组成构建器。
     */
    @ZenMethod
    public BlockArrayBuilder getBlockArrayBuilder() {
        return BlockArrayBuilder.newBuilder(pattern);
    }

    /**
     * 设置此机械是否受并行控制器影响。
     */
    @ZenMethod
    public MachineBuilder setParallelizable(boolean isParallelizable) {
        machine.setParallelizable(isParallelizable);
        return this;
    }

    /**
     * 设置此机械的最大并行数。
     * @param maxParallelism 并行数
     */
    @ZenMethod
    public MachineBuilder setMaxParallelism(int maxParallelism) {
        machine.setMaxParallelism(maxParallelism);
        return this;
    }

    /**
     * 设置此机械的内置并行数。
     *
     * @param parallelism 内置并行数
     */
    @ZenMethod
    public MachineBuilder setInternalParallelism(int parallelism) {
        machine.setInternalParallelism(parallelism);
        return this;
    }

    /**
     * 添加单方块配方修改器。
     *
     * @param x            X
     * @param y            Y
     * @param z            Z
     * @param ctBlockState BlockState，写法请参照 <a href="https://docs.blamejared.com/1.12/en/Vanilla/Blocks/IBlockState">CT Wiki 页面</a>
     * @param description  描述
     * @param modifiers    修改器列表
     */
    @ZenMethod
    public MachineBuilder addSingleBlockModifier(int x, int y, int z, IBlockState ctBlockState, String description, RecipeModifier... modifiers) {
        List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
        stateDescriptorList.add(IBlockStateDescriptor.of(CraftTweakerMC.getBlockState(ctBlockState)));
        addSingleBlockModifier(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList), description, modifiers);

        return this;
    }

    /**
     * 添加单方块配方修改器，判断方块为传入的物品所属的方块。
     *
     * @param x           X
     * @param y           Y
     * @param z           Z
     * @param ctItemStack 物品
     * @param description 描述
     * @param modifiers   修改器列表
     */
    @ZenMethod
    public MachineBuilder addSingleBlockModifier(int x, int y, int z, IItemStack ctItemStack, String description, RecipeModifier... modifiers) {
        ItemStack item = CraftTweakerMC.getItemStack(ctItemStack);
        Block block = Block.getBlockFromItem(item.getItem());
        if (block != Blocks.AIR) {
            List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
            stateDescriptorList.add(IBlockStateDescriptor.of(block));
            addSingleBlockModifier(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList), description, modifiers);
        } else {
            Logger.error(item.getDisplayName() + " cannot convert to Block!");
        }

        return this;
    }

    /**
     * 添加单方块配方修改器，判断方块为传入的方块名称。方块名称写法等同于 JSON 格式的方块名。
     *
     * @param x           X
     * @param y           Y
     * @param z           Z
     * @param blockName   名称，例如：modularmachinery:blockinputbus@1
     * @param description 描述
     * @param modifiers   修改器列表
     */
    @ZenMethod
    public MachineBuilder addSingleBlockModifier(int x, int y, int z, String blockName, String description, RecipeModifier... modifiers) {
        List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();

        try {
            stateDescriptorList.add(BlockArray.BlockInformation.getDescriptor(blockName));
            addSingleBlockModifier(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList), description, modifiers);
        } catch (JsonParseException e) {
            Logger.error(blockName + " is invalid block!", e);
        }

        return this;
    }

    /**
     * 添加多方块升级配方修改器。
     */
    @ZenMethod
    public MachineBuilder addMultiBlockModifier(MultiBlockModifierReplacement multiBlockModifier) {
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
    @ZenMethod
    public MachineBuilder addSmartInterfaceType(SmartInterfaceType type) {
        if (!machine.hasSmartInterfaceType(type.getType())) {
            machine.addSmartInterfaceType(type);
        } else {
            Logger.warn("DynamicMachine `" + machine.getRegistryName() + "` is already has SmartInterfaceType `" + type.getType() + "`!");
        }
        return this;
    }

    /**
     * 添加结构形成事件监听器。
     */
    @ZenMethod
    public MachineBuilder addStructureFormedHandler(IEventHandler<MachineStructureFormedEvent> function) {
        machine.addMachineEventHandler(MachineStructureFormedEvent.class, function::handle);
        return this;
    }

    /**
     * 添加结构更新事件监听器。
     */
    @ZenMethod
    public MachineBuilder addStructureUpdateHandler(IEventHandler<MachineStructureUpdateEvent> function) {
        machine.addMachineEventHandler(MachineStructureUpdateEvent.class, function::handle);
        return this;
    }

    /**
     * 添加机器事件监听器。
     */
    @ZenMethod
    public MachineBuilder addTickHandler(IEventHandler<MachineTickEvent> function) {
        machine.addMachineEventHandler(MachineTickEvent.class, function::handle);
        return this;
    }

    /**
     * 添加控制器 GUI 渲染事件监听器。
     */
    @ZenMethod
    public MachineBuilder addGUIRenderHandler(IEventHandler<ControllerGUIRenderEvent> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerGUIRenderEvent.class, function::handle);
        return this;
    }

    /**
     * 添加控制器 GeckoLib 模型动画事件监听器。
     */
    @ZenMethod
    @Optional.Method(modid = "geckolib3")
    public MachineBuilder addControllerModelAnimationHandler(IEventHandler<ControllerModelAnimationEvent> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerModelAnimationEvent.class, function::handle);
        return this;
    }

    /**
     * 添加控制器 GeckoLib 模型获取事件监听器。
     */
    @ZenMethod
    @Optional.Method(modid = "geckolib3")
    public MachineBuilder addControllerModelGetHandler(IEventHandler<ControllerModelGetEvent> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerModelGetEvent.class, function::handle);
        return this;
    }

    /**
     * 添加智能数据接口更新事件监听器
     */
    @ZenMethod
    public MachineBuilder addSmartInterfaceUpdateHandler(IEventHandler<SmartInterfaceUpdateEvent> function) {
        machine.addMachineEventHandler(SmartInterfaceUpdateEvent.class, function::handle);
        return this;
    }

    /**
     * 设置方块的 NBT 判断，在 addBlock() 之后调用。
     *
     * @param data 要判断相同的 NBT
     */
    @ZenMethod
    public MachineBuilder setNBT(IData data) {
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
    public MachineBuilder setPreviewNBT(IData data) {
        if (lastInformation != null) {
            lastInformation.setPreviewTag(CraftTweakerMC.getNBTCompound(data));
        }
        return this;
    }

    /**
     * 设置方块高级检查器，由魔改员自行编写逻辑（非异步）。
     *
     * @param checker 函数
     */
    @ZenMethod
    public MachineBuilder setBlockChecker(AdvancedBlockCheckerCT checker) {
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
     * 控制器是否需要蓝图
     */
    @ZenMethod
    public MachineBuilder setRequiresBlueprint(boolean requiresBlueprint) {
        this.machine.setRequiresBlueprint(requiresBlueprint);
        return this;
    }

    /**
     * 设置当此机器配方运行失败时的操作
     *
     * @param failureAction Action 可以通过 RecipeFailureActions.getFailureAction(String key) 获得
     */
    @ZenMethod
    public MachineBuilder setFailureAction(RecipeFailureActions failureAction) {
        this.machine.setFailureAction(failureAction);
        return this;
    }

    /**
     * 设置机械颜色，该结构内的其他组件也将会变为此颜色。
     *
     * @param color 颜色，例如：0xFFFFFF
     */
    @ZenMethod
    public MachineBuilder setColor(int color) {
        this.machine.setDefinedColor(color);
        return this;
    }

    /**
     * 设置此机械是否有工厂形式的控制器。
     *
     * @param hasFactory true 即为注册，false 即为不注册
     */
    @ZenMethod
    public MachineBuilder setHasFactory(boolean hasFactory) {
        this.machine.setHasFactory(hasFactory);
        return this;
    }

    /**
     * 设置此机械是否仅有工厂形式的控制器。
     *
     * @param factoryOnly true 即为仅工厂，false 即为普通机械和工厂
     */
    @ZenMethod
    public MachineBuilder setFactoryOnly(boolean factoryOnly) {
        this.machine.setFactoryOnly(factoryOnly);
        return this;
    }

    /**
     * 设置此机械的工厂最大线程数。
     *
     * @param maxThreads 最大线程数
     */
    @ZenMethod
    public MachineBuilder setMaxThreads(int maxThreads) {
        this.machine.setMaxThreads(maxThreads);
        return this;
    }

    @ZenMethod
    public MachineBuilder addCoreThread(FactoryRecipeThread thread) {
        this.machine.addCoreThread(thread);
        return this;
    }

    @ZenMethod
    public MachineBuilder setControllerBoundingBox(IAxisAlignedBB aabb) {
        this.machine.setControllerBoundingBox(CraftTweakerMC.getAxisAlignedBB(aabb));
        return this;
    }

    /**
     * 注册此机械。
     */
    @ZenMethod
    public void build() {
        WAIT_FOR_LOAD.add(machine);
    }

    private void addSingleBlockModifier(BlockPos pos, BlockArray.BlockInformation information, String description, RecipeModifier... modifiers) {
        Map<BlockPos, List<SingleBlockModifierReplacement>> modifierReplacements = this.machine.getModifiers();
        modifierReplacements.putIfAbsent(pos, new ArrayList<>());
        modifierReplacements.get(pos).add(new SingleBlockModifierReplacement(information, Arrays.asList(modifiers), description).setPos(pos));
        lastInformation = information;
        lastPos = pos;
    }

    public DynamicMachine getMachine() {
        return machine;
    }
}
