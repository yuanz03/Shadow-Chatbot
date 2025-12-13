package shadowbuddy.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Represents a dialog box consisting of the speaker's face and text label.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Initializes a DialogBox instance with the given message text and avatar image.
     *
     * @param message The content of the chatbot or user response.
     * @param avatar The profile image representing the chatbot or user.
     */
    private DialogBox(String message, Image avatar) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(message);
        displayPicture.setImage(avatar);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on the right.
     */
    private void flip() {
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(tmp);
        getChildren().setAll(tmp);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }

    /**
     * Applies visual styling to the dialog box based on the given command type.
     *
     * @param commandType The command type String used to determine the styling to apply.
     */
    private void changeDialogStyle(String commandType) {
        switch (commandType) {
        case "TODO", "DEADLINE", "EVENT":
            dialog.getStyleClass().add("add-label");
            break;
        case "MARK", "UNMARK":
            dialog.getStyleClass().add("marked-label");
            break;
        case "DELETE":
            dialog.getStyleClass().add("delete-label");
            break;
        case "UNKNOWN":
            dialog.getStyleClass().add("error-label");
            break;
        default:
            // Do nothing
        }
    }

    /**
     * Creates a DialogBox instance that represents the user.
     *
     * @param message The content of the user response.
     * @param avatar The profile image representing the user.
     * @return A DialogBox configured for the user displayed on the right.
     */
    public static DialogBox getUserDialog(String message, Image avatar) {
        return new DialogBox(message, avatar);
    }

    /**
     * Creates a DialogBox instance that represents the chatbot.
     * An overloaded factory method of DialogBox that takes a greeting message and chatbot image.
     *
     * @param message The content of the chatbot response.
     * @param avatar The profile image representing the chatbot.
     * @return A DialogBox configured for the chatbot displayed on the left.
     */
    public static DialogBox getShadowDialog(String message, Image avatar) {
        var shadowDialog = new DialogBox(message, avatar);
        shadowDialog.flip();
        return shadowDialog;
    }

    /**
     * Creates a DialogBox instance that represents the chatbot.
     * An overloaded factory method of DialogBox that takes a greeting message, chatbot image, and command type.
     *
     * @param message The content of the chatbot response.
     * @param avatar The profile image representing the chatbot.
     * @param commandType The type of the user command to customize the dialog style.
     * @return A DialogBox configured for the chatbot displayed on the left.
     */
    public static DialogBox getShadowDialog(String message, Image avatar, String commandType) {
        var shadowDialog = new DialogBox(message, avatar);
        shadowDialog.flip();
        shadowDialog.changeDialogStyle(commandType);
        return shadowDialog;
    }
}
