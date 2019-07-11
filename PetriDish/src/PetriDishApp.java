import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

/**
 * The App class launches the application, starts associated threads and opens
 * windows. It also contains all user input handling code that is run on the
 * JavaFX thread.
 * 
 * @author Andrey Vorontsov
 */
public class PetriDishApp extends Application {

	public static final int PETRI_DISH_SIZE = 750; // the petri dish is a square of this dimension

	private Group guiRoot; // the root node of the GUI window scene graph
	private Group petriRoot; // the root node of the simulation window scene graph
	private PetriDish petri; // the thread responsible for running the simulation in parallel to the GUI
								// thread

	/**
	 * Launches the Petri Dish application (JavaFX Application thread startup)
	 * 
	 * @param args unused
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Initializes the GUI and simulation windows, then starts the simulation
	 * thread. After this method runs at launch, there are two windows open and two
	 * threads running (one graphics thread updating both windows, one simulation
	 * thread running the petri dish in the background)
	 * 
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 * @param appWindow supplied by JavaFX
	 * @throws Exception by the default JavaFX method - it seems JavaFX is able to
	 *                   handle certain exceptions and keep breathing
	 */
	@Override
	public void start(Stage appWindow) throws Exception {

		// initialize GUI window "control panel" as the master window

		// setup the scene graph
		guiRoot = new Group();
		ObservableList<Node> allGuiNodes = guiRoot.getChildren();

		// scene graph contains only a text message for now
		Text currMsg = new Text("Initializing Petri Dish.");
		currMsg.setX(30);
		currMsg.setY(30);
		allGuiNodes.add(currMsg);

		// set the GUI window's dimensions
		Scene scene = new Scene(guiRoot, 350, 600);

		// set the GUI window's stats incl. title, location
		appWindow.setTitle("Control Panel");
		appWindow.setScene(scene);
		appWindow.setResizable(false);
		appWindow.setX(25);
		appWindow.setY(25);

		// initialize simulation window "petri dish"

		Stage petriWindow = new Stage();

		petriRoot = new Group();

		// no nodes; initially simulation window is blank, so no need to modify
		// petriRoot's children

		Scene petriScene = new Scene(petriRoot, PETRI_DISH_SIZE, PETRI_DISH_SIZE);

		petriWindow.setTitle("Petri Dish");
		petriWindow.setScene(petriScene);
		petriWindow.setResizable(false);
		petriWindow.setX(425);
		petriWindow.setY(100);

		// set up closing behavior of the two windows

		appWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) { // the GUI window closes the whole application before closing itself
				stop();
				petriWindow.close();
			}
		});
		petriWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) { // the simulation window does not close if asked
				event.consume();
			}
		});

		// both windows are ready, show them

		appWindow.show();
		petriWindow.show();

		// start the simulation thread and hook it to this thread

		petri = new PetriDish(this);

	}

	/**
	 * Gets the root node of the scene graph of the simulation window, allowing the
	 * auxiliary thread to get a reference to send graphics to
	 * 
	 * @return the root node of the scene graph of the simulation window
	 */
	public Group getPetriRoot() {
		return petriRoot;
	}

	/**
	 * When the app closes it will close down the simulation thread as well
	 * 
	 * @see javafx.application.Application#stop()
	 */
	@Override
	public void stop() {
		petri.stop();
	}

}
