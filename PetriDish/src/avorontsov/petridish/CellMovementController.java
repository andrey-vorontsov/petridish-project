package avorontsov.petridish;
import avorontsov.cells.*;

import java.util.ArrayList;

/**
 * About the Cell Movement Framework. From Version 0.0.6 (roughly) on, every
 * cell should have a CellMovementController applied. Whenever the cell is
 * updated, it calls move(), which in turn apply()s the CellMovementController.
 * The Controller contains a list of MovementBehaviors, sorted by priority. The
 * Controller apply() encapsulates the logic of what behavior to take this
 * update(). For instance, a Grazer might prioritize the behavior "evade",
 * "Predator" over "graze", "Plant". Specific criteria the Controller might
 * consider includes distance, relative speed and size, maybe even health and
 * color. The Controller and its Behaviors provide a high-level API for
 * elucidating the behavior of any implementation of Cell. Finally, the
 * Controller apply() returns a MovementOrder.
 * 
 * @author Andrey Vorontsov
 */
public class CellMovementController {

	ArrayList<MovementBehavior> allBehaviors; // a sorted list of all behaviors, by priority (highest to lowest, ties
												// broken by recency of addition - newer behaviors are higher)

	/**
	 * Instantiates a CellMovementController with no behaviors defined.
	 */
	public CellMovementController() {
		allBehaviors = new ArrayList<MovementBehavior>();
	}

	/**
	 * Adds a new behavior to this Controller with a given behaviorType,
	 * targetCellSpecies, and priority.
	 * 
	 * @param behaviorType      the behavior type to initialize a Behavior with
	 * @param targetCellSpecies the cell species to initialize a Behavior with
	 * @param priority          the priority (ideally from 1 to 10, 1 highest
	 *                          priority)
	 */
	public void addBehavior(String behaviorType, String targetCellSpecies, int priority) {
		addBehavior(new MovementBehavior(behaviorType, targetCellSpecies, priority));
	}

	/**
	 * Adds a new behavior to this Controller with a given priority.
	 * 
	 * @param behavior the behavior to add
	 */
	public void addBehavior(MovementBehavior behavior) {
		int i = 0;
		while (i < allBehaviors.size() && behavior.getPriority() > allBehaviors.get(i).getPriority()) {
			i++;
		}
		allBehaviors.add(i, behavior); // add the behavior at the first index of its priority level (i.e. the first 2),
										// or failing that at the end
	}

	/**
	 * Applies the movement behavior logic encapsulated in the object, selecting a
	 * Cell target and a behavior to use with regard to it, outputting a
	 * MovementOrder that encapsulates this information as well as the elemental
	 * movement direction information
	 * 
	 * @param me the cell asking for its next movement order
	 * @param visibleCells a list of all cells visible to the cell
	 * @return the MovementOrder for the cell to follow in its move()
	 */
	public MovementOrder getNextMovementOrder(Cell me, ArrayList<Cell> visibleCells) {
		// TODO this logic may also have to eventually be abstract and extended
		// ; ultimately this logic should allow for equal priorities
		// TODO this is fairly suboptimal. a good implementation would search for everything at the same time and then act on the highest priority one only (O(n)) while this is O(n^2)
		Cell target = null;
		for (int i=0; i<allBehaviors.size(); i++) {
			switch (allBehaviors.get(i).getBehaviorType()) {
			case "pursue":
				for (Cell c : visibleCells) { // for now, the closest of correct species is chosen
					if (c.getSpecies().equals(allBehaviors.get(i).getTargetCellSpecies()) && (target == null || PetriDish.distanceBetween(target.getX(),
							target.getY(), me.getX(), me.getY()) > PetriDish.distanceBetween(c.getX(), c.getY(), me.getX(), me.getY()))) {
						target = c;
					}
				}
				if (target != null) {// a target was found, catch it
					return new MovementOrder(me, "pursue", target);
				}
				break;
			case "evade": // unimplemented
				break;
			case "hunt": // unimplemented
				break;
			case "graze": // unimplemented
				break;
			case "wander": // for now, if wander is above any other behavior, it will always overpower it (TODO: maybe a default behavior?)
				return new MovementOrder(me, "wander", null);
			default:
				System.out.println("WARNING: " + me + "'s behavior controller failed to produce a movement order.");
				return new MovementOrder(me, "wander", null);
			}
		}
		
		System.out.println("WARNING: " + me + "'s behavior controller failed to produce a movement order.");
		return new MovementOrder(me, "wander", null);
	}
}
