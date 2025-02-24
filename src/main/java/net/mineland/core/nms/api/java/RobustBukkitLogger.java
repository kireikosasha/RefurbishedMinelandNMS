package net.mineland.core.nms.api.java;

import net.mineland.core.nms.MinelandNMSPlugin;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RobustBukkitLogger {
    private static final Logger LOGGER = MinelandNMSPlugin.getInstance().getLogger();
    private static final String SUPPRESSED_CAPTION = "Suppressed: ";
    private static final String CAUSE_CAPTION = "Caused by: ";

    public static void logException(Throwable throwable) {
        if (throwable == null) return;
        final Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
        dejaVu.add(throwable);
        logThrowable(throwable, "", dejaVu);
    }

    private static void logThrowable(final Throwable throwable, final String prefix, final Set<Throwable> dejaVu) {
        LOGGER.log(Level.SEVERE, prefix + throwable.toString());
        
        for (StackTraceElement traceElement : throwable.getStackTrace()) {
            LOGGER.log(Level.SEVERE, "\tat " + traceElement);
        }
        for (final Throwable suppressed : throwable.getSuppressed()) {
            if (dejaVu.add(suppressed))
                logThrowable(suppressed, prefix + SUPPRESSED_CAPTION, dejaVu);
        }
        final Throwable cause = throwable.getCause();
        if (cause != null && dejaVu.add(cause))
            logThrowable(cause, prefix + CAUSE_CAPTION, dejaVu);
    }
}
