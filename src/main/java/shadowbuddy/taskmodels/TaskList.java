package shadowbuddy.taskmodels;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Manages an ordered collection of Task instances.
 * The TaskList class provides operations to add, remove, and toggle the completion status of tasks.
 * All methods use 1-based indexing to match the TaskList numbering.
 */
public class TaskList {
    protected final ArrayList<Task> storage;
    protected final HashSet<String> uniqueDescriptions;

    /**
     * Initializes an empty TaskList instance with a pre-allocated capacity of 100 elements.
     */
    public TaskList() {
        this.storage = new ArrayList<>(100);
        this.uniqueDescriptions = new HashSet<>();
    }

    /**
     * Appends the given task to the end of the TaskList.
     *
     * @param task The Task to add to the TaskList.
     */
    public void addTask(Task task) {
        assert task != null : "task should not be null";
        this.storage.add(task);
        this.uniqueDescriptions.add(task.getDescription());
    }

    /**
     * Removes the task at the given 1-based index and returns it.
     *
     * @param index The 1-based index of the Task to remove.
     * @return The Task that was removed from the TaskList.
     */
    public Task deleteTask(int index) {
        Task deletedTask = this.storage.remove(index - 1);
        this.uniqueDescriptions.remove(deletedTask.getDescription());
        return deletedTask;
    }

    /**
     * Marks the task at the given 1-based index as done.
     *
     * @param index The 1-based index of the Task to mark.
     */
    public void markTask(int index) {
        this.storage.get(index - 1).markAsDone();
    }

    /**
     * Marks the task at the given 1-based index as not done.
     *
     * @param index The 1-based index of the Task to unmark.
     */
    public void unmarkTask(int index) {
        this.storage.get(index - 1).markAsNotDone();
    }

    /**
     * Returns a TaskList containing only tasks whose descriptions match the given keyword.
     * Iterates through the given TaskList, comparing each task's description against the
     * given keyword, and stores all matching tasks in a new Tasklist.
     *
     * @param taskList The TaskList to traverse through.
     * @param keyword The keyword to match against task descriptions.
     * @return A new TaskList containing all the matching tasks.
     */
    public TaskList getMatchingTasks(TaskList taskList, String keyword) {
        assert taskList != null : "taskList should not be null";
        assert keyword != null : "keyword should not be null";
        TaskList matchingTasks = new TaskList();
        for (int i = 0; i < taskList.getLength(); i++) {
            Task task = taskList.getTask(i + 1);
            if (containsKeyword(task.getDescription(), keyword)) {
                matchingTasks.addTask(task);
            }
        }
        return matchingTasks;
    }

    /**
     * Returns true when the given task description already exists in the TaskList.
     *
     * @param taskDescription The task description being checked.
     * @return True if a task with the same description already exists; False otherwise.
     */
    public boolean containsDuplicate(String taskDescription) {
        assert taskDescription != null : "taskDescription should not be null";
        return this.uniqueDescriptions.contains(taskDescription);
    }

    public Task getTask(int index) {
        return this.storage.get(index - 1);
    }

    public int getLength() {
        return this.storage.size();
    }

    /**
     * Returns true when the given task description contains the specified keyword.
     * Partial matches of the keyword are not considered, and comparisons are case-insensitive.
     *
     * @param description The task description to search through.
     * @param keyword The keyword to match against task descriptions.
     * @return True if the keyword appears in the description; False otherwise.
     */
    private boolean containsKeyword(String description, String keyword) {
        assert description != null : "description should not be null";
        assert keyword != null : "keyword should not be null";
        String[] descriptionDetails = description.split(" ");

        for (int i = 0; i < descriptionDetails.length; i++) {
            if (descriptionDetails[i].equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns this TaskList as a numbered, formatted String.
     * Each task is rendered on a separate line, prefixed with a 1-based index.
     *
     * @return The String representation of the TaskList.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.storage.size(); i++) {
            sb.append(i + 1).append(". ").append(this.storage.get(i)).append("\n");
        }
        return sb.toString();
    }
}
