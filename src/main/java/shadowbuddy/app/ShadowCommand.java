package shadowbuddy.app;

/**
 * Encapsulates a parsed user command and all related task information.
 * The ShadowCommand class models a user command and its associated task data, enabling consistent
 * handling of task operations, such as listing, marking, creating, or deleting tasks.
 */
public class ShadowCommand {
    /**
     * Defines the supported command types recognized by ShadowParser.
     * The CommandType enum lists commands for creating and updating tasks,
     * with UNKNOWN serving as a fallback for unrecognized input.
     */
    public enum CommandType { LIST, MARK, UNMARK, DELETE, FIND, TODO, DEADLINE, EVENT, UNKNOWN }

    private static final String EMPTY_STRING = "";
    private static final int NO_INDEX = -1;

    protected final CommandType commandType;
    protected final String taskDescription;
    protected final String dueDate;
    protected final String startDate;
    protected final String endDate;
    protected final int taskIndex;

    /**
     * Initializes a ShadowCommand instance with the given command type.
     * Overloaded constructor for LIST and UNKNOWN command types.
     */
    public ShadowCommand(CommandType commandType) {
        this.commandType = commandType;
        this.taskDescription = EMPTY_STRING;
        this.dueDate = EMPTY_STRING;
        this.startDate = EMPTY_STRING;
        this.endDate = EMPTY_STRING;
        this.taskIndex = NO_INDEX;
    }

    /**
     * Initializes a ShadowCommand instance with the given command type and task index.
     * Overloaded constructor for MARK, UNMARK, and DELETE command types.
     */
    public ShadowCommand(CommandType commandType, int taskIndex) {
        this.commandType = commandType;
        this.taskDescription = EMPTY_STRING;
        this.dueDate = EMPTY_STRING;
        this.startDate = EMPTY_STRING;
        this.endDate = EMPTY_STRING;
        this.taskIndex = taskIndex;
    }

    /**
     * Initializes a ShadowCommand instance with the given command type and task description.
     * Overloaded constructor for TODO and FIND command types.
     */
    public ShadowCommand(CommandType commandType, String taskDescription) {
        this.commandType = commandType;
        this.taskDescription = taskDescription;
        this.dueDate = EMPTY_STRING;
        this.startDate = EMPTY_STRING;
        this.endDate = EMPTY_STRING;
        this.taskIndex = NO_INDEX;
    }

    /**
     * Initializes a ShadowCommand instance with the given command type, task description, and due date.
     * Overloaded constructor for the DEADLINE command type.
     */
    public ShadowCommand(CommandType commandType, String taskDescription, String dueDate) {
        this.commandType = commandType;
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
        this.startDate = EMPTY_STRING;
        this.endDate = EMPTY_STRING;
        this.taskIndex = NO_INDEX;
    }

    /**
     * Initializes a ShadowCommand instance with the given command type, task description, start date, and end date.
     * Overloaded constructor for the EVENT command type.
     */
    public ShadowCommand(CommandType commandType, String taskDescription, String startDate, String endDate) {
        this.commandType = commandType;
        this.taskDescription = taskDescription;
        this.dueDate = EMPTY_STRING;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskIndex = NO_INDEX;
    }

    /**
     * Returns the String representation of the current command type.
     */
    public String convertCommandTypeToString() {
        return this.commandType.toString();
    }
}
