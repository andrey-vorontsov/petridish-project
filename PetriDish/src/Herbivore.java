import java.util.ArrayList;
import javafx.scene.paint.Color;

/**
 * A simple creature, made for testing out various functions such as cell eating, reproduction, growth and movement.
 * Herbivores search for plants and agars, exhibiting a grazing behavior as well as a predator evasion behavior
 * Currently unfinished.
 * 
 * @author Andrey Vorontsov
 */
public class Herbivore extends Cell {

	/**
	 * Create a herbivore. Herbivores start out with 75 energy (almost enough to start growing right away), and they are green.
	 * 
	 * @see Cell#Cell(double, double, double, double, int)
	 */
	public Herbivore(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		this(petri, x, y, xVelocity, yVelocity, size, 75);
	}
	
	/**
	 * Create a herbivore with a specified amount of starting energy (used for reproducing herbivores).
	 * 
	 * @see Cell#Cell(double, double, double, double, int)
	 */
	public Herbivore(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size, int energy) {
		super(petri, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.LAWNGREEN;
		friction = 0.8;
		species = "Herbivore";
	}

	/**
	 * Herbivore movement aims to eventually emulate food searching, grazing, and predator evasion behaviors.
	 * 
	 * @see Cell#move()
	 */
	@Override
	public void move() {
		
		// gather information about any visible cells
		ArrayList<Cell> visibleCells = petri.getCellsInRange(this, size*15); // TODO vision range configuration
		
		// choose a prey target, if one is available
		Cell target = null;
		for (Cell c : visibleCells) { // for now, the closest agar is chosen
			if (c.getSpecies().equals("Agar") && (target == null || PetriDish.distanceBetween(target.getX(), target.getY(), x, y) > PetriDish.distanceBetween(c.getX(),c.getY(), x, y))) {
				target = c;
			}
		}
		
		// update the targeting vector based on gathered information
		if (target != null) { // if a prey target was found, go there
			targetingVector = getVectorToTarget(target);
		} else if (targetingVector == null || targetingVector.magnitude < 3) { // if no prey target exists, throw out a random vector; if we're approaching the end of the previous random search vector, throw out a new one 
			targetingVector = new CellMovementVector((rng.nextDouble() - 0.5)*40, (rng.nextDouble() - 0.5)*40);
			System.out.println(this + " can't see any food!"); // TODO debug event
		} else { // a bit of random variation for the random search behavior; while moving to a random search vector, vary it a little bit
			targetingVector = new CellMovementVector(targetingVector.getxComponent() + rng.nextDouble() - 0.5, targetingVector.getyComponent() + rng.nextDouble() - 0.5);
		}
		
		// standard code block which should be present in any implementation of move(); follow the vector
		xVelocity += targetingVector.getUnitVector().getxComponent();
		yVelocity += targetingVector.getUnitVector().getyComponent();
		
		// movement costs energy
		energy--;
	}

	/**
	 * Herbivores get energy from harvesting plant growth or agar.
	 * 
	 * @see Cell#eat()
	 */
	@Override
	public void eat() {
		// gather info about any cells we are in contact with
		ArrayList<Cell> eatableCells = petri.getCellsInRange(this, size);
		for (Cell c : eatableCells) {
			if (c.getSpecies().equals("Agar")) { // for now, any agars contacted will be eaten
				energy += c.getEnergy();
				c.kill("eaten");
				System.out.println(this + " consumed " + c + ", receiving " + c.getEnergy() + " energy."); // TODO debug event
			}
		}
	}

	/**
	 * Herbivores grow when well-fed and shrink when starving.
	 * 
	 * @see Cell#grow()
	 */
	@Override
	public void grow() {
		if (energy > 75 && size < 10) { // right now: herbivore spends 5 energy to grow one size
			size++;
			energy -= 5;
			System.out.println(this + " grew one size.");
		} else if (energy < 25 && size > 5) {
			size--;
			energy += 3;
			System.out.println(this + " is starving!");
		}
	}

	/**
	 * Herbivores reproduce after reaching their maximum size and a threshold energy.
	 * 
	 * @see Cell#reproduce()
	 */
	@Override
	public Cell reproduce() {
		Herbivore child = null;
		if (energy > 100 && size >= 10) { // right now: herbivore spends 20 energy to split in half and spawn an offspring, they also split their energy evenly
			size = size/2;
			child = new Herbivore(petri, x, y, 0, 0, 5, (energy-20)/2 );
			energy = (energy-20)/2;
			System.out.println(this + " spawned " + child + ".");
		}
		return child;
	}

}
