Version 0.0.1
Started working on the project.
First functional version.
Underlying application structure, graphics flow, data structures, and simulation flow.
Cells created, can move, eat each other, and die with basic behavior.
EDIT: Versions 0.0.1 through 0.0.4 followed each other in relatively quick succession with baseline elements and early bugs being handled.

Version 0.0.2
Bug fix: Cells dying multiple times, two cells simultaneously eating each other, related issues.

Version 0.0.3
Implemented backend for cell reproduction, but the feature has not been started.
Rewrote graphics tracking code to allow safe manipulation (esp. creation and destruction of cells).
Bug fix: Persistent indexing errors when adding new cells.
Known issue: The graphics thread has started to fall behind the simulation thread, causing lag.

Version 0.0.4
Cells can now grow from gathered energy.
If under starvation conditions, cells will shrink.
After enough energy is gathered, cells can divide.
Cells now push each other away to make room; this is a tentative feature.
For demonstration purposes Herbivore cells now have a better food finding behavior.
(Herbivores existed before but behaved differently. In earlier versions they were cannibals exclusively)
Reorganized and partially rewrote graphics code; result is slower but more robust.
Bug fix: Indexing errors from new cells, this time for real.

Version 0.0.4.1
Herbivore movement code has gone through an iteration.
Cells in general can now remember their selected targets and move towards them in a straight line.
Herbivores specifically now select a random location out of their vision range as a target if stranded far from any food.
Some code cleanup.

Version 0.0.4.2
After an insight, rewrote the graphics update code once again, with performance likely comparable to the original.
Iterated on the herbivore movement code again.
Herbivores now steer away from the edge rather than getting stuck on it.

Version 0.0.4.3
Rewrote the herbivore edge evasion code and tweaked the overall cell targeting code.

Version 0.0.4.4
Renamed Herbivores to Grazers.
Added Predators with some basic behavior.
Predators can eat Grazers and agars but don't have specialized hunting behavior.
Predators have customized movement speed, growth and reproduction stats.
Rewrote the squishing behavior, such that cells of the same variety cannot overlap.
Known issue: Cells get stuck against each other trying to eat the same target.

Version 0.0.5
Introduced a proper thread synchronization system.
This laid the groundwork for frame rate tracking, simulation speed and pause controls.
Bug fix: The graphics thread failing to draw every update tick at high tick rates.
Known issue: Sometimes, very rarely, the graphics thread still has indexing exceptions. Cause unknown.

Version 0.0.5.1
Improved robustness of graphics handling system to enable Rectangular Plant graphic.
Plants introduced.
Plants slowly grow with time without needing to eat.
Plants are modified to squish all other cells away, not just other Plants.
They reproduce similarly to other cells, and their squishing pushes their offspring extra far away to make space.

Version 0.0.5.2
Improved robustness of cell vision and hit detection code, facilitating future changes.

Version 0.0.5.3
Started work on the cell movement framework, laying the keystones of the design.

Version 0.0.5.4
Completed the cell movement framework.
However, still need to fill in code to allow cells to
1. choose between the new abstract behaviors (largely complete but needs generalization)
2. execute those behaviors (the code for target pursuit and random wandering is largely complete)

Version 0.0.5.5
The rare crash from Version 0.0.5 has not reoccured under any conditions; the issue is considered to be resolved.
As an aside, centralized all random number draws to a single Random java object, theoretically allowing a given simulation to be recreated from a seed.
As another aside, repackaged and organized the entire project code, for proper encapsulation.
Much of the coding to incorporate the framework is complete;
Grazers are ready to use their old target pursuit and wandering code, organized under the new system - but I haven't pulled the trigger yet.
Predators are still fully on the old system but should be trivial to copy-paste over.

Version 0.0.5.5a
Pulled the trigger on moving the Grazers over to the new system.
Not without some bugginess, but the mechanism of actually setting the behavior set of a cell is now four lines in the constructor!

Version 0.0.5.6
Finished working on the cell movement framework's behavior selection code.
Pulled the trigger on Predators.
Now both Grazers and Predators have highly abstract internal representations of their movement behavior.
Known issue: Predators now brazenly target Grazers that are too big for them to eat.

