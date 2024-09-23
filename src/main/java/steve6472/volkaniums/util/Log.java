package steve6472.volkaniums.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

public class Log 
{
    private static FileHandler fileHandler;
    private static ConsoleHandler consoleHandler;

    static 
    {
        try 
        {
            fileHandler = new FileHandler("latest.log");
            fileHandler.setFormatter(new CustomFormatter(false));
            fileHandler.setLevel(Level.ALL);

            consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new CustomFormatter(true));
            consoleHandler.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exception(Logger logger, Throwable throwable)
    {
        logger.log(Level.SEVERE, throwable, throwable::getMessage);
    }

    public static Logger getLogger(Class<?> clazz)
    {
        return getLogger(clazz.getSimpleName());
    }

    public static Logger getLogger(String name)
    {
        Logger logger = Logger.getLogger(name);
        logger.addHandler(fileHandler);
        logger.addHandler(consoleHandler);
        logger.setUseParentHandlers(false);

        logger.setLevel(Level.ALL);
        return logger;
    }

    private static class CustomFormatter extends Formatter 
    {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd HH:mm:ss");

        //TODO: reset doesn't work
        private static final String ANSI_RESET = "\u001B[0m";
        private static final Map<Level, String> ANSI_COLORS = new HashMap<>();
        private final boolean isConsole;

        private CustomFormatter(boolean console)
        {
            this.isConsole = console;
        }

        static {
            ANSI_COLORS.put(Level.SEVERE, "\u001B[31m");   // Red
            ANSI_COLORS.put(Level.WARNING, "\u001B[33m");  // Yellow
            ANSI_COLORS.put(Level.INFO, "\u001B[34m");     // Blue
            ANSI_COLORS.put(Level.FINE, "\u001B[38;5;248m");   // GRAY
            ANSI_COLORS.put(Level.FINER, "\u001B[38;5;240m");  // DARK-GRAY
            ANSI_COLORS.put(Level.FINEST, "\u001B[38;5;238m"); // REALLY-DARK-GRAY
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();

            builder.append(dateFormat.format(new Date(record.getMillis())));
            builder.append(" ");

            builder.append("[");
            builder.append(record.getLoggerName());
            builder.append("]");
            builder.append(" ");

            if (isConsole)
            {
                String color = ANSI_COLORS.get(record.getLevel());
                if (color != null) 
                {
                    builder.append(color);
                }
            }

            builder.append(record.getLevel());
            builder.append(": ");
            builder.append(formatMessage(record));

            if (isConsole)
                builder.append(ANSI_RESET);
            builder.append(System.lineSeparator());
            return builder.toString();
        }
    }
}