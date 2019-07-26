package avorontsov.petridish;

import avorontsov.cells.*;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import java.util.Random;
import java.util.ArrayList;

/**
 * This class is instantiated by PetriDishApp and immediately starts its own
 * thread in parallel to the JavaFX GUI thread. On this thread, the petri dish
 * simulation is run and graphics information from the simulation is sent to the
 * GUI thread via Platform.runLater(). In addition, this class includes a
 * handful of helper methods used internally by the simulation.
 * 
 * TODO as a temporary feature, this class contains a hardcoded debug preset
 * (creating cells at the start of the simulation) as well as code to create
 * food during the simulation. These functions will eventually be extracted from
 * this class.
 * 
 * @author Andrey Vorontsov
 */
public class PetriDish implements Runnable {

	public final long SIMULATION_TICK_DELAY_MS = 30L; // TODO should be configurable, and may be moved to PetriDishApp.
														// this is the minimum time between update ticks of the
														// simulation (may be exceeded if processing takes longer). Not
														// a hard limit.
	public final long SIMULATION_TICK_DELAY_NANOS = SIMULATION_TICK_DELAY_MS * 1000000; // for convenience with
																						// System.nanoTime(), which is
																						// used to track performance

	// Some notes. Around 20 ms is the minimum tick delay to avoid inconsistent tick
	// rate, on my machine, with under 1000 cells. Delay grows much faster as cell
	// count increases. As cell number increases, simulation complexity increases
	// faster than graphics complexity. Since the graphics thread is also handling
	// all the JavaFX layers and events, it slows down whenever an event occurs
	// (e.g. user click)

	// timers used to track performance
	long simulationCycleDelta;
	long graphicsCycleDelta = 0; // a sentinel value. in any case the simulation thread waits for the graphics
									// thread to complete its work, at which time it updates this value
	private boolean waitingForGraphics; // set to true when the simulation thread asks for a frame to be drawn; once the
										// graphics frame is done, set to false

	private boolean done = false; // true only when the simulation thread must be stopped
	private PetriDishApp app; // refers to the application thread - aka the graphics thread, used to retrieve
								// the scene graph root that graphics information is built upon

	private Random rng = new Random(); // used for random behavior of the simulation; if set to use a specific seed,
										// the resulting simulation will be identical every time TODO configurable

	private ArrayList<Cell> allCells = new ArrayList<Cell>(); // contains all the single-celled organisms inhabiting the
																// petri dish
	private ArrayList<Cell> cellsToDraw; // re-created after every simulation cycle for use by the graphics thread - to
											// avoid have the two threads reading/writing from/to the same list

	/**
	 * Starts the petri dish simulation thread.
	 * 
	 * @param app a reference to the GUI thread to enable graphics output from this
	 *            thread
	 */
	public PetriDish(PetriDishApp app) {
		this.app = app;
		new Thread(this).start();
	}

