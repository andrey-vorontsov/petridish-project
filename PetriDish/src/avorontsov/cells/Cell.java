package avorontsov.cells;

import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * Represents a single-celled organism inhabiting the petri dish environment.
 * Capable of moving, eating, reproducing, etc. Additionally, holds information
 * associated with the cell's appearance and various stats. This class is used
 * as a data structure by the petri dish simulation thread, as well as holding
 * many helper methods for cell behavior. Classes extending Cell can configure
 * elements of the basic functionality or override methods to achieve desired
 * behavior.
 * 
 * About cell behaviors: Generally, cell behaviors associated with movement,
 * eating/getting energy, and reproduction are encapsulated by the
 * CellBehaviorController very abstractly and are configurable through it.
 * Any other custom behaviors (particularly related to changing the cell's
 * mass due to growth, or to pushing other cells away) can be encapsulated
 * in customizedCellBehaviors().
 * Unfortunately, this system is a tad incomplete/inconsistent with its level of
 * abstraction.
 * 
 * In general, all protected fields of this class should be set in the
 * constructor of any extending class, with the following exceptions: x, y,
 * xVelocity, yVelocity, rng, petri (exposed for ease of writing reproduction
 * methods) and radius (exposed for convenience of changing graphics appearance)
 * 
 * So children should set:
 * SUPPRESS_EVENT_PRINTING, health, energy, mass, color, friction,
 * baseVisionRange, species, maxAge
 * 
 * Additionally, children should:
 * 1. In the constructor, customize and add a CellBehaviorController
 * 2. Optionally, override customizedCellBehaviors() - this is where any custom
 * behavior that is not encapsulated by a CellBehaviorController should be
 * implemented (generally, it should call super.customizedCellBehaviors())
 * 3. Optionally, override behaviorClone() - used to implement the "clone" behavior
 * 4. Optionally, override squish() - default behavior is to push away all
 * cells of the same species to avoid overlapping them
 * 5. Optionally, override getGraphic() - default behavior is to update the radius value, then generate a
 * circle of appropriate radius and color
 * 6. Optionally, override getScaledVisionRange() to apply a customized vision
 * range calculation
 * 
 * Generally, children should AVOID: 1. Overriding any other methods of the Cell
 * class (especially update(), act(), kill(), and updatePhysics())
 * 
 * In any case, when overriding methods, a call to the superclass version of that
 * method is often warranted.
 * 
 * @author Andrey Vorontsov
 */
public abstract class Cell {

	// utility
	protected Random rng; // use the same Random object as the rest of the simulation
	protected PetriDish petri; // a reference to the petri dish the cell lives in
	private static long nextCellID = 1; // each cell is assigned a unique ID
	public final long cellID;
	protected boolean SUPPRESS_EVENT_PRINTING = true; // children of this class may choose to set this to true to
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
	protected int health = 0;
	protected double energy = 0;
	protected int radius; // updated by the getGraphic method at the end of each simulation update
	protected double mass; // used by the simulation for size comparisons - mass determines radius

	// for cell behaviors
	private double targetX;
	private double targetY;
	private CellMovementVector targetingVector;
	private CellBehaviorController behaviors; // defines the set of movement behaviors this cell has
	private String currBehavior;
	private ArrayList<BehaviorCooldown> behaviorCooldowns = new ArrayList<BehaviorCooldown>();

	// 'genetic' information (to be replaced with a more permanent data structure)
	protected Color color;
	protected int maxAge;
	protected double friction; // multiplicative coefficient for the velocity at each tick (smaller = more)
	protected double baseVisionRange; // base distance the cell can see (radius of a circle around its center)
										// hypothetically when the mass is zero (in reality, always more)
	protected String species;

	/**
	 * Constructor for basic physical properties. Cells are also assigned a unique
	 * ID, are alive by default, and start at an age of 0.
	 * 
	 * @param petri     the petri dish the cell will inhabit
	 * @param rng       the Random object used by this cell's randomized behavior
	 * @param x         the x location to put the cell at
	 * @param y         the y location to put the cell at
	 * @param xVelocity the initial x velocity of the cell
	 * @param yVelocity the initial y velocity of the cell
	 * @param mass      the initial mass of the cell
	 */
	public Cell(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, double mass) {
		this.petri = petri;
		this.rng = rng;
		this.x = x;
		this.y = y;
		this.xVelocity = xVelocity;
		this.yVelocity = yVelocity;
		this.mass = mass;

		// defaults
		isAlive = true;
		age = 0;
		targetingVector = new CellMovementVector(0, 0);
		currBehavior = "sleep";
		maxAge = 3000;

		cellID = nextCellID; // assign a unique ID to the cell object
		nextCellID++;
	}

