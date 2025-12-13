package shadowbuddy.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import shadowbuddy.services.Messages;
import shadowbuddy.taskmodels.Deadline;
import shadowbuddy.taskmodels.Event;
import shadowbuddy.taskmodels.Task;
import shadowbuddy.taskmodels.TaskList;
import shadowbuddy.taskmodels.Todo;

/**
 * Manages saving, updating, and loading the chatbot's task list from a file.
 * The ShadowStorage class wraps a file path and provides methods to create the database
 * file, output its contents, write a TaskList to it, and load tasks from it.
 */
public class ShadowStorage {
    protected final String filePath;
    protected final File databaseFile;

    /**
     * Initializes a ShadowStorage instance with the given file path.
     * Creates a ShadowStorage instance that initializes a File instance with the
     * specified file path and uses it to store, write, and retrieve tasks.
     *
     * @param filePath The file path to the task list database file.
     */
    public ShadowStorage(String filePath) {
        assert filePath != null : "filePath should not be null";
        assert !filePath.trim().isEmpty() : "filePath should not be empty";
        this.filePath = filePath;
        this.databaseFile = new File(this.filePath);
    }

    /**
     * Creates the database file and its parent directories if they do not already exist.
     * If any of the parent directories are missing, they will be created.
     * If the database file did not already exist, a new database file will be created.
     * Otherwise, a message is displayed to indicate that the existing task list will be displayed.
     *
     * @return A String describing the file operation performed.
     * @throws IOException If creating the database file fails.
     */
    public String createDatabase() throws IOException {
        File parentFile = this.databaseFile.getParentFile();
        boolean isFolderCreated = parentFile != null && parentFile.mkdirs();

        if (this.databaseFile.createNewFile()) {
            return isFolderCreated
                    ? "The relevant folder did not exist! The database has now been created for you."
                    : "No task list found! A new one has been created for you.";
        } else {
            return "Here is your current task list: ";
        }
    }

    /**
     * Returns the tasks stored in the database file to the screen.
     * A Scanner is used to read each line from the database file.
     * Each input line becomes a numbered entry in the returned String.
     *
     * @return A String containing the entire task list read from the database file.
     * @throws FileNotFoundException If the database file cannot be found or read.
     */
    public String outputDatabase() throws FileNotFoundException {
        Scanner fileScanner = new Scanner(this.databaseFile);
        StringBuilder sb = new StringBuilder();
        int taskIndex = 1;

        while (fileScanner.hasNextLine()) {
            sb.append(taskIndex).append(". ").append(fileScanner.nextLine()).append("\n");
            taskIndex++;
        }
        fileScanner.close();
        return sb.toString();
    }

    /**
     * Writes the given TaskList to the database file, overwriting any existing contents.
     * A FileWriter instance is created using the filePath field.
     * Each task in the TaskList is formatted with the private helper method formatTask,
     * and written to the database file on a separate line.
     *
     * @param taskList The TaskList whose tasks will be saved to the database file.
     * @throws IOException If the database file cannot be written to.
     */
    public void writeToDatabase(TaskList taskList) throws IOException {
        assert taskList != null : "taskList should not be null";
        FileWriter taskWriter = new FileWriter(this.filePath);
        for (int i = 0; i < taskList.getLength(); i++) {
            Task task = taskList.getTask(i + 1);
            taskWriter.write(formatTask(task) + "\n");
        }
        taskWriter.close();
    }

    /**
     * Loads tasks from the database file into the given TaskList.
     * If the database file does not exist, the method will return early.
     * Each line from the database file is split at the "|" symbols, and each segment is
     * trimmed of whitespace. The resulting String data is then converted into a Task instance
     * using the private helper method createTask, and appended to the given task list.
     *
     * @param taskList The TaskList to populate with tasks read from the database file.
     * @throws FileNotFoundException If the database file cannot be found or read.
     */
    public void loadDatabase(TaskList taskList) throws FileNotFoundException {
        assert taskList != null : "taskList should not be null";
        if (!this.databaseFile.exists()) {
            return;
        }

        Scanner fileScanner = new Scanner(this.databaseFile);
        while (fileScanner.hasNextLine()) {
            String[] taskDetails = fileScanner.nextLine().split("\\|");

            for (int i = 0; i < taskDetails.length; i++) {
                taskDetails[i] = taskDetails[i].trim();
            }

            Task currentTask = createTask(taskDetails);
            taskList.addTask(currentTask);
        }
        fileScanner.close();
    }

    /**
     * Returns a Task created from the given String array of database task data.
     *
     * @param taskDetails The String array of task information obtained from the database file.
     * @return A Task representing the given task data.
     */
    private Task createTask(String[] taskDetails) {
        assert taskDetails != null : "taskDetails should not be null";
        String taskType = taskDetails[0];
        boolean isTaskDone = taskDetails[1].equals("1");
        String taskDescription = taskDetails[2];
        Task currentTask;

        switch (taskType) {
        case "T":
            currentTask = new Todo(taskDescription);
            break;
        case "D":
            currentTask = new Deadline(taskDescription, taskDetails[3]);
            break;
        case "E":
            String[] eventTimings = taskDetails[3].split("-");
            currentTask = new Event(taskDescription, eventTimings[0], eventTimings[1]);
            break;
        default:
            throw new IllegalArgumentException(String.format(Messages.MESSAGE_UNKNOWN_TASK_TYPE, taskType));
        }

        if (isTaskDone) {
            currentTask.markAsDone();
        }
        return currentTask;
    }

    /**
     * Returns a formatted String representation of the given Task.
     * The format of the returned String representation is:
     * "type | status | description | additional dates and timings (if present)."
     *
     * @param task The specified Task instance to be formatted for storage.
     * @return A single-line String representing the given Task.
     */
    private String formatTask(Task task) {
        assert task != null : "task should not be null";
        String taskStatus = (task.getStatusIcon().equalsIgnoreCase("X")) ? "1" : "0";

        if (task instanceof Todo todo) {
            return "T | " + taskStatus + " | " + todo.getDescription();
        } else if (task instanceof Deadline deadline) {
            return "D | " + taskStatus + " | " + deadline.getDescription() + " | " + deadline.getDueDate();
        } else if (task instanceof Event event) {
            return "E | " + taskStatus + " | " + event.getDescription() + " | " + event.getStartDate()
                    + "-" + event.getEndDate();
        } else {
            throw new IllegalArgumentException(String.format(Messages.MESSAGE_UNKNOWN_TASK, task));
        }
    }
}
