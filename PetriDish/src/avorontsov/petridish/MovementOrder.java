package avorontsov.petridish;
import avorontsov.cells.*;

/**
 * Contains the specific information of what action the cell must take on this tick.
 * For instance, a Grazer might have a MovementOrder along the lines of "evade", "Predator #123" for this tick.
 * The MovementOrder encapsulates the logic of enforcing this behavior, i.e. specific code representing a given behavior
 * As a baseline, it should be able to produce a CellMovementVector from the directive given by the Controller.
 * 
 * @author Andrey Vorontsov
 *
 */
public class MovementOrder {

	private Cell me;
	private String behaviorType; // valid strings include "evade" "pursue" "hunt" "graze" "wander"
	private Cell target; // the specific cell object to target (may be null) (when the target is null, the MovementOrder relies on its behavior to judge an alternative
	private CellMovementVector vector; // the calculated vector along which the cell must now move
	
	public MovementOrder(Cell me, String behaviorType, Cell target) {
		if (!MovementBehavior.checkValidBehavior(behaviorType)) {
			throw new IllegalArgumentException("An invalid cell behavior string was detected.");
		}
		this.me = me;
		this.behaviorType = behaviorType;
		this.target = target;
		
		vector = enforceBehavior(behaviorType);
	}

	private CellMovementVector enforceBehavior(String behaviorType) throws IllegalArgumentException {
		CellMovementVector oldTargetingVector = me.getTargetingVector(); // for clarity
		
		switch (behaviorType) {
		case "pursue": // pursuit: move along a straight line to the target cell. target must not be null
			return me.getVectorToTarget(target.getX(), target.getY());
			
		case "wander": // wander: generate a random vector using the current position. target may be null.
			double tempTargetX;
			double tempTargetY;
			if (oldTargetingVector.getMagnitude() < 3) { // get a new random movement vector as we approach our last target
				tempTargetX = me.getX() + (me.getRNG().nextDouble() - 0.5) * 100;
				tempTargetY = me.getY() + (me.getRNG().nextDouble() - 0.5) * 100;
				// correct the vector to avoid pointing out of the petri dish (leads to cell stuck on wall)
				if (tempTargetX < 15) {
					tempTargetX = 15;
				} else if (tempTargetX > PetriDishApp.PETRI_DISH_SIZE - 15) {
					tempTargetX = PetriDishApp.PETRI_DISH_SIZE - 15;
				}
				if (tempTargetY < 15) {
					tempTargetY = 15;
				} else if (tempTargetY > PetriDishApp.PETRI_DISH_SIZE - 15) {
					tempTargetY = PetriDishApp.PETRI_DISH_SIZE - 15;
				}
				return me.getVectorToTarget(tempTargetX, tempTargetY);
			}
			return oldTargetingVector; // if not yet close to approaching old destination, just keep going
			
		default:
			throw new IllegalArgumentException("The cell behavior " + behaviorType + " could not be applied.");
		}
		
	}

	/**
	 * @return the behaviorType
	 */
	public String getBehaviorType() {
		return behaviorType;
	}

	/**
	 * @return the target cell
	 */
	public Cell getTarget() {
		return target;
	}
	
	/**
	 * @return the final output vector
	 */
	public CellMovementVector getVector() {
		return vector;
	}
		
}
