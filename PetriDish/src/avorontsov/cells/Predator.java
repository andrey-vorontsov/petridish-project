package avorontsov.cells;
import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * A creature with similar traits to the Grazer, except that in addition to eating agars it preys on small Grazers.
 * As of version 0.0.5 their life cycle is reliant on agars, because newborns are too small to hunt. But because the Grazers are better at getting agars than them, they tend to die. So Grazers win. Funny stuff.
 * 
 * @author Andrey Vorontsov
 */
public class Predator extends Cell {

	/**
	 * Create a predator. Predators start out with 100 energy and are hot pink.
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Predator(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size) {
		this(petri, rng, x, y, xVelocity, yVelocity, size, 100);
	}

	/**
	 * Create a predator with a specific amount of starting energy, for reproduction.
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Predator(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size, int energy) {
		super(petri, rng, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.HOTPINK;
		friction = 0.8;
		species = "Predator";
		visionRange = 70;
		
		// create the set of behaviors used by this cell
		CellMovementController behaviorSet = new CellMovementController();
		behaviorSet.addBehavior("pursue", "Grazer", 1);
		behaviorSet.addBehavior("pursue", "Agar", 2);
		behaviorSet.addBehavior("wander", null, 3);
		setBehaviors(behaviorSet);
		
		SUPPRESS_EVENT_PRINTING = true;
	}

	/**
	 * Predator movement should include hunting and feeding behaviors. When hunting moving prey, the Predator will expend a burst of energy to lunge after it and secure the kill.
	 * 
	 * @see Cell#move(java.util.ArrayList)
	 */
	@Override
	public void move(ArrayList<Cell> visibleCells) {
		super.move(visibleCells);
		
		if (age % 2 == 0)
			energy--;
	}

	/**
	 * Predators get their energy from consuming Grazers or agars.
	 * 
	 * @see Cell#eat(java.util.ArrayList)
	 */
	@Override
	public void eat(ArrayList<Cell> eatableCells) {
		for (Cell c : eatableCells) {
			// for now, Predators can eat agars and any grazers that are at least 3 smaller
			if (c.getSpecies().equals("Agar") || (c.getSpecies().equals("Grazer") && c.getSize() + 3 < size)) {
				energy += c.getEnergy();
				c.kill("eaten");
				if (!SUPPRESS_EVENT_PRINTING)
					System.out.println(this + " consumed " + c + ", receiving " + c.getEnergy() + " energy.");
			}
		}
	}

	/**
	 * Predators grow when well-fed and shrink when starving.
	 * 
	 * @see Cell#grow()
	 */
	@Override
	public void grow() {
		if (energy > 90 && size < 10) { // preds spend 10 to grow one size, similar to grazers
			size++;
			energy -= 8;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " grew one size.");
		} else if (energy < 20 && size > 5) {
			size--;
			energy += 7;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " is starving!");
		}
	}

	/**
	 * Herbivores reproduce after reaching their maximum size and a threshold
	 * energy.
	 * 
	 * @see Cell#reproduce(java.util.ArrayList)
	 */
	@Override
	public Cell reproduce(ArrayList<Cell> visibleCells) {
		Predator child = null;
		if (energy > 150 && size >= 10) { // predator reproduction is more expensive
			size = size / 2;
			energy = (energy - 20) / 2;
			child = new Predator(petri, rng, x, y, 0, 0, size, energy);
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " spawned " + child + ".");
		}
		return child;
	}

}
