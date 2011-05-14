/*
 * Created Apr 28, 2011
 */
package ltg.phenomena.helioroom;

/**
 * TODO Description
 *
 * @author Gugo
 */
public class Degree {
	
	// Always between 0 and 359
	private float value = -1;
	
	
	public Degree(float value) {
		this.value = normalize(value);
	}
	
	
	private float normalize(float v) {
		// Normalize it
		v = v % 360;
		// Change sign if necessary
		if(v<0)
			return 360+v;
		return v;
	}
	
	
	public Degree add(Degree v) {
		return new Degree(value + v.value);
	}
	
	
	/**
	 * Subtracts the value of the parameter from the value of this degree.
	 */
	public Degree sub(Degree v) {
		return new Degree(value - v.value);
	}
	
	
	/** 
	 * Returns true if the value of this degree falls inside the arc measured
	 * counter clockwise from begin to end.
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	public boolean insideCCWArc(Degree begin, Degree end) {
		Degree be, bc, ce = null;
		be = begin.sub(end);
		bc = begin.sub(this);
		ce = this.sub(end);
		if (bc.getValue() > 0f && bc.getValue() < be.getValue() 
				&& ce.getValue() > 0f && ce.getValue() < be.getValue())
			return true;
		return false;
	}
	
	
	public float getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}


}
