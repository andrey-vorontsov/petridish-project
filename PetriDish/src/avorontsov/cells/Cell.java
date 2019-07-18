package avorontsov.cells;
import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * Represents a single-celled organism inhabiting the petri dish environment.
 * Capable of moving, eating, reproducing, etc. Additionally, holds
 * information associated with the cell's appearance and various stats. This
 * class is used as a data structure by the petri dish simulation thread, as
 * well as holding many helper methods for cell behavior. Classes extending Cell
 * can configure the basic functionality or override methods to achieve desired behavior.
 * 
 * In general, protected fields of this class should be set in the constructor of any extending class, with
 * the following exceptions: x, y, xVelocity, yVelocity (exposed for ease of extending methods)
 * So children should set:
 * SUPPRESS_EVENT_PRINTING, health, energy, size, color, friction, visionRange, species
 * Additionally, children should
 * 1. Customize and add a CellBehaviorController
 * 2. Override getGraphic()
 * 
 * @author Andrey Vorontsov
 */
public abstract class Cell {

	// utility
	private Random rng; // use the same Random object as the rest of the simulation
	private PetriDish petri; // a reference to the petri dish the cell lives in
	private static long nextCellID = 1; // each cell is assigned a unique ID
	public final long cellID;
	protected boolean SUPPRESS_EVENT_PRINTING = false; // children of this class may choose to set this to true to
														// prevent status messages from that species from printing

	// physical information
	protected double x;
	protected double y;
	protected double xVelocity;
	protected double yVelocity;

	// information related to the cell's status independent of its genetics
	// for all cells
	private boolean isAlive;
	private int age;
	
	// varies based on cell type, protected fields
	protected int health;
	protected int energy;
	protected int size;
	
	// for cell behaviors
	private double targetX;
	private double targetY;
	private CellMovementVector targetingVector;
	private CellBehaviorController behaviors; // defines the set of movement behaviors this cell has
	private String currBehavior;

	// 'genetic' information (to be replaced with a more permanent data structure)
	protected Color color;
	protected double friction; // multiplicative coefficient for the velocity at each tick (smaller = more
								// friction)
	protected double visionRange; // base distance the cell can see (radius of a circle around its center)
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
	public Cell(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size) {
		this.petri = petri;
		this.rng = rng;
		this.x = x;
		this.y = y;
		this.xVelocity = xVelocity;
		this.yVelocity = yVelocity;
		this.size = size;

		// defaults
		isAlive = true;
		age = 0;
		targetingVector = new CellMovementVector(0, 0);
		currBehavior = "sleep";

		cellID = nextCellID; // assign a unique ID to the cell object
		nextCellID++;
	}

