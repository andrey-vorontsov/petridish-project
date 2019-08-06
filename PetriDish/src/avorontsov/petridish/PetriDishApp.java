package avorontsov.petridish;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * The App class launches the application, starts associated threads and opens
 * windows. It also contains all user input handling code that is run on the
 * JavaFX thread.
 * 
 * @author Andrey Vorontsov
 */
public class PetriDishApp extends Application {
	
	public static final boolean DEFAULT_SIMULATION_PAUSE_STATE = true;

	public static final int DEFAULT_PETRI_DISH_WIDTH = 750;
	public static final int DEFAULT_PETRI_DISH_HEIGHT = 750;
	
	public static final int MIN_PETRI_DISH_DIM = 30;
	public static final int MAX_PETRI_DISH_DIM = 2000;
	
	public static final int DEFAULT_SIMULATION_TICK_DELAY_MS = 30;// this is the default minimum time between update ticks of the simulation. At least this much time will pass between each graphics update request generated by the simulation thread
	// Some notes. Around 20 ms is the minimum tick delay to avoid inconsistent tick
	// rate, on my machine, with under 1000 cells. Delay grows much faster as cell
	// count increases. As cell number increases, simulation complexity increases
	// faster than graphics complexity. Since the graphics thread is also handling
	// all the JavaFX layers and events, it slows down whenever an event occurs
	// (e.g. user click); if delay is uncapped, JavaFX behavior is also quite strange (bursts of motion)
	
	public static final int MIN_SIMULATION_TICK_DELAY_MS = 0;
	public static final int MAX_SIMULATION_TICK_DELAY_MS = 100;
	
	// TODO temp values. Ideally a more robust system for this type of thing later on
	// i.e. a specialized species data structure which will also track certain spawning/etc. hints
	// e.g. species spawn rate; species initial population;
	
	// during sim
	public static final int DEFAULT_AGAR_FEED_FACTOR = 4;

	// startup sim
	public static final int DEFAULT_AGAR_INITIAL_POP = 100;
	public static final int DEFAULT_GRAZER_INITIAL_POP = 5;
	public static final int DEFAULT_PRED_INITIAL_POP = 2;
	public static final int DEFAULT_PLANT_INITIAL_POP = 12;
	
	private Group petriRoot; // the root node of the simulation window scene graph - all cell graphics Nodes are assigned as children of this Group
	private PetriDish petri; // the thread responsible for running the simulation in parallel to the GUI
								// thread
	private Stage petriWindow; // the window in which the simulation will be shown
		
	// GUI state information, protected for convenient access from PetriDish and other classes
	
	protected boolean simulationPaused; // true only when the simulation is paused
	protected int simulationDelay; // ranges from 0 (framerate uncapped) to 100 (10 fps)
									// changes are applied at the end of each simulation cycle
									// these min and max values are hardcoded in the slider and may be changed there
	
	// info affecting a currently running simulation
	protected int runningAgarFeedFactor;
	
	// init info for a newly created simulation
	protected int newSimulationWidth;
	protected int newSimulationHeight;
	protected int newSimulationAgarPop;
	protected int newSimulationGrazerPop;
	protected int newSimulationPredPop;
	protected int newSimulationPlantPop;
	
	// just to organize : this is the label to which the framerate is written
	private Label fps;

	/**
	 * Launches the Petri Dish application (JavaFX Application thread startup).
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
	 * thread running the petri dish in the background).
	 * 
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 * @param appWindow supplied by JavaFX
	 * @throws Exception by the default JavaFX method - it seems JavaFX is able to
	 *                   handle certain exceptions and keep breathing
	 */
	@Override
	public void start(Stage appWindow) throws Exception {
		
		// initialize GUI state information
		
		simulationPaused = DEFAULT_SIMULATION_PAUSE_STATE;
		simulationDelay = DEFAULT_SIMULATION_TICK_DELAY_MS;
		newSimulationHeight = DEFAULT_PETRI_DISH_HEIGHT;
		newSimulationWidth = DEFAULT_PETRI_DISH_WIDTH;
		
		runningAgarFeedFactor = DEFAULT_AGAR_FEED_FACTOR;
		
		newSimulationAgarPop = DEFAULT_AGAR_INITIAL_POP;
		newSimulationGrazerPop = DEFAULT_GRAZER_INITIAL_POP;
		newSimulationPredPop = DEFAULT_PRED_INITIAL_POP;
		newSimulationPlantPop = DEFAULT_PLANT_INITIAL_POP;

		// initializing GUI window "control panel" as the master window

		initializeGuiWindow(appWindow);

		// gui window is ready

		appWindow.show();

	}
	
