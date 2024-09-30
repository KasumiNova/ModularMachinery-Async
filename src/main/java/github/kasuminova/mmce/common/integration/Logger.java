package github.kasuminova.mmce.common.integration;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import crafttweaker.CraftTweakerAPI;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * A logger which logs to CraftTweaker, GroovyScript and this mods log, depending on if the mods are loaded.
 */
public class Logger {

    private static final String CT_PREFIX = "[ModularMachinery] ";
    private static final Object[] NO_ARGS = new Object[0];

    private static boolean isGroovyScriptRunning() {
        return Mods.GROOVYSCRIPT.isPresent() && GroovyScript.getSandbox().isRunning();
    }

    private static String formatMsg(String msg, Object... args) {
        return args == null || args.length == 0 ? msg : new ParameterizedMessage(msg, args).getFormattedMessage();
    }

    public static void error(String msg) {
        error(msg, NO_ARGS);
    }

    public static void warn(String msg) {
        warn(msg, NO_ARGS);
    }

    public static void info(String msg) {
        info(msg, NO_ARGS);
    }

    public static void error(String msg, Object... args) {
        if (isGroovyScriptRunning()) {
            GroovyLog.get().error(msg, args);
        }
        if (Mods.CRAFTTWEAKER.isPresent()) {
            CraftTweakerAPI.logError(CT_PREFIX + formatMsg(msg, args));
        }
        ModularMachinery.log.error(msg, args);
    }

    public static void warn(String msg, Object... args) {
        if (isGroovyScriptRunning()) {
            GroovyLog.get().warn(msg, args);
        }
        if (Mods.CRAFTTWEAKER.isPresent()) {
            CraftTweakerAPI.logWarning(formatMsg(msg, args));
        }
        ModularMachinery.log.warn(msg, args);
    }

    public static void info(String msg, Object... args) {
        if (isGroovyScriptRunning()) {
            GroovyLog.get().info(msg, args);
        }
        if (Mods.CRAFTTWEAKER.isPresent()) {
            CraftTweakerAPI.logInfo(formatMsg(msg, args));
        }
        ModularMachinery.log.info(msg, args);
    }
}
