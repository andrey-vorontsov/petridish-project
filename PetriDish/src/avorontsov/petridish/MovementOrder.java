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
	private int vectorScalar = 1; // the int scalar to adjust the vector by. 1 by default (yields the unit vector) (initialized by constructor -> enforceBehavior())
	
	public MovementOrder(Cell me, String behaviorType, Cell target) {
		if (!Behavior.checkValidBehavior(behaviorType)) {
			throw new IllegalArgumentException("An invalid cell behavior string was detected.");
		}
		this.me = me;
		this.behaviorType = behaviorType;
		this.target = target;
		
		vector = enforceBehavior(behaviorType);
	}

	private CellMovementVector enforceBehavior(String behaviorType) {
		CellMovementVector oldTargetingVector = me.getTargetingVector(); // for clarity
		String oldBehaviorType = me.getCurrBehavior(); // the behavior the cell had on the previous update
		
		// determine vector direction and generate the scalar
		if (behaviorType.equals("pursue")) { // pursuit: move along a straight line to the target cell. target must not be null
			me.setTargetX(target.getX());
			me.setTargetY(target.getY());
			
		} else if (behaviorType.equals("wander")) { // wander: generate a random vector using the current position. target may be null.
			if (!oldBehaviorType.equals("wander") || oldTargetingVector.getMagnitude() < 5) { // get a new random movement vector if we just started wandering or as we approach our last target
				me.setTargetX(me.getX() + (me.getRNG().nextDouble() - 0.5) * 100);
				me.setTargetY(me.getY() + (me.getRNG().nextDouble() - 0.5) * 100);
			} else if (oldBehaviorType.equals("wander")) { // if we're already wandering, shuffle our destination slightly
				me.setTargetX(me.getTargetX() + (me.getRNG().nextDouble() - 0.5) * 3);
				me.setTargetY(me.getTargetY() + (me.getRNG().nextDouble() - 0.5) * 3);
			}
			
		} else if (behaviorType.equals("evade")) { // evasion: like pursuit, but in the opposite direction (currently just a vector from the enemy to me)
			me.setTargetX(2*me.getX() - target.getX()); // yields a point in the opposite direction of the target
			me.setTargetY(2*me.getY() - target.getY());

		} else if (behaviorType.equals("hunt")) { // hunt: like pursuit, but with an extra large vector
			me.setTargetX(target.getX());
			me.setTargetY(target.getY());
			vectorScalar = 3;
			
		} else {
			throw new IllegalArgumentException("Could not enforce the behavior: " + behaviorType + ".");
		}
		
		// correct the vector to avoid pointing out of the petri dish (leads to cell stuck on wall)
		if (me.getTargetX() < 15) {
			me.setTargetX(15);
		} else if (me.getTargetX() > PetriDishApp.PETRI_DISH_SIZE - 15) {
			me.setTargetX(PetriDishApp.PETRI_DISH_SIZE - 15);
		}
		if (me.getTargetY() < 15) {
			me.setTargetY(15);
		} else if (me.getTargetY() > PetriDishApp.PETRI_DISH_SIZE - 15) {
			me.setTargetY(PetriDishApp.PETRI_DISH_SIZE - 15);
		}
		
		// apply energy cost according to formula
		// TODO replace right away
		if (me.getAge() % 4 == 0)
			me.spendEnergy(1);
		
		return me.getVectorToTarget(me.getTargetX(), me.getTargetY()); // if no changes were made to the targetX and Y, the cell will continue moving along the old path
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
	
	/**
	 * @return the vector scalar
	 */
	public int getVectorScalar() {
		return vectorScalar;
	}
		
}
