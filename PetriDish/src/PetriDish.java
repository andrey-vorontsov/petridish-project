import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import java.util.Random;
import java.util.ArrayList;

/**
 * @author Andrey Vorontsov
 * 
 *         TODO
 * 
 */
public class PetriDish implements Runnable {

	public static final int PETRI_DISH_SIZE = 750; // TODO retrieve this stuff from a config file

	private boolean done; // true only when the simulation thread must be stopped
	private PetriDishApp app; // refers to the application thread - aka the GUI thread, needed to send
								// graphics updates to it

	private Random rng; // used for random behavior of the overall simulation

	private ArrayList<Cell> allCells; // contains all the single-celled organisms inhabiting the petri dish
	private ArrayList<Cell> newCells; // contains the cells that have been recently created and need to be properly initialized in the graphics thread
	private ArrayList<Cell> deadCells; // contains the cells that have recently died and must be safely removed from both threads
	
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

		newCells = new ArrayList<Cell>(); // any cells added by the simulation or born by reproduction of existing cells must be tracked in this list; it's very important that the order of the new cells in this list be correct
		deadCells = new ArrayList<Cell>();
		
		// fill the petri dish with cells
		for (int i = 0; i < 15; i++) { // totally random in the upper left hand corner right now
			createCell(new Herbivore(this, 100 + rng.nextInt(100) - 50, 100 + rng.nextInt(100) - 50, 0, 0, 5));
		}
		for (int i = 0; i < 25; i++) { // totally random in the middle area right now
			createCell(new Agar(this, PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50,
					PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50, 0, 0, 3));
		}

		// main simulation loop
		do {
			
			// ask to add any newly created cells to the graphics list
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					
					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list

					//System.out.println("Pre-sync: Graphics thread object #: " + allNodes.size());
					//System.out.println("Pre-sync: Simulation thread object #: " + allCells.size());
					//System.out.println("Pre-sync: Queued new object #: " + newCells.size());

					for (int i = 0; i < newCells.size(); i++) {
						allNodes.add(newCells.get(i).getGraphic());
					}
					
					newCells.clear(); // finished adding any new cells, clear the queue
				}
				
			}); // done sending new cells to graphics thread
			
			// ask to draw the next tick of the simulation on the graphics thread
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list
					
					//System.out.println("Post-sync: Graphics thread object #: " + allNodes.size());
					//System.out.println("Post-sync: Simulation thread object #: " + allCells.size());
					//System.out.println("Post-sync: Queued new object #: " + newCells.size());
					
					// iterate through cells
					for (int i = 0; i < allNodes.size(); i++) { // TODO it's still a mystery. But sometimes, despite my best efforts, the allNodes list ends up being one smaller than the allCells list - but then recovers. how?
						if (allCells.get(i).isAlive()) { // update the graphic for the cell in the nodes list
							allNodes.set(i, allCells.get(i).getGraphic());
						} else { // if the cell has died during the last update cycle, flag it for elimination and move on
							eliminateCell(allCells.get(i));
						}
					}
				}

			}); // done updating graphics
			
			// ask to remove any dead cells from the lists
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					
					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list

					//System.out.println("Pre-cleanup: Graphics thread object #: " + allNodes.size());
					//System.out.println("Pre-cleanup: Simulation thread object #: " + allCells.size());
					//System.out.println("Pre-cleanup: Queued new object #: " + newCells.size());

					for (int i = 0; i < deadCells.size(); i++) {
						int removeFrom = allCells.indexOf(deadCells.get(i)); // hold off on removing the dead cells from allCells until here; we need to know the index
						allCells.remove(removeFrom);
						allNodes.remove(removeFrom);
					}
					
					deadCells.clear(); // finished adding any new cells, clear the queue
				}
				
			});

			// run the simulation by asking all the living cells to take their turns
			// cells that die as a result of the action of a cell earlier in the update
			// cycle are not updated
			for (int i = 0; i < allCells.size(); i++) {
				if (allCells.get(i).isAlive()) { // verify the cell is living before updating it
					Cell newCell = allCells.get(i).update();
					if (newCell != null) {
						createCell(newCell); // service any requests to produce offspring
					}
				}
				if (allCells.size() < 100) { // deploy food
					createCell(new Agar(this, rng.nextInt(PETRI_DISH_SIZE - 29) + 15, rng.nextInt(PETRI_DISH_SIZE - 29) + 15, 0, 0, 3));
				}
			}

			// hard delay between simulation ticks (TODO configurable)
			// TODO thread synchronization should be done here to avoid the graphics thread falling behind (better this thread lags than the other)
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} while (!done);
	}
	
	/**
	 * All newly created cells must pass through this method to ensure synchronization of the graphics thread and the simulation thread
	 * 
	 * @param c the cell to be initialized
	 */
	private void createCell(Cell newCell) {
		allCells.add(newCell);
		newCells.add(newCell);
	}
	
	/**
	 * All newly dead cells must pass through this method to ensure synchronization of the graphics thread and the simulation thread
	 * 
	 * @param c the cell to be initialized
	 */
	private void eliminateCell(Cell oldCell) {
		deadCells.add(oldCell); // queue for removal
	}

	/**
	 * Helper method for cells that want to know what objects they can see in the
	 * petri dish (those within a certain range of them) Said objects must be within
	 * the max distance, alive, and not the querying cell itself
	 * 
	 * @param me the querying cell
	 * @param maxDistance the distance the cell can see or eat from (contextually)
	 * @return a list of cells in the range
	 */
	public ArrayList<Cell> getCellsInRange(Cell me, double maxDistance) {
		ArrayList<Cell> visibleCells = new ArrayList<Cell>();
		for (int i = 0; i < allCells.size(); i++) {
			Cell curr = allCells.get(i);
			if (distanceBetween(curr.getX(), curr.getY(), me.getX(), me.getY()) < maxDistance && curr.isAlive()
					&& !curr.equals(me)) {
				// System.out.println(me + " detected " + curr + " at a distance of "
				// + distanceBetween(curr.getX(), curr.getY(), me.getX(), me.getY()));
				visibleCells.add(curr);
			}
		}
		visibleCells.remove(me); // avoid returning a reference to the cell itself (could lead to the cell eating
									// itself)
									// a bit of hack, but probably not a huge lag source unless there are a huge
									// number of cells in close range of one cell
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
