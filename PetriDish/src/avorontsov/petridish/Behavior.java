package avorontsov.petridish;

/**
 * A high-level representation of a particular behavior a given cell may have.
 * The encapsulated information informs the CellBehaviorController what checks should be made before a given action is taken.
 * 
 * @author Andrey Vorontsov
 */
public class Behavior {
	
	private static final String[] VALID_BEHAVIORS = {"eat", "nibble", "evade", "pursue", "hunt", "wander", "clone", "sleep"};
	
	// Notes on adding new behaviors or adjusting the implementation of old ones.
	// Movement type behaviors are interpreted in ActionOrder.generateMovementVector()
	// TODO complete this list
	
	private String behaviorCategory; // automatically initialized to categorize MOVE, EAT, REPRODUCE behaviors
	private String behaviorType; // valid strings listed in VALID_BEHAVIORS
	
	// properties of the cells to be targeted, with default values
	private String targetCellSpecies;
	private int targetCellMinMass = 0;
	private int targetCellMaxMass = Integer.MAX_VALUE;
	private double targetCellMinDistance = 0;
	private double targetCellMaxDistance = Double.MAX_VALUE;
	
	// relative properties, that is, comparisons between the target cell and this cell
	private int targetCellMinRelMass = Integer.MIN_VALUE;
	private int targetCellMaxRelMass = Integer.MAX_VALUE;
	
	// used for hit detection. touching is used for collision detection, engulfing is used for eatability detection
	private boolean targetCellMustBeTouching = false;
	private boolean targetCellMustBeEngulfed = false;
	
	// properties of this cell that must be satisfied for the behavior
	private int thisCellMinMass = 0;
	private int thisCellMaxMass = Integer.MAX_VALUE;
	private double thisCellMinEnergy = 0;
	private double thisCellMaxEnergy = Double.MAX_VALUE;

	// properties of the overall environment/misc
	private int maximumVisiblePopulation = Integer.MAX_VALUE; // the maximum number of other members of its species in vision range
	
	// energy cost is applied when this behavior is used
	private double energyCost = 0;
	// the number of ticks that must pass before this behavior can be used again
	private int coolDown = 0;
	
	private int priority; // used by CellMovementController to discriminate between higher and lower level
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
		if (behaviorType.equals("eat") || behaviorType.equals("nibble"))
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
	 * @param targetCellMinMass the targetCellMinMass to set
	 */
	public void setTargetCellMinMass(int targetCellMinMass) {
		this.targetCellMinMass = targetCellMinMass;
	}

	/**
	 * @param targetCellMaxMass the targetCellMaxMass to set
	 */
	public void setTargetCellMaxMass(int targetCellMaxMass) {
		this.targetCellMaxMass = targetCellMaxMass;
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
	 * Relsize is positive when this cell is BIGGER. Relmass is negative when this cell is SMALLER.
	 * 
	 * @param targetCellMinRelMass the targetCellMinRelMass to set
	 */
	public void setTargetCellMinRelMass(int targetCellMinRelMass) {
		this.targetCellMinRelMass = targetCellMinRelMass;
	}

	/**
	 * Relsize is positive when this cell is BIGGER. Relmass is negative when this cell is SMALLER.
	 * 
	 * @param targetCellMaxRelMass the targetCellMaxRelMass to set
	 */
	public void setTargetCellMaxRelMass(int targetCellMaxRelMass) {
		this.targetCellMaxRelMass = targetCellMaxRelMass;
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
	 * @param thisCellMinMass the thisCellMinMass to set
	 */
	public void setThisCellMinMass(int thisCellMinMass) {
		this.thisCellMinMass = thisCellMinMass;
	}

	/**
	 * @param thisCellMaxMass the thisCellMaxMass to set
	 */
	public void setThisCellMaxMass(int thisCellMaxMass) {
		this.thisCellMaxMass = thisCellMaxMass;
	}

	/**
	 * @param thisCellMinEnergy the thisCellMinEnergy to set
	 */
	public void setThisCellMinEnergy(double thisCellMinEnergy) {
		this.thisCellMinEnergy = thisCellMinEnergy;
	}

	/**
	 * @param thisCellMaxEnergy the thisCellMaxEnergy to set
	 */
	public void setThisCellMaxEnergy(double thisCellMaxEnergy) {
		this.thisCellMaxEnergy = thisCellMaxEnergy;
	}

	/**
	 * @param energyCost the energyCost to set
	 */
	public void setEnergyCost(double energyCost) {
		this.energyCost = energyCost;
	}

	/**
	 * @param coolDown the coolDown to set
	 */
	public void setCoolDown(int coolDown) {
		this.coolDown = coolDown;
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
	 * @return the targetCellMinMass
	 */
	public int getTargetCellMinMass() {
		return targetCellMinMass;
	}

	/**
	 * @return the targetCellMaxMass
	 */
	public int getTargetCellMaxMass() {
		return targetCellMaxMass;
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
	 * @return the targetCellMinRelMass
	 */
	public int getTargetCellMinRelMass() {
		return targetCellMinRelMass;
	}

	/**
	 * @return the targetCellMaxRelMass
	 */
	public int getTargetCellMaxRelMass() {
		return targetCellMaxRelMass;
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
	 * @return the thisCellMinMass
	 */
	public int getThisCellMinMass() {
		return thisCellMinMass;
	}

	/**
	 * @return the thisCellMaxSize
	 */
	public int getThisCellMaxMass() {
		return thisCellMaxMass;
	}

	/**
	 * @return the thisCellMinEnergy
	 */
	public double getThisCellMinEnergy() {
		return thisCellMinEnergy;
	}

	/**
	 * @return the thisCellMaxEnergy
	 */
	public double getThisCellMaxEnergy() {
		return thisCellMaxEnergy;
	}

	/**
	 * @return the energyCost
	 */
	public double getEnergyCost() {
		return energyCost;
	}

	/**
	 * @return the coolDown
	 */
	public int getCoolDown() {
		return coolDown;
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
