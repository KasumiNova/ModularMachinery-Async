package github.kasuminova.mmce.common.helper;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.block.IBlockStateMatcher;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IFacing;
import crafttweaker.api.world.IWorld;
import github.kasuminova.mmce.common.event.machine.MachineStructureUpdateEvent;
import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.machine.RecipeThread;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@ZenRegister
@ZenClass("mods.modularmachinery.IMachineController")
public interface IMachineController {

    /**
     * 获取控制器所在的世界。
     *
     * @return 世界
     */
    @Optional.Method(modid = "crafttweaker")
    @ZenGetter("world")
    IWorld getIWorld();

    World getWorld();

    /**
     * 获取控制器方块。
     *
     * @return IBlockState
     */
    @Optional.Method(modid = "crafttweaker")
    @ZenGetter("blockState")
    IBlockState getIBlockState();

    net.minecraft.block.state.IBlockState getBlockState();

    /**
     * 获取控制器处于世界中的朝向。
     *
     * @return IFacing
     */
    @Optional.Method(modid = "crafttweaker")
    @ZenGetter("facing")
    IFacing getFacing();

    EnumFacing getEnumFacing();

    /**
     * 获取控制器所在的坐标
     *
     * @return 坐标
     */
    @Optional.Method(modid = "crafttweaker")
    @ZenGetter("pos")
    IBlockPos getIPos();

    BlockPos getPos();

    /**
     * 根据控制器的朝向，将给定的坐标旋转到控制器朝向。
     *
     * @param pos 原始坐标
     * @return 旋转后的坐标
     */
    @ZenMethod
    IBlockPos rotateWithControllerFacing(final IBlockPos pos);

    /**
     * 获取机械在当前世界运行的时间（非世界时间，进入退出世界会被重置）
     */
    @ZenGetter("ticksExisted")
    int getTicksExisted();

    /**
     * 获取机器当前正在执行的配方。
     *
     * @return 配方
     * @deprecated 已弃用，请使用 {@link ActiveMachineRecipe[] getActiveRecipeList}
     */
    @Nullable
    @Deprecated
    @ZenGetter("activeRecipe")
    ActiveMachineRecipe getActiveRecipe();

    /**
     * 获取机器当前正在执行的配方列表。
     *
     * @return 配方
     */
    @ZenGetter("activeRecipeList")
    ActiveMachineRecipe[] getActiveRecipeList();

    /**
     * 获取机器所有的配方线程。
     *
     * @return 线程列表，所有元素均不为空，包括闲置线程。
     */
    @ZenGetter("recipeThreadList")
    RecipeThread[] getRecipeThreadList();

    /**
     * 获取工厂控制器的额外配方线程数。
     *
     * @return 额外配方线程数，如果机械控制器是普通控制器则始终返回 0。
     */
    @ZenGetter("extraThreadCount")
    int getExtraThreadCount();

    /**
     * 设置工厂控制器的额外配方线程数，如果机械控制器是普通控制器则此方法无效。
     */
    @ZenSetter("extraThreadCount")
    void setExtraThreadCount(int extraThreadCount);

    /**
     * 机器是否在工作。
     *
     * @return true 为工作，反之为闲置或未形成结构
     */
    @ZenGetter("isWorking")
    boolean isWorking();

    /**
     * 获取形成的机械结构名称。
     *
     * @return 机械的注册名，如果未形成结构则返回 null
     */
    @Nullable
    @ZenGetter("formedMachineName")
    String getFormedMachineName();

    /**
     * 获取自定义 NBT 信息。
     *
     * @return IData
     */
    @Optional.Method(modid = "crafttweaker")
    @ZenGetter("customData")
    IData getCustomData();

    /**
     * 设置自定义 NBT 信息。
     *
     * @param data IData
     */
    @Optional.Method(modid = "crafttweaker")
    @ZenSetter("customData")
    void setCustomData(IData data);

    NBTTagCompound getCustomNbt();

    void setCustomNbt(NBTTagCompound nbt);

    /**
     * 添加一个半永久 RecipeModifier，会在配方完成的时候自动删除。
     *
     * @param key      KEY，方便删除使用
     * @param modifier Modifier
     */

    @ZenMethod
    void addModifier(String key, RecipeModifier modifier);

    /**
     * 删除一个半永久 RecipeModifier。
     *
     * @param key KEY
     */

    @ZenMethod
    void removeModifier(String key);

    /**
     * 添加一个永久性的 RecipeModifier。
     *
     * @param key      KEY，方便删除使用
     * @param modifier Modifier
     */
    @ZenMethod
    void addPermanentModifier(String key, RecipeModifier modifier);

    /**
     * 删除一个永久性的 RecipeModifier。
     *
     * @param key KEY
     */
    @ZenMethod
    void removePermanentModifier(String key);

    /**
     * 检查某个 RecipeModifier 是否已存在。
     *
     * @param key KEY
     * @return 存在返回 true，反之 false
     */
    @ZenMethod
    boolean hasModifier(String key);

    /**
     * 覆盖控制器的状态消息。
     *
     * @param newInfo 新消息
     */
    @Deprecated
    @ZenSetter("statusMessage")
    void overrideStatusInfo(String newInfo);

