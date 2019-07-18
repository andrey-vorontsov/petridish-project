package avorontsov.petridish;

/**
 * A high-level representation of a particular behavior a given cell may have.
 * The encapsulated information informs the CellMovementController how a
 * particular species should be treated by this cell.
 * 
 * @author Andrey Vorontsov
 */
public class Behavior {
	
	private static final String[] VALID_BEHAVIORS = {"eat", "graze", "evade", "pursue", "hunt", "wander", "mate"};
	// eat and graze are related to eat(). evade, pursue, hunt, wander are move() modes. mate is used by reproduce()

	private String behaviorCategory; // automatically initalized to sort eat(), move(), reproduce() behaviors
	private String behaviorType; // valid strings listed in VALID_BEHAVIORS
	private String targetCellSpecies; // valid string include "Predator" "Grazer" "Agar" "Plant" // TODO review and update handlers
	
	// target cell properties evaluated by the Controller
	private int targetCellMinSize = 0;
	private int targetCellMaxSize = Integer.MAX_VALUE;
	private double targetCellMinDistance = 0;
	private double targetCellMaxDistance = Double.MAX_VALUE;
	private int thisCellMinEnergy = 0; // the minimum energy for this cell to attempt this behavior
	// these properties are calculated relative to the cell itself
	private int targetCellMinRelVelocity = 0;
	private int targetCellMaxRelVelocity = Integer.MAX_VALUE;
	private int targetCellMinRelSize = -Integer.MAX_VALUE;
	private int targetCellMaxRelSize = Integer.MAX_VALUE;
	// used for hit detection. touching is used for collision detection, engulfing is used for eatability detection
	private boolean targetCellMustBeTouching = false;
	private boolean targetCellMustBeEngulfed = false;
	// TODO valid eat and squish detection must use these
	
	int priority; // used by CellMovementController to discriminate between higher and lower level
					// importance behaviors (ideally scale from 1 to 10, 1 highest)

	/**
	 * Produces a minimal Behavior object. Optional fields may be initialized as needed.
	 * 
	 * @param behaviorType      valid strings include "evade" "pursue" "hunt"
	 *                          "graze", etc.
	 * @param priority          gives the importance of the behavior (ideally scale
	 *                          from 1 to 10, 1 highest)
	 */
	public Behavior(String behaviorType, int priority) {
		this(behaviorType, null, priority);
	}

