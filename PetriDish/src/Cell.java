import java.util.ArrayList;
import java.util.Random;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * Represents a single-celled organism inhabiting the petri dish environment.
 * Capable of moving, eating, reproducing, and dying. Additionally, holds
 * information associated with the cell's appearance and various stats. This
 * class is used as a data structure by the petri dish simulation thread, as
 * well as holding many helper methods to support implementations of cell
 * behavior by children of this class.
 * 
 * @author Andrey Vorontsov
 */
public abstract class Cell {

	// utility
	protected Random rng = new Random();
	protected PetriDish petri; // need a reference to the petri dish the cell lives in
	private static int nextCellID = 1; // each cell is assigned a unique ID
	public final int cellID;
	protected boolean SUPPRESS_EVENT_PRINTING = false; // children of this class may choose to set this to true to
														// prevent status messages from that species from printing to
														// console

	// physical information
	protected double x;
	protected double y;
	protected double xVelocity;
	protected double yVelocity;

	// information related to the cell's status independent of its genetics
	// for all cells
	private boolean isAlive;
	protected int age;
	// varies based on cell type
	protected int health;
	protected int energy;
	protected int size;
	// for cell behaviors
	protected double targetX;
	protected double targetY;
	protected CellMovementVector targetingVector;
	private CellMovementBehavior behaviors; // defines the set of movement behaviors this cell has

	// 'genetic' information (to be replaced with a more permanent data structure)
	protected Color color;
	protected double friction; // multiplicative coefficient for the velocity at each tick (smaller = more
								// friction)
	protected double visionRange; // distance the cell can see (radius of a circle around its center) (warning; does not directly match the output of getVisionRange())
	protected String species;

	/**
	 * Constructor for basic physical properties. Cells are also assigned a unique
	 * ID, are alive by default, and start at an age of 0.
	 * 
	 * @param petri     the petri dish the cell will inhabit
	 * @param x         the x location to put the cell at
	 * @param y         the y location to put the cell at
	 * @param xVelocity the initial x velocity of the cell
	 * @param yVelocity the initial y velocity of the cell
	 * @param size      the initial size of the cell
	 */
	public Cell(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		this.petri = petri;
		this.x = x;
		this.y = y;
		this.xVelocity = xVelocity;
		this.yVelocity = yVelocity;
		this.size = size;

		isAlive = true;
		age = 0;
		targetingVector = new CellMovementVector(0, 0);

		cellID = nextCellID; // assign a unique ID to the cell object
		nextCellID++;
	}

	/**
	 * Core method, representing the basic actions the cell can take each tick of
	 * the simulation.
	 * 
	 * @param visibleCells a list of cells visible to this cell, based on the cell's vision range
	 * @param touchedCells a list of cells contacting this cell (the distance between them is less than the sum of their radii)
	 * @param eatableCells a list of cells that this cell could eat (the centerpoints of which are within the radius of this cell)
	 * @return an offspring produced by this cell during this update, unless one was
	 *         not produced, in which case return null
	 */
	public Cell update(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells, ArrayList<Cell> eatableCells) {
		age++;

		move(visibleCells); // the cell has a chance to affect its own movement
		eat(eatableCells); // the cell has a chance to consume cells it is touching

		Cell newCell = null;
		if (age > 1) { // certain unexpected/risky things occur if these things are allowed to happen on the same update that a cell is born
			grow(); // the cell has a chance to grow itself
			newCell = reproduce(visibleCells); // the cell has a chance to spawn offspring (visibleCells argument included to possibly support future sexual reproduction)
			squish(touchedCells); // stop cells from overlapping others of the same species
		}

		updatePhysics(); // the cell moves according to physics
		
		if (energy <= 0) { // the cell checks itself for death by starvation
			kill("starvation");
		}

		return newCell;
	}

	/**
	 * The cell may expend energy to accelerate itself. This should be done with a
	 * module that generates a CellMovementVector, then a module that adjusts
	 * velocity and applies energy cost accordingly.
	 * 
	 * @param visibleCells a list of cells this cell can see based on its vision range and size
	 */
	abstract void move(ArrayList<Cell> visibleCells);

	/**
	 * The cell may consume certain other cells, or gain energy by other means. The general rule for eating cells is that the target cell's centerpoint must fall within this cell's circle
	 * 
	 * @param visibleCells a list of cells this cell can see based on its vision range and size
	 */
	abstract void eat(ArrayList<Cell> visibleCells);

	/**
	 * The cell may expend energy to increase its size.
	 */
	abstract void grow(); // TODO consider growth as volume rather than radius

	/**
	 * The cell may expend energy to spawn an offspring.
	 *
	 * @param visibleCells a list of cells this cell can see based on its vision range and size
	 * @return an offspring, if one is produced; otherwise return null
	 */
	abstract Cell reproduce(ArrayList<Cell> visibleCells);

