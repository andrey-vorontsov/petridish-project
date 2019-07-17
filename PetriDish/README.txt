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

ROADMAP (Current: 0.0.5.8)

Features for 0.0.6
	Fix cell misprioritizing their targets by proximity (multiple possible causes, evaluate)
	Fix the indexing crash
		Prerequisite: Refine thread sync system - time simulation thread
	Rewrite petri dish dimension limitations
	
Launch 0.0.6
	
Features for 0.0.7
	Work eating, grazing, sleeping, and mating behaviors into the generalized behavior framework
		New behaviors
			Plant maximum density checks
			Grazers grazing
			Cells sleeping to save energy
		Old behaviors (reimplement)
			Eating
			
	Rework size calculation
		Modify function of MovementOrder method
			Calculate energy consumption based on size (later area) and vector magnitude
				Cells should have a field controlling energy efficiency (per velocity unit)
		Current size variable should represent area of a circle
		Cells should have different speed and energy consumption with larger size (consider mass)
		Tweak vision range formula

Features for 0.1.0
	Fix known issues: Cells stuck against each other trying to reach the same target.
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
			Can select a cell and see its stats
			Can drag a cell to a new location in the dish
			Can kill a cell
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