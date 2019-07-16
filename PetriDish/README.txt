Version 0.0.1
Started working on the project.
First functional version.
Underlying application structure, graphics flow, data structures, and simulation flow.
Cells created, can move, eat each other, and die with basic behavior.

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


ROADMAP (Current: 0.0.5.2)

Cell Movement Framework concept.
Every cell should have a CellMovementController applied.
Whenever the cell is updated, it calls move(), which in turn apply()s the CellMovementController.
The Controller contains a list of MovementBehaviors, sorted by priority.
Behaviors are completely abstract and simply contain the information - how a given species should be treated.
The Controller apply() encapsulates the logic of what behavior to take this update().
For instance, a Grazer might prioritize the behavior "evade", "Predator" over "graze", "Plant".
Specific criteria the Controller might consider includes distance, relative speed and size, maybe even health and color.
The Controller and its Behaviors provide a high-level API for elucidating the behavior of any implementation of Cell.
Finally, the Controller apply() returns a MovementOrder, which contains the specific information of what action to take on this tick.
For instance, a Grazer might have a MovementOrder along the lines of "evade", "Predator #123" for this tick.
The MovementOrder encapsulates the logic of enforcing this behavior.
As a baseline, it should be able to produce a CellMovementVector from the directive given by the Controller.
Eventually, some of this functionality can be expanded.
		
Features for 0.0.5.4
	Cell movement framework completion as described above.
		
Features for 0.0.5.5
	Incorporate framework back into existing behaviors
		Pursuit of target (grazers, predators --> agars, agars and grazers)
	Incorporate framework into new behaviors
		Evasion of target (grazers --> predators)
		Hunting of target (predators --> grazers)
		Grazing of target (grazers --> plants)
	
Features for 0.0.6
	Evaluate and start moving configurable variables out
		Rewrite petri dish dimension limitations
	At least one 0.1.0 feature
	Consider reproduction behavior framework
		Plant maximum density
	
Launch 0.0.6
	
Features for 0.0.7

Features for 0.1.0
	Fix known issues: Cells stuck against each other trying to reach the same target. Graphics indexing crash.
	Better size; based on area, not radius
		Adjust vision range formula based on size
		Slower movement based on size
	Basic GUI
		Simulation speed control
			Slider to control speed
			Pause/unpause button
	Refine thread sync system - time simulation thread as well
	Simple animations for plants
	Reasonably interesting initial spawning setup
	Reasonably balanced stats for the demo creatures
		Cells die from age and drop agars
	
Features for 1.0.0
	GUI
		Mouse interaction with petri dish
			Can create cells of any variety
			Can select a cell and see its stats
			Can drag a cell to a new location in the dish
			Can kill a cell
		Simulation speed control
			Robust frame rate tracking
			Pause/unpause button has full interaction while paused
		Saving to file
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
	Genetic heredity
		Basics
			Data structure
			Color/superficial features
			Behaviors
		Mutation
			TBD
		Evolution
			Cells develop behaviors dynamically (powergoal)
			Cells speciate (powergoal)