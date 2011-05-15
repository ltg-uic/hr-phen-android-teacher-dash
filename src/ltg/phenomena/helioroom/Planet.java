package ltg.phenomena.helioroom;



public class Planet {
	
	private String name = null;
	private String color = null;
	private String colorName = null;
	private int classOrbitalTime = -1;
	private double speed = -1;
	private Degree startPosition = null; 
	private Degree currentPosition = null;
	// Graphic data
	private double x = 0;
	private double y = 0;
	
	
	public Planet(String name, String color, String colorName, int classOrbitalTime, int startPosition) {
		this.name = name;
		this.color = color.replaceAll("0x", "#");
		this.colorName = colorName;
		this.classOrbitalTime = classOrbitalTime;
		this.speed = 6 / (double) classOrbitalTime;
		this.startPosition = new Degree(startPosition);
		this.currentPosition = this.startPosition;
	}


	public String getName() {
		return name;
	}


	public String getColor() {
		return color;
	}
	
	
	public String getColorName() {
		return colorName;
	}


	public int getClassOrbitalTime() {
		return classOrbitalTime;
	}	

	
	public Degree getStartPosition() {
		return startPosition;
	}
	
	public float getX() {
		return (float) this.x;
	}

	public float getY() {
		return (float) this.y;
	}
	
	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}


	public void computePosition(double timeDelta) {
		currentPosition = new Degree(startPosition.getValue() - speed * timeDelta);
		x = (double) Math.cos(Math.toRadians(currentPosition.getValue()));
		y = (double) Math.sin(Math.toRadians(currentPosition.getValue()));
	}


	public String getCurrentPosition() {
		return String.valueOf(currentPosition.getValue());
	}
}
