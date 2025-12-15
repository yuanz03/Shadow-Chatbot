package shadowbuddy.services;

/**
 * Container class for user visible error messages.
 */
// Solution below inspired from AB3 code in using a Messages class to centralize exception messages.
public class Messages {
    public static final String PREFIX_EMPTY_COMMAND = "Empty request! ";
    public static final String PREFIX_UNKNOWN_COMMAND = "Invalid request! ";

    public static final String MESSAGE_EMPTY_TASK_DESCRIPTION = "Please provide a description for your %s.";
    public static final String MESSAGE_DUPLICATE_TASK_DESCRIPTION = "A task with this description already exists!";

    public static final String MESSAGE_ALREADY_MARKED_DONE = "The task indicated is already marked as done!";
    public static final String MESSAGE_ALREADY_MARKED_NOT_DONE = "The task indicated is already marked as not done!";

    public static final String MESSAGE_EMPTY_TASK_LIST = "Your task list is empty!";
    public static final String MESSAGE_INVALID_TASK_INDEX = "Please provide a numeric index for your request!";
    public static final String MESSAGE_INVALID_TASK_INDEX_BOUNDS = "Please enter a number between 1 and %s.";

    public static final String MESSAGE_INVALID_DEADLINE_DATE = "Invalid due date! Please use: ";
    public static final String MESSAGE_INVALID_EVENT_DATE = "Invalid start or end date! Please use: ";
    public static final String MESSAGE_INVALID_DATE_RANGE = "Start date must be before end date!";

    public static final String MESSAGE_NO_KEYWORD = "Please provide a keyword for your find.";
    public static final String MESSAGE_MULTIPLE_KEYWORDS = "Please provide only ONE keyword for your find.";

    public static final String MESSAGE_DEADLINE_FORMAT = "deadline DESCRIPTION /by d/M/yyyy HHmm.";
    public static final String MESSAGE_EVENT_FORMAT = "event DESCRIPTION /from d/M/yyyy HHmm /to d/M/yyyy HHmm.";
    public static final String MESSAGE_COMMANDS_GUIDE = "Try one of these commands: list, mark, unmark, todo, "
            + "delete, event, or deadline, and I'll handle it for you.";

    public static final String MESSAGE_UNKNOWN_TASK = "Unknown task: %s";
    public static final String MESSAGE_UNKNOWN_TASK_TYPE = "Unknown task type: %s";
    public static final String MESSAGE_INVALID_TIMESTAMP_ARGUMENT_COUNT =
            "validateAndFormatDateRange method expects only 1 or 2 timestamps!";
}
