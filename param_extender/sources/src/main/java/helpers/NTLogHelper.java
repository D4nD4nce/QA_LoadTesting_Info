package helpers;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class NTLogHelper {

    private Logger logger;
//    private Logger logger;// = LogManager.getRootLogger();

    private final static String SEPARATOR = "--------------------------";
    private final static String CLASS_SEPARATION = ":\n";
    private final static String NO_EXCEPTION = "NO EXCEPTION MESSAGE";

    public static final String LOGIN_ERROR                  = "login";
    public static final String LOGOUT_ERROR                 = "logout";

    public NTLogHelper(Class currentClass) {
        //PropertyConfigurator.configure("src\\main\\resources\\log_param.properties");
        //logger = Logger.getLogger(currentClass.getName());
        logger = LogManager.getLogger(currentClass);
        logger.addAppender(new ConsoleAppender(new PatternLayout("%-5p- %m%n")));

    }

    /**
     * log massage
     * @param message - text to log
     */
    public void logInfo(String message) {
        logger.info(getInfoMessage(message));
    }

    /**
     * log error massage
     * @param description - error description
     */
    public void logError(String description) {
        logError(description, null);
    }

    /**
     * log error massage with exception
     * @param description - info about error
     * @param error - thrown exception
     */
    public void logError(String description, Exception error) {
        logger.error(getStandardErrorMessage(description, getMessageFromException(error)));
    }

    /**
     * log error massage with current transaction name
     * @param transaction - transaction name
     * @param description - error info
     */
    public void logTransactionError(String transaction, String description) {
        logTransactionError(transaction, description, null);
    }

    /**
     * log error massage with transaction name and exception
     * @param transaction - transaction name
     * @param description - error info
     * @param error - thrown exception
     */
    public void logTransactionError(String transaction, String description, Exception error) {
        logger.error(getFullErrorMessage(transaction, description, getMessageFromException(error)));
    }

    /**
     * form params into error massage for logs
     * @param description - error description
     * @param errorMsg - info from exception
     * @return - formatted string with ready massage to log
     */
    private String getStandardErrorMessage(String description, String errorMsg) {
        return "\n" + SEPARATOR +
                //"\n" + className +
                "\n" + "ERROR:" +
                "\n" + description +
                "\n" + errorMsg +
                "\n" + SEPARATOR;
    }

    /**
     * form params into full error massage for logs
     * @param transaction - transaction name
     * @param description - error comments
     * @param errorMsg - all info from Exception
     * @return - ready massage to log
     */
    private String getFullErrorMessage(String transaction, String description, String errorMsg) {
        return "\n" + SEPARATOR +
                //"\n" + className +
                "\n" + "TRANSACTION ERROR:" +
                "\n" + description +
                "\n" + transaction +
                "\n" + errorMsg +
                "\n" + SEPARATOR;
    }

    /**
     * create massage with info
     * @param message - text to log
     * @return - ready massage to log
     */
    private String getInfoMessage(String message) {
//        return message;
        return //"\n" + className +
//                "\n" + "INFO:" +
//                "\n" + SEPARATOR +
                message;
//                "\n" + SEPARATOR ;
    }

    /**
     * get al info from exception object
     * @param error - exception object
     * @return - formatted string with exception info
     */
    private String getMessageFromException(Exception error) {
        if (error == null) {
            return NO_EXCEPTION;
        }
        StringBuffer resultMessage = new StringBuffer(error.getMessage());
        for (StackTraceElement stackTraceElement : error.getStackTrace()) {
            resultMessage
                    .append("\n")
                    .append(stackTraceElement.getClassName());
        }
        return resultMessage.toString();
    }
}
