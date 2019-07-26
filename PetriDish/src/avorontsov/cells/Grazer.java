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
	public Grazer(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, double mass) {
		this(petri, rng, x, y, xVelocity, yVelocity, mass, 75);
	}

	/**
	 * Create a Grazer with a specified amount of starting energy (used for
	 * reproducing).
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Grazer(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, double mass,
			double energy) {
		super(petri, rng, x, y, xVelocity, yVelocity, mass);
		health = 100;
		this.energy = energy;
		color = Color.LAWNGREEN;
		friction = 0.85;
		species = "Grazer";
		baseVisionRange = 50;

		// create the set of behaviors used by this cell
		CellBehaviorController behaviorSet = new CellBehaviorController();

		// TODO review the behavior list
		
		// eating agars behavior description
		Behavior eatAgars = new Behavior("eat", "Agar", 1);
		eatAgars.setTargetCellMustBeEngulfed(true);
		behaviorSet.addBehavior(eatAgars);
		
		// reproduction
		Behavior cloneMyself = new Behavior("clone", null, 2);
		cloneMyself.setThisCellMinEnergy(150);
		cloneMyself.setThisCellMinMass(140);
		behaviorSet.addBehavior(cloneMyself);

		Behavior avoidPredators = new Behavior("evade", "Predator", 1); // higher priority
		avoidPredators.setTargetCellMinDistance(45); // stay just outside of lunging range
		avoidPredators.setTargetCellMaxRelMass(-100); // only bother evading if we are small enough to be eaten (less than -3 bigger, aka more than 3 smaller) 
		avoidPredators.setEnergyCost(.25);
		behaviorSet.addBehavior(avoidPredators);

		Behavior chaseAgars = new Behavior("pursue", "Agar", 2);
		chaseAgars.setEnergyCost(.25);
		behaviorSet.addBehavior(chaseAgars);
		
		Behavior nibblePlants = new Behavior("nibble", "Plant", 1);
		nibblePlants.setTargetCellMustBeTouching(true);
		nibblePlants.setTargetCellMinMass(50);
		nibblePlants.setCoolDown(5);
		behaviorSet.addBehavior(nibblePlants);
		
		Behavior grazePlants = new Behavior("pursue", "Plant", 3);
		grazePlants.setEnergyCost(.25);
		grazePlants.setTargetCellMinMass(50);
		behaviorSet.addBehavior(grazePlants);
		
		Behavior sleepWhenStarving = new Behavior("sleep", 4);
		sleepWhenStarving.setThisCellMaxEnergy(10);
		sleepWhenStarving.setEnergyCost(.1);
		behaviorSet.addBehavior(sleepWhenStarving);
		
		Behavior wander = new Behavior("wander", null, 5);
		wander.setEnergyCost(.25);
		behaviorSet.addBehavior(wander);
		
		setBehaviorController(behaviorSet);

		SUPPRESS_EVENT_PRINTING = false;
	}

	/**
	 * Customized Grazer behavior. Grazers can grow up to a maximum and starve down
	 * to a minimum size, depending on available energy.
	 * 
	 * @see Cell#customizedCellBehaviors(ArrayList, ArrayList)
	 */
	@Override
	public void customizedCellBehaviors(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells) {
		if (energy > 75 && mass < 150) {
			mass+= 20;
			energy -= 3;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " grew one size.");
		} else if (energy < 25 && mass > 30) {
			mass -= 20;
			energy += 1;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " is starving!");
		}

		super.customizedCellBehaviors(visibleCells, touchedCells); // squish() and starvation check

	}
	
	/**
	 * Grazers spend 20 energy to divide.
	 * 
	 * @see avorontsov.cells.Cell#behaviorClone()
	 */
	@Override
	public Cell behaviorClone() {
		energy = (energy-20)/2;
		mass = mass/2;
		return new Grazer(petri, rng, x, y, xVelocity, yVelocity, mass, energy);
	}

}
