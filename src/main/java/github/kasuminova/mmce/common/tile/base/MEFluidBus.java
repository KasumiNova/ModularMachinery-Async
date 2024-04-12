package github.kasuminova.mmce.common.tile.base;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.util.IConfigManager;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import github.kasuminova.mmce.common.util.AEFluidInventoryUpgradeable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.stream.IntStream;

public abstract class MEFluidBus extends MEMachineComponent implements
        IAEFluidInventory,
        IUpgradeableHost,
        IConfigManagerHost,
        IAEAppEngInventory {

    public static final int TANK_SLOT_AMOUNT = 9;
    public static final int TANK_DEFAULT_CAPACITY = 8000;

    protected final IFluidStorageChannel channel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    protected final ConfigManager cm = new ConfigManager(this);
    protected final BitSet changedSlots = new BitSet();
    protected final UpgradeInventory upgrades;
    protected final AEFluidInventoryUpgradeable tanks;
    protected int fullCheckCounter = 5;

    public MEFluidBus() {
        this.tanks = new AEFluidInventoryUpgradeable(this, TANK_SLOT_AMOUNT, TANK_DEFAULT_CAPACITY);
        this.upgrades = new StackUpgradeInventory(proxy.getMachineRepresentation(), this, 5);
    }

    protected int[] getNeedUpdateSlots() {
        fullCheckCounter++;
        if (fullCheckCounter >= 5) {
            fullCheckCounter = 0;
            return IntStream.range(0, tanks.getSlots()).toArray();
        }
        return changedSlots.stream().toArray();
    }

    public IAEFluidTank getTanks() {
        return tanks;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        if (capability == cap) {
            return cap.cast(tanks);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        upgrades.readFromNBT(compound, "upgrades");
        tanks.readFromNBT(compound, "tanks");
        updateTankCapacity();
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        upgrades.writeToNBT(compound, "upgrades");
        tanks.writeToNBT(compound, "tanks");
    }

    // AE Compat

    @Override
    public IConfigManager getConfigManager() {
        return cm;
    }

    @Override
    public IItemHandlerModifiable getInventoryByName(final String name) {
        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        return null;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        if (upgrades == null) {
            return 0;
        }
        return upgrades.getInstalledUpgrades( u );
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {

    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        updateTankCapacity();
        markForUpdateSync();
    }

    private void updateTankCapacity() {
        tanks.setCapacity(
                (int) (Math.pow(4, getInstalledUpgrades(Upgrades.CAPACITY) + 1) * (MEFluidBus.TANK_DEFAULT_CAPACITY / 4)));
    }

    @Override
    public void onFluidInventoryChanged(final IAEFluidTank inv, final int slot) {
        changedSlots.set(slot);
        markNoUpdateSync();
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public TileEntity getTile() {
        return this;
    }
}
