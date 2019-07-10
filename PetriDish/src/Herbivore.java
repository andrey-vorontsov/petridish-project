import java.util.ArrayList;

import javafx.scene.paint.Color;

public class Herbivore extends Cell {

	public Herbivore(PetriDish petri, double x, double y, double xVelocity, double yVelocity, int size) {
		super(petri, x, y, xVelocity, yVelocity, size);
		health = 100;
		energy = 100;
		color = Color.LAWNGREEN;
		friction = 0.8;
		species = "Herbivore";
	}

	@Override
	void move() {
		ArrayList<Cell> visibleCells = petri.getCellsInRange(this, 300);		
		ArrayList<Cell> targets = new ArrayList<Cell>();
		
		for (Cell c : visibleCells) {
			if (c.getSpecies().equals("Agar")) {
				targets.add(c);
			}
		}
		
		if (targets.size() > 0) {
			
			int randomTarget = 0;//rng.nextInt(targets.size());
			
			double targetX = targets.get(randomTarget).getX();
			double targetY = targets.get(randomTarget).getY();
			
			if (targetX > x)
				xVelocity += 0.75;
			if (targetX < x)
				xVelocity -= 0.75;
			if (targetY > y)
				yVelocity += 0.75;
			if (targetY < y)
				yVelocity -= 0.75;
			
		}
		energy--;
	}

	/**
	 * Herbivores get energy from killing other cells.
	 * 
	 * @see Cell#eat()
	 */
	@Override
	void eat() {
		ArrayList<Cell> eatableCells = petri.getCellsInRange(this, size);
		for (Cell c : eatableCells) {
			if (c.getSpecies().equals("Agar")) {
				energy += c.getEnergy();
				c.kill("eaten");
				System.out.println(this + " consumed " + c + ", recieving "+ c.getEnergy() + " energy.");
			}
		}
	}

}
