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

	// timers and stuff used to track performance
	long framesPerSecond; // initialized only after first simulation loop completes
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
	private ArrayList<Node> graphicsToDraw = new ArrayList<Node>(); // populated by the simulation thread at the end of every update
																		// contains graphics objects produced from every cell

	// the dimensions of this simulation petri dish are fixed at instantiation time
	private final int simulationWidth;
	private final int simulationHeight;
	
	/**
	 * Starts the petri dish simulation thread.
	 * 
	 * @param app a reference to the GUI thread to enable graphics output from this
	 *            thread
	 */
	public PetriDish(PetriDishApp app) {
		this.app = app;
		
		simulationWidth = app.newSimulationWidth.get();
		simulationHeight = app.newSimulationHeight.get();
		
		new Thread(this).start();
	}

	/**
	 * The primary simulation loop, controlled from the GUI thread. Sends regular
	 * requests to the GUI thread to redraw the contents of the petri dish.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		setupSimulation(); // spawns cells to start off the simulation (TODO this for debug)
		
		// main simulation loop (labeled)
		main:
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
					
					app.updateFrameRateDisplay(framesPerSecond); // before we draw the simulation itself, send the frame rate information from the last update

					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list
					
					allNodes.setAll(graphicsToDraw); // replace the old list with a fresh new list of graphics
					
					// placing the graphics in allNodes assigns them all to be children of the petri window's root
					// so the scene graph looks like the root, with a couple hundred direct children (tree height = 1)

					graphicsCycleDelta = System.nanoTime() - cycleStartTime; // stop this thread's work timer
					waitingForGraphics = false; // graphics thread finished its work. simulation thread can continue
												// once it is also finished
				}

			}); // end of code for the graphics thread
			
			// start of code for simulation thread
			
			ArrayList<Node> newGraphicsToDraw = new ArrayList<Node>(); // to avoid concurrent modification; the simulation thread loads graphics into a temporary list, then shallow copies it to allow the graphics thread to use it on the next cycle

			// run the simulation by asking all the living cells to take their turns

			for (int i = 0; i < allCells.size(); i++) {
				
				// before updating the cell, consult GUI state info and take any necessary action
				
				// if the simulation has been paused by the user since we last checked, put the loop on hold until we get unpaused
				// this delays the completion of this cycle until unpaused and generates a warning message
				while (app.simulationPaused.get()) {
					if (done) {
						break main; // oh, we're 100% finished
					}
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (done) {
					break main; // oh, we're 100% finished
				}
				
				// update the cell

				// verify the cell is living before updating it
				if (allCells.get(i).isAlive()) {

					// for each update, the cell is given lists of visible and touched cells
					// also given the opportunity to return a reference to a single new offspring

					ArrayList<Cell> newCells = allCells.get(i)
							.update(getCellsInRange(allCells.get(i), allCells.get(i).getScaledVisionRange()),
									getTouchingCells(allCells.get(i)));
					
					// after updating, save the refreshed graphic
					newGraphicsToDraw.add(allCells.get(i).getGraphic());
					
					if (newCells != null) {
						allCells.addAll(newCells); // if an offspring was produced the allCells list grows in size. note
												// that newborn cells are updated on the same cycle they are born
					}

				} else { // if a cell died, the allCells list shrinks in size
					// note that order of the allCells list doesn't matter; so rather than running
					// O(n) remove(), we can do a O(1) swap with the last entry

					allCells.set(i, allCells.get(allCells.size() - 1)); // swap with the end
					allCells.remove(allCells.size() - 1); // trim off the end
					i--; // remember to update the swapped element too
					// note that for the last element, the call to set() does nothing
				}
				
				// done updating this cell

			} // finished updating all petri dish inhabitants and saving copies of their graphics

			divineIntervention(); // make any changes to the simulation that do not follow from the cells' own actions
			// any magically summoned cells aren't drawn until the next cycle, which is fine I reckon (they don't get updated either)
			
			graphicsToDraw = newGraphicsToDraw; // prepare the graphicsToDraw list for the next cycle

			// stop this thread's work timer
			simulationCycleDelta = System.nanoTime() - cycleStartTime;
			
			// end of code for the simulation thread
			
			// the rest of this code is run on the simulation thread and should be kept brief

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
				timeRemainingNanos = app.simulationDelay.get() * 1000000 - graphicsCycleDelta;
				if (timeRemainingNanos < -1000000) { // at least 1 ms has been lost, warning
					System.out.println("WARNING: The graphics thread is lagging. Lost "
							+ (-1 * timeRemainingNanos) / 1000000 + " milliseconds."); // accurate to within 1 ms
				}

			} else { // program is bottlenecked by simulation thread
				timeRemainingNanos = app.simulationDelay.get() * 1000000 - simulationCycleDelta;
				if (timeRemainingNanos < -1000000) { // at least 1 ms has been lost, warning
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
			
			// finally, calculate the true time we spent on this cycle
			// max of the minimum time and the actual times spent by each thread
			long thisCycleDelta = Math.max(app.simulationDelay.get() * 1000000, Math.max(simulationCycleDelta, graphicsCycleDelta)); // the real time elapsed
			
			framesPerSecond = 1000000000/thisCycleDelta;
			
		} while (!done); // check if we have gotten an order to stop since the last tick
	}
	
	/**
	 * Helper method to set up the petri dish simulation. Creates assorted single-celled life.
	 */
	private void setupSimulation() {
		
		// set up simulation debug preset TODO
		for (int i = 0; i < app.newSimulationGrazerPop.get(); i++) { // a herd of herbivores, to the left
			allCells.add(new Grazer(this, rng, simulationWidth / 4 + rng.nextInt(100) - 50,
					simulationHeight / 2 + rng.nextInt(100) - 50, 0, 0, 50));
		}
		for (int i = 0; i < app.newSimulationPredPop.get(); i++) { // a herd of predators, to the right
			allCells.add(new Predator(this, rng, simulationWidth * 3 / 4 + rng.nextInt(100) - 50,
					simulationHeight / 2 + rng.nextInt(100) - 50, 0, 0, 100));
		}
		for (int i = 0; i < app.newSimulationAgarPop.get(); i++) { // scatter some food to start
			allCells.add(new Agar(this, rng,
					rng.nextInt((simulationWidth - 29)) + 15,
					rng.nextInt((simulationHeight - 29)) + 15, 0, 0, 35));
		}
		for (int i = 0; i < app.newSimulationPlantPop.get(); i++) { // plants at totally random locations
			allCells.add(new Plant(this, rng, rng.nextInt((simulationWidth - 29)) + 15,
					rng.nextInt((simulationHeight - 29)) + 15, 0, 0, 100));
		}

		// fill the graphics list for initial setup
		for (Cell c: allCells) {
			graphicsToDraw.add(c.getGraphic());
		}

	}
	
	/**
	 * Helper method that currently spawns cells randomly during the simulation. TODO in the future, this method will fulfill certain GUI requests by acting on the simulation safely between updates.
	 */
	private void divineIntervention() {
		
		for (int i=0; i<rng.nextInt(app.runningAgarFeedFactor.get() + 1); i++) {
			allCells.add(new Agar(this, rng, rng.nextInt((int) (simulationWidth - 29)) + 15,
					rng.nextInt((int) (simulationHeight - 29)) + 15, 0, 0, 35));
		}
//		if (rng.nextInt(1000) == 1) {
//			allCells.add(new Grazer(this, rng, rng.nextInt(PetriDishApp.PETRI_DISH_WIDTH - 29) + 15,
//					rng.nextInt(PetriDishApp.PETRI_DISH_HEIGHT - 29) + 15, 0, 0, 50));
//		}
//		if (rng.nextInt(2000) == 1) {
//			allCells.add(new Predator(this, rng, rng.nextInt(PetriDishApp.PETRI_DISH_WIDTH - 29) + 15,
//					rng.nextInt(PetriDishApp.PETRI_DISH_HEIGHT - 29) + 15, 0, 0, 100));
//		}
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
		// this shortcut saves some time because many Cells have a vision range of 0
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
			if (distanceBetween(curr.getX(), curr.getY(), me.getX(), me.getY()) < me.getRadius() + curr.getRadius()
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
	 * @return a reference to the GUI thread API enabling retrieval of GUI state information
	 */
	public PetriDishApp getApp() {
		return app;
	}

	/**
	 * @return the simulationWidth
	 */
	public int getSimulationWidth() {
		return simulationWidth;
	}

	/**
	 * @return the simulationHeight
	 */
	public int getSimulationHeight() {
		return simulationHeight;
	}

	/**
	 * @return the framesPerSecond value of this petri dish simulation (calculated for every tick) (not averaged)
	 */
	public long getFramesPerSecond() {
		return framesPerSecond;
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
