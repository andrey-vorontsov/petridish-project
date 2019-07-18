package avorontsov.petridish;

import avorontsov.cells.*;

/**
 * Contains the specific information of what action the cell must take on this
 * tick, as produced by the CellBehaviorController. In addition, has the
 * function of translating a movement order into a specific vector to isolate
 * that code from the Cell class. For non-movement orders, ActionOrder behaves
 * as a simple wrapper class to allow Cell to interface with the
 * CellBehaviorController.
 * 
 * For example, a Grazer might have an ActionOrder along the lines of "evade"
 * "Predator #12" for this tick, and this ActionOrder could be queried for a
 * vector pointing away from Predator #12. Another example might be an
 * ActionOrder to "eat" "Agar #300", which is trivial from the perspective of
 * the Cell class and thus doesn't need any other code to be isolated.
 * 
 * @author Andrey Vorontsov
 */
public class ActionOrder {

	private Cell me;
	private Behavior sourceBehavior; // the behavior which generated this action order
	private Cell target; // the specific cell object to target (may be null if the sourceBehavior doesn't
							// require a target)

	// calculated only for MOVE category behaviors
	private double newTargetX; // the coordinates of the new target location
	private double newTargetY;
	private CellMovementVector newTargetingVector; // the calculated vector along which the cell may need to move; has
													// an unknown magnitude depending on how far away the target is
	private int vectorScalar = 1; // the int scalar to scale the vector to. 1 by default (unit vector)

	/**
	 * Constructs an ActionOrder for the given Cell 'me' with a particular
	 * sourceBehavior and with respect to a target, if one exists.
	 * 
	 * @param me             the Cell that this order applies to
	 * @param sourceBehavior the particular behavior that cell has which produced
	 *                       this order
	 * @param target         a target Cell if said behavior requires a target
	 */
	public ActionOrder(Cell me, Behavior sourceBehavior, Cell target) {
		this.me = me;
		this.sourceBehavior = sourceBehavior;
		this.target = target;

		// movement orders generate an associated targeting vector with a scalar
		if (sourceBehavior.getBehaviorCategory().equals("MOVE"))
			generateMovementVector();
	}

	/**
	 * Helper method for MOVE category orders, which take on the role of calculating
	 * an appropriate vector for the cell to move along to enact the order.
	 */
	private void generateMovementVector() {

		// load the cell's old targeting vector and target location
		CellMovementVector oldTargetingVector = me.getTargetingVector();
		newTargetX = me.getTargetX();
		newTargetY = me.getTargetY();

		String oldBehaviorType = me.getCurrBehavior(); // the behavior the cell had on the previous update
		String newBehaviorType = sourceBehavior.getBehaviorType(); // the behavior which this order will apply

		// depending on the movement-type behavior; calculate the new target coordinates
		// and vector

		if (newBehaviorType.equals("pursue")) { // pursuit: move along a straight line to the target cell

			// for pursuit, simply set the target coordinates to the location of the target
			// cell
			// movement vector scalar remains at default (1)
			newTargetX = target.getX();
			newTargetY = target.getY();

		} else if (newBehaviorType.equals("wander")) { // wander: generate a random vector using the current position

			// if we just started wandering or as we are approaching our last wander target,
			// choose a new random target location
			if (!oldBehaviorType.equals("wander") || oldTargetingVector.getMagnitude() < 5) {
				newTargetX = me.getX() + (me.getRNG().nextDouble() - 0.5) * 200;
				newTargetY = me.getY() + (me.getRNG().nextDouble() - 0.5) * 200;

			} else if (oldBehaviorType.equals("wander")) { // if we're already wandering, shuffle our destination
															// slightly
				newTargetX = me.getTargetX() + (me.getRNG().nextDouble() - 0.5) * 6;
				newTargetY = me.getTargetY() + (me.getRNG().nextDouble() - 0.5) * 6;
			}

		} else if (newBehaviorType.equals("evade")) { // evasion: like pursuit, but in the opposite direction

			// target a point in the opposite direction of the threat
			// movement vector scalar default at 1
			newTargetX = 2 * me.getX() - target.getX();
			newTargetY = 2 * me.getY() - target.getY();

		} else if (newBehaviorType.equals("hunt")) { // hunt: like pursuit, but with an extra large vector

			// target our victim, like with pursuit
			me.setTargetX(target.getX());
			me.setTargetY(target.getY());
			// but we can expend a burst of energy to chase them down
			vectorScalar = 3;

		} else {
			System.out.println("WARNING: Unrecognized behavior: " + newBehaviorType + ".");
		}

		// correct the vector to avoid pointing out of the petri dish (leads to cell
		// stuck on wall)
		if (newTargetX < 15) {
			newTargetX = 15;
		} else if (newTargetX > PetriDishApp.PETRI_DISH_WIDTH - 15) {
			newTargetX = PetriDishApp.PETRI_DISH_WIDTH - 15;
		}
		if (newTargetY < 15) {
			newTargetY = 15;
		} else if (newTargetY > PetriDishApp.PETRI_DISH_HEIGHT - 15) {
			newTargetY = PetriDishApp.PETRI_DISH_HEIGHT - 15;
		}

		newTargetingVector = me.getVectorToTarget(newTargetX, newTargetY); // if no changes were made to the targetX and
																			// Y, the cell will continue moving along
																			// the old path (based on old target coords)
	}

	/**
	 * @return the behavior which produced this action order
	 */
	public Behavior getSourceBehavior() {
		return sourceBehavior;
	}

	/**
	 * @return the target cell of this ActionOrder if applicable (otherwise null)
	 */
	public Cell getTarget() {
		return target;
	}

	/**
	 * @return a vector of unknown magnitude in the direction that the cell should
	 *         move (for movement orders only; null for non-movement orders)
	 */
	public CellMovementVector getVector() {
		return newTargetingVector;
	}

	/**
	 * @return the newTargetX for the cell to use
	 */
	public double getNewTargetX() {
		return newTargetX;
	}

	/**
	 * @return the newTargetY for the cell to use
	 */
	public double getNewTargetY() {
		return newTargetY;
	}

	/**
	 * @return the vector scalar to use for movement (typically 1)
	 */
	public int getVectorScalar() {
		return vectorScalar;
	}

}