	// core functionality methods

	/**
	 * Core method, representing the basic actions the cell can take each tick of
	 * the simulation.
	 * 
	 * @param visibleCells a list of cells visible to this cell, based on the cell's
	 *                     vision range
	 * @return any offspring produced by this cell during this update
	 */
	public ArrayList<Cell> update(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells) {
		age++; // cells have an age of 0 after being created; but new cells are updated on the
				// same cycle they are created, so they end the cycle at age 1.
		
		ArrayList<Cell> newCells = new ArrayList<Cell>();

		newCells.addAll(act(visibleCells)); // the cell invokes its CellBehaviorController to enact policies regarding
											// movement, eating, and reproduction

		newCells.addAll(customizedCellBehaviors(visibleCells, touchedCells)); // any behaviors not defined in the CellBehaviorController
																// are enforced here by custom implementation

		newCells.addAll(dieOfOldAge());
		
		updatePhysics(); // the cell moves according to physics
		
		// update any cooldowns that behaviors might have
		for (int i=0; i<behaviorCooldowns.size(); i++) {
			if (behaviorCooldowns.get(i).update()) { // for each behavior on cooldown, update it, and if it ran out - get rid of it
				behaviorCooldowns.remove(i);
				i--;
			}
		}

		return newCells;
	}

	/**
	 * The cell invokes its CellBehaviorController to choose an action, and then
	 * enforces it.
	 * 
	 * @param visibleCells a list of cells this cell can see based on its vision
	 *                     range
	 * @return a list of Cell offsprings, if any were produced by reproduction
	 */
	public ArrayList<Cell> act(ArrayList<Cell> visibleCells) {
		if (behaviors == null) {
			throw new NullPointerException("Cell " + this + " does not have a movement controller!");
		}

		ArrayList<Cell> children = new ArrayList<Cell>(); // prepare to reproduce

		// engage the behavior controller's encapsulated logic to choose an appropriate
		// behavior to enforce this update
		ActionOrder nextOrder = behaviors.getNextActionOrder(this, visibleCells);

		// update the cell's current behavior String to keep track of what it chose to
		// do
		currBehavior = nextOrder.getSourceBehavior().getBehaviorType();

		// for movement behaviors, we adjust the cell's target information and its
		// velocity
		if (nextOrder.getSourceBehavior().getBehaviorCategory().equals("MOVE")) {

			// get the vector leading to our next target
			targetingVector = nextOrder.getVector();

			// update our target coordinates
			targetX = targetingVector.getXComponent() + x;
			targetY = targetingVector.getYComponent() + y;

			// adjust our velocity by the appropriate amount
			xVelocity += targetingVector.getUnitVector().getXComponent() * nextOrder.getVectorScalar();
			yVelocity += targetingVector.getUnitVector().getYComponent() * nextOrder.getVectorScalar();

		}

		// for eating/energy gain behaviors, we currently enforce the following:
		// "eat" - kill the target, take all of its energy and burn its mass for energy
		// "nibble" - for Plants - chew on the target, leeching some energy
		if (nextOrder.getSourceBehavior().getBehaviorCategory().equals("EAT")) {
			if (nextOrder.getSourceBehavior().getBehaviorType().equals("eat")) {
				energy += nextOrder.getTarget().getEnergy();
				energy += nextOrder.getTarget().getMass() / 12; // TODO efficiency of mass conversion gene
				nextOrder.getTarget().kill("eaten");
				if (!SUPPRESS_EVENT_PRINTING)
					System.out.println(this + " consumed " + nextOrder.getTarget() + ", receiving "
							+ (nextOrder.getTarget().getEnergy() + nextOrder.getTarget().getMass() / 12) + " energy.");
			}
			
			if (nextOrder.getSourceBehavior().getBehaviorType().equals("nibble")) {
				energy += 8;
				nextOrder.getTarget().setEnergy(nextOrder.getTarget().getEnergy() - 8);
				if (!SUPPRESS_EVENT_PRINTING)
					System.out.println(this + " nibbled on " + nextOrder.getTarget() + ", receiving 8 energy.");
			}
		}

		// for reproduction behaviors, we currently enforce the following:
		// "clone" - produce a new instance of this cell
		if (nextOrder.getSourceBehavior().getBehaviorCategory().equals("REPRODUCE")) {
			if (nextOrder.getSourceBehavior().getBehaviorType().equals("clone")) {
					children = behaviorClone();
					if (!SUPPRESS_EVENT_PRINTING)
						System.out.println(this + " spawned " + children + ".");
			}

		}
		
		// set the action order's source behavior's cooldown, if applicable
		if (nextOrder.getSourceBehavior().getCoolDown() != 0) {
			behaviorCooldowns.add(new BehaviorCooldown(nextOrder.getSourceBehavior()));
		}

		// apply the energy cost of the action order
		energy -= nextOrder.getSourceBehavior().getEnergyCost();

		return children; // null, unless initialized by reproduction

	}

