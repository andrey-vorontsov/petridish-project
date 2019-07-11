import java.util.ArrayList;
import javafx.scene.paint.Color;

/**
 * A simple creature, made for testing out various functions such as cell
 * eating, reproduction, growth and movement. Herbivores search for plants and
 * agars, exhibiting a grazing behavior as well as a predator evasion behavior
 * Currently unfinished.
 * 
 * @author Andrey Vorontsov
 */
public class Herbivore extends Cell {

	/**
	 * Create a herbivore. Herbivores start out with 75 energy (almost enough to
	 * start growing right away), and they are green.
	 * 
	 * @see Cell#Cell(double, double, double, double, int)
	 */
	public Herbivore(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		this(petri, x, y, xVelocity, yVelocity, size, 75);
	}

	/**
	 * Create a herbivore with a specified amount of starting energy (used for
	 * reproducing herbivores).
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
	 * Herbivore movement aims to eventually emulate food searching, grazing, and
	 * predator evasion behaviors.
	 * 
	 * @see Cell#move()
	 */
	@Override
	public void move() {

		// gather information about any visible cells
		ArrayList<Cell> visibleCells = petri.getCellsInRange(this, size * 15); // TODO vision range configuration

		// choose a prey target, if one is available
		Cell target = null;
		for (Cell c : visibleCells) { // for now, the closest agar is chosen
			if (c.getSpecies().equals("Agar") && (target == null || PetriDish.distanceBetween(target.getX(),
					target.getY(), x, y) > PetriDish.distanceBetween(c.getX(), c.getY(), x, y))) {
				target = c;
			}
		}

		// update the targeting vector based on gathered information

		if (target != null) { // if a prey target was found, go there
			targetX = target.getX();
			targetY = target.getY();
		} else if (targetingVector.magnitude < 5) { // no prey target found, set a random vector instead (but only if
													// we've almost finished following the previous vector)
			targetX = x + (rng.nextDouble() - 0.5) * 100;
			targetY = y + (rng.nextDouble() - 0.5) * 100;
		}

		// adjust the targeting vector to steer away from the edge if near

		if (targetX < 15) {
			targetX = 15;
		} else if (targetX > petri.PETRI_DISH_SIZE - 15) {
			targetX = petri.PETRI_DISH_SIZE - 15;
		}
		if (targetY < 15) {
			targetY = 15;
		} else if (targetY > petri.PETRI_DISH_SIZE - 15) {
			targetY = petri.PETRI_DISH_SIZE - 15;
		}

		// set the vector to point to the newly selected target
		targetingVector = getVectorToTarget(targetX, targetY);

		// standard code block which should be present in any implementation of move();
		// follow the vector
		xVelocity += targetingVector.getUnitVector().getXComponent();
		yVelocity += targetingVector.getUnitVector().getYComponent();

		// movement costs energy
		energy--;

		System.out.println("Current movement target: (" + targetX + ", " + targetY + ")");
		System.out.println("Current movement vector: " + targetingVector);
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
				System.out.println(this + " consumed " + c + ", receiving " + c.getEnergy() + " energy."); // TODO debug
																											// event
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
		if (energy > 90 && size < 10) { // right now: herbivore spends 5 energy to grow one size
			size++;
			energy -= 15;
			System.out.println(this + " grew one size."); // TODO debug print
		} else if (energy < 25 && size > 5) {
			size--;
			energy += 10;
			System.out.println(this + " is starving!"); // TODO debug print
		}
	}

	/**
	 * Herbivores reproduce after reaching their maximum size and a threshold
	 * energy.
	 * 
	 * @see Cell#reproduce()
	 */
	@Override
	public Cell reproduce() {
		Herbivore child = null;
		if (energy > 250 && size >= 12) { // right now: herbivore spends 20 energy to split in half and spawn an
											// offspring, they also split their energy evenly
			size = size / 2;
			energy = (energy - 20) / 2;
			child = new Herbivore(petri, x, y, 0, 0, size, energy);
			System.out.println(this + " spawned " + child + "."); // TODO debug print
		}
		return child;
	}

}
