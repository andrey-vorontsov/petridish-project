package avorontsov.cells;
import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * A creature with similar traits to the Grazer, except that in addition to eating agars it preys on small Grazers. Newborns are too small to hunt, so they are reliant on agars.
 * 
 * @author Andrey Vorontsov
 */
public class Predator extends Cell {

	/**
	 * Predators start out with 100 energy and are hot pink.
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
	public Predator(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size, double energy) {
		super(petri, rng, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.HOTPINK;
		friction = 0.8;
		species = "Predator";
		baseVisionRange = 70;
		
		// TODO review the behavior list
		
		// create the set of behaviors used by this cell
		CellBehaviorController behaviorSet = new CellBehaviorController();
				
		Behavior eatAgars = new Behavior("eat", "Agar", 1);
		eatAgars.setTargetCellMustBeEngulfed(true); // cell has to be engulfed to be eaten
		behaviorSet.addBehavior(eatAgars);
		
		Behavior eatGrazers = new Behavior("eat", "Grazer", 1);
		eatGrazers.setTargetCellMinRelSize(3); // the predator must be at least 3 bigger to eat
		eatGrazers.setTargetCellMustBeEngulfed(true); // cell has to be engulfed to be eaten
		behaviorSet.addBehavior(eatGrazers);
		
		Behavior cloneMyself = new Behavior("clone", null, 2);
		cloneMyself.setThisCellMinEnergy(200);
		cloneMyself.setThisCellMinSize(10);
		behaviorSet.addBehavior(cloneMyself);
		
		Behavior huntingGrazers = new Behavior("hunt", "Grazer", 3);
		huntingGrazers.setTargetCellMaxDistance(43);
		huntingGrazers.setTargetCellMinRelSize(3); // the predator must be at least 3 bigger
		huntingGrazers.setTargetCellMinDistance(10); // avoid overshooting/oversteering
		huntingGrazers.setThisCellMinEnergy(20); // don't risk it unless we have a bit of energy left over
		huntingGrazers.setEnergyCost(2); // the vector is three times longer; so this is fair
		behaviorSet.addBehavior(huntingGrazers);
		
		Behavior pursuitGrazers = new Behavior("pursue", "Grazer", 4);
		pursuitGrazers.setTargetCellMinRelSize(3); // the predator must be at least 3 bigger
		pursuitGrazers.setEnergyCost(.3);
		behaviorSet.addBehavior(pursuitGrazers);
		
		Behavior pursuitAgars = new Behavior("pursue", "Agar", 3);
		pursuitAgars.setEnergyCost(.3);
		behaviorSet.addBehavior(pursuitAgars); // agars pursued indiscrimnately
		
		Behavior sleepWhenStarving = new Behavior("sleep", 5);
		sleepWhenStarving.setThisCellMaxEnergy(7);
		sleepWhenStarving.setEnergyCost(.1);
		behaviorSet.addBehavior(sleepWhenStarving);
		
		Behavior wander = new Behavior("wander", null, 6);
		wander.setEnergyCost(.3);
		behaviorSet.addBehavior(wander);
		
		setBehaviorController(behaviorSet);
		
		SUPPRESS_EVENT_PRINTING = false;
	}

	/**
	 * Predators grow when well-fed and shrink when starving, akin to Grazers
	 * 
	 * @see Cell#customizedCellBehaviors(ArrayList, ArrayList)
	 */
	@Override
	public void customizedCellBehaviors(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells) {
		if (energy > 90 && size < 10) {
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
		
		super.customizedCellBehaviors(visibleCells, touchedCells); // squish() and starvation check

	}
	
	/**
	 * Predators spend 20 energy to divide.
	 * 
	 * @see avorontsov.cells.Cell#behaviorClone()
	 */
	@Override
	public Cell behaviorClone() {
		energy = (energy-20)/2;
		size = size/2;
		return new Predator(petri, rng, x, y, xVelocity, yVelocity, size, energy);
	}

}
