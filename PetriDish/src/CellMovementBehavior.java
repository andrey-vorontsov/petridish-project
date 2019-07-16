
public class CellMovementBehavior {
	// TODO
	// ideas for making this take shape
	// internal priority ranking
	// breaking ties by distance
	// core input method - argument is the visibleCells list
	// ^which simply returns which of several behaviors the cell should take with respect to what target
	// should abstract away the logic of the move() method completely - each cell subclass may no longer need to override it, instead just initializes a behavior object and applies it to itself in the constructor
}
