package seedu.address.ui;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.CalendarRequestEvent;
import seedu.address.commons.events.ui.ExitAppRequestEvent;
import seedu.address.commons.events.ui.JumpToListAllTagsRequestEvent;
import seedu.address.commons.events.ui.MassEmailRequestEvent;
import seedu.address.commons.events.ui.PersonPanelSelectionChangedEvent;
import seedu.address.commons.events.ui.ShowHelpRequestEvent;
import seedu.address.commons.util.FxViewUtil;
import seedu.address.logic.Logic;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ReadOnlyPerson;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Region> {

    private static final String ICON = "/images/address_book_32.png";
    private static final String FXML = "MainWindow.fxml";
    private static final int MIN_HEIGHT = 600;
    private static final int MIN_WIDTH = 450;

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private BrowserPanel browserPanel;
    private EmailPanel emailPanel;
    private CalendarPanel calendarPanel;
    private PersonListPanel personListPanel;
    private PersonInfo personInfo;
    private StatusBarFooter statusBarFooter;
    private ResultDisplay resultDisplay;
    private CommandBox commandBox;
    private LoginPanel loginPanel;
    private CommandPredictionPanel commandPredictionPanel;
    private TagListPanel tagListPanel;
    private Config config;
    private UserPrefs prefs;

    @FXML
    private StackPane browserPlaceholder;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    public MainWindow(Stage primaryStage, Config config, UserPrefs prefs, Logic logic) {
        super(FXML);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;

        // Configure the UI
        setTitle(config.getAppTitle());
        setIcon(ICON);
        setWindowMinSize();
        setWindowDefaultSize(prefs);
        Scene scene = new Scene(getRoot());
        primaryStage.setScene(scene);

        setAccelerators();
        registerAsAnEventHandler(this);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window for login.
     */
    void fillLogin() {
        loginPanel = new LoginPanel();
        browserPanel = new BrowserPanel();
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        statusBarFooter = new StatusBarFooter(prefs.getAddressBookFilePath(), logic.getFilteredPersonList().size());
        resultDisplay = new ResultDisplay();
        commandBox = new CommandBox(logic);
        browserPlaceholder.getChildren().add(loginPanel.getRoot());
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        //browserPanel = new BrowserPanel();
        browserPlaceholder.getChildren().clear();
        browserPlaceholder.getChildren().add(browserPanel.getRoot());

        //personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        //ResultDisplay resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        // Overlay CommandPredictionPanel over ResultDisplay
        commandPredictionPanel = new CommandPredictionPanel();
        resultDisplayPlaceholder.getChildren().add(commandPredictionPanel.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(prefs.getAddressBookFilePath(),
                logic.getFilteredPersonList().size());

        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        //CommandBox commandBox = new CommandBox(logic);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    void hide() {
        primaryStage.hide();
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    /**
     * Sets the given image as the icon of the main window.
     * @param iconSource e.g. {@code "/images/help_icon.png"}
     */
    private void setIcon(String iconSource) {
        FxViewUtil.setStageIcon(primaryStage, iconSource);
    }

    /**
     * Sets the default size based on user preferences.
     */
    private void setWindowDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    private void setWindowMinSize() {
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
    }

    /**
     * Returns the current size and the position of the main Window.
     */
    GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
    }

    /**
     * Opens the help window.
     */
    @FXML
    public void handleHelp() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.show();
    }

    /**
     * Switch to the Email panel.
     */
    @FXML
   public void handleEmail(ArrayList<String> emails) {
        emailPanel = new EmailPanel(emails);
        browserPlaceholder.getChildren().add(emailPanel.getRoot());
        browserPlaceholder.getChildren().setAll(emailPanel.getRoot());
    }

    /**
     * Switch to the Email panel.
     */
    @FXML
    public void handleCalendar() {
        calendarPanel = new CalendarPanel();
        browserPlaceholder.getChildren().add(calendarPanel.getRoot());
        browserPlaceholder.getChildren().setAll(calendarPanel.getRoot());
    }

    /**
     * Switch to the Browser panel.
     */

    @FXML
    public void handleBrowser() {
        browserPlaceholder.getChildren().setAll(browserPanel.getRoot());
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        raise(new ExitAppRequestEvent());
    }

    public PersonListPanel getPersonListPanel() {
        return this.personListPanel;
    }

    /**
     * Opens the tag list panel
     */
    public void handleTagListPanel() {
        tagListPanel = new TagListPanel(logic.getFilteredTagList());
        browserPlaceholder.getChildren().clear();
        browserPlaceholder.getChildren().add(tagListPanel.getRoot());
    }

    /**
     * Loads the information of the person in the BrowserPanel position
     * @param person
     */
    private void loadPersonInfo(ReadOnlyPerson person) {
        personInfo = new PersonInfo(person);
        browserPlaceholder.getChildren().clear();
        browserPlaceholder.getChildren().add(personInfo.getRoot());
    }

    void releaseResources() {
        browserPanel.freeResources();
        calendarPanel.freeResources();
    }

    @Subscribe
    private void handleShowHelpEvent(ShowHelpRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleHelp();
    }

    @Subscribe
    private void handleMassEmailEvent(MassEmailRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleEmail(event.getEmailList());
    }

    @Subscribe
    private void handleCalendarRequestEvent(CalendarRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleCalendar();
    }

    @Subscribe
    private void handlePersonPanelSelectionChangedEvent(PersonPanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        loadPersonInfo(event.getNewSelection().person);
    }

    @Subscribe
    private void handleListAllTagsEvent(JumpToListAllTagsRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleTagListPanel();
    }
}
