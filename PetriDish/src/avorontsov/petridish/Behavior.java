package avorontsov.petridish;

/**
 * A high-level representation of a particular behavior a given cell may have.
 * The encapsulated information informs the CellBehaviorController what checks should be made before a given action is taken.
 * 
 * @author Andrey Vorontsov
 */
public class Behavior {
	
	private static final String[] VALID_BEHAVIORS = {"eat", "graze", "evade", "pursue", "hunt", "wander", "clone", "sleep", "grow", "starve"};
	
	// Notes on adding new behaviors or adjusting the implementation of old ones.
	// Movement type behaviors are interpreted in ActionOrder.generateMovementVector()
	// TODO complete this list
	
	private String behaviorCategory; // automatically initialized to categorize MOVE, EAT, REPRODUCE behaviors
	private String behaviorType; // valid strings listed in VALID_BEHAVIORS
	
	// properties of the cells to be targeted, with default values
	private String targetCellSpecies;
	private int targetCellMinSize = 0;
	private int targetCellMaxSize = Integer.MAX_VALUE;
	private double targetCellMinDistance = 0;
	private double targetCellMaxDistance = Double.MAX_VALUE;
	
	// relative properties, that is, comparisons between the target cell and this cell
	private int targetCellMinRelSize = Integer.MIN_VALUE;
	private int targetCellMaxRelSize = Integer.MAX_VALUE;
	
	// used for hit detection. touching is used for collision detection, engulfing is used for eatability detection
	private boolean targetCellMustBeTouching = false;
	private boolean targetCellMustBeEngulfed = false;
	
	// properties of this cell that must be satisfied for the behavior
	private int thisCellMinSize = 0;
	private int thisCellMaxSize = Integer.MAX_VALUE;
	private int thisCellMinEnergy = 0;
	private int thisCellMaxEnergy = Integer.MAX_VALUE;

	// properties of the overall environment/misc
	private int maximumVisiblePopulation = Integer.MAX_VALUE; // the maximum number of other members of its species in vision range
	
	int priority; // used by CellMovementController to discriminate between higher and lower level
					// importance behaviors (scale from 1 to 10, 1 highest)

	/**
	 * Produces a basic Behavior object. Optional fields may be initialized as needed. For certain behaviors that do not require a target, this constructor may be sufficient.
	 * 
	 * @param behaviorType      valid strings include "evade" "pursue" "hunt"
	 *                          "eat", etc.
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
	 *                          "eat", etc.
	 * @param targetCellSpecies the species this behavior targets (e.g. "Predator")
	 * @param priority          gives the importance of the behavior (ideally scale
	 *                          from 1 to 10, 1 highest)
	 */
	public Behavior(String behaviorType, String targetCellSpecies, int priority) {
		if (!checkValidBehavior(behaviorType)) {
			throw new IllegalArgumentException("WARNING: Misconfigured or invalid cell behavior: " + behaviorType + ".");
		}
		this.behaviorType = behaviorType;
		
		// assign behavior category
		if (behaviorType.equals("eat") || behaviorType.equals("graze"))
			behaviorCategory = "EAT";
		else if (behaviorType.equals("clone"))
			behaviorCategory = "REPRODUCE";
		else // safe to use else because we validated the behaviorType
			behaviorCategory = "MOVE"; 
		
		this.targetCellSpecies = targetCellSpecies;
		this.priority = priority;
	}
	
	/**
	 * Used to validate that a behavior string is accepted as defined in the Behavior class.
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
	 * @param thisCellMinSize the thisCellMinSize to set
	 */
	public void setThisCellMinSize(int thisCellMinSize) {
		this.thisCellMinSize = thisCellMinSize;
	}

	/**
	 * @param thisCellMaxSize the thisCellMaxSize to set
	 */
	public void setThisCellMaxSize(int thisCellMaxSize) {
		this.thisCellMaxSize = thisCellMaxSize;
	}

	/**
	 * @param thisCellMinEnergy the thisCellMinEnergy to set
	 */
	public void setThisCellMinEnergy(int thisCellMinEnergy) {
		this.thisCellMinEnergy = thisCellMinEnergy;
	}

	/**
	 * @param thisCellMaxEnergy the thisCellMaxEnergy to set
	 */
	public void setThisCellMaxEnergy(int thisCellMaxEnergy) {
		this.thisCellMaxEnergy = thisCellMaxEnergy;
	}

	/**
	 * @param maximumVisiblePopulation the maximumVisiblePopulation to set
	 */
	public void setMaximumVisiblePopulation(int maximumVisiblePopulation) {
		this.maximumVisiblePopulation = maximumVisiblePopulation;
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
	 * @return the thisCellMinSize
	 */
	public int getThisCellMinSize() {
		return thisCellMinSize;
	}

	/**
	 * @return the thisCellMaxSize
	 */
	public int getThisCellMaxSize() {
		return thisCellMaxSize;
	}

	/**
	 * @return the thisCellMinEnergy
	 */
	public int getThisCellMinEnergy() {
		return thisCellMinEnergy;
	}

	/**
	 * @return the thisCellMaxEnergy
	 */
	public int getThisCellMaxEnergy() {
		return thisCellMaxEnergy;
	}

	/**
	 * @return the maximumVisiblePopulation
	 */
	public int getMaximumVisiblePopulation() {
		return maximumVisiblePopulation;
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
