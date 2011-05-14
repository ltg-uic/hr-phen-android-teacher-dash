/*
 * Created Jan 18, 2011
 */
package ltg.phenomena.helioroom;


/**
 * TODO Description
 *
 * @author Gugo
 */
public class HelioroomWindow {
	
	private String windowName = null;
	private Helioroom hr = null;
	private int viewAngleBegin = -1;
	private int viewAngleEnd = -1;
	
	
	public HelioroomWindow(String windowName, int viewAngleBegin, int viewAngleEnd) {
		this.windowName  = windowName;
		this.viewAngleBegin = viewAngleBegin;
		this.viewAngleEnd = viewAngleEnd;
	}


	public int getViewAngleBegin() {
		return viewAngleBegin;
	}


	public int getViewAngleEnd() {
		return viewAngleEnd;
	}

}