Version 0.0.5.7
Fully set up Grazers and Predators in the new framework.
Evasion and hunting are implemented as placeholders
Expanded functionality of the Behavior data type to allow relative controls (i.e. relative size of predator and prey cells)
Known issue: The rare crash has reoccured. A solution is identified.
Known issue: Cells become invisible without a death message.

Version 0.0.5.7a
Bug fix: Invisible cells, the cause was a debug printing oversight, not an underlying issue.
Bug fix: Hunting behavior now works as intended, the cause was a commented line of code.

Version 0.0.5.8
Bug fix: Evasion behavior now works as intended
Wandering code has been reinstated to a similar way to how it used to work.
Cells now remember the behavior they had on the previous turn.
Bug fix: Cells getting stuck against each other has been lessened but not resolved by the change to wandering code.
Behaviors now allow a cell to verify that it has enough energy to try it.
Predators now only lunge after prey if they have the energy.
Grazers now try to stay out of lunging range, but will ignore predators that are too far to threaten them.
Known issue: Sometime along the way we have caused cells to sometimes fail to prioritize the closest agar.

Version 0.0.6
Rewrote the petri dish dimension limitations to allow rectangular dishes for debug purposes.
Known issue: Strange jitteriness of cells - either a threading issue, a problem with squishing, or low frame rate.

Version 0.0.6.1
Rewrote the thread synchronization system, with full infrastructure for future frame rate tracking, etc. now in place.
Bug fix: The rare indexing crash should also now be fixed, through use of a buffer array (at a substantial performance cost).

Version 0.0.6.1a
Bug fix: Cells now prioritize targets by proximity again.

Version 0.0.6.2
Working on code cleanup.
Eating reimplemented; cells now eat INSTEAD of moving on a given tick, and can only eat a single target per turn.
We paid a slight performance price for increased modularity, will make an effort to optimize the system.
Grazers now don't wander while a plant is in sight, nor do they expend energy; this is expected, because the grazing behavior is undefined.
As a test, I gave plants and agars a wandering behavior. As expected, they did not move because their friction value is 0 (they also do not spend energy by the current model, which will need to be replaced).
All in all, the reimplementation of eating, moving, and energy consumption mechanics seems to have gone off without a hitch.

Version 0.0.6.3
Transferred reproduction into the same framework.
The result is a little unlike what we used to have but appears to work well.
Plants now do not reproduce into clusters of more than three (this functionality is not yet enabled).
Code cleanup is underway; slightly optimized the core simulation code.
Squishing temporarily disabled for testing purposes.

Version 0.0.6.3a
This version is nonfunctional.
Code cleanup is still underway with some slight optimizations and robustness improvements coming along.

Version 0.0.6.4
Cleanup completed for a lot of mucky code, radically improving clarity.
This version is once again functional.
Squishing should be re-enabled.
The energy mechanic was temporarily disabled (cells spend no energy to move/act).
Known issue: Cells do nothing except reproduce constantly, rapidly creating thousands of cells.

Version 0.0.6.5
Behaviors now support an energy cost for applying that behavior.
In sight of this, re-implemented energy costs for all cell behaviors.
Energy is now represented as a double value to allow fractional energies.
Known issue: Reproduction energy cost isn't applied the way I want.
Known issue: Plants need their custom reproduction scheme re-implemented. Energy costs currently make the parent instantly die after reproducing.

Version 0.0.6.6
This version is once again properly polished and shippable!
Isolated reproduction code back into a separate method, undermining the organization plan but simplifying accessibility to that code.
Bug fix: Reproduction energy costs.
Bug fix: Plant children somehow banish their parents to the shadow realm (NaN, NaN)
Plant reproduction now fully implemented (cluster size capped at 3, proper squishing behavior).
Known - shippable issue: There is a 1 in 7e308 chance, whenever a cell is born, that the parent will be squished to coordinates (NaN, NaN).
Known issue: Plants sometimes somehow get squished out of the way of a cell (they should always be the ones squishing).
Known issue: Cells get stuck trying to get through Plants obstructing their path.

