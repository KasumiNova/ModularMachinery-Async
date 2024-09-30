package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.registry.NamedRegistry;
import github.kasuminova.mmce.common.capability.CapabilityUpgrade;
import github.kasuminova.mmce.common.integration.Logger;
import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import github.kasuminova.mmce.common.upgrade.SimpleDynamicMachineUpgrade;
import github.kasuminova.mmce.common.upgrade.SimpleMachineUpgrade;
import github.kasuminova.mmce.common.upgrade.registry.RegistryUpgrade;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.upgrade.MachineUpgradeBuilder;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class MachineUpgrades extends NamedRegistry {

    public MachineUpgradeBuilder builder(String name, String localizedName, float level, int maxStack) {
        return MachineUpgradeBuilder.newBuilder(name, localizedName, level, maxStack);
    }

    /**
     * 为一个物品注册升级能力，只有在注册了之后才能够添加升级。
     *
     * @param stack 物品
     */
    public void registerSupportedItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            RegistryUpgrade.addSupportedItem(stack);
        }
    }

    /**
     * 为一个物品添加固定的机械升级，会在物品被创建时自动添加物品。
     *
     * @param stack 物品
     * @param upgradeName 升级名称
     */
    public void addFixedUpgrade(ItemStack stack, String upgradeName) {
        if (stack.isEmpty()) {
            return;
        }
        MachineUpgrade upgrade = RegistryUpgrade.getUpgrade(upgradeName);
        if (upgrade == null) {
            Logger.error("Cloud not find MachineUpgrade " + upgradeName + '!');
            return;
        }
        RegistryUpgrade.addFixedUpgrade(stack, upgrade);
    }

    /**
     * 将一个升级应用至机械升级，相当于直接写入相关升级的 NBT.
     *
     * @param stack  物品
     * @param upgradeName 名称
     * @return 添加了目标机械升级的物品。
     */
    public ItemStack addUpgradeToIItemStack(ItemStack stack, String upgradeName) {
        if (!RegistryUpgrade.supportsUpgrade(stack)) {
            Logger.warn(stack.getItem().getRegistryName() + " does not support upgrade!");
            return stack;
        }
        CapabilityUpgrade capability = stack.getCapability(CapabilityUpgrade.MACHINE_UPGRADE_CAPABILITY, null);
        if (capability == null) {
            return stack;
        }
        MachineUpgrade upgrade = RegistryUpgrade.getUpgrade(upgradeName);
        if (upgrade == null) {
            Logger.warn("Cloud not found MachineUpgrade " + upgradeName + '!');
            return stack;
        }
        capability.getUpgrades().add(upgrade);
        return stack;
    }

    /**
     * 获取一个已注册的机械升级。
     *
     * @param upgradeName 升级名称。
     * @return 机械升级，如果无则为 null
     */
    @Nullable
    public MachineUpgrade getUpgrade(String upgradeName) {
        return RegistryUpgrade.getUpgrade(upgradeName);
    }

    /**
     * 将 MachineUpgrade 转换为 SimpleDynamicMachineUpgrade
     *
     * @param upgrade 升级
     * @return 强制转换后的升级，如果不支持则为 null。
     */
    public SimpleDynamicMachineUpgrade castToSimpleDynamicMachineUpgrade(MachineUpgrade upgrade) {
        return upgrade instanceof SimpleDynamicMachineUpgrade ? (SimpleDynamicMachineUpgrade) upgrade : null;
    }

    /**
     * 将 MachineUpgrade 转换为 SimpleMachineUpgrade
     *
     * @param upgrade 升级
     * @return 强制转换后的升级，如果不支持则为 null。
     */
    public SimpleMachineUpgrade castToSimpleMachineUpgrade(MachineUpgrade upgrade) {
        return upgrade instanceof SimpleMachineUpgrade ? (SimpleMachineUpgrade) upgrade : null;
    }
}
