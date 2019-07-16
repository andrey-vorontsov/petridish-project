
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

	private String behaviorType; // valid strings include "evade" "pursue" "hunt" "graze" "wander"
	private Cell target; // the specific cell object to target (may be null) (when the target is null, the MovementOrder relies on its behavior to judge an alternative
	private CellMovementVector vector; // the calculated vector along which the cell must now move
	
	public MovementOrder(Cell me, String behaviorType, Cell target) {
		this.behaviorType = behaviorType;
		this.target = target;
		
		vector = enforceBehavior(behaviorType);
	}

	private CellMovementVector enforceBehavior(String behaviorType) throws IllegalArgumentException {
		if (!MovementBehavior.checkValidBehavior(behaviorType)) {
			throw new IllegalArgumentException("An invalid cell behavior string was detected.");
		}
		switch (behaviorType) {
		case "":
			
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
