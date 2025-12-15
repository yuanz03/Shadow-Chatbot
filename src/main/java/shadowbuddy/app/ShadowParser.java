package shadowbuddy.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import shadowbuddy.ml.DescriptionExtractor;
import shadowbuddy.ml.IntentClassifier;
import shadowbuddy.services.Messages;
import shadowbuddy.services.ShadowException;

/**
 * Parses raw user input into {@code ShadowCommand} objects used by the controller.
 * The {@code ShadowParser} class interprets user commands defined in {@code ShadowCommand},
 * validates input, and converts raw timestamps into a standardized format.
 */
public class ShadowParser {
    private static final String INPUT_DATE_PATTERN = "d/M/yyyy HHmm";
    private static final String OUTPUT_DATE_PATTERN = "MMM d yyyy HH:mm";

    /**
     * Parses raw {@code userInput} String into a {@code ShadowCommand} instance.
     * Intent detection is delegated to {@code IntentClassifier}, before returning the corresponding
     * {@code ShadowCommand} instance. Parsing of deadline and event commands are delegated to specialised parsers.
     *
     * @param userInput The raw user input String to parse.
     * @return A {@code ShadowCommand} instance representing the user intent.
     * @throws ShadowException If the user input is syntactically invalid.
     * @throws Exception If text preprocessing or intent classification fails.
     */
    public static ShadowCommand parse(String userInput) throws Exception {
        assert userInput != null : "user input should not be null";
        if (userInput.isEmpty()) {
            throw new ShadowException(Messages.PREFIX_EMPTY_COMMAND + Messages.MESSAGE_COMMANDS_GUIDE);
        }

        String intent = IntentClassifier.classify(userInput);
        switch (intent) {
        case "query":
            return new ShadowCommand(ShadowCommand.CommandType.LIST);
        case "mark":
            int markIndex = convertStringToIndex(userInput);
            return new ShadowCommand(ShadowCommand.CommandType.MARK, markIndex);
        case "unmark":
            int unmarkIndex = convertStringToIndex(userInput);
            return new ShadowCommand(ShadowCommand.CommandType.UNMARK, unmarkIndex);
        case "delete":
            int deleteIndex = convertStringToIndex(userInput);
            return new ShadowCommand(ShadowCommand.CommandType.DELETE, deleteIndex);
        case "todo":
            String requestDetails = DescriptionExtractor.extract(userInput);
            return new ShadowCommand(ShadowCommand.CommandType.TODO, requestDetails);
        case "deadline":
            return parseDeadline(userInput);
        case "event":
            return parseEvent(userInput);
        default:
            return new ShadowCommand(ShadowCommand.CommandType.UNKNOWN);
        }
    }

    /**
     * Parses Deadline command details and returns the corresponding {@code ShadowCommand} instance.
     * Expects the deadline to include the command word, a task description, and due date in the format "d/M/yyyy HHmm".
     *
     * @return A {@code ShadowCommand} instance representing the Deadline details.
     * @throws ShadowException If {@code userInput} has an invalid deadline date, or if description cannot be extracted.
     */
    private static ShadowCommand parseDeadline(String userInput) throws ShadowException {
        String[] inputDetails = userInput.split(" ");
        String dueDate = extractDates(inputDetails).get(0);
        String formattedDueDate;
        String description;

        try {
            formattedDueDate = validateAndFormatDateRange(dueDate)[0];
        } catch (DateTimeParseException exception) {
            throw new ShadowException(Messages.MESSAGE_INVALID_DEADLINE_DATE + Messages.MESSAGE_DEADLINE_FORMAT);
        }

        try { // remove extracted date occurrence before extracting task description
            description = DescriptionExtractor.extract(userInput.replace(dueDate, "").trim());
        } catch (Exception exception) {
            throw new ShadowException(Messages.PREFIX_UNKNOWN_COMMAND + Messages.MESSAGE_COMMANDS_GUIDE);
        }
        return new ShadowCommand(ShadowCommand.CommandType.DEADLINE, description, formattedDueDate);
    }

