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

	ArrayList<Behavior> allBehaviors; // a sorted list of all behaviors, by priority (highest to lowest, ties
												// broken by recency of addition - newer behaviors are higher)

	/**
	 * Instantiates a CellMovementController with no behaviors defined.
	 */
	public CellMovementController() {
		allBehaviors = new ArrayList<Behavior>();
	}

	/**
	 * Adds a new behavior to this Controller with a given priority.
	 * 
	 * @param behavior the behavior to add
	 */
	public void addBehavior(Behavior behavior) {
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
		// TODO this is extremely suboptimal. a good implementation would search for everything at the same time and then act on the highest priority one only (O(n)) while this is O(n^2)
		
		Cell target = null;
		for (int i=0; i<allBehaviors.size(); i++) {
			// load the next behavior to consider
			Behavior currBehavior = allBehaviors.get(i);
			String currBehaviorType = currBehavior.getBehaviorType();
			
			// ignore the behavior if it is not a movement category behavior
			if (currBehavior.getBehaviorCategory().equals("MOVE")) {
				
				// first, if the behavior is to wander, then no target needs to be considered
				if (currBehaviorType.equals("wander"))
					return new MovementOrder(me, "wander", null);

				// check all visible cells to find closest cell matching the behavior's targeting specifications
				for (Cell c : visibleCells) {
					
					// load some useful values for the comparisons
					double distanceToCell = PetriDish.distanceBetween(c.getX(), c.getY(), me.getX(), me.getY());
					double distanceToTarget = Double.MAX_VALUE; // used for comparison to find closest matching target. safe as long as the petri dish is not insanely large (and other stuff will break before then)
					int cellsRelSize = me.getSize() - c.getSize(); // positive when this cell is bigger
					double cellsRelVel = 0; // TODO
					
					// the ultimate if statement. checks all the behavior's conditions
					if (c.getSpecies().equals(currBehavior.getTargetCellSpecies()) // target matches species
							&& c.getSize() >= currBehavior.getTargetCellMinSize() // target is within size constraints
							&& c.getSize() <= currBehavior.getTargetCellMaxSize()
							
							&& distanceToCell > currBehavior.getTargetCellMinDistance() // target is within distance constraints
							&& distanceToCell < currBehavior.getTargetCellMaxDistance()
							
							&& cellsRelSize >= currBehavior.getTargetCellMinRelSize() // target is within relative size constraints
							&& cellsRelSize <= currBehavior.getTargetCellMaxRelSize()
							
							&& cellsRelVel >= currBehavior.getTargetCellMinRelVelocity() // target is within relative velocity constraints
							&& cellsRelVel <= currBehavior.getTargetCellMaxRelVelocity()
							
							&& (target == null || distanceToTarget > distanceToCell)) { // finally, target is closer than any matching previous target
						target = c;
						distanceToTarget = PetriDish.distanceBetween(target.getX(), target.getY(), me.getX(), me.getY());
					}
				}
				if (target != null) {// a target was found, generate the appropriate order
					return new MovementOrder(me, currBehaviorType, target);
				} // no target found, check the next behavior
			}
		}
		
		System.out.println("WARNING: " + me + "'s behavior controller failed to produce a movement order.");
		return new MovementOrder(me, "wander", null);
	}
}
