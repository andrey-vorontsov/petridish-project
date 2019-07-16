import java.util.ArrayList;

public class CellMovementController {
	
	ArrayList<MovementBehavior> allBehaviors;
	
	public CellMovementController(ArrayList<MovementBehavior> behaviors) {
		allBehaviors = behaviors;
	}
	
	public CellMovementController() {
		allBehaviors = new ArrayList<MovementBehavior>();
	}
	
	public void addBehavior() {
		
	}

	/**
	 * Applies the movement behavior logic encapsulated in the object, updating the cell's behavior string and returning the its target cell
	 * Warning: This method has the intended side effect of updating the Cell's behavior string, identifying what behavior must be taken with respect to the target Cell
	 * 
	 * @return the Cell which is the target of the selected behavior, if the behavior requires a target; otherwise, returns null
	 */
	public Cell apply(Cell me, ArrayList<Cell> visibleCells) {
		
		return null;
	}
}