    /**
     * Parses Event command details and returns the corresponding {@code ShadowCommand} instance.
     * Expects the event to include the command word, a task description,
     * and a start and end date in the format "d/M/yyyy HHmm".
     *
     * @return A {@code ShadowCommand} instance representing the Event details.
     * @throws ShadowException If {@code userInput} has invalid event dates, or if description cannot be extracted.
     */
    private static ShadowCommand parseEvent(String userInput) throws ShadowException {
        String[] inputDetails = userInput.split(" ");
        List<String> dates = extractDates(inputDetails);
        String[] formattedDates;
        String description;

        if (dates.size() != 2) { // validate that exactly two event dates are found
            throw new ShadowException(Messages.MESSAGE_INVALID_EVENT_DATE + Messages.MESSAGE_EVENT_FORMAT);
        }

        try {
            formattedDates = validateAndFormatDateRange(dates.get(0), dates.get(1));
        } catch (DateTimeParseException exception) {
            throw new ShadowException(Messages.MESSAGE_INVALID_EVENT_DATE + Messages.MESSAGE_EVENT_FORMAT);
        }

        String cleanedInput = userInput;
        for (String date: dates) { // remove each extracted date occurrence before extracting task description
            cleanedInput = cleanedInput.replace(date, "");
        }

        try {
            description = DescriptionExtractor.extract(cleanedInput.trim());
        } catch (Exception exception) {
            throw new ShadowException(Messages.PREFIX_UNKNOWN_COMMAND + Messages.MESSAGE_COMMANDS_GUIDE);
        }
        return new ShadowCommand(ShadowCommand.CommandType.EVENT, description, formattedDates[0], formattedDates[1]);
    }

    /**
     * Scans an array of tokenised strings to find substrings that can be parsed as timestamps.
     *
     * @param details Tokenised input produced by splitting the raw {@code userInput}.
     * @return A list of matched timestamp strings in the order that they were found.
     */
    private static List<String> extractDates(String[] details) {
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < details.length - 1; i++) {
            /* Attempts parsing of every adjacent pair of tokens (i and i+1) as a valid timestamp */
            String possibleDate = details[i] + " " + details[i + 1]; // timestamps occupy exactly two consecutive tokens
            try {
                LocalDateTime.parse(possibleDate, DateTimeFormatter.ofPattern(INPUT_DATE_PATTERN));
                dates.add(possibleDate); // add timestamp to the results list if parsing succeeds
            } catch (DateTimeParseException exception) {
                // Ignore parse failures (invalid dates) and continue scanning
            }
        }
        return dates;
    }

    /**
     * Returns a String array containing the formatted timestamp(s) for the given input timestamp(s).
     * Array length is 1 for a single deadline due date, or 2 for an event start and end date.
     * If given two timestamps, validate that they form a valid chronological range.
     * This helper function converts the given timestamp using the DateTimeFormatter class.
     *
     * @param timestamps One or two timestamps in "d/M/yyyy HHmm" format.
     * @return An array of formatted timestamps in "MMM d yyyy HH:mm" format.
     * @throws ShadowException If two timestamps are supplied and the end date is before the start date.
     */
    private static String[] validateAndFormatDateRange(String... timestamps) throws ShadowException {
        assert timestamps != null : "timestamps should not be null";
        DateTimeFormatter taskInputFormatter = DateTimeFormatter.ofPattern(INPUT_DATE_PATTERN);
        DateTimeFormatter taskOutputFormatter = DateTimeFormatter.ofPattern(OUTPUT_DATE_PATTERN);

        if (timestamps.length == 1) {
            LocalDateTime dueDate = LocalDateTime.parse(timestamps[0], taskInputFormatter);
            return new String[] { dueDate.format(taskOutputFormatter) };
        } else if (timestamps.length == 2) {
            LocalDateTime startDate = LocalDateTime.parse(timestamps[0], taskInputFormatter);
            LocalDateTime endDate = LocalDateTime.parse(timestamps[1], taskInputFormatter);
            if (endDate.isBefore(startDate)) {
                throw new ShadowException(Messages.PREFIX_UNKNOWN_COMMAND + Messages.MESSAGE_INVALID_DATE_RANGE);
            }
            return new String[] { startDate.format(taskOutputFormatter), endDate.format(taskOutputFormatter) };
        } else {
            throw new IllegalArgumentException(Messages.MESSAGE_INVALID_TIMESTAMP_ARGUMENT_COUNT);
        }
    }

    /**
     * Extracts the first contiguous integer token from the given {@code userInput} and converts it to an integer.
     * This helper function parses the given task for TaskList indexing in MARK, UNMARK, and DELETE command types.
     *
     * @return The parsed integer index.
     * @throws ShadowException If no numeric token is found or if the numeric token is syntactically invalid.
     */
    private static int convertStringToIndex(String userInput) throws ShadowException {
        StringBuilder taskIndex = new StringBuilder();

        /* Iterates through each character and collect digits until a non-digit is encountered. */
        for (char c: userInput.toCharArray()) {
            if (Character.isDigit(c)) {
                taskIndex.append(c);
            } else if (!taskIndex.isEmpty()) {
                break;
            }
        }

        if (!taskIndex.isEmpty()) {
            try {
                return Integer.parseInt(taskIndex.toString());
            } catch (NumberFormatException exception) {
                throw new ShadowException(Messages.PREFIX_UNKNOWN_COMMAND + Messages.MESSAGE_INVALID_TASK_INDEX);
            }
        }
        throw new ShadowException(Messages.PREFIX_UNKNOWN_COMMAND + Messages.MESSAGE_INVALID_TASK_INDEX);
    }
}
