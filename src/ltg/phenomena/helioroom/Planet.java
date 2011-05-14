package ltg.phenomena.helioroom;

public class Planet {
	
	private String name = null;
	private String color = null;
	private String colorName = null;
	private int classOrbitalTime = -1;
	private int startPosition = 0;
	
	
	public Planet(String name, String color, String colorName, int classOrbitalTime, int startPosition) {
		this.name = name;
		this.color = color;
		this.colorName = colorName;
		this.classOrbitalTime = classOrbitalTime;
		this.startPosition = startPosition;
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

	
	public int getStartPosition() {
		return startPosition;
	}
}
