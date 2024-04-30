/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.component.*;
import net.minecraft.util.ResourceLocation;

import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryComponentTypes
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:51
 */
public class RegistryComponentTypes {

    private RegistryComponentTypes() {
    }

    public static void initialize() {
        COMPONENT_ITEM = register(new ComponentItem(), KEY_COMPONENT_ITEM);
        COMPONENT_FLUID = register(new ComponentFluid(), KEY_COMPONENT_FLUID);
        COMPONENT_ITEM_FLUID = register(new ComponentItemFluid(), KEY_COMPONENT_ITEM_FLUID);
        COMPONENT_ENERGY = register(new ComponentEnergy(), KEY_COMPONENT_ENERGY);
        COMPONENT_GAS = register(new ComponentGas(), KEY_COMPONENT_GAS);
        COMPONENT_SMART_INTERFACE = register(new ComponentSmartInterface(), KEY_COMPONENT_SMART_INTERFACE);
        COMPONENT_PARALLEL_CONTROLLER = register(new ComponentParallelController(), KEY_COMPONENT_PARALLEL_CONTROLLER);
        COMPONENT_UPGRADE_BUS = register(new ComponentUpgradeBus(), KEY_COMPONENT_UPGRADE_BUS);
    }

    private static <T extends ComponentType> T register(T componentType, ResourceLocation registryName) {
        componentType.setRegistryName(registryName);
        CommonProxy.registryPrimer.register(componentType);
        return componentType;
    }

}
