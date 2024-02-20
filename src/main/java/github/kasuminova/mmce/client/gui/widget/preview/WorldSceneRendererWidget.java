package github.kasuminova.mmce.client.gui.widget.preview;

import com.cleanroommc.client.preview.renderer.scene.ISceneRenderHook;
import com.cleanroommc.client.preview.renderer.scene.ImmediateWorldSceneRenderer;
import com.cleanroommc.client.preview.renderer.scene.WorldSceneRenderer;
import com.cleanroommc.client.util.BlockInfo;
import com.cleanroommc.client.util.TrackedDummyWorld;
import com.cleanroommc.client.util.world.LRDummyWorld;
import github.kasuminova.mmce.client.gui.util.AnimationValue;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.mmce.client.gui.widget.event.WorldRendererCacheCleanEvent;
import github.kasuminova.mmce.common.util.DynamicPattern;
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Consumer;

public class WorldSceneRendererWidget extends DynamicWidget {
    protected final DynamicMachine machine;
    protected final WorldSceneRenderer renderer = new ImmediateWorldSceneRenderer(
            new LRDummyWorld(new TrackedDummyWorld(), new TrackedDummyWorld())
    );

    protected BlockArray pattern = null;

    protected BlockPos offset = BlockPos.ORIGIN;
    protected boolean useLayerRender = false;
    protected int renderLayer = 0;

    protected int dynamicPatternSize = 0;
    protected boolean resetZoom = false;

    protected boolean structureFormed = false;

    protected Vector3f center = new Vector3f();
    protected float rotationYaw = 25;
    protected float rotationPitch = -135;

    protected AnimationValue zoom = AnimationValue.ofFinished(5, 200, .25, .1, .25, 1);

    protected boolean dragging;
    protected int lastClickedMouseX = 0;
    protected int lastClickedMouseY = 0;

    protected int lastMouseX;
    protected int lastMouseY;

    protected boolean requireRefreshPattern = false;

    protected boolean cycleBlocks = true;
    protected long tickSnap = ClientScheduler.getClientTick();

    protected long lastPatternUpdate = -1;

    protected Consumer<WorldSceneRendererWidget> onPatternUpdate = null;
    protected Consumer<BlockPos> onBlockSelected = null;

    public WorldSceneRendererWidget(final DynamicMachine machine) {
        this.machine = machine;
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
        initRenderer();
    }

    /**
     * Prevents coordinates from being too low for a block to be placed into the dummy world.
     */
    protected static int getYOffset(final BlockPos pos) {
        int y = pos.getY();
        if (y < 0) {
            return -y;
        }
        return 0;
    }

    protected void initPattern(final DynamicMachine machine, final boolean resetZoom) {
        initializePattern(machine);

        TrackedDummyWorld world = renderer.getLRDummyWorld().getAnotherWorld();
        Map<BlockPos, BlockInfo> converted = new HashMap<>();
        BlockPos min = pattern.getMin();
        BlockPos max = pattern.getMax();
        offset = new BlockPos(0, getYOffset(min), 0);
        pattern.getPattern().forEach((pos, info) -> {
            if (useLayerRender && pos.getY() != renderLayer) {
                return;
            }
            IBlockState sampleState = info.getSampleState(tickSnap);
            TileEntity te = null;
            Block block = sampleState.getBlock();
            if (block.hasTileEntity(sampleState)) {
                te = block.createTileEntity(world, sampleState);
            }
            converted.put(pos.add(offset), new BlockInfo(sampleState, te));
        });

        world.addBlocks(converted);
        preInitNextRenderedCore(min.add(offset), max.add(offset), converted.keySet(), (_1, _2, _3) -> {}, this.resetZoom || resetZoom);
        this.resetZoom = false;
        if (onPatternUpdate != null) {
            onPatternUpdate.accept(this);
        }
    }

    private void initializePattern(final DynamicMachine machine) {
        pattern = new BlockArray(machine.getPattern());
        addDynamicPatternToPattern(machine);
        addControllerToPattern(machine);
    }

