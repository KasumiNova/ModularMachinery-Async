/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.lib;

import github.kasuminova.mmce.common.block.appeng.BlockMEFluidInputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEFluidOutputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEItemInputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEItemOutputBus;
import hellfirepvp.modularmachinery.common.block.*;
import kport.gugu_utils.common.block.*;
import kport.modularmagic.common.block.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlocksMM
 * Created by HellFirePvP
 * Date: 28.06.2017 / 20:22
 */
public class BlocksMM {

    public static BlockController blockController;
    public static BlockFactoryController blockFactoryController;

    public static BlockCasing blockCasing;

    public static BlockInputBus itemInputBus;
    public static BlockOutputBus itemOutputBus;
    public static BlockFluidInputHatch fluidInputHatch;
    public static BlockFluidOutputHatch fluidOutputHatch;
    public static BlockEnergyInputHatch energyInputHatch;
    public static BlockEnergyOutputHatch energyOutputHatch;
    public static BlockEnvironmentHatch blockEnvironmentHatch;

    public static BlockSmartInterface smartInterface;
    public static BlockParallelController parallelController;
    public static BlockUpgradeBus upgradeBus;

    public static BlockMEItemOutputBus meItemOutputBus;
    public static BlockMEItemInputBus meItemInputBus;
    public static BlockMEFluidOutputBus meFluidOutputBus;
    public static BlockMEFluidInputBus meFluidInputBus;

    public static BlockWillProviderInput blockWillProviderInput;
    public static BlockWillProviderOutput blockWillProviderOutput;

    public static BlockLifeEssenceProviderInput blockLifeEssenceProviderInput;
    public static BlockLifeEssenceProviderOutput blockLifeEssenceProviderOutput;

    public static BlockGridProviderInput blockGridProviderInput;
    public static BlockGridProviderOutput blockGridProviderOutput;

    public static BlockRainbowProvider blockRainbowProvider;

    public static BlockAuraProviderInput blockAuraProviderInput;
    public static BlockAuraProviderOutput blockAuraProviderOutput;

    public static BlockStarLightInputHatch blockStarLightProviderInput;
    public static BlockStarlightProviderOutput blockStarlightProviderOutput;

    public static BlockConstellationProvider blockConstellationProvider;
    public static BlockSparkManaHatch blockSparkManaProvider;
    public static BlockImpetusProviderInput blockImpetusProviderInput;
    public static BlockImpetusProviderOutput blockImpetusProviderOutput;
    public static BlockLargeAspectHatch blockLargeAspectProvider;

    public static BlockPressureHatch blockPressureProvider;
    public static BlockEmberInputHatch blockEmberInputProvider;
    public static BlockHotAirInputHatch blockHotAirHatch;
}
