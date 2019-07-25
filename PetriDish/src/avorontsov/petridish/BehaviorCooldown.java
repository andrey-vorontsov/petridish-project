package avorontsov.petridish;

public class BehaviorCooldown {
	
	private int ticksRemaining; // the number of update ticks needed before a cell can use its behavior again
	private Behavior behavior; // the behavior the cell cannot use until this cooldown expires
	
	/**
	 * Sets a behavior cooldown.
	 * 
	 * @param ticksRemaining
	 * @param behaviorType
	 */
	public BehaviorCooldown(int ticksRemaining, Behavior behavior) {
		this.ticksRemaining = ticksRemaining;
		this.behavior = behavior;
	}
	
	/**
	 * Sets a behavior cooldown using the behavior's default cooldown.
	 * 
	 * @param ticksRemaining
	 * @param behaviorType
	 */
	public BehaviorCooldown(Behavior behavior) {
		ticksRemaining = behavior.getCoolDown();
		this.behavior = behavior;
	}
	
	/**
	 * Decrements the ticksRemaining and reports whether the cooldown is finished.
	 * 
	 * @return true only if this cooldown is expired
	 */
	public boolean update() {
		ticksRemaining--;
		return ticksRemaining <= 0;
	}

	/**
	 * @return the ticksRemaining
	 */
	public int getTicksRemaining() {
		return ticksRemaining;
	}

	/**
	 * @return the behavior
	 */
	public Behavior getBehavior() {
		return behavior;
	}

	/**
	 * @param ticksRemaining the ticksRemaining to set
	 */
	public void setTicksRemaining(int ticksRemaining) {
		this.ticksRemaining = ticksRemaining;
	}

	/**
	 * @param behavior the behavior to set
	 */
	public void setBehavior(Behavior behavior) {
		this.behavior = behavior;
	}
	
}