	/**
	 * This method is called on every update. Cell behaviors that cannot be
	 * abstractly described by its configuration of its CellBehaviorController can
	 * be implemented here. Ideally, overriding methods should have their contents
	 * wrapped into clearly named helper methods and be as simple as possible. The
	 * Cell update() method provides this method with the visibleCells and
	 * touchedCells lists.
	 * 
	 * Default behavior is to call squish(), which may be overriden separately, to
	 * prevent this cell from overlapping any cells of its own species; and also to
	 * check whether the cell died of starvation (energy <= 0). These behaviors may
	 * be disabled in custom cells.
	 * 
	 * @param visibleCells a list of Cells this cell can see
	 * @param touchedCells a list of Cells this cell is touching
	 * 
	 * @return any cells that are produced as a result of customized reproduction code etc, by default cells drop agars when they die for reasons other than being eaten
	 */
	public ArrayList<Cell> customizedCellBehaviors(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells) {
		ArrayList<Cell> droppedCells = new ArrayList<Cell>();
		
		if (age > 3 && mass > 35) { // anything too young or too small isn't allowed to push other things away
			squish(touchedCells);
		}
		if (energy <= 0) { // the cell checks itself for death by starvation
			kill("starvation");
			while (mass > 0) {
				mass -= 40;
				Agar droppedEnergy = new Agar(petri, rng, x + (rng.nextDouble() * 4 - 2), y + (rng.nextDouble() * 4 - 2), 0, 0, 20);
				droppedEnergy.setEnergy(10);
				droppedCells.add(droppedEnergy); // drop at least one agar
				
			}
		}
		return droppedCells;
	}
	
	/**
	 * A method to encapsulate the functionality of the clone behavior, to allow
	 * for proper customization of the reproduction functionality. In most cases,
	 * the cell should apply a reasonable energy cost to itself, before dividing
	 * in two (mass = mass/2, create a new child)
	 * 
	 * Unfortunately, another deviation from the original Cell Behavior plan;
	 * though this method ends up being activated by a corresponding "clone"
	 * behavior, we really didn't accomplish anything by making the steps to its
	 * activation that much more convoluted.
	 * 
	 * @return any child cells, if produced
	 */
	protected ArrayList<Cell> behaviorClone() {
		return null;
	}
	

	/**
	 * Private. No need to override or even mention, cells can configure or turn off their max age easily.
	 * 
	 * @return a list of cells that are dropped at death
	 */
	private ArrayList<Cell> dieOfOldAge() {
		ArrayList<Cell> droppedCells = new ArrayList<Cell>();

		if (maxAge != -1 && age > maxAge && rng.nextInt(100) < 6) {
			kill("old age");
			while (mass > 0) {
				mass -= 40;
				Agar droppedEnergy = new Agar(petri, rng, x + (rng.nextDouble() * 4 - 2), y  + (rng.nextDouble() * 4 - 2), 0, 0, 20);
				droppedEnergy.setEnergy(10);
				droppedCells.add(droppedEnergy); // drop at least one agar
				
			}
		}
		
		return droppedCells;
	}

	/**
	 * Cells die when they are killed.
	 * 
	 * @param reason the String reason for the death. Examples of reasons include
	 *               "starvation" and "eaten". Used to generate a death message.
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
			case "old age":
				System.out.println(this + " died of old age at age " + age + ".");
				break;
			default:
				System.out.println(this + " died for the reason \"" + reason + "\" at age " + age + ".");
				break;
			}
		}

	}

	/**
	 * Adjusts the cell's physical location based on its velocity and the friction
	 * factor it experiences, and keeps cells in-bounds
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
		} else if (x > petri.getSimulationWidth() - 15) {
			xVelocity = -1;
			x = petri.getSimulationWidth() - 15;
		}
		if (y < 15) {
			yVelocity = 1;
			y = 15;
		} else if (y > petri.getSimulationHeight() - 15) {
			yVelocity = -1;
			y = petri.getSimulationHeight() - 15;
		}
	}

	/**
	 * This method may be overriden for custom "squishing" behavior. By default, all
	 * cells of the same species are pushed away to avoid overlap.
	 * 
	 * @param touchedCells the list of cells to squish away
	 */
	public void squish(ArrayList<Cell> touchedCells) {
		for (Cell c : touchedCells) {
			if (c.getSpecies().equals(species)) {
				// get the unit vector along which to push, then scale it so that the magnitude
				// is equal to the sum of the radii of the cells
				CellMovementVector pushUnit = getVectorToTarget(c.getX(), c.getY()).getUnitVector();
				double pushMagnitude = c.getRadius() + radius;
				CellMovementVector push = new CellMovementVector(pushUnit.getXComponent() * pushMagnitude,
						pushUnit.getYComponent() * pushMagnitude);
				// use the scaled vector to place the other cell at the appropriate distance,
				// plus a tiny margin
				c.setX(x + 1.05 * push.getXComponent());
				c.setY(y + 1.05 * push.getYComponent());
			}
		}
	}

