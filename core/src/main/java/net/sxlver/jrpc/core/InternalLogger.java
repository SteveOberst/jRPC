package net.sxlver.jrpc.core;


import net.sxlver.jrpc.core.util.TimeUtil;

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
        logger.info(String.format(message.replace("{}", "%s"), args));
    }

    public void warn(final String message, final Object... args) {
        logger.warning(String.format(message.replace("{}", "%s"), args));
    }

    public void fatal(final String message, final Object... args) {
        logger.severe(String.format(message.replace("{}", "%s"), args));
    }

    public void debug(final String message, final Object... args) {
        logger.fine(String.format(message.replace("{}", "%s"), args));
    }

    private Class<?> getCallerClass() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
    }

    public enum AnsiColor {
        RED("\u001b[31m"),
        YELLOW("\u001b[33m"),
        BLUE("\u001b[34m"),
        RESET("\u001b[0m");

        private final String ansiColor;

        private AnsiColor(final String ansiColor) {
            this.ansiColor = ansiColor;
        }

        public String getAnsiColor() {
            return this.ansiColor;
        }

        public String toString() {
            return this.ansiColor;
        }
    }

    static class LogFormatter extends Formatter {

        public String format(LogRecord record) {

            return "[" + TimeUtil.logTimeFromMillis(record.getMillis()) + "] [" + Thread.currentThread().getName() + "/" + record.getLevel() +  "] " + formatMessage(record) + "\n";
        }

        public String getHead(Handler h) {
            return super.getHead(h);
        }

        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
}
