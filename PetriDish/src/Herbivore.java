import java.util.ArrayList;
import javafx.scene.paint.Color;

/**
 * @author Andrey Vorontsov
 * 
 * TODO
 *
 */
public class Herbivore extends Cell {

	/**
	 * TODO
	 * 
	 * @param petri
	 * @param x
	 * @param y
	 * @param xVelocity
	 * @param yVelocity
	 * @param size
	 */
	public Herbivore(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		super(petri, x, y, xVelocity, yVelocity, size);
		health = 100;
		energy = 75;
		color = Color.LAWNGREEN;
		friction = 0.8;
		species = "Herbivore";
	}
	
	/**
	 * TODO
	 * 
	 * @param petri
	 * @param x
	 * @param y
	 * @param xVelocity
	 * @param yVelocity
	 * @param size
	 */
	public Herbivore(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size, int energy) {
		super(petri, x, y, xVelocity, yVelocity, size);
		health = 100;
		this.energy = energy;
		color = Color.LAWNGREEN;
		friction = 0.8;
		species = "Herbivore";
	}

	/**
	 * TODO
	 * 
	 * @see Cell#move()
	 */
	@Override
	public void move() {
		// this behavior is temporary and should be replaced TODO
		ArrayList<Cell> visibleCells = petri.getCellsInRange(this, size*30);
		Cell target = null;
		for (Cell c : visibleCells) {
			if (c.getSpecies().equals("Agar") && (target == null || PetriDish.distanceBetween(target.getX(), target.getY(), x, y) > PetriDish.distanceBetween(c.getX(),c.getY(), x, y))) {
				target = c;
			}
		}
		
		if (target != null) {
			
			if (target.getX() > x)
				xVelocity += 0.75;
			if (target.getX() < x)
				xVelocity -= 0.75;
			if (target.getY() > y)
				yVelocity += 0.75;
			if (target.getY() < y)
				yVelocity -= 0.75;
		} else {
			xVelocity += rng.nextDouble()-0.5;
			yVelocity += rng.nextDouble()-0.5;
		}
		energy--;
	}

	/**
	 * Herbivores get energy from harvesting plant growth or agar.
	 * 
	 * @see Cell#eat()
	 */
	@Override
	public void eat() {
		// TODO unfinished, review behavior
		ArrayList<Cell> eatableCells = petri.getCellsInRange(this, size);
		for (Cell c : eatableCells) {
			if (c.getSpecies().equals("Agar")) {
				energy += c.getEnergy();
				c.kill("eaten");
				System.out.println(this + " consumed " + c + ", receiving " + c.getEnergy() + " energy.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see Cell#grow()
	 */
	@Override
	public void grow() {
		if (energy > 75 && size < 10) { // right now: herbivore spends 5 energy to grow one size
			size++;
			energy -= 5;
			System.out.println(this + " grew one size.");
		} else if (energy < 25 && size > 5) {
			size--;
			energy += 3;
			System.out.println(this + " is starving!");
		}
	}

	/* (non-Javadoc)
	 * @see Cell#reproduce()
	 */
	@Override
	public Cell reproduce() {
		Herbivore child = null;
		if (energy > 100 && size >= 10) { // right now: herbivore spends 20 energy to split in half and spawn an offspring, they also split their energy evenly
			size = size/2;
			child = new Herbivore(petri, x, y, 0, 0, 5, (energy-20)/2 );
			energy = (energy-20)/2;
			System.out.println(this + " spawned " + child + ".");
		}
		return child;
	}

}