Version 0.0.6.7
Implemented the grazing behavior, with some other changes made to accomodate the change.
Grazers now approach Plants and 'nibble' on them to slowly leech energy away.
As a prerequisite, implemented backend functionality to allow certain behaviors to have 'cooldowns' - only usable every x ticks.
Mobile cells also go to sleep when starving to save energy.
Rebalanced a few stats here and there to make the simulation more interesting to watch.
Known issue: Grazers look pretty awkward piling on to and spinning around Plants. 
Known issue: Sleeping cells vibrate.
Known issue: Cells clip into each other and spaz out.

Version 0.0.6.7a
Messed with some cell stats to improve simulation balance/increased playability.

Version 0.0.6.7b
Bug fix: Sleeping cells vibrate.
Bug fix: Cells get stuck trying to get through obstructing Plants.
Messed with balance some more (Predators are still dying out too fast).


KNOWN ISSUES

Cells get stuck against each other trying to eat the same target; increasingly rare in current versions.
Strange jitteriness of cells - cause unknown, inconsistent.
Plants sometimes get squished out of the way of a moving cell (very rare).
Grazers look pretty awkward piling on to and spinning around Plants.
Cells clip into each other and spaz out.

NOTES

REGARDING THE BEHAVIOR SYSTEM
ActionOrder.generateMovementVector() must be updated for new movement orders
Behavior.Behavior() must have its behavior category assignment and behavior type validation updated
Any new properties need to be added as fields to Behavior together with getters and setters
CellBehaviorController.getNextActionOrder() must be updated with any new property checks
Cell.act() contains the meat, actually enforcing the ActionOrder

USAGE OF SIZE VARIABLE
The size variable currently has a dual functionality; it serves as the radius of the cell circle, and also as the abstract representation of the cell's physical mass.
The desired implementation is for the radius to be a separate variable from the mass (which should be proportionate to the circle's area).
To extricate these functionalities, a list of usages will be compiled, then the variable appropriately refactored.
The old size variable will become the radius. A new variable "mass" will be calculated from it, and updated to reflect energy consumption.
The radius will be recalculated from mass on a call to getGraphic().

ROADMAP (Current: 0.0.6.7)

Features for 0.0.6.8
	Rework size calculation
		Current size variable refactored as "radius" and represents only the hitbox
		New size variable refers to the area of the circle

Features for 0.0.6.9
	Propagate size calculation changes
		Cells should have correctly calculated speed
			(each MovementOrder should have an instead associated FORCE, based on mass * acceleration)
				(mass proportional to size = area)
				(cells have a field controlling energy efficiency considering force exerted)
		Tweak vision range formula

Release 0.0.7

Features for 0.1.0
	Fix known issues:
		Cells stuck against each other trying to reach the same target
		Strange jitteriness of cells - possibly caused by squishing function
	Basic GUI
		Simulation speed control
			Slider to control speed
			Pause/unpause button
	Simple animations for plants
	Reasonably interesting initial spawning setup
	Reasonably balanced stats for the demo creatures
		Cells die from age and drop agars
	
Features for 1.0.0
	GUI
		Mouse interaction with petri dish
			Can create cells of any variety
			Can select a cell
				to see its stats
				to make changes to it
					kill the cell
					clone it
					etc.
				Can select many cells
			Can drag a cell to a new location in the dish
		Simulation speed control
			Robust frame rate tracking
			Pause/unpause button has full interaction while paused
		Saving to file
			Configurable variables must be evaluated and moved out
			Settings/config file
			Simulation state save file
			Loading from files
		Other controls
			Light intensity - plant growth rate
			Various cell stats TBD
			Mutation rate/genetics control
			TBD
	Cell behaviors
		Cells remember locations of recently seen objects
		Cells remember 'what they were doing'
		Allow and optimize equal prioritization of behaviors
			Esp. eating
		Improve overall robustness by allowing multiple acceptable behaviors to occur
			Esp. grow and starve - currently implemented as custom behaviors
	Genetic heredity
		Basics
			Data structure
			Color/superficial features
			Stats
			Behaviors
		Mutation
			TBD
		Evolution
			Cells develop behaviors dynamically (powergoal)
			Cells speciate (powergoal)