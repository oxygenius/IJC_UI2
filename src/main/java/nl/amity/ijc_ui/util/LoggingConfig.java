package nl.amity.ijc_ui.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Utility to configure application logging directly to a file and console.
 */
public class LoggingConfig {

    private static boolean initialized = false;

    /**
     * Initializes logging to write directly to a local log file.
     *
     * @param logFileName name of the target log file (e.g. "IJC_UI.log")
     * @param append whether to append to existing logs or overwrite on restart
     */
    public static synchronized void initialize(String logFileName, boolean append) {
        if (initialized) {
            return;
        }

        try {
            Logger rootLogger = Logger.getLogger("");

            // 1. Remove existing handlers (e.g. default ConsoleHandler)
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // 2. Formatter for both file and console output
            Formatter formatter = new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                @Override
                public synchronized String format(LogRecord record) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(dateFormat.format(new Date(record.getMillis()))).append(" ");
                    sb.append(String.format("%-7s", record.getLevel())).append(" ");
                    sb.append("[").append(record.getSourceClassName() != null ? record.getSourceClassName() : record.getLoggerName()).append("] ");
                    sb.append(formatMessage(record));
                    sb.append(System.lineSeparator());

                    if (record.getThrown() != null) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        record.getThrown().printStackTrace(pw);
                        sb.append(sw);
                    }
                    return sb.toString();
                }
            };

            // 3. Configure FileHandler (5 MB limit, single log file: IJC_UI.log)
            int limitBytes = 5 * 1024 * 1024; // 5 MB
            FileHandler fileHandler = new FileHandler(logFileName, limitBytes, 1, append);
            fileHandler.setLevel(Level.ALL);
            try {
                fileHandler.setEncoding("UTF-8");
            } catch (Exception ignored) {
            }
            fileHandler.setFormatter(formatter);

            // 4. Configure ConsoleHandler for IDE development
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            try {
                consoleHandler.setEncoding("UTF-8");
            } catch (Exception ignored) {
            }
            consoleHandler.setFormatter(formatter);

            // 5. Attach handlers and set global root level
            rootLogger.addHandler(fileHandler);
            rootLogger.addHandler(consoleHandler);
            rootLogger.setLevel(Level.INFO);

            initialized = true;
            Logger.getLogger(LoggingConfig.class.getName()).info("Logging initialized directly to file: " + logFileName);

        } catch (IOException e) {
            System.err.println("Failed to initialize file logging: " + e.getMessage());
        }
    }
}