	/**
	 * Core method, representing the basic actions the cell can take each tick of
	 * the simulation.
	 * 
	 * @param visibleCells a list of cells visible to this cell, based on the cell's vision range and size
	 * @return an offspring produced by this cell during this update, unless one was
	 *         not produced, in which case return null
	 */
	public Cell update(ArrayList<Cell> visibleCells) {
		age++;

		Cell newCell = act(visibleCells); // the cell invokes its CellBehaviorController

		if (age > 1) { // certain unexpected/risky things occur if these things are allowed to happen on the same update that a cell is born
			grow(); // the cell has a chance to grow itself
			ArrayList<Cell> touchedCells = new ArrayList<Cell>();
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
	 * velocity and applies energy cost accordingly. TODO
	 * 
	 * @param visibleCells a list of cells this cell can see based on its vision range and size
	 */
	public Cell act(ArrayList<Cell> visibleCells) {
		if (behaviors == null) {
			throw new NullPointerException("Cell " + this + " does not have a movement controller!");
		}
		// calculate the next move order (this process also updates targetX and targetY)
		ActionOrder nextOrder = behaviors.getNextActionOrder(this, visibleCells);
		// update the cell's current behavior
		currBehavior = nextOrder.getSourceBehavior().getBehaviorType();
		
		// for movement behaviors, we adjust velocity
		if (nextOrder.getSourceBehavior().getBehaviorCategory().equals("MOVE")) {
			// get and apply the corresponding vector
			targetingVector = nextOrder.getVector();
			xVelocity += targetingVector.getUnitVector().getXComponent() * nextOrder.getVectorScalar();
			yVelocity += targetingVector.getUnitVector().getYComponent() * nextOrder.getVectorScalar();
			
			// and apply energy cost according to formula
			// TODO replace right away
			if (species.equals("Grazer") || species.equals("Predator"))
				if (getAge() % 4 == 0)
					spendEnergy(1);
		}
		
		// TODO eating code
		if (nextOrder.getSourceBehavior().getBehaviorCategory().equals("EAT")) {
			if (nextOrder.getSourceBehavior().getBehaviorType().equals("eat")) {
				energy += nextOrder.getTarget().getEnergy();
				nextOrder.getTarget().kill("eaten");
				if (!SUPPRESS_EVENT_PRINTING)
					System.out.println(this + " consumed " + nextOrder.getTarget() + ", receiving " + nextOrder.getTarget().getEnergy() + " energy.");
			}
		
		}
		
		// TODO breeding code
		if (nextOrder.getSourceBehavior().getBehaviorCategory().equals("REPRODUCE")) {
			if (nextOrder.getSourceBehavior().getBehaviorType().equals("clone")) {
				Cell child = null;
				if (size >= 8) { // copy pasted from predator for now; plant custom reproduction no work
					size = size / 2;
					energy = (energy - 20) / 2;
					// this is a hack. eventually this gets weeded out as well
					if (this instanceof Predator)
						child = new Predator(petri, rng, x, y, 0, 0, size, energy);
					if (this instanceof Grazer)
						child = new Grazer(petri, rng, x, y, 0, 0, size, energy);
					if (this instanceof Plant)
						child = new Plant(petri, rng, x, y, 0, 0, size, energy);
					if (SUPPRESS_EVENT_PRINTING)
						System.out.println(this + " spawned " + child + ".");
				}
				return child;
			}
		
		}
		
		if (this instanceof Plant) {
			Plant me = (Plant)this;
			me.updateGraphicSideLength(); // TODO this needs to be done in act() now instead, an override
		}
		
		return null; // did not breed this round
		
		// TODO currently, energy costs for movement are calculated trivially by the cell's move method; ideally, moveOrder should calculate energy costs
		// TODO goal is that this method will no longer need to be overriden (instead cells will apply controllers to themselves)
	}

	/**
	 * The cell may expend energy to increase its size.
	 */
	abstract void grow(); // TODO consider growth as volume rather than radius

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
				System.out.println(this + " died for the reason: " + reason);
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
		} else if (x > PetriDishApp.PETRI_DISH_WIDTH - 15) {
			xVelocity = -1;
			x = PetriDishApp.PETRI_DISH_WIDTH - 15;
		}
		if (y < 15) {
			yVelocity = 1;
			y = 15;
		} else if (y > PetriDishApp.PETRI_DISH_HEIGHT - 15) {
			yVelocity = -1;
			y = PetriDishApp.PETRI_DISH_HEIGHT - 15;
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
	 * The cell is debited energy for taking an action.
	 * @param energySpent the energy the cell has expended
	 */
	public void spendEnergy(int energySpent) {
		energy -= energySpent;
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
		return visionRange + size*6; // TODO fix
	}

	/**
	 * 
	 * @return true only if the cell can see
	 */
	public boolean canSee() {
		return !(visionRange == 0);
	}

	/**
	 * @return the targetX
	 */
	public double getTargetX() {
		return targetX;
	}

	/**
	 * @param targetX the targetX to set
	 */
	public void setTargetX(double targetX) {
		this.targetX = targetX;
	}

	/**
	 * @return the targetY
	 */
	public double getTargetY() {
		return targetY;
	}

	/**
	 * @param targetY the targetY to set
	 */
	public void setTargetY(double targetY) {
		this.targetY = targetY;
	}

	/**
	 * @return the Cell's targetingVector
	 */
	public CellMovementVector getTargetingVector() {
		return targetingVector;
	}

	/**
	 * @param behaviors a CellMovementController object encapsulating the movement behaviors (evasion, pursuit, hunting, etc.) that a cell can take with respect to types of targets, and their priority levels
	 */
	public void setBehaviors(CellBehaviorController behaviors) {
		this.behaviors = behaviors;
	}

	/**
	 * @return the current behavior of this cell
	 */
	public String getCurrBehavior() {
		return currBehavior;
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
	 * A single Random object is instantiated by the petri dish simulation and all random numbers are drawn from it. Given no changes to the code, a particular seed should always produce the same simulation outcome
	 * 
	 * @return the Random object to use
	 */
	public Random getRNG() {
		return rng;
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
