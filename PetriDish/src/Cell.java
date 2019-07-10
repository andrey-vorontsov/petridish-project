import java.util.Random;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Represents a single-celled organism inhabiting the petri dish environment.
 * Capable of moving, eating, reproducing, and dying. Additionally, holds
 * information associated with the cell's appearance and various stats. This
 * class is used as a data structure by the petri dish simulation thread.
 * 
 * @author Andrey Vorontsov
 */
public abstract class Cell {

	// utility
	protected Random rng = new Random();
	protected PetriDish petri; // need a reference to the petri dish the cell lives in
	private static int nextCellID = 1;
	public final int cellID;

	// physical information
	protected double x;
	protected double y;
	protected double xVelocity;
	protected double yVelocity;

	// information related to the cell's status independent of its genetics
	// for all cells
	private boolean isAlive = true;
	// varies based on cell type
	protected int health;
	protected int energy;
	protected int size;

	// TODO eventually this information will be held in the genome
	protected Color color;
	protected double friction; // multiplicative coefficient for the velocity at each tick (smaller = more
								// friction)
	protected String species;

	/**
	 * Constructor for basic physical properties
	 * 
	 * @param x         the x location to put the cell at
	 * @param y         the y location to put the cell at
	 * @param xVelocity the initial x velocity of the cell
	 * @param yVelocity the initial y velocity of the cell
	 */
	public Cell(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		this.petri = petri;
		this.x = x;
		this.y = y;
		this.xVelocity = xVelocity;
		this.yVelocity = yVelocity;
		this.size = size;

		cellID = nextCellID; // assign a unique ID to the cell object
		nextCellID++;
	}

	/**
	 * Core method, representing the basic actions the cell can take each tick of
	 * the simulation
	 */
	public void update() {
		// TODO
		move(); // the cell has a chance to affect its own movement
		eat(); // the cell has a chance to consume cells it is touching
		updatePhysics(); // the cell moves according to physics
		if (energy <= 0) {
			kill("starvation");
		}
	}

	/**
	 * The cell may expend energy to accelerate itself.
	 */
	abstract void move();

	/**
	 * The cell may consume certain other cells.
	 */
	abstract void eat();

	/**
	 * Cells die when they are killed.
	 */
	public void kill(String reason) {
		if (isAlive = false) {
			System.out.println("WARNING: " + this + " has died multiple times.");
		}

		isAlive = false;

		switch (reason) {
		case "starvation":
			System.out.println(this + " starved.");
			break;
		case "eaten":
			System.out.println(this + " was eaten.");
			break;
		default:
			System.out.println(this + "died.");
			break;
		}

	}

	/**
	 * Adjusts the cell's physical location based on its velocity and the friction
	 * factor it experiences
	 */
	private void updatePhysics() {

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
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * @return a Circle object that represents this cell
	 */
	public Circle getGraphic() {
		Circle graphic = new Circle(x, y, size);
		graphic.setFill(color);
		return graphic;
	}

	/**
	 * String representation of a cell is of the form "Species #cellID" For example,
	 * "Herbivore #5"
	 * 
	 * @see java.lang.Object#toString()
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
