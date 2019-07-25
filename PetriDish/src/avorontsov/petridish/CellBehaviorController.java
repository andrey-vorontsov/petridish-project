package avorontsov.petridish;
import avorontsov.cells.*;

import java.util.ArrayList;

/**
 * About the Cell Behavior Framework. From Version 0.0.6 (roughly) on, every
 * cell should create, configure, and apply a CellBehaviorController in its
 * constructor. Whenever the cell is updated, it ultimately calls
 * getNextActionOrder on its CellBehaviorController to find out what it needs to
 * do next. The Controller contains a list of Behaviors, sorted by priority.
 * getNextActionOrder() encapsulates the logic of what behavior to take this
 * update(). For instance, a Grazer might prioritize the behavior "evade"
 * "Predator" over "graze" "Plant". Specific criteria the Controller might
 * consider includes distance, relative speed and size, maybe even health and
 * color. The Controller and its Behaviors provide a high-level API for
 * elucidating the behavior of any implementation of Cell. Eventually, the
 * Controller gives the Cell an ActionOrder, representing the exact behavior
 * which was chosen and relevant details (such as a CellMovementVector for MOVE
 * category behaviors).
 * 
 * @author Andrey Vorontsov
 */
public class CellBehaviorController {

	private ArrayList<Behavior> allBehaviors; // a sorted list of all behaviors, by priority (highest to lowest, ties
												// broken by order of addition - older is higher)

	/**
	 * Instantiates a CellBehaviorController with no behaviors defined. If queried
	 * for an ActionOrder, it will provide a default "wander" order.
	 */
	public CellBehaviorController() {
		allBehaviors = new ArrayList<Behavior>();
	}

	/**
	 * Adds a new behavior to this Controller.
	 * 
	 * @param behavior the behavior to add
	 */
	public void addBehavior(Behavior behavior) {
		int i = 0;
		while (i < allBehaviors.size() && behavior.getPriority() >= allBehaviors.get(i).getPriority()) {
			i++;
		}
		allBehaviors.add(i, behavior); // add the behavior at the last index of its priority level
	}

	/**
	 * Applies the behavior logic encapsulated in the object, selecting a behavior
	 * to use and a Cell target if one is needed outputting an ActionOrder that
	 * encapsulates this information.
	 * 
	 * @param me           the cell asking for its next movement order
	 * @param visibleCells a list of all cells visible to the cell
	 * @return the ActionOrder for the cell to follow on this update()
	 */
	public ActionOrder getNextActionOrder(Cell me, ArrayList<Cell> visibleCells) {

		// as a temporary measure, we simply consider every behavior in order of
		// priority (TODO support for considering equal priority behaviors
		// concurrently)
		for (int i = 0; i < allBehaviors.size(); i++) {
			// load the next behavior to consider
			Behavior currBehavior = allBehaviors.get(i);

			// first check : does this cell match the conditions to take this behavior?

			if (currBehavior.getThisCellMinEnergy() <= me.getEnergy() // this cell is in energy spec
					&& currBehavior.getThisCellMaxEnergy() >= me.getEnergy()

					&& currBehavior.getThisCellMinSize() <= me.getSize() // this cell is in size spec
					&& currBehavior.getThisCellMaxSize() >= me.getSize()) {

				// second check : do the environmental conditions match?

				// A: only calculate relevant stats if the behavior demands it
				// B: only mark a failure if condition failed
				boolean populationCheckPassed = true;

				if (currBehavior.getMaximumVisiblePopulation() != Integer.MAX_VALUE) {
					// yes, the max population limit was set

					// so we need to count them up
					int visiblePopulationCount = 1; // incl. myself
					for (Cell c : visibleCells) {
						if (c.getSpecies().equals(me.getSpecies())) {
							visiblePopulationCount++;
						}
					}
					if (visiblePopulationCount >= currBehavior.getMaximumVisiblePopulation()) {
						populationCheckPassed = false; // the population density is too high!
															// can't take this behavior
					}
				}

				// did we fail any environmental checks?
				if (populationCheckPassed) {

					// third check : does this cell need a target? if yes, go on to target search
					// code
					if (currBehavior.requiresTarget()) {

						// fourth check : check all visible cells to find closest cell matching the
						// behavior's targeting specifications
						Cell target = null;
						double distanceToTarget = Double.MAX_VALUE; // used for comparison to find closest matching
																	// target cell
						for (Cell c : visibleCells) {

							// load some useful values for the comparisons
							double distanceToCell = PetriDish.distanceBetween(c.getX(), c.getY(), me.getX(), me.getY());
							int cellsRelSize = me.getSize() - c.getSize(); // positive when this cell is bigger

							// the big if. checks all the behavior's conditions
							if (c.getSpecies().equals(currBehavior.getTargetCellSpecies()) // target matches species
									&& c.getSize() >= currBehavior.getTargetCellMinSize() // size
									&& c.getSize() <= currBehavior.getTargetCellMaxSize() // constraints

									&& distanceToCell >= currBehavior.getTargetCellMinDistance() // distance
									&& distanceToCell <= currBehavior.getTargetCellMaxDistance() // constraints
									
									&& cellsRelSize >= currBehavior.getTargetCellMinRelSize() // relative size
									&& cellsRelSize <= currBehavior.getTargetCellMaxRelSize() // constraints

									&& (distanceToTarget > distanceToCell)) { // finally, target is closer than any
																				// matching previous target

								// additional range checks based on hitboxes (NOT redundant with max distance!)

								// if either hitbox condition is both set and fulfilled
								if ((currBehavior.doesTargetCellHaveToBeEngulfed() && distanceToCell < me.getSize())
										|| (currBehavior.doesTargetCellHaveToBeTouching()
												&& distanceToCell < (me.getSize() + c.getSize()))
										// or neither of them is set
										|| (!currBehavior.doesTargetCellHaveToBeEngulfed()
												&& !currBehavior.doesTargetCellHaveToBeTouching())) {
									// only THEN can we choose the cell as a target

									target = c;
									distanceToTarget = distanceToCell;
								}
							} // no? try the next candidate cell
						} // complete target search loop
						
						if (target != null) {// a target was found, generate the appropriate order
							return new ActionOrder(me, currBehavior, target);
						}
						// if target still == null here, no target was found, the behavior cannot be
						// used. try the next highest one

						// end target search code
					} else { // if no target required for this behavior
						// no more checks are necessary
						return new ActionOrder(me, currBehavior, null);

					} // a target was required but no target was found, fourth check not satisfied

				} // this behavior's second check conditions were not satisfied

			} // this behavior's first check conditions were not satisfied, try the next highest priority 
		}

		// loop completed, no ActionOrder produced

		System.out.println("WARNING: " + me + "'s behavior controller failed to produce an action order.");
		return new ActionOrder(me, new Behavior("sleep", 0), null); // a default ActionOrder
	}
}