    protected void addDynamicPatternToPattern(final DynamicMachine machine) {
        Map<String, DynamicPattern> dynamicPatterns = machine.getDynamicPatterns();
        for (final DynamicPattern pattern : dynamicPatterns.values()) {
            this.dynamicPatternSize = Math.max(dynamicPatternSize, pattern.getMinSize());
        }
        for (final DynamicPattern pattern : dynamicPatterns.values()) {
            pattern.addPatternToBlockArray(
                    this.pattern,
                    Math.min(Math.max(pattern.getMinSize(), dynamicPatternSize), pattern.getMaxSize()),
                    pattern.getFaces().iterator().next(),
                    EnumFacing.NORTH);
        }
    }

    protected void addControllerToPattern(DynamicMachine machine) {
        // Factory Only
        if (machine.isHasFactory() && machine.isFactoryOnly()) {
            BlockFactoryController factory = BlockFactoryController.getControllerWithMachine(machine);
            if (factory == null) factory = BlocksMM.blockFactoryController;
            pattern.addBlock(BlockPos.ORIGIN, new BlockArray.BlockInformation(
                    Collections.singletonList(new IBlockStateDescriptor(factory.getDefaultState()))));
            return;
        }

        List<IBlockStateDescriptor> descriptors = new ArrayList<>();

        // Controller
        BlockController ctrl = BlockController.getControllerWithMachine(machine);
        if (ctrl == null) ctrl = BlocksMM.blockController;
        descriptors.add(new IBlockStateDescriptor(ctrl.getDefaultState()));

        // Factory
        if (machine.isHasFactory() || Config.enableFactoryControllerByDefault) {
            BlockFactoryController factory = BlockFactoryController.getControllerWithMachine(machine);
            if (factory == null) factory = BlocksMM.blockFactoryController;
            descriptors.add(new IBlockStateDescriptor(factory.getDefaultState()));
        }

        pattern.addBlock(new BlockPos(BlockPos.ORIGIN), new BlockArray.BlockInformation(descriptors));
    }

    protected void initRenderer() {
        initPattern(machine, true);
        renderer.setOnLookingAt(ray -> {});
        renderer.setCameraLookAt(center, zoom.get(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        renderer.useCacheBuffer(true);
        lastPatternUpdate = System.currentTimeMillis();
    }

    @Override
    public void update(final WidgetGui gui) {
        super.update(gui);
        if ((System.currentTimeMillis() - lastPatternUpdate >= 1500 && cycleBlocks) || requireRefreshPattern) {
            if (cycleBlocks) {
                tickSnap = ClientScheduler.getClientTick();
            }
            refreshPattern();
        }
    }

    protected void refreshPattern() {
        if (!renderer.isCompiling() && !renderer.isCompilerThreadAlive()) {
            renderer.getLRDummyWorld().setAnotherWorld(new TrackedDummyWorld());
            initPattern(machine, false);
            renderer.needCompileCache();
            requireRefreshPattern = false;
            lastPatternUpdate = System.currentTimeMillis();
        } else {
            requireRefreshPattern = true;
        }
    }

    @Override
    public void preRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        handleZoomAnim();
        handleMouseMove();

        final int guiLeft = gui.getGuiLeft();
        final int guiTop = gui.getGuiTop();

        RenderPos renderOffset = new RenderPos(guiLeft, guiTop);
        RenderPos realRenderPos = renderPos.add(renderOffset);
        MousePos realMousePos = mousePos.add(renderPos).add(renderOffset);

        renderer.render(
                realRenderPos.posX(), realRenderPos.posY(),
                renderSize.width(), renderSize.height(),
                realMousePos.mouseX(), realMousePos.mouseY()
        );
    }

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
    }

    @Override
    public boolean onMouseDWheel(final MousePos mousePos, final RenderPos renderPos, final int wheel) {
        if (isMouseOver(mousePos)) {
            zoom.set(MathHelper.clamp(zoom.getTargetValue() + (wheel < 0 ? 1.5f : -1.5f), 0.1, 999));
            return true;
        }
        return super.onMouseDWheel(mousePos, renderPos, wheel);
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        dragging = true;
        lastMouseX = Mouse.getX();
        lastMouseY = Mouse.getY();
        lastClickedMouseX = lastMouseX;
        lastClickedMouseY = lastMouseY;
        // Ensure that other components can be clicked on properly.
        return true;
    }

