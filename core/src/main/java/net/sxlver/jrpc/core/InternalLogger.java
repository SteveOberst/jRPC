package net.sxlver.jrpc.core;


import lombok.NonNull;
import net.sxlver.jrpc.core.util.TimeUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class InternalLogger {
    private final Logger logger;
    private ConsoleHandler consoleHandler;
    private FileHandler fileHandler;

    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm-ss");

    public InternalLogger(final Class<?> cls, final @NonNull File logFolder) {
        this.logger = Logger.getLogger(cls.getSimpleName());

        if(logFolder.exists() && !logFolder.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s isn't a directory", logFolder.getAbsolutePath()));
        }

        if(!logFolder.exists()){
            logFolder.mkdir();
        }

        try {
            this.logger.setUseParentHandlers(false);
            this.fileHandler = new FileHandler(Path.of(logFolder.getAbsolutePath(), "log_" + df.format(LocalDateTime.now())) + ".txt");
            this.consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new LogFormatter(true));
            fileHandler.setFormatter(new LogFormatter(false));
            this.logger.addHandler(consoleHandler);
            this.logger.addHandler(fileHandler);
        }catch(IOException exception) {
            exception.printStackTrace();
        }
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

    public void fatal(final Throwable throwable) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        final String message = ExceptionUtils.getStackTrace(throwable);
        logger.severe(String.format(prependCallerClass(message, callCls)));
    }

    public void debugFine(final String message, final Object... args) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        log(Level.FINE, callCls, String.format("%s %s", "[DEBUG]", message), args);
    }

    public void debugFiner(final String message, final Object... args) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        log(Level.FINER, callCls, String.format("%s %s", "[DEBUG]", message), args);
    }
    public void debugFinest(final String message, final Object... args) {
        final String callCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        log(Level.FINEST, callCls, String.format("%s %s", "[DEBUG]", message), args);
    }
    public void log(final Level level, @Nullable String callerCls, final String message, final Object... args) {
        if(callerCls == null) {
            callerCls = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
        }
        logger.log(level, String.format(prependCallerClass(message, callerCls).replace("{}", "%s"), args));
    }

    public void setLogLevel(final Level logLevel) {
        logger.setLevel(logLevel);
        consoleHandler.setLevel(logLevel);
        fileHandler.setLevel(logLevel);
    }

    private String prependCallerClass(final String message, final String callCls) {
        return "[" + callCls + "] " + message;
    }

    public enum AnsiColor {
        RED("\u001b[31m"),
        YELLOW("\u001b[33m"),
        BLUE("\u001B[34m"),
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
                case "FINE", "FINER", "FINEST" -> {
                    return BLUE;
                }
                case "INFO" -> {
                    return WHITE;
                }
                case "WARNING" -> {
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

        private final boolean colorize;

        public LogFormatter(final boolean colorize) {
            this.colorize = colorize;
        }

        public String format(LogRecord record) {

            return getColor(record.getLevel()) + "[" + TimeUtil.logTimeFromMillis(record.getMillis()) + "] [" +
                    Thread.currentThread().getName() + "/" + record.getLevel() +  "] " +
                    formatMessage(record) + AnsiColor.RESET + "\n";
        }

        private String getColor(final Level level) {
            return colorize ? AnsiColor.getByLevel(level).toString() : "";
        }

        public String getHead(Handler h) {
            return super.getHead(h);
        }

        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
}
