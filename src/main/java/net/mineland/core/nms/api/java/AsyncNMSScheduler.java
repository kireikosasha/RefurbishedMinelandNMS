package net.mineland.core.nms.api.java;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;

import java.lang.reflect.Field;
import java.util.concurrent.*;
import java.util.function.Function;
@Log
public class AsyncNMSScheduler {
    /*
    Улучшенная версия стандартного AsyncScheduler
    Локализация и снос лишних зависимостей...
     */
    private static final char INNER_CLASS_SEPARATOR_CHAR = '$';
    public static int STOP_WATCH_TIME_MILLIS = 500;
    @Getter
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(24,
        new ThreadFactoryBuilder().setNameFormat("MinelandNMS Thread %d").build());
    public static void shutdown() {
        TryIgnore.ignore(scheduler::shutdownNow);
    }

    private AsyncNMSScheduler() {
        
    }
    public static Future<?> run(Runnable runnable) {
        return scheduler.submit(new DecoratedRunnable(runnable));
    }
    public static <T> Future<T> run(Callable<T> callable) {
        return scheduler.submit(new DecoratedCallable<>(callable));
    }
    public static ScheduledFuture<?> later(Runnable runnable, long delay, TimeUnit time) {
        return scheduler.schedule(new DecoratedRunnable(runnable), delay, time);
    }
    public static ScheduledFuture<?> timer(Runnable runnable, long delay, long period, TimeUnit time) {
        return scheduler.scheduleAtFixedRate(new DecoratedRunnable(runnable), delay, period, time);
    }

    public static void cancel(ScheduledFuture<?> timer) {
        try {
            if (timer != null) {
                timer.cancel(true);
            }
        } catch (Exception ignored) {
        }
    }

    @ToString
    public static class DecoratedRunnable implements Runnable {
        @Setter
        private static Function<Runnable, Runnable> hotfixDecorator = runnable -> runnable;

        private final Runnable originalRunnable;
        private final Runnable decoratedRunnable;

        public DecoratedRunnable(Runnable originalRunnable) {
            this.originalRunnable = originalRunnable;
            this.decoratedRunnable = hotfixDecorator.apply(originalRunnable);
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try {
                decoratedRunnable.run();
            } catch (Throwable e) {
                log.severe("Error during execution of asynchronous task " + AsyncNMSScheduler.toString(originalRunnable));
                e.printStackTrace();
                if (e instanceof InterruptedException)
                    throw e;

            } finally {
                long after = System.currentTimeMillis() - start;
                if (after > STOP_WATCH_TIME_MILLIS) {
                    String l = AsyncNMSScheduler.toString(originalRunnable);
                    if (l.length() > 26) l = l.substring(0, 26) + "...";
                    log.warning("Busy task " + l + ", it was performed " + after + "ms.");
                }
            }
        }
    }
    @ToString
    public static class DecoratedCallable<T> implements Callable<T> {
        @Setter
        private static Function<Callable<?>, Callable<?>> hotfixDecorator = callable -> callable;

        private final Callable<T> originalCallable;
        private final Callable<T> decoratedCallable;

        @SuppressWarnings("unchecked")
        public DecoratedCallable(Callable<T> originalCallable) {
            this.originalCallable = originalCallable;
            this.decoratedCallable = (Callable<T>) hotfixDecorator.apply(originalCallable);
        }

        @Override
        public T call() throws Exception {
            long start = System.currentTimeMillis();
            try {
                return decoratedCallable.call();
            } catch (Throwable e) {
                log.severe("Error while accepting to call method: " + AsyncNMSScheduler.toString(originalCallable));
                e.printStackTrace();
                throw e;
            } finally {
                long after = System.currentTimeMillis() - start;
                if (after > STOP_WATCH_TIME_MILLIS) {
                    String l = AsyncNMSScheduler.toString(originalCallable);
                    if (l.length() > 26) l = l.substring(0, 26) + "...";
                    log.warning("Busy task " + l + ", it was performed " + after + "ms.");
                }
            }
        }
    }
    public static String toString(Object object) {
        if (object == null) {
            return "null";
        }

        Class<?> clazz = object.getClass();
        StringBuilder sb = new StringBuilder(clazz.getSimpleName() + "{");

        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            try {
                if (field.getName().indexOf(INNER_CLASS_SEPARATOR_CHAR) != -1) {
                    sb.append(field.getName()).append("=");
                    Object value = field.get(object);
                    sb.append(value == null ? "null" : value.toString());
                }
            } catch (IllegalAccessException e) {
                sb.append(field.getName()).append("=<access denied>");
            }

            if (i < fields.length - 1) {
                sb.append(", ");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
