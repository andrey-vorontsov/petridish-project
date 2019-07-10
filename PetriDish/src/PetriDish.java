import java.util.ArrayList;
import java.util.Random;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * @author Andrey Vorontsov
 * 
 *         TODO
 * 
 *         TODO an important remark. two cells may still eat each other
 *         simultaneously.
 */
public class PetriDish implements Runnable {

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

		// fill the petri dish with cells
		allCells = new ArrayList<Cell>();
		for (int i = 0; i < 15; i++) { // totally random in the upper left hand corner right now
			allCells.add(new Herbivore(this, 100 + rng.nextInt(100) - 50, 100 + rng.nextInt(100) - 50, 0, 0, 5));
		}
		for (int i = 0; i < 50; i++) { // totally random in the middle area right now
			allCells.add(new Agar(this, app.PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50,
					app.PETRI_DISH_SIZE / 2 + rng.nextInt(100) - 50, 0, 0, 5));
		}

		// on the graphics thread, initialize the display information with the initial
		// contents
		// of the petri dish
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list
				for (int i = 0; i < allCells.size(); i++) {
					allNodes.add(allCells.get(i).getGraphic());
				}
			}

		});

		// regarding the above procedure.
		// the general contract is that at all times the list of allNodes (the children
		// of petriRoot in the App) has an equal size to the list of allCells, such that
		// each cell in allCells at an index i has its corresponding graphic at the
		// index i in allNodes

		// main simulation loop
		do {
			// ask to draw the next tick of the simulation on the graphics thread
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					ObservableList<Node> allNodes = app.getPetriRoot().getChildren(); // fetch the graphics list

					// TODO initialize display information for any newly born cells here

					// iterate through cells
					for (int i = 0; i < allCells.size(); i++) {
						if (allCells.get(i).isAlive()) { // update the graphic for the cell in the nodes list
							allNodes.set(i, allCells.get(i).getGraphic());
						} else { // if the cell has died during the last update cycle, two cases
							if (!(i == allCells.size() - 1)) { // the cell is not at the end of the lists, so swap it
																// with the ending entry (this is constant rather than
																// linear O)
								allCells.set(i, allCells.remove(allCells.size() - 1));
								allNodes.set(i, allNodes.remove(allNodes.size() - 1));
								i--; // back up i by one to process the cell pulled from the end
							} else { // the cell is at the end, so just remove it without replacing it
								allCells.remove(allCells.size() - 1);
								allNodes.remove(allNodes.size() - 1);
							}
						}
					}
				}

			});

			// run the simulation by asking all the living cells to take their turns
			// cells that die as a result of the action of a cell earlier in the update
			// cycle are not updated
			for (int i = 0; i < allCells.size(); i++) {
				if (allCells.get(i).isAlive()) // verify the cell is living before updating it (dead cells don't talk)
					allCells.get(i).update();
			}

			// hard delay between simulation ticks (TODO configurable)
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} while (!done);
	}

	/**
	 * Helper method for cells that want to know what objects they can see in the
	 * petri dish (those within a certain range of them) Said objects must be within
	 * the max distance, alive, and not the querying cell itself
	 * 
	 * @param x           the x coordinate of the querying cell
	 * @param y           the y coordinate of the querying cell
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
	private double distanceBetween(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	/**
	 * Causes the main loop to exit shortly after call
	 */
	public void stop() {
		done = true;
	}

}
