package net.sxlver.jrpc.core;


import com.google.gson.internal.reflect.ReflectionHelper;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.ReflectionUtil;
import net.sxlver.jrpc.core.util.TimeUtil;
import sun.reflect.ReflectionFactory;

import java.util.logging.*;

public class InternalLogger {
    private Logger logger;

    public InternalLogger(Class<?> cls) {
        this.logger = Logger.getLogger(cls.getSimpleName());

        this.logger.setUseParentHandlers(false);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormatter());
        this.logger.addHandler(handler);
    }

    public void info(final String message, final Object... args) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        logger.info(String.format(prependCallerClass(message, callCls).replace("{}", "%s"), args));
    }

    public void warn(final String message, final Object... args) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        logger.warning(String.format(prependCallerClass(message, callCls).replace("{}", "%s"), args));
    }

    public void fatal(final String message, final Object... args) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        logger.severe(String.format(prependCallerClass(message, callCls).replace("{}", "%s"), args));
    }

    public void debug(final String message, final Object... args) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        logger.fine(String.format(prependCallerClass(message, callCls).replace("{}", "%s"), args));
    }

    public void setLogLevel(final Level logLevel) {
        logger.setLevel(logLevel);
    }

    private String prependCallerClass(final String message, final String callCls) {

        return "[" + callCls + "] " + message;
    }
    public enum AnsiColor {
        RED("\u001b[31m"),
        YELLOW("\u001b[33m"),
        BLUE("\u001b[34m"),
        RESET("\u001b[0m"),
        WHITE("\u001B[37m");

        private final String ansiColor;

        AnsiColor(final String ansiColor) {
            this.ansiColor = ansiColor;
        }

        public String getAnsiColor() {
            return this.ansiColor;
        }

        @Override
        public String toString() {
            return this.ansiColor;
        }

        public static AnsiColor getByLevel(final Level logLevel) {
            switch(logLevel.getName()) {
                case "DEBUG" -> {
                    return BLUE;
                }
                case "INFO" -> {
                    return WHITE;
                }
                case "WARN" -> {
                    return YELLOW;
                }
                case "SEVERE" -> {
                    return RED;
                }
                default -> {
                    return RESET;
                }
            }
        }
    }

    static class LogFormatter extends Formatter {

        public String format(LogRecord record) {

            return AnsiColor.getByLevel(record.getLevel()) + "[" + TimeUtil.logTimeFromMillis(record.getMillis()) + "] [" +
                    Thread.currentThread().getName() + "/" + record.getLevel() +  "] " +
                    formatMessage(record) + "\n";
        }

        public String getHead(Handler h) {
            return super.getHead(h);
        }

        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
}
