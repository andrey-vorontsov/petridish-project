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
 * Controller apply() returns a MovementOrder. TODO this description is outdated and overrated (cell behaviors activated)
 * 
 * @author Andrey Vorontsov
 */
public class CellBehaviorController {

	ArrayList<Behavior> allBehaviors; // a sorted list of all behaviors, by priority (highest to lowest, ties
												// broken by recency of addition - newer behaviors are higher)

	/**
	 * Instantiates a CellMovementController with no behaviors defined.
	 */
	public CellBehaviorController() {
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
	public ActionOrder getNextActionOrder(Cell me, ArrayList<Cell> visibleCells) {
		// TODO this logic may also have to eventually be abstract and extended
		// ; ultimately this logic should allow for equal priorities
		
		for (int i=0; i<allBehaviors.size(); i++) {
			// load the next behavior to consider
			Behavior currBehavior = allBehaviors.get(i);
			String currBehaviorType = currBehavior.getBehaviorType();
			
			// if the behavior requires a target, choose one
			// otherwise leave the target as null (currently: used for wander behavior)
			
			if (currBehavior.getThisCellMinEnergy() <= me.getEnergy()) {
				if (currBehavior.requiresTarget()) {
						
					// check all visible cells to find closest cell matching the behavior's targeting specifications
					Cell target = null;
					double distanceToTarget = Double.MAX_VALUE; // used for comparison to find closest matching target. safe as long as the petri dish is not insanely large (and other stuff will break before then)
					for (Cell c : visibleCells) {
						
						// load some useful values for the comparisons
						double distanceToCell = PetriDish.distanceBetween(c.getX(), c.getY(), me.getX(), me.getY());
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
							
							// additional range checks based on hitboxes (NOT redundant with max distance!) (used by eat and squish behaviors)
							
							// if either hitbox condition is both set and fulfilled
							if ((currBehavior.doesTargetCellHaveToBeEngulfed() && distanceToCell < me.getSize())
									|| (currBehavior.doesTargetCellHaveToBeTouching() && distanceToCell < (me.getSize() + c.getSize()))
									// or neither of them is set
									|| (!currBehavior.doesTargetCellHaveToBeEngulfed() && !currBehavior.doesTargetCellHaveToBeTouching())) {
								// only THEN can we choose the cell as a target
								target = c;
								distanceToTarget = distanceToCell;
							}
						}
					}
					if (target != null) {// a target was found, generate the appropriate order - applies to hunt, evade, and pursue
						return new ActionOrder(me, currBehavior, target);
					} // no target found, check the next behavior
					
				} else { // handle no-target behaviors here (current only wander, which is just passed straight on)
					return new ActionOrder(me, currBehavior, null); // technically could be the same statement as the targeted one since target is null anyway but this is clearer I hope
				}
			} // couldn't muster the energy, try the next behavior
		}
		
		System.out.println("WARNING: " + me + "'s behavior controller failed to produce an action order.");
		return new ActionOrder(me, new Behavior("wander", 0), null); // a default wander behavior with a sentinel priority value
	}
}