	/**
	 * The primary simulation loop, controlled from the GUI thread. Sends regular
	 * requests to the GUI thread to redraw the contents of the petri dish.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("unchecked") // I cast the Object returned by ArrayList.clone() to the ArrayList of the type.
	@Override
	public void run() {

		// set up simulation debug preset TODO extract this functionality
		for (int i = 0; i < 16; i++) { // a herd of herbivores, to the left
			allCells.add(new Grazer(this, rng, PetriDishApp.PETRI_DISH_WIDTH / 4 + rng.nextInt(100) - 50,
					PetriDishApp.PETRI_DISH_HEIGHT / 2 + rng.nextInt(100) - 50, 0, 0, 3));
		}
		for (int i = 0; i < 6; i++) { // a herd of predators, to the right
			allCells.add(new Predator(this, rng, PetriDishApp.PETRI_DISH_WIDTH * 3 / 4 + rng.nextInt(100) - 50,
					PetriDishApp.PETRI_DISH_HEIGHT / 2 + rng.nextInt(100) - 50, 0, 0, 6));
		}
		for (int i = 0; i < 200; i++) { // scatter some food to start
			allCells.add(new Agar(this, rng,
					rng.nextInt(PetriDishApp.PETRI_DISH_WIDTH - 29) + 15,
					rng.nextInt(PetriDishApp.PETRI_DISH_HEIGHT - 29) + 15, 0, 0, 3));
		}
		for (int i = 0; i < 20; i++) { // three plants at totally random locations in the dish
			allCells.add(new Plant(this, rng, rng.nextInt(PetriDishApp.PETRI_DISH_WIDTH - 29) + 15,
					rng.nextInt(PetriDishApp.PETRI_DISH_HEIGHT - 29) + 15, 0, 0, 6));
		}

		cellsToDraw = (ArrayList<Cell>) allCells.clone(); // this array is used by the simulation thread to push a list
															// of cells to draw to the graphics thread. this step is
															// necessary because the threads run in parallel, and it is
															// dangerous if the simulation thread starts modifying the
															// allCells array while the graphics thread is reading it

		// main simulation loop
		do {

			// set timers for this cycle
			long cycleStartTime = System.nanoTime();
			waitingForGraphics = true;

			// this request is sent to the graphics thread and runs in parallel to this
			// thread; thus, the graphics thread draws the previous tick while the
			// simulation prepares the next tick.
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list - all
																						// graphics are made to be
																						// children of the petri dish
																						// window's scene graph root

					// iterate through the graphics nodes list and replace it with refreshed graphics
					for (int i = 0; i < Math.min(allNodes.size(), cellsToDraw.size()); i++) {
						allNodes.set(i, cellsToDraw.get(i).getGraphic()); // set() is used to avoid having to move other
																			// entries in the list
					}

					// if the number of cells grew since last time, we must expand the graphics list
					// to match using add()
					if (allNodes.size() < cellsToDraw.size()) {
						for (int i = allNodes.size(); i < cellsToDraw.size(); i++) {
							allNodes.add(cellsToDraw.get(i).getGraphic());
						}
					}

					// if the number of cells decreased since last time, we must contract the
					// graphics list by removing from the end
					if (allNodes.size() > cellsToDraw.size()) {
						for (int i = allNodes.size(); i > cellsToDraw.size(); i--) {
							allNodes.remove(allNodes.size() - 1); // removes the last n graphics nodes, where n is
																	// allNodes.size() - allCells.size()
						}
					}

					// after these actions, the scene graph of the petri dish window has been
					// completely replaced

					graphicsCycleDelta = System.nanoTime() - cycleStartTime; // stop this thread's work timer
					waitingForGraphics = false; // graphics thread finished its work. simulation thread can continue
												// once it is also finished
				}

			}); // end of code for the graphics thread

			// run the simulation by asking all the living cells to take their turns

			for (int i = 0; i < allCells.size(); i++) {

				// verify the cell is living before updating it
				if (allCells.get(i).isAlive()) {

					// for each update, the cell is given lists of visible and touched cells
					// also given the opportunity to return a reference to a single new offspring

					Cell newCell = allCells.get(i)
							.update(getCellsInRange(allCells.get(i), allCells.get(i).getScaledVisionRange()),
									getTouchingCells(allCells.get(i)));

					if (newCell != null) {
						allCells.add(newCell); // if an offspring was produced the allCells list grows in size. note
												// that newborn cells are updated on the same cycle they are born
					}

				} else { // if a cell died, the allCells list shrinks in size
					// note that order of the allCells list doesn't matter; so rather than running
					// O(n) remove(), we can do a O(1) swap with the last entry

					allCells.set(i, allCells.get(allCells.size() - 1)); // swap with the end
					allCells.remove(allCells.size() - 1); // trim off the end
					// note that for the last element, the call to set() does nothing
				}

				// below a certain total population, sprinkle food at random points
				// TODO extract, this is here for debug only
				while (allCells.size() < 40) {
					allCells.add(new Agar(this, rng, rng.nextInt(PetriDishApp.PETRI_DISH_WIDTH - 29) + 15,
							rng.nextInt(PetriDishApp.PETRI_DISH_HEIGHT - 29) + 15, 0, 0, 3));
				}

			} // finished updating all petri dish inhabitants

			cellsToDraw = (ArrayList<Cell>) allCells.clone(); // prepare the cells to draw list for the next cycle

			// stop this thread's work timer
			simulationCycleDelta = System.nanoTime() - cycleStartTime;

			// wait for the graphics thread to catch up if needed
			while (waitingForGraphics) {
				try {
					Thread.sleep(0, 100); // presumably waits for 100 nanoseconds; in reality, Windows granularity means
											// we only have ~1 ms precision
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// now both threads are finished and have reported their delta time

			// calculate how much time we have left after our threads finish
			long timeRemainingNanos;

			if (graphicsCycleDelta > simulationCycleDelta) { // program is bottlenecked by graphics thread
				timeRemainingNanos = SIMULATION_TICK_DELAY_NANOS - graphicsCycleDelta;
				if (timeRemainingNanos < 0) {
					System.out.println("WARNING: The graphics thread is lagging. Lost "
							+ (-1 * timeRemainingNanos) / 1000000 + " milliseconds."); // accurate to within 1 ms
				}

			} else { // program is bottlenecked by simulation thread
				timeRemainingNanos = SIMULATION_TICK_DELAY_NANOS - simulationCycleDelta;
				if (timeRemainingNanos < 0) {
					System.out.println("WARNING: The simulation thread is lagging. Lost "
							+ (-1 * timeRemainingNanos) / 1000000 + " milliseconds."); // accurate to within 1 ms
				}
			}

			// if any time remains, sleep until it's time to start working on the next cycle
			long timeRemainingMillis = timeRemainingNanos / 1000000; // java rounds down, but <1 ms precision is
																		// retained
			if (timeRemainingMillis > 0) {
				try {
					Thread.sleep(timeRemainingMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} while (!done); // check if we have gotten an order to stop since the last tick
	}

	/**
	 * Helper method for cells that want to know what objects they can see in the
	 * petri dish (those within a certain range of them). Said objects must be
	 * within the max distance, alive, and not the querying cell itself. If the
	 * maxDistance is exactly zero, returns an empty ArrayList.
	 * 
	 * @param me          the querying cell
	 * @param maxDistance the distance to search within
	 * @return a list of cells in the range
	 */
	public ArrayList<Cell> getCellsInRange(Cell me, double maxDistance) {
		// this shortcut saves some time because some Cells have a vision range of 0
		if (maxDistance == 0) {
			return new ArrayList<Cell>();
		}

		ArrayList<Cell> visibleCells = new ArrayList<Cell>();
		for (int i = 0; i < allCells.size(); i++) {
			Cell curr = allCells.get(i);
			if (distanceBetween(curr.getX(), curr.getY(), me.getX(), me.getY()) < maxDistance
					&& curr.isAlive()
					&& !curr.equals(me)) { // a cell is in my range iff it is closer than the max distance, it is alive,
											// and it is not myself

				visibleCells.add(curr);
			}
		}
		return visibleCells;
	}

