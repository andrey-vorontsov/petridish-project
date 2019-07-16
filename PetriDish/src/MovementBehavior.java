
/**
 * A high-level representation of a particular behavior a given cell may have. The encapsulated information informs the CellMovementController how a particular species should be treated by this cell.
 * 
 * @author Andrey Vorontsov
 */
public class MovementBehavior {
	
	private String behaviorType; // valid strings include "evade" "pursue" "hunt" "graze"
	private String targetCellSpecies; // valid string TODO include "Predator" "Grazer" "Agar" "Plant"
	
	/**
	 * Produces a MovementBehavior object representing a general instruction to "behaviorType" all members of "targetCellSpecies"
	 * E.g. "evade" all "Predators"
	 * 
	 * @param behaviorType
	 * @param targetCellSpecies
	 */
	public MovementBehavior(String behaviorType, String targetCellSpecies) {
		this.behaviorType = behaviorType;
		this.targetCellSpecies = targetCellSpecies;
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
	
}
