import javafx.scene.paint.Color;

public class Agar extends Cell {

	/**
	 * In addition to normally initializing, agar always spawns with 25 energy
	 * 
	 * @see Cell#Cell(double, double, double, double, int)
	 */
	public Agar(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		super(petri, x, y, xVelocity, yVelocity, size);
		energy = 25;
		color = Color.YELLOW;
		species = "Agar";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Cell#move()
	 */
	@Override
	void move() {
		// agar doesn't move
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Cell#eat()
	 */
	@Override
	void eat() {
		// agar can't eat anything else
	}

}
