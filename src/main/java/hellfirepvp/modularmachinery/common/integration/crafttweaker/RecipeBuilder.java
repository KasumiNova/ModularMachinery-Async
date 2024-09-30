/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.integration.Logger;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeBuilder
 * Created by HellFirePvP
 * Date: 02.01.2018 / 18:16
 */
@GroovyBlacklist
@ZenRegister
@ZenClass("mods.modularmachinery.RecipeBuilder")
public class RecipeBuilder {
    @ZenMethod
    public static RecipePrimer newBuilder(String recipeRegistryName, String associatedMachineRegistryName, int processingTickTime) {
        return newBuilder(recipeRegistryName, associatedMachineRegistryName, processingTickTime, 0);
    }

    @ZenMethod
    public static RecipePrimer newBuilder(String recipeRegistryName, String associatedMachineRegistryName, int processingTickTime, int sortingPriority) {
        return newBuilder(recipeRegistryName, associatedMachineRegistryName, processingTickTime, sortingPriority, false);
    }

    @ZenMethod
    public static RecipePrimer newBuilder(String recipeRegistryName, String associatedMachineRegistryName, int processingTickTime, int sortingPriority, boolean cancelIfPerTickFails) {
        ResourceLocation recipeLoc = new ResourceLocation(recipeRegistryName);
        String path = recipeLoc.getPath();

        if (recipeLoc.getNamespace().equals("minecraft")) {
            recipeLoc = new ResourceLocation(ModularMachinery.MODID, path);
        }
        ResourceLocation machineLoc = new ResourceLocation(associatedMachineRegistryName);
        if (machineLoc.getNamespace().equals("minecraft")) {
            machineLoc = new ResourceLocation(ModularMachinery.MODID, machineLoc.getPath());
        }

        if (!path.matches("[._\\-a-zA-Z][._\\-a-zA-Z0-9]*")) {
            Logger.warn("Dangerous recipe registry name: " + path);
        }
        if (processingTickTime <= 0) {
            Logger.warn("Recipe processing tick time has to be at least 1 tick!");
            processingTickTime = 1;
        }

        return new RecipePrimer(recipeLoc, machineLoc, processingTickTime, sortingPriority, cancelIfPerTickFails);
    }

}
