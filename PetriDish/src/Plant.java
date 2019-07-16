import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * A non-moving creature which accumulates energy passively through photosynthesis and reproduces by planting its offspring nearby.
 * 
 * @author Andrey Vorontsov
 */
public class Plant extends Cell {
	
	// side length calculated from size for graphics handling purposes
	double side;

	/**
	 * Create a Plant. Plants are deep green and square. Their size, as opposed to being the radius, is equal to .75 times half of the diagonal of the square. The x,y is still the center of the graphic.
	 * 
	 * @see Cell#Cell(double, double, double, double, int)
	 */
	public Plant(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		this(petri, x, y, xVelocity, yVelocity, size, 100);
	}

	/**
	 * Create a Grazer with a specified amount of starting energy (used for
	 * reproducing).
	 * 
	 * @see Cell#Cell(double, double, double, double, int)
	 */
	public Plant(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size, int energy) {
		super(petri, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.FORESTGREEN;
		species = "Plant";
		visionRange = 0;
		
		updateGraphicSideLength();
		
		SUPPRESS_EVENT_PRINTING = false;
	}
	
	/**
	 * Helper method to update the Plant's graphic appearance in response to size changes
	 */
	private void updateGraphicSideLength() {
		// (size/.75)*2 yields the full diagonal of the square (equivalent to 8/3 the size)
		// this squared yields the square of the hypotenuse of the right triangle
		// divide that by two and take the sqrt by pythagorean theorem to get side length of the square
		side = Math.sqrt(Math.pow(((double)size/.75)*2,2)/2);
	}

	/* (non-Javadoc)
	 * @see Cell#move(java.util.ArrayList)
	 */
	@Override
	public void move(ArrayList<Cell> visibleCells) {
		// Plants don't move
	}

	/**
	 * Plants 'eat' by photosynthesis. In the future, the energy production rate of Plants will be configurable.
	 * 
	 * @see Cell#eat()
	 */
	/**
	 * 
	 * @see Cell#eat(java.util.ArrayList)
	 */
	@Override
	public void eat(ArrayList<Cell> eatableCells) {
		if (age % 2 == 0)
			energy++;
	}

	/**
	 * Plants slowly grow above a certain energy. Plants can't starve.
	 * 
	 * @see Cell#grow()
	 */
	@Override
	public void grow() {
		if (energy > 200 && size < 16 && rng.nextInt(100) < 5) {
			size++;
			energy -= 20;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " grew one size.");
		}
	}

	/**
	 * Plants reproduce by 'spores'. Their children get squish()ed away from them. TODO Plants should have a density cap
	 * 
	 * @see Cell#reproduce(java.util.ArrayList)
	 */
	@Override
	public Cell reproduce(ArrayList<Cell> visibleCells) {
		Plant child = null;
		if (energy > 100 && size >= 14 && age % 3 == 0 && rng.nextInt(100) < 1) { 
			size -= 3;
			energy -= 50;
			child = new Plant(petri, x + rng.nextDouble() - 0.5, y + rng.nextDouble() - 0.5, 0, 0, 2, energy);
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " spawned " + child + ".");
		}
		// any size changes will occur either in grow() or here
		// so we should update side length here
		updateGraphicSideLength();
		
		return child;
	}
	
	/**
	 * Plants push all other cells away.
	 * 
	 * @see Cell#squish()
	 */
	@Override
	public void squish(ArrayList<Cell> touchedCells) {
		for (Cell c : touchedCells) {
			if (true) { // all species get squished out of the way by plants
				// get the unit vector along which to push, then scale it so that the magnitude is equal to the sum of the radii of the cells
				CellMovementVector pushUnit = getVectorToTarget(c.getX(), c.getY()).getUnitVector();
				double pushMagnitude = c.getSize() + size;
				CellMovementVector push = new CellMovementVector(pushUnit.getXComponent() * pushMagnitude, pushUnit.getYComponent() * pushMagnitude);
				// use the scaled vector to place the other cell at the appropriate distance, plus a tiny margin
				
				// push their offspring twice as far)
				if (c.getAge() < 2 && c.getSpecies().equals("Plant")) {
					c.setX(x + 2 * push.getXComponent() + 0.01);
					c.setY(y + 2 * push.getYComponent() + 0.01);
				} else {
					c.setX(x + push.getXComponent() + 0.01);
					c.setY(y + push.getYComponent() + 0.01);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see Cell#getGraphic()
	 */
	@Override
	public Node getGraphic() {
		Rectangle graphic = new Rectangle(x-(side/2), y-(side/2), side, side);
		graphic.setFill(color);
		return graphic;
	}

}
