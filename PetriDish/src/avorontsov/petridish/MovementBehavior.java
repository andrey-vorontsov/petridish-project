package avorontsov.petridish;

/**
 * A high-level representation of a particular behavior a given cell may have.
 * The encapsulated information informs the CellMovementController how a
 * particular species should be treated by this cell.
 * 
 * @author Andrey Vorontsov
 */
public class MovementBehavior {
	
	private static final String[] VALID_BEHAVIORS = {"evade", "pursue", "hunt", "graze", "wander"};

	private String behaviorType; // valid strings listed in VALID_BEHAVIORS
	private String targetCellSpecies; // valid string include "Predator" "Grazer" "Agar" "Plant" // TODO review and update handlers
	private int targetCellMinSize; // TODO use for predation pursuit
	private double targetCellMaxDistance; // TODO use for predation hunting
	// private int targetCellMaxRelativeVelocity; // TODO implement here and in Controller for multiple possibilities
	int priority; // used by CellMovementController to discriminate between higher and lower level
					// importance behaviors (ideally scale from 1 to 10, 1 highest)

	/**
	 * Produces a MovementBehavior object representing a general instruction to
	 * "behaviorType" all members of "targetCellSpecies" E.g. "evade" all
	 * "Predators"
	 * 
	 * @param behaviorType      valid strings include "evade" "pursue" "hunt"
	 *                          "graze"
	 * @param targetCellSpecies valid string TODO include "Predator" "Grazer" "Agar"
	 *                          "Plant"
	 * @param priority          gives the importance of the behavior (ideally scale
	 *                          from 1 to 10, 1 highest)
	 */
	public MovementBehavior(String behaviorType, String targetCellSpecies, int priority) {
		if (!checkValidBehavior(behaviorType)) {
			throw new IllegalArgumentException("An invalid cell behavior string was detected.");
		}
		this.behaviorType = behaviorType;
		this.targetCellSpecies = targetCellSpecies;
		this.priority = priority;
	}
	
	/**
	 * Used to validate that a behavior string is accepted as defined in the MovementBehavior class.
	 * In reality, to be acceptable, a behavior string must also have a full implementation in the MovementOrder class that actually encapsulates the behavior code.
	 * 
	 * @returns true only when the behavior string is acceptable.
	 */
	public static boolean checkValidBehavior(String testBehavior) throws IllegalArgumentException {
		boolean acceptable = false;
		for (int i=0; i<VALID_BEHAVIORS.length; i++) {
			if (testBehavior.equals(VALID_BEHAVIORS[i])) { // found a matching behavior
				acceptable = true;
				break;
			}
		}
		return acceptable;
	}

	/**
	 * @return the behaviorType
	 */
	public String getBehaviorType() {
		return behaviorType;
	}

	/**
	 * @return the targetCellSpecies
	 */
	public String getTargetCellSpecies() {
		return targetCellSpecies;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Generates a brief text description of this behavior.
	 * 
	 * @see java.lang.Object#toString()
	 * @return the String form of this Behavior
	 */
	@Override
	public String toString() {
		return "Behavior: " + behaviorType + " all " + targetCellSpecies;
	}
}