    /**
     * 获取控制器绑定的指定智能数据接口数据。
     *
     * @param type 类型过滤
     * @return 智能数据接口的内部数据，如果没有则为 null
     */
    @Nullable
    @ZenMethod
    SmartInterfaceData getSmartInterfaceData(String type);

    /**
     * 获取控制器绑定的所有智能数据接口数据。
     *
     * @return 一组智能数据接口的内部数据，如果没有则为空数组，但不会为 null
     */
    @ZenGetter("smartInterfaceDataList")
    SmartInterfaceData[] getSmartInterfaceDataList();

    /**
     * 获取控制器检测到的配方修改器升级名称。
     *
     * @return 返回找到的所有升级名称，如果没有则为空数组，但不会为 null
     */
    @ZenGetter("foundModifiers")
    String[] getFoundModifierReplacements();

    /**
     * 获取机械中的某个 modifierReplacement 是否存在
     */
    @ZenMethod
    boolean hasModifierReplacement(String modifierName);

    /**
     * 机械是否存在给定名称的机械升级。
     *
     * @param upgradeName 名称
     * @return 存在返回 true，否则返回 false
     */
    @ZenMethod
    boolean hasMachineUpgrade(String upgradeName);

    /**
     * 根据给定的名称，获取控制器中的一类机械升级。
     * 如果不存在时不返回 null，返回空数组。
     *
     * @param upgradeName 名称
     * @return 机械升级，如果无则返回空数组
     */
    @ZenMethod
    MachineUpgrade[] getMachineUpgrade(String upgradeName);

    /**
     * 根据给定的名称，获取控制器对应机械的一个动态结构。
     * 如果不存在给定的动态结构，则返回 null。
     *
     * @param patternName 名称
     * @return 动态结构信息，如果无则返回 null
     */
    @Nullable
    @ZenMethod
    IDynamicPatternInfo getDynamicPattern(String patternName);

    /**
     * 获取控制器的结构中指定方块的存在数量，只能在控制器成型时使用。<br/>
     * 会过滤空气方块。<br/>
     * 注意：此操作的性能开销稍大，通常情况下只建议在 {@link MachineStructureUpdateEvent} 事件触发时使用。
     *
     * @param blockStack 要判断的方块对应的物品，会被自动转换成对应的 IBlockState，如果转换出现问题会输出错误日志。
     * @return 存在数量。
     */
    @net.minecraftforge.fml.common.Optional.Method(modid = "crafttweaker")
    @ZenMethod
    int getBlocksInPattern(final IItemStack blockStack);

    int getBlocksInPattern(ItemStack itemStack);

    /**
     * 获取控制器的结构中指定方块的存在数量，只能在控制器成型时使用。<br/>
     * 会过滤空气方块。<br/>
     * 注意：此操作的<strong>性能开销稍大</strong>，通常情况下只建议在 {@link MachineStructureUpdateEvent} 事件触发时使用。
     *
     * @param blockStateMatcher 要判断的方块对应的 IBlockStateMatcher
     * @return 存在数量。
     */
    @net.minecraftforge.fml.common.Optional.Method(modid = "crafttweaker")
    @ZenMethod
    int getBlocksInPattern(final IBlockStateMatcher blockStateMatcher);

    /**
     * 获取控制器的结构中指定方块的存在数量，只能在控制器成型时使用。<br/>
     * 会过滤空气方块。<br/>
     * 注意：此操作的<strong>性能开销稍大</strong>，通常情况下只建议在 {@link MachineStructureUpdateEvent} 事件触发时使用。
     *
     * @param blockName 要判断的方块对应的注册名，解析方式参考机械 JSON 文件的格式。
     * @return 存在数量。
     */
    @ZenMethod
    int getBlocksInPattern(final String blockName);

    /**
     * 获取控制器的结构中指定方块的存在数量，只能在控制器成型时使用。<br/>
     * 会过滤空气方块。<br/>
     * 注意：此操作的<strong>性能开销稍大</strong>，通常情况下只建议在 {@link MachineStructureUpdateEvent} 事件触发时使用。
     *
     * @param predicate 自定义判断逻辑。
     * @return 存在数量。
     */
    @net.minecraftforge.fml.common.Optional.Method(modid = "crafttweaker")
    @ZenMethod
    int getBlocksInPattern(final IBlockStatePredicate predicate);

    int getBlocksInPattern(Predicate<net.minecraft.block.state.IBlockState> predicate);

    /**
     * 获取控制器的拥有者，如果玩家不在线则返回 null。
     *
     * @return 玩家
     */
    @Nullable
    @ZenGetter("ownerPlayer")
    IPlayer getOwnerIPlayer();

    /**
     * 获取控制器的拥有者名称，如果服务端无缓存则返回 null。
     *
     * @return 玩家名称
     */
    @Nullable
    @ZenGetter("ownerName")
    String getOwnerName();

    /**
     * 获取控制器的拥有者 UUID，如果没有所有者则返回 null。
     *
     * @return 玩家 UUID
     */
    @Nullable
    @ZenGetter("ownerUUID")
    String getOwnerUUIDString();

    TileMultiblockMachineController getController();
}