	/**
	 * Helper method to lay out the GUI window at launch. Also contains code for the GUI controls.
	 * 
	 * @param appWindow the window supplied by JavaFX
	 */
	private void initializeGuiWindow(Stage appWindow) {
		
		// setup the scene graph
		// the layout used has a box at the top, bottom, and a center column that stretches between them
		// note that in each layout area, GUI elements are added left to right or top to bottom

		BorderPane guiLayout = new BorderPane();
		
		HBox topBox = new HBox();
		TabPane center = new TabPane();
		HBox botBox = new HBox();
		
		guiLayout.setTop(topBox);
		guiLayout.setCenter(center);
		guiLayout.setBottom(botBox);
		
		// configure boxes with spacing, padding, and alignment
		topBox.setPadding(new Insets(10, 5, 10, 5));
		topBox.setSpacing(10);
		topBox.setAlignment(Pos.CENTER);
		botBox.setPadding(new Insets(10, 5, 10, 5));
		botBox.setSpacing(10);
		botBox.setAlignment(Pos.CENTER);
		
		// TOP BOX
		
				// welcome message/status info
				Label currMsg = new Label("Welcome to Petri Dish.");
				topBox.getChildren().add(currMsg);
				
				// frame rate display
				fps = new Label("FPS: 0"); // TODO made into a field. bad decision? decide later
				topBox.getChildren().add(fps);
				
		// END OF TOP BOX
				
		// BOTTOM BOX
				
				// simulation start/close new buttons
				
				Button restartSim = new Button("Start");
				restartSim.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						if (restartSim.getText().equals("Close")) {
							if (petri != null) { // if a simulation is currently running
								stopSimulationThread();
								petriWindow.close();
							}
							petri = null;
							restartSim.setText("Start");
							currMsg.setText("No simulation running.");
						} else {
							if (petri == null) { // if no simulation is currently running
								initializeSimulationWindow();
								petriWindow.show();
								petri = new PetriDish(PetriDishApp.this);
							}
							restartSim.setText("Close");
							currMsg.setText("Restarted simulation.");
						}
					}
					
				});
				botBox.getChildren().add(restartSim);
				
				// finished simulation close and start new
				
				// END OF BOTTOM BOX
				
		// ALL TABS LISTED HERE
				

		center.getTabs().add(new CreateTab(this));
		center.getTabs().add(new EditTab(this));
		
		// FINISHED ADDING TABS
		
		// set the GUI window's dimensions
		Scene scene = new Scene(guiLayout, 350, 750);
		
		// GUI window takes focus if user clicks outside any GUI element (deselection)
		scene.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		       guiLayout.requestFocus();
		    }
		});

		// set up closing behavior

		appWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) { // the GUI window closes the whole application before closing itself
				if (petri != null) { // if there is currently a simulation going
					stop();
					petriWindow.close();
				}
			}
		});
		
		// set the GUI window's stats incl. title, location
		appWindow.setTitle("Control Panel");
		appWindow.setScene(scene);
		appWindow.setResizable(false);
		appWindow.setX(25);
		appWindow.setY(25);
		
		// finished setting up the GUI window
	}
	
	/**
	 * Helper method to  set up the simulation window, initially blank
	 * 
	 * TODO this information is outdated. Invalid states of existing simulation/window may cause unknown behavior.
	 * 
	 * This method is used on the fly to set up new simulations as requested. When called, it initializes the petridish window and scene graph but does not start the simulation. If a simulation already is running, it will start outputting to the set up window. The result is equivalent to clearSimulationWindow().
	 * A simulation can run without a window, generating no output; an internal scene graph continues to be updated even with no window to show it.
	 * If a simulation is attempted to be started before this method has been called at least once, the program will crash.
	 */
	private void initializeSimulationWindow() {
		petriWindow = new Stage();

		petriRoot = new Group();

		// no nodes; initially simulation window is blank, so no need to modify
		// petriRoot's children

		// set the petri dish window dimensions from configuration
		Scene petriScene = new Scene(petriRoot, newSimulationWidth, newSimulationHeight);

		// set the petri dish window's stats, incl. title, location
		petriWindow.setTitle("Petri Dish");
		petriWindow.setScene(petriScene);
		petriWindow.setResizable(false);
		petriWindow.setX(400);
		petriWindow.setY(25);
		
		// Petri window takes focus if user clicks outside any cell (deselection)
		petriScene.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		       petriRoot.requestFocus();
		    }
		});
		
		// closing behavior
		petriWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) { // the simulation window does not close if asked
				event.consume();
			}
		});
	}
	
	/**
	 * Invoked by the simulation during every update to send info to the fps display.
	 * 
	 * @param framesPerSecond the frame rate as calculated by the simulation on the past update
	 */
	public void updateFrameRateDisplay(long framesPerSecond) {
		fps.setText("FPS: " + framesPerSecond);
	}

	/**
	 * Gets the root node of the scene graph of the simulation window, allowing the
	 * auxiliary thread to get a reference to send graphics to.
	 * 
	 * @return the root node of the scene graph of the simulation window
	 */
	public Group getPetriRoot() {
		return petriRoot;
	}

	/**
	 * Gets the window the simulation is running in, allowing for information to be retrieved.
	 * 
	 * @return the window which the simulation is currently being sent to, if one exists.
	 */
	public Stage getPetriWindow() {
		return petriWindow;
	}

	/**
	 * When the app closes it will terminate the simulation thread as well.
	 * 
	 * @see javafx.application.Application#stop()
	 */
	@Override
	public void stop() {
		stopSimulationThread();
	}
	
	/**
	 * The simulation will terminate ASAP.
	 */
	public void stopSimulationThread() {
		if (petri != null)
			petri.stop();
	}
	
	/**
	 * Clears the simulation window. Note that if the simulation thread is still running it will promptly redraw itself on the window.
	 */
	public void clearSimulationWindow() {
		petriRoot.getChildren().clear();
	}

	/**
	 * @return true only while the simulation has been paused
	 */
	public boolean isSimulationPaused() {
		return simulationPaused;
	}

	/**
	 * @return the delay (ms) between every simulation update
	 */
	public long getSimulationDelay() {
		return simulationDelay;
	}

}
