/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.base;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: Mods
 * Created by HellFirePvP
 * Date: 02.03.2019 / 17:42
 */
public enum Mods {
    GROOVYSCRIPT("groovyscript"),
    CRAFTTWEAKER("crafttweaker"),
    JEI("jei"),
    GREGTECH("gregtech"),
    GREGTECHCEU("gregtech") {
        private boolean initialized = false;
        private boolean detected = false;

        @Override
        public boolean isPresent() {
            if (initialized) {
                return detected;
            }
            initialized = true;
            if (!super.isPresent()) {
                return detected = false;
            }
            try {
                Class.forName("gregtech.client.utils.BloomEffectUtil");
                return detected = true;
            } catch (Exception e) {
                return detected = false;
            }
        }
    },
    DRACONICEVOLUTION("draconicevolution"),
    REDSTONEFLUXAPI("redstoneflux"),
    MEKANISM("mekanism"),
    IC2("ic2"),
    TOP("theoneprobe"),
    NUCLEARCRAFT_OVERHAULED("nuclearcraft"),
    RESOURCELOADER("resourceloader"),
    // TXLoader uses fully private variables and I can't get the correct resource folder.
    // Bad design.
    TX_LOADER("txloader"),
    FLUX_NETWORKS("fluxnetworks"),
    ZEN_UTILS("zenutils"),
    TCONSTRUCT("tconstruct"),
    AE2("appliedenergistics2"),
    AE2EL("appliedenergistics2") {
        private boolean initialized = false;
        private boolean detected = false;

        @Override
        public boolean isPresent() {
            if (initialized) {
                return detected;
            }
            initialized = true;
            if (!super.isPresent()) {
                return detected = false;
            }
            try {
                Class.forName("appeng.core.AE2ELCore");
                return detected = true;
            } catch (Exception e) {
                return detected = false;
            }
        }
    },
    BM2("bloodmagic"),
    TC6("thaumcraft"),
    TA("thaumicaugmentation"),
    TAHUMIC_JEI("thaumicjei"),
    EXU2("extrautils2"),
    ASTRAL_SORCERY("astralsorcery"),
    NATURESAURA("naturesaura"),
    BOTANIA("botania"),
    GECKOLIB("geckolib3"),
    /**
     * Hmmm... Use MBD's client-side transparent block rendering feature instead of reimplementing one.
     */
    MBD("multiblocked"),
    /**
     * An add-on mod that provides the ability to hide block rendering similar to Multiblocked.
     */
    COMPONENT_MODEL_HIDER("component_model_hider"),
    AE2FCR("ae2fc") {
        @Override
        public boolean isPresent() {
            ModContainer ae2fc = Loader.instance().getIndexedModList().get("ae2fc");
            return ae2fc != null && ae2fc.getVersion().endsWith("-r");
        }
    },
    MEKENG("mekeng"),
    /**
     * A addon mod provides bloom effect, extracted from GregTechCEu.
     */
    LUMENIZED("lumenized"),
    ALFHEIM("alfheim"),
    THERMAL_EXPANSION("thermalexpansion"),
    ;

    public final String modid;
    private final boolean loaded;

    Mods(String modName) {
        this.modid = modName;
        this.loaded = Loader.isModLoaded(this.modid);
    }

    public boolean isPresent() {
        return loaded;
    }

}
