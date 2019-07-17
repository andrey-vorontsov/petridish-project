package avorontsov.cells;
import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * A simple creature, made for testing out various functions such as cell
 * eating, reproduction, growth and movement. Grazers search for plants and
 * agars, exhibiting a grazing behavior as well as a predator evasion behavior
 * Currently unfinished.
 * 
 * @author Andrey Vorontsov
 */
public class Grazer extends Cell {

	/**
	 * Create a Grazer. Grazers start out with 75 energy (almost enough to
	 * start growing right away), and they are green.
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Grazer(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size) {
		this(petri, rng, x, y, xVelocity, yVelocity, size, 75);
	}

	/**
	 * Create a Grazer with a specified amount of starting energy (used for
	 * reproducing).
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Grazer(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size, int energy) {
		super(petri, rng, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.LAWNGREEN;
		friction = 0.85;
		species = "Grazer";
		visionRange = 50;
		
		// create the set of behaviors used by this cell
		CellMovementController behaviorSet = new CellMovementController();
		Behavior avoidPredators = new Behavior("evade", "Predator", 1); // higher priority
		avoidPredators.setTargetCellMinDistance(45); // stay just outside of lunging range
		behaviorSet.addBehavior(avoidPredators);
		
		behaviorSet.addBehavior(new Behavior("pursue", "Agar", 2));
		behaviorSet.addBehavior(new Behavior("graze", "Plant", 3));
		behaviorSet.addBehavior(new Behavior("wander", null, 4));
		setBehaviors(behaviorSet);
		
		SUPPRESS_EVENT_PRINTING = false;
	}

	/**
	 * Grazers get energy from harvesting plant growth or agar.
	 * 
	 * @see Cell#eat(java.util.ArrayList)
	 */
	@Override
	public void eat(ArrayList<Cell> eatableCells) {
		for (Cell c : eatableCells) {
			if (c.getSpecies().equals("Agar")) { // for now, any agars contacted will be eaten
				energy += c.getEnergy();
				c.kill("eaten");
				if (!SUPPRESS_EVENT_PRINTING)
					System.out.println(this + " consumed " + c + ", receiving " + c.getEnergy() + " energy."); // TODO debug
																											// event
			}
		}
	}

	/**
	 * Grazers grow when well-fed and shrink when starving.
	 * 
	 * @see Cell#grow()
	 */
	@Override
	public void grow() {
		if (energy > 75 && size < 8) { // right now: herbivore spends 10 energy to grow one size
			size++;
			energy -= 8;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " grew one size.");
		} else if (energy < 25 && size > 5) {
			size--;
			energy += 7;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " is starving!");
		}
	}

	/**
	 * Grazers reproduce after reaching their maximum size and a threshold
	 * energy.
	 * 
	 * @see Cell#reproduce(java.util.ArrayList)
	 */
	@Override
	public Cell reproduce(ArrayList<Cell> visibleCells) {
		Grazer child = null;
		if (energy > 100 && size >= 8) { // right now: herbivore spends 20 energy to split in half and spawn an
											// offspring, they also split their energy evenly
			size = size / 2;
			energy = (energy - 20) / 2;
			child = new Grazer(petri, rng, x, y, 0, 0, size, energy);
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " spawned " + child + ".");
		}
		return child;
	}

}
