import java.util.ArrayList;

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
	 * @see Cell#Cell(double, double, double, double, int)
	 */
	public Agar(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		super(petri, x, y, xVelocity, yVelocity, size);
		energy = 25;
		color = Color.YELLOW;
		species = "Agar";
		visionRange = 0;
		SUPPRESS_EVENT_PRINTING = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Cell#move(java.util.ArrayList)
	 */
	@Override
	public void move(ArrayList<Cell> visibleCells) {
		// agar doesn't move
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Cell#eat(java.util.ArrayList)
	 */
	@Override
	public void eat(ArrayList<Cell> eatableCells) {
		// agar can't eat anything else
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see Cell#reproduce(java.util.ArrayList)
	 */
	@Override
	public Cell reproduce(ArrayList<Cell> visibleCells) {
		// agar doesn't reproduce
		return null;
	}

}
