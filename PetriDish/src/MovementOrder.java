
public class MovementOrder { // TODO duplicated from behavior temporarily
	// this class will actually generate the movement vector for the cell

	private String behaviorType;
	private String targetCellSpecies;
	
	public MovementOrder(String behaviorType, String targetCellSpecies) {
		this.behaviorType = behaviorType;
		this.targetCellSpecies = targetCellSpecies;
	}

	/**
	 * @return the behaviorType
	 */
	public String getBehaviorType() {
		return behaviorType;
	}

	/**
	 * @return the targetCellSpecies
	 */
	public String getTargetCellSpecies() {
		return targetCellSpecies;
	}
		
}
