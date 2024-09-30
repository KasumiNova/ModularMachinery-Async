/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import github.kasuminova.mmce.common.block.appeng.*;
import github.kasuminova.mmce.common.integration.groovyscript.GroovyMachine;
import github.kasuminova.mmce.common.tile.*;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.*;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineBuilder;
import hellfirepvp.modularmachinery.common.item.*;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.tiles.*;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import kport.modularmagic.common.block.*;
import kport.modularmagic.common.tile.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static hellfirepvp.modularmachinery.common.lib.BlocksMM.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryBlocks
 * Created by HellFirePvP
 * Date: 28.06.2017 / 20:22
 */
public class RegistryBlocks {

    public static final List<BlockDynamicColor> pendingIBlockColorBlocks = new LinkedList<>();
    private static final List<Block> blockModelRegister = new ArrayList<>();

    public static void initialize() {
        registerBlocks();
        if (Mods.GROOVYSCRIPT.isPresent()) {
            // groovyscript can load resources too
            try {
                RegistryBlocks.writeAllCustomControllerModels("groovy/assets");
            } catch (IOException e) {
                ModularMachinery.log.error("Failed to write controller models", e);
            }
        } else if (Mods.RESOURCELOADER.isPresent() || Mods.TX_LOADER.isPresent()) {
            try {
                RegistryBlocks.writeAllCustomControllerModels("resources");
            } catch (IOException e) {
                ModularMachinery.log.error("Failed to write controller models", e);
            }
        }

        registerTiles();
        registerBlockModels();
    }

    private static void registerBlocks() {
        blockController = prepareRegister(new BlockController());
        prepareItemBlockRegister(new ItemBlockController(blockController));
        blockFactoryController = prepareRegister(new BlockFactoryController());
        prepareItemBlockRegister(blockFactoryController);

        registerCustomControllers();

        registerExampleStatedMachineComponent();
        registerCustomStatedMachineComponent();

        blockCasing = prepareRegister(new BlockCasing());
        prepareItemBlockRegister(blockCasing);

        itemInputBus = prepareRegister(new BlockInputBus());
        prepareItemBlockRegister(itemInputBus);
        itemOutputBus = prepareRegister(new BlockOutputBus());
        prepareItemBlockRegister(itemOutputBus);
        fluidInputHatch = prepareRegister(new BlockFluidInputHatch());
        prepareItemBlockRegister(fluidInputHatch);
        fluidOutputHatch = prepareRegister(new BlockFluidOutputHatch());
        prepareItemBlockRegister(fluidOutputHatch);
        energyInputHatch = prepareRegister(new BlockEnergyInputHatch());
        prepareItemBlockRegister(energyInputHatch);
        energyOutputHatch = prepareRegister(new BlockEnergyOutputHatch());
        prepareItemBlockRegister(energyOutputHatch);
        smartInterface = prepareRegister(new BlockSmartInterface());
        prepareItemBlockRegister(smartInterface);
        parallelController = prepareRegister(new BlockParallelController());
        prepareItemBlockRegister(parallelController);
        upgradeBus = prepareRegister(new BlockUpgradeBus());
        prepareItemBlockRegister(upgradeBus);

        if (Mods.AE2.isPresent()) {
            meItemOutputBus = prepareRegister(new BlockMEItemOutputBus());
            ItemsMM.meItemOutputBus = prepareItemBlockRegister(meItemOutputBus);
            meItemInputBus = prepareRegister(new BlockMEItemInputBus());
            ItemsMM.meItemInputBus = prepareItemBlockRegister(meItemInputBus);
            
            meFluidOutputBus = prepareRegister(new BlockMEFluidOutputBus());
            ItemsMM.meFluidOutputBus = prepareItemBlockRegister(meFluidOutputBus);
            meFluidInputBus = prepareRegister(new BlockMEFluidInputBus());
            ItemsMM.meFluidInputBus = prepareItemBlockRegister(meFluidInputBus);

            if (Mods.MEKANISM.isPresent() && Mods.MEKENG.isPresent()) {
                meGasOutputBus = prepareRegister(new BlockMEGasOutputBus());
                ItemsMM.meGasOutputBus = prepareItemBlockRegister(meGasOutputBus);
                meGasInputBus = prepareRegister(new BlockMEGasInputBus());
                ItemsMM.meGasInputBus = prepareItemBlockRegister(meGasInputBus);
            }

            mePatternProvider = prepareRegister(new BlockMEPatternProvider());
            ItemsMM.mePatternProvider = prepareItemBlockRegister(mePatternProvider);
        }

        if (Mods.BM2.isPresent()) {
            blockWillProviderInput = prepareRegister(new BlockWillProviderInput());
            blockWillProviderOutput = prepareRegister(new BlockWillProviderOutput());
            blockLifeEssenceProviderInput = prepareRegister(new BlockLifeEssenceProviderInput());
            blockLifeEssenceProviderOutput = prepareRegister(new BlockLifeEssenceProviderOutput());

            prepareItemBlockRegister(blockWillProviderInput);
            prepareItemBlockRegister(blockWillProviderOutput);
            prepareItemBlockRegister(blockLifeEssenceProviderInput);
            prepareItemBlockRegister(blockLifeEssenceProviderOutput);
        }

        if (Mods.TC6.isPresent()) {
            blockAspectProviderInput = prepareRegister(new BlockAspectProviderInput());
            blockAspectProviderOutput = prepareRegister(new BlockAspectProviderOutput());

            prepareItemBlockRegister(blockAspectProviderInput);
            prepareItemBlockRegister(blockAspectProviderOutput);
        }

        if (Mods.TA.isPresent()) {
            blockImpetusProviderInput = prepareRegister(new BlockImpetusProviderInput());
            blockImpetusProviderOutput = prepareRegister(new BlockImpetusProviderOutput());

            prepareItemBlockRegister(blockImpetusProviderInput);
            prepareItemBlockRegister(blockImpetusProviderOutput);
        }

        if (Mods.EXU2.isPresent()) {
            blockGridProviderInput = prepareRegister(new BlockGridProviderInput());
            blockGridProviderOutput = prepareRegister(new BlockGridProviderOutput());
            blockRainbowProvider = prepareRegister(new BlockRainbowProvider());

            prepareItemBlockRegister(blockGridProviderInput);
            prepareItemBlockRegister(blockGridProviderOutput);
            prepareItemBlockRegister(blockRainbowProvider);
        }

        if (Mods.ASTRAL_SORCERY.isPresent()) {
            blockStarlightProviderInput = prepareRegister(new BlockStarlightProviderInput());
            blockStarlightProviderOutput = prepareRegister(new BlockStarlightProviderOutput());
            blockConstellationProvider = prepareRegister(new BlockConstellationProvider());

            prepareItemBlockRegister(blockStarlightProviderInput);
            prepareItemBlockRegister(blockStarlightProviderOutput);
            prepareItemBlockRegister(blockConstellationProvider);
        }

        if (Mods.NATURESAURA.isPresent()) {
            blockAuraProviderInput = prepareRegister(new BlockAuraProviderInput());
            blockAuraProviderOutput = prepareRegister(new BlockAuraProviderOutput());

            prepareItemBlockRegister(blockAuraProviderInput);
            prepareItemBlockRegister(blockAuraProviderOutput);
        }

        if (Mods.BOTANIA.isPresent()) {
            blockManaProviderInput = prepareRegister(new BlockManaProviderInput());
            blockManaProviderOutput = prepareRegister(new BlockManaProviderOutput());

            prepareItemBlockRegister(blockManaProviderInput);
            prepareItemBlockRegister(blockManaProviderOutput);
        }
    }

