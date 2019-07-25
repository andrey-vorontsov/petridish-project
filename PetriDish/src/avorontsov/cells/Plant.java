package avorontsov.cells;
import avorontsov.petridish.*;

import java.util.ArrayList;
import java.util.Random;
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
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Plant(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size) {
		this(petri, rng, x, y, xVelocity, yVelocity, size, 100);
	}

	/**
	 * Create a Plant with a specified amount of starting energy (used for
	 * reproducing).
	 * 
	 * @see Cell#Cell(PetriDish, Random, double, double, double, double, int)
	 */
	public Plant(PetriDish petri, Random rng, double x, double y, double xVelocity, double yVelocity, int size, double energy) {
		super(petri, rng, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.FORESTGREEN;
		friction = 0; // cannot move
		species = "Plant";
		baseVisionRange = 100; // needs to be able to see to evaluate density of its population
		
		updateGraphicSideLength(); // custom method necessary to use the square graphic
		
		// TODO review the behavior list
		
		CellBehaviorController behaviorSet = new CellBehaviorController();

		// reproduction behavior description
		Behavior sporePlants = new Behavior("clone", 1);
		sporePlants.setMaximumVisiblePopulation(3);
		sporePlants.setThisCellMinSize(12);
		sporePlants.setThisCellMinEnergy(175);
		sporePlants.setEnergyCost(100);
		behaviorSet.addBehavior(sporePlants);
		
		// passive behavior description
		behaviorSet.addBehavior(new Behavior("sleep", null, 2));
		setBehaviorController(behaviorSet);
		
		SUPPRESS_EVENT_PRINTING = false;
	}

	/**
	 * Plants slowly grow above a certain energy, but can't decrease in size. They also passively gain energy every update.
	 * 
	 * @see Cell#customizedCellBehaviors(ArrayList, ArrayList)
	 */
	@Override
	public void customizedCellBehaviors(ArrayList<Cell> visibleCells, ArrayList<Cell> touchedCells) {
		energy += .5;
		if (energy > 200 && size < 16 && getRNG().nextInt(100) < 5) {
			size++;
			energy -= 20;
			if (!SUPPRESS_EVENT_PRINTING)
				System.out.println(this + " grew one size.");
		}
		// replace the functionality of the superclass method
		// which calls the customized squish() and checks for death by starvation
		squish(touchedCells);
		if (energy <= 0) {
			kill("starvation");
		}
		
		updateGraphicSideLength(); // updates this cell's custom graphic
	}

	/**
	 * Customized squish() method. Plants push all other cells away, and push other Plants extra far. Plants use this technique to send their children a slight extra distance away.
	 * 
	 * @see Cell#squish(ArrayList)
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
				
				// push their offspring extra far
				if (c.getSpecies().equals("Plant")) {
					c.setX(x + 3 * push.getXComponent() + 0.01);
					c.setY(y + 3 * push.getYComponent() + 0.01);
				} else {
					c.setX(x + push.getXComponent() + 0.01);
					c.setY(y + push.getYComponent() + 0.01);
				}
			}
		}
		
	}

	/**
	 * Customzied getGraphic(). Plants use a square.
	 * 
	 * @see Cell#getGraphic()
	 */
	@Override
	public Node getGraphic() {
		updateGraphicSideLength();
		Rectangle graphic = new Rectangle(x-(side/2), y-(side/2), side, side);
		graphic.setFill(color);
		return graphic;
	}
	
	/**
	 * Helper method to update the Plant's graphic appearance in response to size changes
	 */
	protected void updateGraphicSideLength() {
		// (size/.75)*2 yields the full diagonal of the square (equivalent to 8/3 the size)
		// this squared yields the square of the hypotenuse of the right triangle
		// divide that by two and take the sqrt by pythagorean theorem to get side length of the square
		side = Math.sqrt(Math.pow(((double)size/.75)*2,2)/2);
	}


}