	/**
	 * A similar method to getCellsInRange(); however, this method takes into
	 * account the radii of both the querying cell and the other cells to judge
	 * whether the two cells are touching - that is, if there is any overlap at all
	 * between their circles - as opposed to getCellsInRange() which looks at
	 * distance between their centerpoints (without considering their sizes)
	 * 
	 * @param me the querying cell
	 * @return a list of cells touching this cell
	 */
	public ArrayList<Cell> getTouchingCells(Cell me) {
		ArrayList<Cell> touchedCells = new ArrayList<Cell>();
		for (int i = 0; i < allCells.size(); i++) {
			Cell curr = allCells.get(i);
			if (distanceBetween(curr.getX(), curr.getY(), me.getX(), me.getY()) < me.getSize() + curr.getSize()
					&& curr.isAlive() && !curr.equals(me)) { // a cell is touching me iff it is closer than the sum of
																// our radii, it is alive,
																// and it is not myself
				touchedCells.add(curr);
			}
		}
		return touchedCells;
	}
	
	/**
	 * @return the Random object used by the simulation
	 */
	public Random getRNG() {
		return rng;
	}

	/**
	 * Helper method to get distance between two points in the petri dish.
	 * 
	 * @param x1 first point's x
	 * @param y1 first point's y
	 * @param x2 second point's x
	 * @param y2 second point's y
	 * @return the distance
	 */
	public static double distanceBetween(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	/**
	 * Causes the simulation thread to terminate ASAP after call
	 */
	public void stop() {
		done = true;
	}

}
