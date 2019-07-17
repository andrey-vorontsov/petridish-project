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
 * @author Andrey Vorontsov
 */
public class PetriDish implements Runnable {

	public final long SIMULATION_TICK_DELAY_MS = 30L; // TODO source from config. this is the minimum time between update ticks of the simulation (may be exceeded if processing takes longer)
	public final long SIMULATION_TICK_DELAY_NANOS = SIMULATION_TICK_DELAY_MS * 1000000;  
	// note to self. 15 ms (or perhaps 13 or 14) is the minimum tick delay to avoid inconsistent tick rate at least on my machine
	// note to self. as cell number increases, simulation complexity increases as a factorial (I think - because each cell checks each other cell multiple times per tick) - but graphics complexity (I assume) is linear - so the simulation stops getting ahead and starts being the slow one
	// note to self. Since the graphics thread is also handling all the JavaFX layers and events, it slows down whenever an event occurs (e.g. user click) and this can lasts up to ~30 ms easily
	
	// timers used to track performance
	long simulationCycleDelta;
	long graphicsCycleDelta = 0; // a sentinel value. in any case the simulation thread waits for the graphics thread to complete its work, at which time it updates this value

	
	private boolean done; // true only when the simulation thread must be stopped
	private PetriDishApp app; // refers to the application thread - aka the GUI thread, needed to send
								// graphics updates to it
	private boolean waitingForGraphics; // set to true when the simulation thread asks for a frame to be drawn; once the graphics frame is done, set to false
	
	private Random rng; // used for random behavior of the overall simulation

	private ArrayList<Cell> allCells; // contains all the single-celled organisms inhabiting the petri dish
	private ArrayList<Cell> cellsToDraw; // re-created after every simulation cycle for use by the graphics thread
	
