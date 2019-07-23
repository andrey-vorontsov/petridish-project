package avorontsov.cells;

import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * A simple creature, made for testing out various functions such as cell
 * eating, reproduction, growth and movement. Grazers search for plants and
 * agars, exhibiting a grazing behavior as well as a predator evasion behavior.
 * Currently unfinished.
 * 
 * @author Andrey Vorontsov
 */
public class Grazer extends Cell {

	/**
	 * Create a Grazer. Grazers start out with 75 energy (almost enough to start
	 * growing right away), and they are green.
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
	public Grazer(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size,
			int energy) {
		super(petri, rng, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.LAWNGREEN;
		friction = 0.85;
		species = "Grazer";
		baseVisionRange = 50;

		// create the set of behaviors used by this cell
		CellBehaviorController behaviorSet = new CellBehaviorController();

		// TODO go through these one more time and double check

		Behavior eatAgars = new Behavior("eat", "Agar", 1);
		eatAgars.setTargetCellMustBeEngulfed(true); // cell has to be engulfed to be eaten
		behaviorSet.addBehavior(eatAgars);

		Behavior cloneMyself = new Behavior("clone", null, 2);
		cloneMyself.setThisCellMinEnergy(100);
		behaviorSet.addBehavior(cloneMyself);

		Behavior avoidPredators = new Behavior("evade", "Predator", 1); // higher priority
		avoidPredators.setTargetCellMinDistance(45); // stay just outside of lunging range
		behaviorSet.addBehavior(avoidPredators);

		behaviorSet.addBehavior(new Behavior("pursue", "Agar", 2));
		behaviorSet.addBehavior(new Behavior("graze", "Plant", 3));
		behaviorSet.addBehavior(new Behavior("wander", null, 4));
		setBehaviorController(behaviorSet);

		SUPPRESS_EVENT_PRINTING = true;
	}

	/**
	 * Customized Grazer behavior. Grazers can grow up to a maximum and starve down
	 * to a minimum size, depending on available energy.
	 * 
	 * @see Cell#customizedCellBehaviors(ArrayList, ArrayList)
	 */
	@Override
	public void customizedCellBehaviors(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells) {
		if (energy > 75 && size < 8) {
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

		super.customizedCellBehaviors(visibleCells, touchedCells); // squish() and starvation check

	}

}
