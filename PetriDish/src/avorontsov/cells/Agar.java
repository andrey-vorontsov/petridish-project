package avorontsov.cells;
import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * Not really a creature, just a little pellet of food. No behaviors, exist to
 * feed other cells.
 * 
 * @author Andrey Vorontsov
 */
public class Agar extends Cell {

	/**
	 * Agar is intended as a simple food unit worth 25 energy. They are yellow.
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Agar(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size) {
		super(petri, rng, x, y, xVelocity, yVelocity, size);
		energy = 25;
		color = Color.YELLOW;
		species = "Agar";
		visionRange = 0;
		
		CellBehaviorController behaviorSet = new CellBehaviorController();
		behaviorSet.addBehavior(new Behavior("wander", null, 4));
		setBehaviors(behaviorSet);
		
		SUPPRESS_EVENT_PRINTING = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Cell#grow()
	 */
	@Override
	public void grow() {
		// agar can't grow

	}
	
}
