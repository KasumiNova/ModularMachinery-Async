package github.kasuminova.mmce.common.event.machine;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.handler.UpgradeMachineEventHandler;
import github.kasuminova.mmce.common.helper.IMachineController;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTileNotifiable;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.Event;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineEvent")
public class MachineEvent extends Event {
    protected final TileMultiblockMachineController controller;

    public MachineEvent(TileMultiblockMachineController controller) {
        this.controller = controller;
    }

    @ZenGetter("controller")
    public IMachineController getIMachineController() {
        return controller;
    }

    public TileMultiblockMachineController getController() {
        return controller;
    }

    public void postEvent() {
        try {
//            ModularMachinery.EVENT_BUS.post(this);
            postEventToComponents();
            UpgradeMachineEventHandler.onMachineEvent(this);
            if (!isCanceled()) {
                postCrTEvent();
            }
        } catch (Exception e) {
            ModularMachinery.log.warn("Caught an exception when post event!", e);
        }
    }

    public void postEventToComponents() {
        for (TileEntity tileEntity : controller.getFoundComponents().keySet()) {
            if (tileEntity instanceof final MachineComponentTileNotifiable componentTile) {
                componentTile.onMachineEvent(this);
                if (isCanceled()) {
                    break;
                }
            }
        }
    }

    public void postCrTEvent() {
        DynamicMachine foundMachine = controller.getFoundMachine();
        if (foundMachine == null) {
            return;
        }
        List<IEventHandler<MachineEvent>> handlers = foundMachine.getMachineEventHandlers(getClass());
        if (handlers == null) {
            return;
        }

        for (IEventHandler<MachineEvent> handler : handlers) {
            handler.invoke(this);
            if (isCanceled()) {
                break;
            }
        }
    }

    @Override
    @ZenSetter("canceled")
    public void setCanceled(boolean cancel) {
        super.setCanceled(cancel);
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

}
