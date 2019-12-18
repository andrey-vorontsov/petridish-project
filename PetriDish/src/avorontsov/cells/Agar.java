package avorontsov.cells;

import avorontsov.petridish.*;

import java.util.Random;
import javafx.scene.paint.Color;

/**
 * Not really a creature, just a little pellet of food. No behaviors, exists to
 * feed other cells.
 * 
 * @author Andrey Vorontsov
 */
public class Agar extends Cell {

	/**
	 * Agars are intended as a food unit worth 25 energy. They are yellow and cannot
	 * move.
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Agar(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, double mass) {
		super(petri, rng, x, y, xVelocity, yVelocity, mass);
		health = 0;
		energy = 25;
		color = Color.YELLOW;
		maxAge = -1; // can't die of old age
		friction = 0; // cannot move
		species = "Agar";
		baseVisionRange = 0; // cannot see

		CellBehaviorController behaviorSet = new CellBehaviorController();
		behaviorSet.addBehavior(new Behavior("sleep", null, 1)); // does nothing
		setBehaviorController(behaviorSet);

		SUPPRESS_EVENT_PRINTING = true;

	}

	// agar requires no further customization

}