	/**
	 * Starts the petri dish simulation thread.
	 * 
	 * @param app a reference to the GUI thread to enable graphics output of this
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
	@SuppressWarnings("unchecked") // I cast the Object returned by ArrayList.clone() to the ArrayList of the type, which I know. this is safe
	@Override
	public void run() {

		// set up simulation
		done = false;
		rng = new Random(); // no fixed seed for testing purposes TODO configurable
		allCells = new ArrayList<Cell>();

		// fill the petri dish with cells TODO this is for debug
		for (int i = 0; i < 20; i++) { // a herd of herbivores, to the left
			allCells.add(new Grazer(this, rng, PetriDishApp.PETRI_DISH_WIDTH / 4 + rng.nextInt(100) - 50,
					PetriDishApp.PETRI_DISH_HEIGHT / 2 + rng.nextInt(100) - 50, 0, 0, 5));
		}
		for (int i = 0; i < 2; i++) { // a herd of predators, to the right
			allCells.add(new Predator(this, rng, PetriDishApp.PETRI_DISH_WIDTH * 3 / 4 + rng.nextInt(100) - 50,
					PetriDishApp.PETRI_DISH_HEIGHT / 2 + rng.nextInt(100) - 50, 0, 0, 6));
		}
		for (int i = 0; i < 60; i++) { // a small pile of food, in the center
			allCells.add(new Agar(this, rng, PetriDishApp.PETRI_DISH_WIDTH / 2 + rng.nextInt(100) - 50,
					PetriDishApp.PETRI_DISH_HEIGHT / 2 + rng.nextInt(100) - 50, 0, 0, 3));
		}
		//allCells.add(new Plant(this, rng, PetriDishApp.PETRI_DISH_WIDTH / 2 + rng.nextInt(50) - 25,
		//		PetriDishApp.PETRI_DISH_HEIGHT / 2 + rng.nextInt(50) - 25, 0, 0, 3));
		
		cellsToDraw = (ArrayList<Cell>)allCells.clone(); // this array is used by the simulation thread to push a list of cells to draw to the graphics thread. this step is necessary because the threads run in parallel, and it is dangerous if the simulation thread starts modifying the allCells array while the graphics thread is digging around in it
		
		// main simulation loop
		do {
			
			// set timers for this cycle
			long cycleStartTime = System.nanoTime();
			waitingForGraphics = true;

			// ask to draw the next tick of the simulation on the graphics thread
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list
										
					// iterate through the graphics nodes list and replace it with refreshed
					// graphics
					for (int i = 0; i < Math.min(allNodes.size(), cellsToDraw.size()); i++) {
						allNodes.set(i, cellsToDraw.get(i).getGraphic()); // update the graphic for the cell in the nodes
																		// list
					}

					// load graphics from new cells
					if (allNodes.size() < cellsToDraw.size()) { // in the past update, more cells were born than died. so,
																// we need to expand the allNodes list
						for (int i = allNodes.size(); i < cellsToDraw.size(); i++) {
							allNodes.add(cellsToDraw.get(i).getGraphic());
						}
					}

					// forget graphics from old cells
					if (allNodes.size() > cellsToDraw.size()) { // in the past update, more cells died than were born. so,
																// we need to contract the allNodes list
						for (int i = allNodes.size(); i > cellsToDraw.size(); i--) {
							allNodes.remove(allNodes.size() - 1); // removes the last n graphics nodes, where n is
																	// allNodes.size() - allCells.size()
						}
					}
					
					graphicsCycleDelta = System.nanoTime() - cycleStartTime;
					waitingForGraphics = false; // graphics thread finished its work. simulation thread can continue once it is also finished
				}

			}); // this request is sent to the graphics thread and runs in parallel to this
				// thread; thus, the graphics thread draws the previous tick while the
				// simulation prepares the next tick.

			// run the simulation by asking all the living cells to take their turns
			
			for (int i = 0; i < allCells.size(); i++) {
				if (allCells.get(i).isAlive()) { // verify the cell is living before updating it
					Cell newCell = allCells.get(i).update(getCellsInRange(allCells.get(i), allCells.get(i).getVisionRange()),
							getTouchingCells(allCells.get(i)),
							getEatableCells(allCells.get(i))); // give the cell a list of visible cells to reference, as well as lists of cells it is touching and is in range to eat
					if (newCell != null) {
						allCells.add(newCell); // if an offspring was produced the allCells list grows in size. note
												// that newborn cells are updated on the same cycle they are born (cell
												// implementations should handle this somehow)
					}
				} else { // if a cell died, the allCells list shrinks in size
					allCells.remove(i);
					i--; // decrement i to avoid skipping over a cell
				}
				while (allCells.size() < 1000) { // deploy food at any random point TODO for debug purposes
					allCells.add(new Agar(this, rng, rng.nextInt(PetriDishApp.PETRI_DISH_WIDTH - 29) + 15,
							rng.nextInt(PetriDishApp.PETRI_DISH_HEIGHT - 29) + 15, 0, 0, 3));
				}
			}
			
			cellsToDraw = (ArrayList<Cell>)allCells.clone(); // prepare the cells to draw list for the next cycle
						
			// the simulation sometimes runs a lot faster than the graphics thread (which is
			// full of heavy JavaFX bloat); so it might finish working before waitingForGraphics becomes false again

			// record how much time the simulation thread took
			simulationCycleDelta = System.nanoTime() - cycleStartTime;
			
			// wait for the graphics thread to catch up if needed
			while (waitingForGraphics) {
				try {
					Thread.sleep(0, 100); // unsure of the exact outcome of this call TODO study
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// now both threads are finished and have reported their delta time
			
			// this should never happen
			if (graphicsCycleDelta == 0)
				throw new IllegalStateException("Petri Dish cannot continue. The graphics thread failed to responding.");
			
			long timeRemainingNanos; // calculate how much time we have left after our threads finish
			if (graphicsCycleDelta > simulationCycleDelta) { // program is bottlenecked by graphics thread
				timeRemainingNanos = SIMULATION_TICK_DELAY_NANOS - graphicsCycleDelta;
				if (timeRemainingNanos < 0) {
					System.out.println("WARNING: The graphics thread is lagging. Lost " + (-1*timeRemainingNanos)/1000000 + " milliseconds."); // rounding down
				}
			} else { // program is bottlenecked by simulation thread
				timeRemainingNanos = SIMULATION_TICK_DELAY_NANOS - simulationCycleDelta;
				if (timeRemainingNanos < 0) {
					System.out.println("WARNING: The simulation thread is lagging. Lost " + (-1*timeRemainingNanos)/1000000 + " milliseconds."); // rounding down
				}
			}
			
			// if any time remains, sleep until it's time to start working on the next cycle
			long timeRemainingMillis = timeRemainingNanos / 1000000; // java rounds down... shouldn't matter that much, we might gain a millisecond worst case scenario
			if (timeRemainingMillis > 0) {
				try {
					Thread.sleep(timeRemainingMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			

		} while (!done);
	}

	/**
	 * Helper method for cells that want to know what objects they can see in the
	 * petri dish (those within a certain range of them) Said objects must be within
	 * the max distance, alive, and not the querying cell itself
	 * If the maxDistance is exactly zero, return null.
	 * 
	 * @param me          the querying cell
	 * @param maxDistance the distance to search within
	 * @return a list of cells in the range
	 */
	public ArrayList<Cell> getCellsInRange(Cell me, double maxDistance) {
		if (maxDistance == 0) {
			return null;
		}
		ArrayList<Cell> visibleCells = new ArrayList<Cell>();
		for (int i = 0; i < allCells.size(); i++) {
			Cell curr = allCells.get(i);
			if (distanceBetween(curr.getX(), curr.getY(), me.getX(), me.getY()) < maxDistance && curr.isAlive()
					&& !curr.equals(me)) { // a cell is in my range iff it is closer than the max distance, it is alive,
											// and it is not myself
				visibleCells.add(curr);
			}
		}
		return visibleCells;
	}
	
	/**
	 * Helper method returning a list of cells that are in range to eat
	 * 
	 * @param me          the querying cell
	 * @return a list of edible cells
	 */
	public ArrayList<Cell> getEatableCells(Cell me) {
		return getCellsInRange(me, me.getSize());
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
					&& curr.isAlive() && !curr.equals(me)) { // a cell is in my range iff it is closer than the sum of
																// our radii, it is alive,
																// and it is not myself
				touchedCells.add(curr);
			}
		}
		return touchedCells;
	}

	/**
	 * Helper method to get distance between two points in the petri dish
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
	 * Causes the main loop to exit shortly after call
	 */
	public void stop() {
		done = true;
	}

}