	/**
	 * Produces a simple Behavior object with a defined target species (all members of the species will be targeted). Other optional fields may be initialized as needed.
	 * 
	 * @param behaviorType      valid strings include "evade" "pursue" "hunt"
	 *                          "graze", etc.
	 *                          @param targetCellSpecies the species this behavior targets (e.g. "evade Predators")
	 * @param priority          gives the importance of the behavior (ideally scale
	 *                          from 1 to 10, 1 highest)
	 */
	public Behavior(String behaviorType, String targetCellSpecies, int priority) {
		if (!checkValidBehavior(behaviorType)) {
			throw new IllegalArgumentException("An invalid cell behavior string was detected.");
		}
		this.behaviorType = behaviorType;
		
		// assign behavior category. defaults to move
		if (behaviorType.equals("eat") || behaviorType.equals("graze"))
			behaviorCategory = "EAT";
		else if (behaviorType.equals("mate"))
			behaviorCategory = "REPRODUCE";
		else
			behaviorCategory = "MOVE";
		
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
	
	// setters for optional fields

	/**
	 * @param targetCellSpecies the targetCellSpecies to set
	 */
	public void setTargetCellSpecies(String targetCellSpecies) {
		this.targetCellSpecies = targetCellSpecies;
	}

	/**
	 * @param targetCellMinSize the targetCellMinSize to set
	 */
	public void setTargetCellMinSize(int targetCellMinSize) {
		this.targetCellMinSize = targetCellMinSize;
	}

	/**
	 * @param targetCellMaxSize the targetCellMaxSize to set
	 */
	public void setTargetCellMaxSize(int targetCellMaxSize) {
		this.targetCellMaxSize = targetCellMaxSize;
	}

	/**
	 * @param targetCellMinDistance the targetCellMinDistance to set
	 */
	public void setTargetCellMinDistance(double targetCellMinDistance) {
		this.targetCellMinDistance = targetCellMinDistance;
	}

	/**
	 * @param targetCellMaxDistance the targetCellMaxDistance to set
	 */
	public void setTargetCellMaxDistance(double targetCellMaxDistance) {
		this.targetCellMaxDistance = targetCellMaxDistance;
	}

	/**
	 * @param targetCellMinRelVelocity the targetCellMinRelVelocity to set
	 */
	public void setTargetCellMinRelVelocity(int targetCellMinRelVelocity) {
		this.targetCellMinRelVelocity = targetCellMinRelVelocity;
	}

	/**
	 * @param targetCellMaxRelVelocity the targetCellMaxRelVelocity to set
	 */
	public void setTargetCellMaxRelVelocity(int targetCellMaxRelVelocity) {
		this.targetCellMaxRelVelocity = targetCellMaxRelVelocity;
	}

	/**
	 * @param targetCellMinRelSize the targetCellMinRelSize to set
	 */
	public void setTargetCellMinRelSize(int targetCellMinRelSize) {
		this.targetCellMinRelSize = targetCellMinRelSize;
	}

	/**
	 * @param targetCellMaxRelSize the targetCellMaxRelSize to set
	 */
	public void setTargetCellMaxRelSize(int targetCellMaxRelSize) {
		this.targetCellMaxRelSize = targetCellMaxRelSize;
	}

	/**
	 * @param targetCellMustBeTouching the targetCellMustBeTouching to set
	 */
	public void setTargetCellMustBeTouching(boolean targetCellMustBeTouching) {
		this.targetCellMustBeTouching = targetCellMustBeTouching;
	}

	/**
	 * @param targetCellMustBeEngulfed the targetCellMustBeEngulfed to set
	 */
	public void setTargetCellMustBeEngulfed(boolean targetCellMustBeEngulfed) {
		this.targetCellMustBeEngulfed = targetCellMustBeEngulfed;
	}

	/**
	 * @return the behaviorCategory
	 */
	public String getBehaviorCategory() {
		return behaviorCategory;
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
	 * @return the targetCellMinSize
	 */
	public int getTargetCellMinSize() {
		return targetCellMinSize;
	}

	/**
	 * @return the targetCellMaxSize
	 */
	public int getTargetCellMaxSize() {
		return targetCellMaxSize;
	}

	/**
	 * @return the targetCellMinDistance
	 */
	public double getTargetCellMinDistance() {
		return targetCellMinDistance;
	}

	/**
	 * @return the targetCellMaxDistance
	 */
	public double getTargetCellMaxDistance() {
		return targetCellMaxDistance;
	}

	/**
	 * @return the thisCellMinEnergy
	 */
	public int getThisCellMinEnergy() {
		return thisCellMinEnergy;
	}

	/**
	 * @param thisCellMinEnergy the thisCellMinEnergy to set
	 */
	public void setThisCellMinEnergy(int thisCellMinEnergy) {
		this.thisCellMinEnergy = thisCellMinEnergy;
	}

	/**
	 * @return the targetCellMinRelVelocity
	 */
	public int getTargetCellMinRelVelocity() {
		return targetCellMinRelVelocity;
	}

	/**
	 * @return the targetCellMaxRelVelocity
	 */
	public int getTargetCellMaxRelVelocity() {
		return targetCellMaxRelVelocity;
	}

	/**
	 * @return the targetCellMinRelSize
	 */
	public int getTargetCellMinRelSize() {
		return targetCellMinRelSize;
	}

	/**
	 * @return the targetCellMaxRelSize
	 */
	public int getTargetCellMaxRelSize() {
		return targetCellMaxRelSize;
	}

	/**
	 * @return the targetCellMustBeTouching
	 */
	public boolean doesTargetCellHaveToBeTouching() {
		return targetCellMustBeTouching;
	}

	/**
	 * @return the targetCellMustBeEngulfed
	 */
	public boolean doesTargetCellHaveToBeEngulfed() {
		return targetCellMustBeEngulfed;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * @return true only when this behavior requires a target cell to be meaningful
	 */
	public boolean requiresTarget() {
		return !(targetCellSpecies == null);
	}

	/**
	 * Generates a brief text description of this behavior, without information about the specific limits set.
	 * 
	 * @see java.lang.Object#toString()
	 * @return the String form of this Behavior
	 */
	@Override
	public String toString() {
		String returnString = "Behavior: " + behaviorType;
		if (targetCellSpecies != null) {
			returnString += " " + targetCellSpecies + "s";
		}
		return returnString;
	}
}
