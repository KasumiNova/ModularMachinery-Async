package github.kasuminova.mmce.client.gui.widget.preview;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.Button4State;
import github.kasuminova.mmce.client.gui.widget.Scrollbar;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Column;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static github.kasuminova.mmce.client.gui.widget.preview.MachineStructurePreviewPanel.WIDGETS_TEX_LOCATION;

public class LayerRenderScrollbar extends Column {
    protected int scrollBgTexX = 244;
    protected int scrollBgTexY = 0;
    protected int scrollBgTexYOffset = 12;
    protected int scrollBgTexWidth = 9;
    protected int scrollBgTexHeight = 90;

    protected Consumer<Integer> onScrollChanged = null;

    protected final Scrollbar scrollbar;

    public LayerRenderScrollbar(final WorldSceneRendererWidget renderer) {
        scrollbar = (Scrollbar) new Scrollbar().setMarginVertical(2);
        Button4State up = new Button4State();
        Button4State down = new Button4State();

        up.setMouseDownTextureXY(184 + 11 + 11, 121)
                .setHoveredTextureXY(184 + 11, 121)
                .setTextureXY(184, 121)
                .setUnavailableTextureXY(184 + 11 + 11 + 11, 121)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> {
                    int minY = renderer.getPattern().getMin().getY();
                    int maxY = renderer.getPattern().getMax().getY();
                    int renderLayer = scrollbar.getCurrentScroll();
                    return Arrays.asList(
                            I18n.format("gui.preview.button.layer_render_scrollbar.up.tip"),
                            I18n.format("gui.preview.button.layer_render_scrollbar.state.tip",
                                    (maxY - renderLayer) + minY, scrollbar.getMaxScroll())
                    );
                })
                .setWidthHeight(9, 9);
        down.setMouseDownTextureXY(184 + 11 + 11, 132)
                .setHoveredTextureXY(184 + 11, 132)
                .setTextureXY(184, 132)
                .setUnavailableTextureXY(184 + 11 + 11 + 11, 132)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> {
                    int minY = renderer.getPattern().getMin().getY();
                    int maxY = renderer.getPattern().getMax().getY();
                    int renderLayer = scrollbar.getCurrentScroll();
                    return Arrays.asList(
                            I18n.format("gui.preview.button.layer_render_scrollbar.down.tip"),
                            I18n.format("gui.preview.button.layer_render_scrollbar.state.tip",
                                    (maxY - renderLayer) + minY, scrollbar.getMaxScroll())
                    );
                })
                .setWidthHeight(9, 9);
        scrollbar.getScroll()
                .setMouseDownTextureXY(202, 158)
                .setHoveredTextureXY(193, 158)
                .setTextureXY(184, 158)
                .setUnavailableTextureXY(211, 158)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> {
                    int minY = renderer.getPattern().getMin().getY();
                    int maxY = renderer.getPattern().getMax().getY();
                    int renderLayer = scrollbar.getCurrentScroll();
                    return Arrays.asList(
                            I18n.format("gui.preview.button.layer_render_scrollbar.tip"),
                            I18n.format("gui.preview.button.layer_render_scrollbar.state.tip",
                                    (maxY - renderLayer) + minY, scrollbar.getMaxScroll())
                    );
                })
                .setWidthHeight(7, 15);
        scrollbar.setWidthHeight(7, 88)
                .setMarginHorizontal(1)
                .setMarginVertical(4);

        up.setOnClickedListener(btn -> scrollbar.setCurrentScroll(scrollbar.getCurrentScroll() - 1));
        down.setOnClickedListener(btn -> scrollbar.setCurrentScroll(scrollbar.getCurrentScroll() + 1));
        scrollbar.setOnValueChanged(scroll -> {
            if (onScrollChanged != null) {
                onScrollChanged.accept(scroll.getCurrentScroll());
            }
            up.setAvailable(scroll.getCurrentScroll() > scroll.getMinScroll());
            down.setAvailable(scroll.getCurrentScroll() < scroll.getMaxScroll());
        });

        addWidgets(up, scrollbar, down);
    }

    @Override
    protected void renderInternal(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        drawScrollbarBg(widgetGui, renderPos);
        super.renderInternal(widgetGui, renderSize, renderPos, mousePos);
    }

    protected void drawScrollbarBg(final WidgetGui widgetGui, final RenderPos renderPos) {
        GuiScreen gui = widgetGui.getGui();
        gui.mc.getTextureManager().bindTexture(WIDGETS_TEX_LOCATION);
        gui.drawTexturedModalRect(
                renderPos.posX(), renderPos.posY() + scrollBgTexYOffset,
                scrollBgTexX, scrollBgTexY,
                scrollBgTexWidth, scrollBgTexHeight
        );
    }

    public Consumer<Integer> getOnScrollChanged() {
        return onScrollChanged;
    }

    public LayerRenderScrollbar setOnScrollChanged(final Consumer<Integer> onScrollChanged) {
        this.onScrollChanged = onScrollChanged;
        return this;
    }

    public Scrollbar getScrollbar() {
        return scrollbar;
    }
}