    private static void registerTiles() {
        registerTile(TileColorableMachineComponent.class);

        registerTile(TileMachineController.class);
        registerTile(TileFactoryController.class);

        registerTile(TileFluidInputHatch.class);
        registerTile(TileFluidOutputHatch.class);
        registerTile(TileItemOutputBus.class);
        registerTile(TileItemInputBus.class);
        registerTile(TileEnergyInputHatch.class);
        registerTile(TileEnergyOutputHatch.class);

        registerTile(TileSmartInterface.class);
        registerTile(TileParallelController.class);
        registerTile(TileUpgradeBus.class);

        if (Mods.AE2.isPresent()) {
            registerTileWithModID(MEItemOutputBus.class);
            registerTileWithModID(MEItemInputBus.class);
            registerTileWithModID(MEFluidOutputBus.class);
            registerTileWithModID(MEFluidInputBus.class);
            if (Mods.MEKANISM.isPresent() && Mods.MEKENG.isPresent()) {
                registerTileWithModID(MEGasOutputBus.class);
                registerTileWithModID(MEGasInputBus.class);
            }
            registerTileWithModID(MEPatternProvider.class);
        }
        if (Mods.BM2.isPresent()) {
            GameRegistry.registerTileEntity(TileWillProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilewillproviderinput"));
            GameRegistry.registerTileEntity(TileWillProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilewillprovideroutput"));
            GameRegistry.registerTileEntity(TileLifeEssenceProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilelifeessenceproviderinput"));
            GameRegistry.registerTileEntity(TileLifeEssenceProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilelifeessenceprovideroutput"));
        }
        if (Mods.TC6.isPresent()) {
            GameRegistry.registerTileEntity(TileAspectProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tileaspectproviderinput"));
            GameRegistry.registerTileEntity(TileAspectProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tileaspectprovideroutput"));
        }
        if (Mods.EXU2.isPresent()) {
            GameRegistry.registerTileEntity(TileGridProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilegridproviderinput"));
            GameRegistry.registerTileEntity(TileGridProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilegridprovideroutput"));
            GameRegistry.registerTileEntity(TileRainbowProvider.class, new ResourceLocation(ModularMachinery.MODID, "tilerainbowprovider"));
        }
        if (Mods.ASTRAL_SORCERY.isPresent()) {
            GameRegistry.registerTileEntity(TileStarlightInput.class, new ResourceLocation(ModularMachinery.MODID, "tilestarlightinput"));
            GameRegistry.registerTileEntity(TileStarlightOutput.class, new ResourceLocation(ModularMachinery.MODID, "tilestarlightoutput"));
            GameRegistry.registerTileEntity(TileConstellationProvider.class, new ResourceLocation(ModularMachinery.MODID, "tileconstellationprovider"));
        }
        if (Mods.NATURESAURA.isPresent()) {
            GameRegistry.registerTileEntity(TileAuraProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tileauraproviderinput"));
            GameRegistry.registerTileEntity(TileAuraProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tileauraprovideroutput"));
        }
        if (Mods.BOTANIA.isPresent()) {
            GameRegistry.registerTileEntity(TileManaProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilemanainput"));
            GameRegistry.registerTileEntity(TileManaProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilemanaoutput"));
        }
        if (Mods.TA.isPresent()) {
            GameRegistry.registerTileEntity(TileImpetusComponent.Input.class, new ResourceLocation(ModularMachinery.MODID, "impetusinput"));
            GameRegistry.registerTileEntity(TileImpetusComponent.Output.class, new ResourceLocation(ModularMachinery.MODID, "impetusoutput"));
        }
    }

    private static void registerExampleStatedMachineComponent() {
        registerStatedMachineComponent((BlockStatedMachineComponent)
                                               new BlockStatedMachineComponent().setRegistryName("crushing_wheels"));
    }

    private static void registerCustomStatedMachineComponent() {
        for (final BlockStatedMachineComponent block : BlockStatedMachineComponent.WAIT_FOR_REGISTRY) {
            registerStatedMachineComponent(block);
        }
        BlockStatedMachineComponent.WAIT_FOR_REGISTRY.clear();
    }

    public static void registerStatedMachineComponent(final BlockStatedMachineComponent block) {
        prepareRegisterWithCustomName(block);
        ItemBlockCustomName itemBlock = new ItemBlockCustomName(block) {
            @Nonnull
            @Override
            @SideOnly(Side.CLIENT)
            public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
                return block.getLocalizedName();
            }
        };
        itemBlock.setRegistryName(block.getRegistryName());
        prepareItemBlockRegisterWithCustomName(itemBlock);
    }

    private static void registerCustomControllers() {
        if (Config.onlyOneMachineController) {
            return;
        }

        List<DynamicMachine> waitForLoadMachines = MachineRegistry.getWaitForLoadMachines();
        for (MachineBuilder builder : MachineBuilder.PRE_LOAD_MACHINES.values()) {
            waitForLoadMachines.add(builder.getMachine());
        }
        MachineBuilder.PRE_LOAD_MACHINES.clear();
        waitForLoadMachines.addAll(GroovyMachine.PRE_LOAD_MACHINES.values());
        GroovyMachine.PRE_LOAD_MACHINES.clear();

        if (Config.mocCompatibleMode) {
            for (DynamicMachine machine : waitForLoadMachines) {
                if (machine.isFactoryOnly()) {
                    continue;
                }

                BlockController ctrlBlock = prepareRegisterWithCustomName(new BlockController("modularcontroller", machine));
                BlockController.MOC_MACHINE_CONTROLLERS.put(machine, ctrlBlock);

                ItemBlockController ctrlBlockItem = (ItemBlockController) new ItemBlockController(ctrlBlock).setRegistryName(Objects.requireNonNull(ctrlBlock.getRegistryName()));
                prepareItemBlockRegisterWithCustomName(ctrlBlockItem);
            }
        }

        for (DynamicMachine machine : waitForLoadMachines) {
            if (machine.isHasFactory() || Config.enableFactoryControllerByDefault) {
                BlockFactoryController factoryBlock = prepareRegisterWithCustomName(new BlockFactoryController(machine));
                BlockFactoryController.FACTORY_CONTROLLERS.put(machine, factoryBlock);

                ItemBlockController ctrlBlockItem = (ItemBlockController) new ItemBlockController(factoryBlock).setRegistryName(Objects.requireNonNull(factoryBlock.getRegistryName()));
                prepareItemBlockRegisterWithCustomName(ctrlBlockItem);
            }

            if (machine.isFactoryOnly()) {
                continue;
            }

            BlockController ctrlBlock = prepareRegisterWithCustomName(new BlockController(machine));
            BlockController.MACHINE_CONTROLLERS.put(machine, ctrlBlock);

            ItemBlockController ctrlBlockItem = (ItemBlockController) new ItemBlockController(ctrlBlock).setRegistryName(Objects.requireNonNull(ctrlBlock.getRegistryName()));
            prepareItemBlockRegisterWithCustomName(ctrlBlockItem);
        }
    }

    private static void registerBlockModels() {
        for (Block block : blockModelRegister) {
            ModularMachinery.proxy.registerBlockModel(block);
        }
    }

    private static void registerTile(Class<? extends TileEntity> tile, String name) {
        GameRegistry.registerTileEntity(tile, name);
    }

    private static void registerTile(Class<? extends TileEntity> tile) {
        registerTile(tile, tile.getSimpleName().toLowerCase());
    }

    private static void registerTileWithModID(final Class<? extends TileEntity> aClass) {
        GameRegistry.registerTileEntity(aClass, new ResourceLocation(ModularMachinery.MODID, aClass.getSimpleName().toLowerCase()));
    }

    private static ItemBlock prepareItemBlockRegister(Block block) {
        if (block instanceof BlockMachineComponent) {
            if (block instanceof BlockMEMachineComponent) {
                return prepareItemBlockRegister(new ItemBlockMEMachineComponent(block));
            } else if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockMachineComponentCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlockMachineComponent(block));
            }
        } else {
            if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlock(block));
            }
        }
    }

    private static <T extends ItemBlock> T prepareItemBlockRegister(T item) {
        String name = item.getBlock().getClass().getSimpleName().toLowerCase();
        item.setRegistryName(ModularMachinery.MODID, name).setTranslationKey(ModularMachinery.MODID + '.' + name);

        RegistryItems.ITEM_BLOCKS.add(item);
        return item;
    }

    private static <T extends ItemBlock> T prepareItemBlockRegisterWithCustomName(T item) {
        RegistryItems.ITEM_BLOCKS_WITH_CUSTOM_NAME.add(item);
        return item;
    }

    private static <T extends Block> T prepareRegister(T block) {
        String name = block.getClass().getSimpleName().toLowerCase();
        block.setRegistryName(ModularMachinery.MODID, name).setTranslationKey(ModularMachinery.MODID + '.' + name);

        return prepareRegisterWithCustomName(block);
    }

    private static <T extends Block> T prepareRegisterWithCustomName(T block) {
        blockModelRegister.add(block);
        CommonProxy.registryPrimer.register(block);
        if (block instanceof BlockDynamicColor) {
            pendingIBlockColorBlocks.add((BlockDynamicColor) block);
        }
        return block;
    }

    public static void writeAllCustomControllerModels(String basePath) throws IOException {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        for (BlockController controller : BlockController.MACHINE_CONTROLLERS.values()) {
            writeMachineControllerModelInternal(basePath, resourceManager, controller);
        }
        for (BlockFactoryController controller : BlockFactoryController.FACTORY_CONTROLLERS.values()) {
            writeFactoryControllerModelInternal(basePath, resourceManager, controller);
        }
    }

    private static void writeMachineControllerModelInternal(String basePath, IResourceManager resourceManager, BlockController controller) throws IOException {
        IResource blockStateResource = resourceManager.getResource(
                new ResourceLocation(ModularMachinery.MODID, "blockstates/block_machine_controller.json"));

        File blockStateFile = new File(basePath, "modularmachinery/blockstates/" + controller.getRegistryName().getPath() + ".json");
        if (blockStateFile.exists()) {
            return;
        }
        final InputStream inputStream = blockStateResource.getInputStream();
        final FileOutputStream fileOutputStream = FileUtils.openOutputStream(blockStateFile);
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
    }

    private static void writeFactoryControllerModelInternal(String basePath, IResourceManager resourceManager, BlockFactoryController controller) throws IOException {
        IResource blockStateResource = resourceManager.getResource(
                new ResourceLocation(ModularMachinery.MODID, "blockstates/block_factory_controller.json"));

        File blockStateFile = new File(basePath, "modularmachinery/blockstates/" + controller.getRegistryName().getPath() + ".json");
        if (blockStateFile.exists()) {
            return;
        }
        final InputStream inputStream = blockStateResource.getInputStream();
        final FileOutputStream fileOutputStream = FileUtils.openOutputStream(blockStateFile);
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
    }
}
