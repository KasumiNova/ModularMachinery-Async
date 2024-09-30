/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery;

import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import github.kasuminova.mmce.common.network.*;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.command.CommandGetBluePrint;
import hellfirepvp.modularmachinery.common.command.CommandHand;
import hellfirepvp.modularmachinery.common.command.CommandPerformanceReport;
import hellfirepvp.modularmachinery.common.command.CommandSyntax;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.command.CommandCTReload;
import hellfirepvp.modularmachinery.common.network.*;
import kport.modularmagic.common.event.RegistrationEvent;
import kport.modularmagic.common.network.StarlightMessage;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on GitHub.
 * Class: ModularMachinery
 * Created by HellFirePvP
 * Date: 26.06.2017 / 20:26
 */
@Mod(modid = ModularMachinery.MODID, name = ModularMachinery.NAME, version = ModularMachinery.VERSION,
        dependencies = "required-after:forge@[14.21.0.2371,);" +
                "after:crafttweaker@[4.0.4,);" +
                //"after:groovyscript@[1.0.0,);" + // TODO wait for grs 1.0.1
                "after:zenutils@[1.12.8,);" +
                "after:jei@[4.13.1.222,);" +
                "after:gregtech@[2.7.4-beta,);" +
                "after:appliedenergistics2@[rv6-stable-7,);" +
                "after:fluxnetworks@[4.1.0,);" +
                "after:tconstruct@[1.12.2-2.12.0.157,);" +
                "after:thermalexpansion@[5.5.0,);",
        acceptedMinecraftVersions = "[1.12, 1.13)",
        acceptableRemoteVersions = "[2.0.0, 2.1.0)"
)
public class ModularMachinery {

    public static final String MODID = "modularmachinery";
    public static final String NAME = "Modular Machinery: Community Edition";
    public static final String VERSION = "2.0.0";
    public static final String CLIENT_PROXY = "hellfirepvp.modularmachinery.client.ClientProxy";
    public static final String COMMON_PROXY = "hellfirepvp.modularmachinery.common.CommonProxy";
    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    public static final TaskExecutor EXECUTE_MANAGER = new TaskExecutor();
    public static final EventBus EVENT_BUS = new EventBus();

    @Mod.Instance(MODID)
    public static ModularMachinery instance;
    public static Logger log;
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;
    private static boolean devEnvCache = false;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static boolean isRunningInDevEnvironment() {
        return devEnvCache;
    }

    public ModularMachinery() {
        MinecraftForge.EVENT_BUS.register(RegistrationEvent.class);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = VERSION;
        log = event.getModLog();
        devEnvCache = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        NET_CHANNEL.registerMessage(PktCopyToClipboard.class, PktCopyToClipboard.class, 0, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktSyncSelection.class, PktSyncSelection.class, 1, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktPerformanceReport.class, PktPerformanceReport.class, 2, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktAssemblyReport.class, PktAssemblyReport.class, 3, Side.CLIENT);
        if (Mods.AE2.isPresent()) {
            NET_CHANNEL.registerMessage(PktMEPatternProviderHandlerItems.class, PktMEPatternProviderHandlerItems.class, 4, Side.CLIENT);
        }
        if (Mods.ASTRAL_SORCERY.isPresent()) {
            NET_CHANNEL.registerMessage(StarlightMessage.StarlightMessageHandler.class, StarlightMessage.class, 5, Side.CLIENT);
        }

        NET_CHANNEL.registerMessage(PktInteractFluidTankGui.class, PktInteractFluidTankGui.class, 100, Side.SERVER);
        NET_CHANNEL.registerMessage(PktSmartInterfaceUpdate.class, PktSmartInterfaceUpdate.class, 101, Side.SERVER);
        NET_CHANNEL.registerMessage(PktParallelControllerUpdate.class, PktParallelControllerUpdate.class, 102, Side.SERVER);
        if (Mods.AE2.isPresent()) {
            NET_CHANNEL.registerMessage(PktMEInputBusInvAction.class, PktMEInputBusInvAction.class, 103, Side.SERVER);
        }
        NET_CHANNEL.registerMessage(PktAutoAssemblyRequest.class, PktAutoAssemblyRequest.class, 104, Side.SERVER);
        if (Mods.AE2.isPresent()) {
            NET_CHANNEL.registerMessage(PktMEPatternProviderAction.class, PktMEPatternProviderAction.class, 105, Side.SERVER);
        }
        if (Mods.ASTRAL_SORCERY.isPresent()) {
            NET_CHANNEL.registerMessage(StarlightMessage.StarlightMessageHandler.class, StarlightMessage.class, 106, Side.SERVER);
        }

        CommonProxy.loadModData(event.getModConfigurationDirectory());
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete();
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        //Cmd registration
        event.registerServerCommand(new CommandSyntax());
        event.registerServerCommand(new CommandHand());
        event.registerServerCommand(new CommandGetBluePrint());
        event.registerServerCommand(new CommandPerformanceReport());

        if (Mods.ZEN_UTILS.isPresent()) {
            event.registerServerCommand(new CommandCTReload());
        }
    }

}
