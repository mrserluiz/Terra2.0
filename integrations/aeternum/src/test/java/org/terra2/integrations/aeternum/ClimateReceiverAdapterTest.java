package org.terra2.integrations.aeternum;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.bukkit.plugin.Plugin;
import java.util.function.Function;
public class ClimateReceiverAdapterTest {
    public static class Receiver {
        public void registerWorldClimateProvider(Plugin owner, Function callback) { }
        public void unregisterWorldClimateProvider(Plugin owner) { }
    }
    public static class Wrapper {
        final Receiver receiver = new Receiver();
        public Receiver getSeasons() { return receiver; }
    }
    public static class Unsupported { public Object getSeasons() { return new Object(); } }
    @Test void rootReceiverDoesNotRequireAccessor() throws Exception {
        Receiver receiver = new Receiver(); assertSame(receiver, ClimateReceiverAdapter.find(receiver, "missing"));
    }
    @Test void nestedReceiverUsesPublicAccessor() throws Exception {
        Wrapper wrapper = new Wrapper(); assertSame(wrapper.receiver, ClimateReceiverAdapter.find(wrapper, "getSeasons"));
    }
    @Test void unsupportedReceiverIsRejected() {
        assertThrows(NoSuchMethodException.class, () -> ClimateReceiverAdapter.find(new Unsupported(), "getSeasons"));
        assertThrows(NoSuchMethodException.class, () -> ClimateReceiverAdapter.find(new Object(), ""));
    }
}
