package avorontsov.petridish;

/**
 * @author Andrey Vorontsov
 * 
 *         A convenience class to package movement data for a cell and
 *         facilitate calculations for moving towards a target.
 */
public class CellMovementVector {

	// vector info
	double xComponent;
	double yComponent;
	double magnitude;

	/**
	 * Create a vector.
	 * 
	 * @param xComponent of the vector
	 * @param yComponent of the vector
	 * @param magnitude  of the vector
	 */
	public CellMovementVector(double xComponent, double yComponent) {
		this.xComponent = xComponent;
		this.yComponent = yComponent;
		this.magnitude = Math.sqrt(Math.pow(xComponent, 2) + Math.pow(yComponent, 2));
	}

	/**
	 * @return a unit vector from this vector
	 */
	public CellMovementVector getUnitVector() {
		return new CellMovementVector(xComponent / magnitude, yComponent / magnitude);
	}

	/**
	 * Scales the vector. A call to this method with an argument of 1 is equivalent
	 * to a call to getUnitVector()
	 * 
	 * @param scalar the scalar by which to adjust this vector
	 * @return a scaled vector from this vector
	 */
	public CellMovementVector getScaledVector(int scalar) {
		return new CellMovementVector(getUnitVector().getXComponent() * scalar,
				getUnitVector().getYComponent() * scalar);
	}

	/**
	 * @return the xComponent
	 */
	public double getXComponent() {
		return xComponent;
	}

	/**
	 * @param xComponent the xComponent to set
	 */
	public void setXComponent(double xComponent) {
		this.xComponent = xComponent;
	}

	/**
	 * @return the yComponent
	 */
	public double getYComponent() {
		return yComponent;
	}

	/**
	 * @param yComponent the yComponent to set
	 */
	public void setYComponent(double yComponent) {
		this.yComponent = yComponent;
	}

	/**
	 * @return the magnitude
	 */
	public double getMagnitude() {
		return magnitude;
	}

	/**
	 * @param magnitude the magnitude to set
	 */
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<" + xComponent + ", " + yComponent + ">";
	}

}
