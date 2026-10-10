package org.terra2.integrations.aeternum;

import java.lang.reflect.Method;
import java.util.function.Function;
import org.bukkit.plugin.Plugin;

/** Capability-based discovery. Never accesses fields or changes receiver state. */
final class ClimateReceiverAdapter {
    private ClimateReceiverAdapter() { }
    static Object find(Object plugin, String accessor) throws ReflectiveOperationException {
        if (compatible(plugin)) return plugin;
        if (accessor == null || accessor.isBlank()) throw new NoSuchMethodException("No receiver accessor configured");
        Method getter = plugin.getClass().getMethod(accessor);
        Object receiver = getter.invoke(plugin);
        if (!compatible(receiver)) throw new NoSuchMethodException("Missing climate receiver methods");
        return receiver;
    }
    private static boolean compatible(Object receiver) {
        if (receiver == null) return false;
        try {
            Method register = receiver.getClass().getMethod("registerWorldClimateProvider", Plugin.class, Function.class);
            Method remove = receiver.getClass().getMethod("unregisterWorldClimateProvider", Plugin.class);
            return register.getReturnType() == void.class && remove.getReturnType() == void.class;
        } catch (NoSuchMethodException missing) { return false; }
    }
}