    protected void handleZoomAnim() {
        if (!zoom.isAnimFinished()) {
            renderer.setCameraLookAt(center, zoom.get(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
    }

    /**
     * Using {@link DynamicWidget#onMouseClickMove} only maintains a maximum frame rate of 20 fps,
     * so we'll consider listening for mouse movement at render time.
     */
    protected void handleMouseMove() {
        if (!dragging) {
            return;
        }

        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();

        rotationPitch += ((mouseX - lastMouseX) * 0.25F) + 360;
        rotationPitch = rotationPitch % 360F;
        rotationYaw = (float) MathHelper.clamp(rotationYaw - ((mouseY - lastMouseY) * 0.25F), -89.9, 89.9);
        renderer.setCameraLookAt(center, zoom.get(), Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos) {
        dragging = false;
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        // Improve click tolerance.
        if (Math.abs(lastClickedMouseX - mouseX) <= 5 && Math.abs(lastClickedMouseY - mouseY) <= 5) {
            handleBlockClick();
        }
        return false;
    }

    protected void handleBlockClick() {
        RayTraceResult traceResult = renderer.getLastTraceResult();
        if (onBlockSelected != null) {
            onBlockSelected.accept(traceResult != null ? traceResult.getBlockPos() : null);
        }
    }

    public WorldSceneRendererWidget preInitNextRenderedCore(BlockPos min, BlockPos max, Collection<BlockPos> blocks, ISceneRenderHook renderHook, boolean resetZoom) {
        int minX = min.getX(), minY = min.getY(), minZ = min.getZ();
        int maxX = max.getX(), maxY = max.getY(), maxZ = max.getZ();
        center = new Vector3f(
                (minX + maxX) / 2f + 0.5F,
                (minY + maxY) / 2f + 0.5F,
                (minZ + maxZ) / 2f + 0.5F
        );
        renderer.addRenderedBlocksToAnotherWorld(blocks, renderHook);

        if (resetZoom) {
            double zoom = this.zoom.setImmediate((3.5 * Math.sqrt(Math.max(Math.max(Math.max(maxX - minX + 1, maxY - minY + 1), maxZ - minZ + 1), 1)))).get();
            renderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
        return this;
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof WorldRendererCacheCleanEvent) {
            renderer.deleteCacheBuffer();
            return true;
        }
        return false;
    }

    public BlockArray getPattern() {
        return pattern;
    }

    public boolean isStructureFormed() {
        return structureFormed;
    }

    public WorldSceneRendererWidget setStructureFormed(final boolean structureFormed) {
        this.structureFormed = structureFormed;
        return this;
    }

    public boolean isUseLayerRender() {
        return useLayerRender;
    }

    public WorldSceneRendererWidget useLayerRender() {
        if (!useLayerRender) {
            this.useLayerRender = true;
            refreshPattern();
        }
        return this;
    }

    public WorldSceneRendererWidget use3DRender() {
        if (useLayerRender) {
            this.useLayerRender = false;
            refreshPattern();
        }
        return this;
    }

    public int getRenderLayer() {
        return renderLayer;
    }

    public WorldSceneRendererWidget setRenderLayer(final int renderLayer) {
        if (this.renderLayer != renderLayer) {
            this.renderLayer = renderLayer;
            if (useLayerRender) {
                refreshPattern();
            }
        }
        return this;
    }

    public boolean isCycleBlocks() {
        return cycleBlocks;
    }

    public WorldSceneRendererWidget setCycleBlocks(final boolean cycleBlocks) {
        this.cycleBlocks = cycleBlocks;
        return this;
    }

    public long getTickSnap() {
        return tickSnap;
    }

    public int getDynamicPatternSize() {
        return dynamicPatternSize;
    }

    public WorldSceneRendererWidget setDynamicPatternSize(final int dynamicPatternSize) {
        if (this.dynamicPatternSize != dynamicPatternSize) {
            this.dynamicPatternSize = dynamicPatternSize;
            this.resetZoom = true;
            refreshPattern();
        }
        return this;
    }

    public WorldSceneRenderer getWorldRenderer() {
        return renderer;
    }

    public BlockPos getRenderOffset() {
        return offset;
    }

    public WorldSceneRendererWidget setOnPatternUpdate(final Consumer<WorldSceneRendererWidget> onPatternUpdate) {
        this.onPatternUpdate = onPatternUpdate;
        return this;
    }

    public WorldSceneRendererWidget setOnBlockSelected(final Consumer<BlockPos> onBlockSelected) {
        this.onBlockSelected = onBlockSelected;
        return this;
    }
}