	/**
	 * This method should be overriden by any extending class that wants to
	 * customize its graphic. By default, the graphic is a circle with the cell's
	 * radius and color.
	 * 
	 * This method is responsible for updating the radius from the mass using
	 * A = pi*r^2, where mass is taken to be area (they are proportionate).
	 * Any overriding methods should include the statement
	 * radius = (int) Math.round(Math.sqrt(mass/Math.PI));
	 * Which should be safe as long as cell radii are reasonably small.
	 * 
	 * @return any kind of Graphics object that can represent the cell (typically a
	 *         JavaFX Circle or Square)
	 */
	public Node getGraphic() {
		radius = (int) Math.round(Math.sqrt(mass/Math.PI));
		
		Circle graphic = new Circle(x, y, radius);
		graphic.setFill(color);
		return graphic;
	}

	/**
	 * Helper method for the cell to refresh its targeting vector to another,
	 * possibly moving, point.
	 * 
	 * @param targetX the location to target
	 * @param targetY the location to targer
	 * @return a vector representing the movement vector from this cell to the
	 *         target
	 */
	public CellMovementVector getVectorToTarget(double targetX, double targetY) {
		return new CellMovementVector(targetX - x, targetY - y);
	}

	/**
	 * Vision range is calculated by the base value + mass * 6 by default If the
	 * cell cannot see, returns 0.
	 * 
	 * @return how far the cell can actually see
	 */
	public double getScaledVisionRange() {
		if (!canSee())
			return 0;
		return baseVisionRange + mass / 3.5;
	}

	/**
	 * @return true only if the cell can see (its baseVisionRange > 0)
	 */
	public boolean canSee() {
		return baseVisionRange > 0;
	}

	// getters and setters
	
	/**
	 * @return a reference to the petri dish object that this cell is located in 
	 */
	public PetriDish getPetri() {
		return petri;
	}

	/**
	 * @return the Random object used by the simulation
	 */
	public Random getRNG() {
		return rng;
	}

	/**
	 * @return the x position of the cell
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y position of the cell
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the xVelocity of the cell
	 */
	public double getXVelocity() {
		return xVelocity;
	}

	/**
	 * @return the yVelocity of the cell
	 */
	public double getYVelocity() {
		return yVelocity;
	}

	/**
	 * @return true only if the cell is alive
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
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
	public double getEnergy() {
		return energy;
	}

	/**
	 * @return the radius
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * @return the mass
	 */
	public double getMass() {
		return mass;
	}

	/**
	 * @return the x coordinate of the location the cell is currently moving to
	 */
	public double getTargetX() {
		return targetX;
	}

	/**
	 * @return the y coordinate of the location the cell is currently moving to
	 */
	public double getTargetY() {
		return targetY;
	}

	/**
	 * @return the targetingVector along which the cell is moving
	 */
	public CellMovementVector getTargetingVector() {
		return targetingVector;
	}

	/**
	 * @return the CellBehaviorController currently applied to this cell
	 */
	public CellBehaviorController getBehaviors() {
		return behaviors;
	}

	/**
	 * @return the behaviorCooldowns
	 */
	public ArrayList<BehaviorCooldown> getBehaviorCooldowns() {
		return behaviorCooldowns;
	}

	/**
	 * @return the behavior type String which represents the type of action the Cell
	 *         last took
	 */
	public String getCurrBehavior() {
		return currBehavior;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return the friction
	 */
	public double getFriction() {
		return friction;
	}

	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * @param x the x position to put this cell at
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y the y position to put this cell at
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * @param energy the energy to put this cell at
	 */
	public void setEnergy(double energy) {
		this.energy = energy;
	}

	/**
	 * @param mass the mass to set
	 */
	public void setMass(double mass) {
		this.mass = mass;
	}

	/**
	 * @param behaviors the CellBehaviorController that this cell will use to govern
	 *                  its behavior
	 */
	public void setBehaviorController(CellBehaviorController behaviors) {
		this.behaviors = behaviors;
	}

	// utility methods

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

	/**
	 * Cells are equal iff they have the same cellID field.
	 * 
	 * @param other another Object to which to compare.
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
