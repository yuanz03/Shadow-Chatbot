package shadowbuddy.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import shadowbuddy.services.ShadowException;
import shadowbuddy.storage.ShadowStorage;

public class ShadowControllerTest {
    private static final ShadowCommand MARK_COMMAND = new ShadowCommand(ShadowCommand.CommandType.MARK, 1);
    private static final ShadowCommand UNMARK_COMMAND = new ShadowCommand(ShadowCommand.CommandType.UNMARK, 1);
    private static final ShadowCommand DELETE_COMMAND = new ShadowCommand(ShadowCommand.CommandType.DELETE, 1);
    private static final ShadowCommand FIND_COMMAND = new ShadowCommand(ShadowCommand.CommandType.FIND, "book");
    private static final ShadowCommand TODO_COMMAND = new ShadowCommand(ShadowCommand.CommandType.TODO,
            "borrow book");
    private static final ShadowCommand DEADLINE_COMMAND = new ShadowCommand(ShadowCommand.CommandType.DEADLINE,
            "return book", "Dec 2 2025 18:00");
    private static final ShadowCommand EVENT_COMMAND = new ShadowCommand(ShadowCommand.CommandType.EVENT,
            "project meeting", "Jul 4 2025 16:00", "Jul 5 2025 20:00");
    private static final ShadowCommand UNKNOWN_COMMAND = new ShadowCommand(ShadowCommand.CommandType.UNKNOWN);

    @Test
    public void execute_todoTaskCreation(@TempDir Path tempDir) throws ShadowException {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        dummyController.executeCommand(TODO_COMMAND, dummyUi);
        assertEquals("[T][ ] borrow book", dummyController.getTaskList().getTask(1).toString());
    }

    @Test
    public void execute_deadlineTaskCreation(@TempDir Path tempDir) throws ShadowException {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        dummyController.executeCommand(DEADLINE_COMMAND, dummyUi);
        assertEquals("[D][ ] return book (by: Dec 2 2025 18:00)",
                dummyController.getTaskList().getTask(1).toString());
    }

    @Test
    public void execute_eventTaskCreation(@TempDir Path tempDir) throws ShadowException {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        dummyController.executeCommand(EVENT_COMMAND, dummyUi);
        assertEquals("[E][ ] project meeting (from: Jul 4 2025 16:00 to: Jul 5 2025 20:00)",
                dummyController.getTaskList().getTask(1).toString());
    }

    @Test
    public void execute_markTask(@TempDir Path tempDir) throws ShadowException {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        dummyController.executeCommand(TODO_COMMAND, dummyUi);
        dummyController.executeCommand(MARK_COMMAND, dummyUi);
        assertEquals("[T][X] borrow book", dummyController.getTaskList().getTask(1).toString());
    }

    @Test
    public void execute_unmarkTask(@TempDir Path tempDir) throws ShadowException {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        dummyController.executeCommand(TODO_COMMAND, dummyUi);
        dummyController.executeCommand(MARK_COMMAND, dummyUi);
        dummyController.executeCommand(UNMARK_COMMAND, dummyUi);
        assertEquals("[T][ ] borrow book", dummyController.getTaskList().getTask(1).toString());
    }

    @Test
    public void execute_deleteTask(@TempDir Path tempDir) throws ShadowException {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        dummyController.executeCommand(TODO_COMMAND, dummyUi);
        dummyController.executeCommand(DELETE_COMMAND, dummyUi);
        assertEquals(0, dummyController.getTaskList().getLength());
    }

