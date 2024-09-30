package github.kasuminova.mmce.common.event.machine;

import groovy.lang.Closure;
import net.minecraftforge.fml.common.Optional;

public interface IEventHandler<T> {

    void invoke(T event);

    @Optional.Method(modid = "groovyscript")
    static <T> IEventHandler<T> of(Closure<?> listener) {
        return listener::call;
    }

    @Optional.Method(modid = "crafttweaker")
    static <T> IEventHandler<T> of(crafttweaker.util.IEventHandler<T> eventHandler) {
        return eventHandler::handle;
    }
}
