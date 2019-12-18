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
	public Predator(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, double mass) {
		this(petri, rng, x, y, xVelocity, yVelocity, mass, 100);
	}

	/**
	 * Create a predator with a specific amount of starting energy, for reproduction.
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Predator(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, double mass, double energy) {
		super(petri, rng, x, y, xVelocity, yVelocity, mass);
		health = 100;
		this.energy = energy;
		color = Color.HOTPINK;
		maxAge = 2500;
		friction = 0.81;
		species = "Predator";
		baseVisionRange = 100;
		
		// TODO review the behavior list
		
		// create the set of behaviors used by this cell
		CellBehaviorController behaviorSet = new CellBehaviorController();
				
		Behavior eatAgars = new Behavior("eat", "Agar", 1);
		eatAgars.setTargetCellMustBeEngulfed(true); // cell has to be engulfed to be eaten
		behaviorSet.addBehavior(eatAgars);
		
		Behavior eatGrazers = new Behavior("eat", "Grazer", 1);
		eatGrazers.setTargetCellMinRelMass(42); // the predator must be at least this bigger to eat
		eatGrazers.setTargetCellMustBeEngulfed(true); // cell has to be engulfed to be eaten
		behaviorSet.addBehavior(eatGrazers);
		
		Behavior cloneMyself = new Behavior("clone", null, 2);
		cloneMyself.setThisCellMinEnergy(150);
		cloneMyself.setThisCellMinMass(310);
		behaviorSet.addBehavior(cloneMyself);
		
		Behavior huntingGrazers = new Behavior("hunt", "Grazer", 3);
		huntingGrazers.setTargetCellMaxDistance(38);
		huntingGrazers.setTargetCellMinRelMass(51); // the predator must be at least this bigger
		huntingGrazers.setTargetCellMinDistance(12); // avoid overshooting/oversteering
		huntingGrazers.setThisCellMinEnergy(30); // don't risk it unless we have a bit of energy left over
		huntingGrazers.setEnergyCost(3); // the vector is three times longer; so this is fair
		behaviorSet.addBehavior(huntingGrazers);
		
		Behavior pursuitGrazers = new Behavior("pursue", "Grazer", 4);
		pursuitGrazers.setTargetCellMinRelMass(50); // the predator must be at least this bigger
		pursuitGrazers.setEnergyCost(.5);
		behaviorSet.addBehavior(pursuitGrazers);
		
		Behavior pursuitAgars = new Behavior("pursue", "Agar", 3);
		pursuitAgars.setEnergyCost(.5);
		behaviorSet.addBehavior(pursuitAgars); // agars pursued indiscrimnately
		
		Behavior sleepWhenStarving = new Behavior("sleep", 5);
		sleepWhenStarving.setThisCellMaxEnergy(3);
		sleepWhenStarving.setEnergyCost(.1);
		behaviorSet.addBehavior(sleepWhenStarving);
		
		Behavior wander = new Behavior("wander", null, 6);
		wander.setEnergyCost(.25);
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
	public ArrayList<Cell> customizedCellBehaviors(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells) {
		if (energy > 90 && mass < 330) {
			mass += 10;
			energy -= 5;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " grew one size.");
		} else if (energy < 20 && mass > 100) {
			mass -= 10;
			energy += 4;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " is starving!");
		}
		
		return super.customizedCellBehaviors(visibleCells, touchedCells); // squish() and starvation check

	}
	
	/**
	 * Predators spend 20 energy to divide.
	 * 
	 * @see avorontsov.cells.Cell#behaviorClone()
	 */
	@Override
	public ArrayList<Cell> behaviorClone() {
		energy = (energy-20)/2;
		mass = mass/2;
		
		ArrayList<Cell> newCell = new ArrayList<Cell>();
		newCell.add(new Predator(petri, rng, x, y, xVelocity, yVelocity, mass, energy));
		return newCell;
	}

}
