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
		CellBehaviorController behaviorSet = new CellBehaviorController();
		
		Behavior eatAgars = new Behavior("eat", "Agar", 1);
		eatAgars.setTargetCellMustBeEngulfed(true); // cell has to be engulfed to be eaten
		behaviorSet.addBehavior(eatAgars);
		
		Behavior eatGrazers = new Behavior("eat", "Grazer", 1);
		eatGrazers.setTargetCellMinRelSize(3); // the predator must be at least 3 bigger to eat
		eatGrazers.setTargetCellMustBeEngulfed(true); // cell has to be engulfed to be eaten
		behaviorSet.addBehavior(eatGrazers);
		
		Behavior huntingGrazers = new Behavior("hunt", "Grazer", 3);
		huntingGrazers.setTargetCellMaxDistance(40);
		huntingGrazers.setTargetCellMinRelSize(3); // the predator must be at least 3 bigger
		huntingGrazers.setTargetCellMinDistance(10); // avoid overshooting/oversteering
		huntingGrazers.setThisCellMinEnergy(20); // don't risk it unless we have a bit of energy left over
		behaviorSet.addBehavior(huntingGrazers);
		
		Behavior pursuitGrazers = new Behavior("pursue", "Grazer", 4);
		pursuitGrazers.setTargetCellMinRelSize(3); // the predator must be at least 3 bigger
		behaviorSet.addBehavior(pursuitGrazers);
		
		behaviorSet.addBehavior(new Behavior("pursue", "Agar", 5)); // agars pursued indiscrimnately
		behaviorSet.addBehavior(new Behavior("wander", null, 6));
		setBehaviors(behaviorSet);
		
		SUPPRESS_EVENT_PRINTING = true;
	}

//	/**
//	 * Predators get their energy from consuming Grazers or agars.
//	 * 
//	 * @see Cell#eat(java.util.ArrayList)
//	 */
//	@Override
//	public void eat(ArrayList<Cell> eatableCells) {
//		for (Cell c : eatableCells) {
//			// for now, Predators can eat agars and any grazers that are at least 3 smaller
//			if (c.getSpecies().equals("Agar") || (c.getSpecies().equals("Grazer") && c.getSize() + 3 <= size)) {
//				
//		}
//	}

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
