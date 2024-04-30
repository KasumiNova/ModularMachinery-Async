/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiContainerBase
 * Created by HellFirePvP
 * Date: 09.07.2017 / 11:20
 */
public abstract class GuiContainerBase<T extends Container> extends GuiContainer {

    public static final ResourceLocation TEXTURES_EMPTY_GUI = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guismartinterface.png");
    protected final T container;

    public GuiContainerBase(ContainerBase<?> container) {
        super(container);
        this.container = (T) container;
    }

    public GuiContainerBase(T container) {
        super(container);
        this.container = container;
    }

    public T getContainer() {
        return container;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void initGui() {
        setWidthHeight();
        super.initGui();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    protected abstract void setWidthHeight();

}