	/**
	 * Cells die when they are killed.
	 * 
	 * @param reason the String reason for the death. Examples of reasons include
	 *               "starvation" and "eaten".
	 */
	public void kill(String reason) {

		isAlive = false;

		if (!SUPPRESS_EVENT_PRINTING) {
			switch (reason) {
			case "starvation":
				System.out.println(this + " starved at age " + age + ".");
				break;
			case "eaten":
				System.out.println(this + " was eaten at age " + age + ".");
				break;
			default:
				System.out.println(this + "died for the reason: " + reason);
				break;
			}
		}

	}

	/**
	 * Adjusts the cell's physical location based on its velocity and the friction
	 * factor it experiences
	 */
	public void updatePhysics() {

		// update velocity due to friction
		xVelocity = xVelocity * friction;
		yVelocity = yVelocity * friction;

		// update position
		x += xVelocity;
		y += yVelocity;

		// update velocity due to collision with the walls of the petri dish
		if (x < 15) {
			xVelocity = 1;
			x = 15;
		} else if (x > PetriDishApp.PETRI_DISH_SIZE - 15) {
			xVelocity = -1;
			x = PetriDishApp.PETRI_DISH_SIZE - 15;
		}
		if (y < 15) {
			yVelocity = 1;
			y = 15;
		} else if (y > PetriDishApp.PETRI_DISH_SIZE - 15) {
			yVelocity = -1;
			y = PetriDishApp.PETRI_DISH_SIZE - 15;
		}
	}

	/**
	 * Cells should avoid overlapping cells of the same species. This is accomplished by pushing other cells out of the way.
	 * 
	 * @param touchedCells the list of cells to squish away
	 */
	public void squish(ArrayList<Cell> touchedCells) {
		for (Cell c : touchedCells) {
			if (c.getSpecies().equals(species)) {
				// get the unit vector along which to push, then scale it so that the magnitude is equal to the sum of the radii of the cells
				CellMovementVector pushUnit = getVectorToTarget(c.getX(), c.getY()).getUnitVector();
				double pushMagnitude = c.getSize() + size;
				CellMovementVector push = new CellMovementVector(pushUnit.getXComponent() * pushMagnitude, pushUnit.getYComponent() * pushMagnitude);
				// use the scaled vector to place the other cell at the appropriate distance, plus a tiny margin
				c.setX(x + push.getXComponent() + 0.01);
				c.setY(y + push.getYComponent() + 0.01);
			}
		}
	}

	/**
	 * Helper method for the cell to refresh its targeting vector to another,
	 * possibly moving, point
	 * 
	 * @param targetX
	 * @param targetY
	 * @return a vector representing the movement vector from this cell to the
	 *         target
	 */
	public CellMovementVector getVectorToTarget(double targetX, double targetY) {
		return new CellMovementVector(targetX - x, targetY - y);
	}

	/**
	 * @return the x location of this cell's center
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y location of this cell's center
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Set the x position
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Set the y position
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return this cell's color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return true only while this cell is alive (existing in the petri dish)
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * @return the health
	 */
	public int getHealth() {
		return health;
	}

	/**
	 * @return the energy
	 */
	public int getEnergy() {
		return energy;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}
	
	/**
	 * Vision range is calculated by the base value + size * 6 by default 
	 * If the cell cannot see, returns 0.
	 * TODO review this
	 * 
	 * @return the Cell's vision range
	 */
	public double getVisionRange() {
		if (!canSee())
			return 0;
		return visionRange + size*6;
	}

	/**
	 * 
	 * @return true only if the cell can see
	 */
	public boolean canSee() {
		return !(visionRange == 0);
	}

	/**
	 * @return the Cell's targetingVector
	 */
	public CellMovementVector getTargetingVector() {
		return targetingVector;
	}

	/**
	 * @param behaviors a CellMovementBehavior object encapsulating the movement behaviors (evasion, pursuit, hunting, etc.) that a cell can take with respect to types of targets, and their priority levels
	 */
	public void setBehaviors(CellMovementBehavior behaviors) {
		this.behaviors = behaviors;
	}

	/**
	 * @return any kind of Graphics object that can represent the cell (typically a JavaFX Circle or Square)
	 */
	public Node getGraphic() {
		Circle graphic = new Circle(x, y, size);
		graphic.setFill(color);
		return graphic;
	}

	/**
	 * String representation of a cell is of the form "Species #cellID" For example,
	 * "Herbivore #5"
	 * 
	 * @see java.lang.Object#toString()
	 * @return the String form of this cell
	 */
	@Override
	public String toString() {
		return species + " #" + cellID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Cell)) // if not a cell, cannot compare
			return false;
		Cell otherCell = (Cell) other; // cast is safe based on above check

		return otherCell.cellID == cellID;
	}

}
