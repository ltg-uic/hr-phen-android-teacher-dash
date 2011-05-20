package ltg.phenomena.helioroom;

import java.util.List;



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
	private String nextWindow = null;
	private int secToNextWin = -2;
	
	
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
	
	public String getNextWin() {
		return nextWindow;
	}
	
	public String timeToNextWin() {
		if (secToNextWin==-1)
			return "-";
		String s = "";
		if (secToNextWin/60<1) {
			// just seconds
			s = secToNextWin + "s";
		} else if (secToNextWin/3600<1) {
			// minutes and seconds
			int min = (int) Math.floor(secToNextWin/60);
			int secs = secToNextWin % 60;
			s = min + "min " + secs +"s";
		} else if (secToNextWin/86400<1) {
			// hours, minutes and seconds
			int hours = (int) Math.floor(secToNextWin/3600);
			int min = secToNextWin % 3600;
			min = (int) Math.floor(min/60);
			int secs = secToNextWin % 60;
			s = hours + "h " + min + "min " + secs +"s";
		} else {
			int days = (int) Math.floor(secToNextWin/86400);
			int hours = secToNextWin % 86400;
			hours = (int) Math.floor(hours/3600);
			int min = secToNextWin % 3600;
			min = (int) Math.floor(min/60);
			int secs = secToNextWin % 60;
			s = days + "d " + hours + "h " + min + "min " + secs +"s";
		}
		return s;
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
		return String.format("%.3f",currentPosition.getValue());
	}


	public String getOrbitTime() {
		String s = "";
		if (classOrbitalTime/1440>=1) {
			// More than a day
			int days = classOrbitalTime/1440;
			int mins = classOrbitalTime % 1440;
			int hours = mins / 60;
			mins = mins % 60;
			s = days + "d " + hours + "h " + mins + "min";
		} else if (classOrbitalTime/60>=1) {
			// More than an hour
			int hours = classOrbitalTime / 60;
			int minutes = classOrbitalTime % 60;
			s = hours + "h " + minutes + "min";
		} else {
			// just minutes
			s = classOrbitalTime + "min";
		}
		return s;
	}


	public void findNextWindow(List<HelioroomWindow> windows) {
		for (HelioroomWindow w: windows) {
			if (currentPosition.insideCCWArc(new Degree(w.getViewAngleBegin()), new Degree(w.getViewAngleEnd()))) {
				// inside a window... 
				nextWindow = "Inside " + w.getName();
				secToNextWin = -1;
			} 
		}
		// check the gaps
		if (windows.size()>1) {
			// start with first-last gap
			if (currentPosition.insideCCWArc(new Degree(windows.get(windows.size()-1).getViewAngleEnd()), new Degree(windows.get(0).getViewAngleBegin()))) {
				nextWindow = windows.get(0).getName();
				Degree end = new Degree(windows.get(0).getViewAngleBegin());
				double degreesToNextWin = currentPosition.sub(end).getValue();
				secToNextWin =  (int) Math.round(degreesToNextWin/speed);
			}
			for (int i=0; i<windows.size()-1; i++) {
				//check the others
				if (currentPosition.insideCCWArc(new Degree(windows.get(i).getViewAngleEnd()), new Degree(windows.get(i+1).getViewAngleBegin()))) {
					nextWindow = windows.get(i+1).getName();
					Degree end = new Degree(windows.get(i+1).getViewAngleBegin());
					double degreesToNextWin = currentPosition.sub(end).getValue(); 
					secToNextWin =  (int) Math.round(degreesToNextWin/speed);
				}
			}
		}
	}
}
