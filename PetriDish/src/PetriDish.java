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

	public final int PETRI_DISH_SIZE = 750; // currently copied from PetriDishApp (temp)

	private boolean done; // true only when the simulation thread must be stopped
	private PetriDishApp app; // refers to the application thread - aka the GUI thread, needed to send
								// graphics updates to it

	private Random rng; // used for random behavior of the overall simulation

	private ArrayList<Cell> allCells; // contains all the single-celled organisms inhabiting the petri dish

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
	@Override
	public void run() {

		// set up simulation
		done = false;
		rng = new Random();
		allCells = new ArrayList<Cell>();

		// fill the petri dish with cells TODO this is for debug
		for (int i = 0; i < 1; i++) { // a herd of herbivores, in the center
			allCells.add(new Herbivore(this, PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50,
					PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50, 0, 0, 5));
		}
		for (int i = 0; i < 25; i++) { // a small pile of food, in the center
			allCells.add(new Agar(this, PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50,
					PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50, 0, 0, 3));
		}

		// main simulation loop
		do {

			// ask to draw the next tick of the simulation on the graphics thread
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list

					// iterate through the graphics nodes list and replace it with refreshed
					// graphics
					for (int i = 0; i < Math.min(allNodes.size(), allCells.size()); i++) {
						allNodes.set(i, allCells.get(i).getGraphic()); // update the graphic for the cell in the nodes
																		// list
					}

					// load graphics from new cells
					if (allNodes.size() < allCells.size()) { // in the past update, more cells were born than died. so,
																// we need to expand the allNodes list
						for (int i = allNodes.size(); i < allCells.size(); i++) {
							allNodes.add(allCells.get(i).getGraphic());
						}
					}

					// forget graphics from old cells
					if (allNodes.size() > allCells.size()) { // in the past update, more cells died than were born. so,
																// we need to contract the allNodes list
						for (int i = allNodes.size(); i > allCells.size(); i--) {
							allNodes.remove(allNodes.size() - 1); // removes the last n graphics nodes, where n is
																	// allNodes.size() - allCells.size()
						}
					}

					// System.out.println("NEW FRAME!");
				}

			}); // this request is sent to the graphics thread and runs in parallel to this
				// thread; thus, the graphics thread draws the previous tick while the
				// simulation prepares the next tick.

			// the simulation is running a lot faster than the graphics thread (which is
			// full of heavy JavaFX bloat); need to make a solution to avoid letting the
			// update thread get ahead of the graphics thread

			// run the simulation by asking all the living cells to take their turns
			for (int i = 0; i < allCells.size(); i++) {
				if (allCells.get(i).isAlive()) { // verify the cell is living before updating it
					Cell newCell = allCells.get(i).update();
					if (newCell != null) {
						allCells.add(newCell); // if an offspring was produced the allCells list grows in size. note
												// that newborn cells are updated on the same cycle they are born (cell
												// implementations should handle this somehow)
					}
				} else { // if a cell died, the allCells list shrinks in size
					allCells.remove(i);
					i--; // decrement i to avoid skipping over a cell
				}
				if (allCells.size() < 10) { // deploy food TODO for debug purposes
					allCells.add(new Agar(this, rng.nextInt(PETRI_DISH_SIZE - 29) + 15,
							rng.nextInt(PETRI_DISH_SIZE - 29) + 15, 0, 0, 3));
				}
			}

			// hard delay between simulation ticks (TODO configurable)
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// System.out.println("NEW UPDATE!");

		} while (!done);
	}

	/**
	 * Helper method for cells that want to know what objects they can see in the
	 * petri dish (those within a certain range of them) Said objects must be within
	 * the max distance, alive, and not the querying cell itself
	 * 
	 * @param me          the querying cell
	 * @param maxDistance the distance to search within
	 * @return a list of cells in the range
	 */
	public ArrayList<Cell> getCellsInRange(Cell me, double maxDistance) {
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