    @Test
    public void execute_findKeyword(@TempDir Path tempDir) throws ShadowException {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        dummyController.executeCommand(TODO_COMMAND, dummyUi);
        dummyController.executeCommand(DEADLINE_COMMAND, dummyUi);
        dummyController.executeCommand(EVENT_COMMAND, dummyUi);

        String actualOutput = dummyController.executeCommand(FIND_COMMAND, dummyUi);
        String expectedOutput = "Here are the matching tasks in your list:\n"
                + "1. [T][ ] borrow book\n"
                + "2. [D][ ] return book (by: Dec 2 2025 18:00)\n";
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void execute_emptyTaskList_exceptionThrown(@TempDir Path tempDir) {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        try {
            dummyController.executeCommand(MARK_COMMAND, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! Your task list is empty!", exception.getMessage());
        }

        try {
            dummyController.executeCommand(UNMARK_COMMAND, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! Your task list is empty!", exception.getMessage());
        }

        try {
            dummyController.executeCommand(DELETE_COMMAND, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! Your task list is empty!", exception.getMessage());
        }
    }

    @Test
    public void execute_invalidTaskIndex_exceptionThrown(@TempDir Path tempDir) {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        ShadowCommand invalidMarkCommand = new ShadowCommand(ShadowCommand.CommandType.MARK, 2);
        ShadowCommand invalidUnmarkCommand = new ShadowCommand(ShadowCommand.CommandType.UNMARK, 3);
        ShadowCommand invalidDeleteCommand = new ShadowCommand(ShadowCommand.CommandType.DELETE, 4);

        try {
            dummyController.executeCommand(TODO_COMMAND, dummyUi);
            dummyController.executeCommand(invalidMarkCommand, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! Please enter a number between 1 and "
                    + dummyController.getTaskList().getLength() + ".", exception.getMessage());
        }

        try {
            dummyController.executeCommand(DEADLINE_COMMAND, dummyUi);
            dummyController.executeCommand(invalidUnmarkCommand, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! Please enter a number between 1 and "
                    + dummyController.getTaskList().getLength() + ".", exception.getMessage());
        }

        try {
            dummyController.executeCommand(EVENT_COMMAND, dummyUi);
            dummyController.executeCommand(invalidDeleteCommand, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! Please enter a number between 1 and "
                    + dummyController.getTaskList().getLength() + ".", exception.getMessage());
        }
    }

    @Test
    public void execute_duplicateTaskDescription_exceptionThrown(@TempDir Path tempDir) {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        ShadowCommand duplicateDeadlineCommand = new ShadowCommand(ShadowCommand.CommandType.DEADLINE,
                "borrow book", "Dec 2 2025 18:00");

        try {
            dummyController.executeCommand(TODO_COMMAND, dummyUi);
            dummyController.executeCommand(TODO_COMMAND, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! A task with this description already exists!",
                    exception.getMessage());
        }

        try {
            dummyController.executeCommand(duplicateDeadlineCommand, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! A task with this description already exists!",
                    exception.getMessage());
        }
    }

    @Test
    public void execute_invalidTaskDoneStatus_exceptionThrown(@TempDir Path tempDir) {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        try {
            dummyController.executeCommand(TODO_COMMAND, dummyUi);
            dummyController.executeCommand(MARK_COMMAND, dummyUi);
            dummyController.executeCommand(MARK_COMMAND, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! The task indicated is already marked as done!", exception.getMessage());
        }

        try {
            dummyController.executeCommand(UNMARK_COMMAND, dummyUi);
            dummyController.executeCommand(UNMARK_COMMAND, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! The task indicated is already marked as not done!", exception.getMessage());
        }
    }

    @Test
    public void execute_unknownCommand_exceptionThrown(@TempDir Path tempDir) {
        Path tempFile = tempDir.resolve("dummy.txt");
        ShadowStorage dummyStorage = new ShadowStorage(tempFile.toString());
        ShadowController dummyController = new ShadowController(dummyStorage);
        ShadowUi dummyUi = new ShadowUi();

        try {
            dummyController.executeCommand(UNKNOWN_COMMAND, dummyUi);
            fail();
        } catch (ShadowException exception) {
            assertEquals("Invalid request! Try one of these commands: list, mark, unmark, todo, "
                    + "delete, event, or deadline, and I'll handle it for you.", exception.getMessage());
        }
    }
}
