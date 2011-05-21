/*
 * Created Oct 29, 2010
 */
package ltg.phenomena.helioroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

import ltg.StringUtilities;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.util.Log;

/**
 * TODO Description
 *
 * @author Gugo
 */
public class Helioroom extends Observable {
	
	// Planets representation constants
	public final static String IMAGES 	= "images";
	public final static String SPHERES 	= "spheres";
	
	// Planets names constants
	public final static String NONE 	= "none";
	public final static String NAMES 	= "names";
	public final static String COLORS 	= "color";

	// Simulation data
	private String instanceId = null;
	private String planetRepresentation = null;
	private String planetNames = null;
	private long startTime = -1;
	private List<Planet> planets = new ArrayList<Planet>();
	private List<HelioroomWindow> phenWindows = new ArrayList<HelioroomWindow>();
	
	// Network Time Correction Factor
	// Android doesn't update the time using NTP frequently enough so there is an 
	// offset between the time returned by System.getCurrentTime() and the real time.
	private long ntcf  = 0;

	
	// Default empty constructor
	public Helioroom() {
	}
	
	
	synchronized public void parse (String configXML) {
		Document doc = null;
		Element firstBodyEl = null;
		// filter presence / message
		try {
			doc = DocumentHelper.parseText(StringUtilities.toJava(configXML));
			if (doc.getRootElement().getName().equals("message")) {
				// In case other messages (like errors) are automatically sent by the server
				if(doc.getRootElement().attribute("type")!=null)
					return;
				firstBodyEl = (Element) doc.getRootElement().element("body").elements().get(0);
				instanceId = firstBodyEl.getName();
				configureWindows(firstBodyEl.element("windows").asXML());
				configure(firstBodyEl.element("config").asXML());
				this.setChanged();
				this.notifyObservers(this);
				Log.d("Helioroom", "Helioroom is now configured and ready to be rendered.");
			}
		} catch (DocumentException e) {
			Log.e(this.getClass().getSimpleName(), "Impossible to parse helioroom");
		}
	}


	private void configure(String configXML) {
		// reset the phenomena state
		planetRepresentation = null;
		planetNames = null;
		startTime = -1;
		planets.clear();
		// load state from XML
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(configXML);
			Element el = doc.getRootElement();
			// Phenomena properties
			planetRepresentation = el.elementTextTrim("planetRepresentation");
			planetNames = el.elementTextTrim("planetNames");
			startTime = Long.parseLong(el.elementTextTrim("startTime")) + ntcf;
			@SuppressWarnings("unchecked")
			List<Element> plans = el.element("planets").elements();
			for (Element el1: plans) {
				planets.add(new Planet(
						el1.elementTextTrim("name"), 
						el1.elementTextTrim("color"),
						el1.elementTextTrim("colorName"),
						Integer.valueOf(el1.elementTextTrim("classOrbitalTime")),
						Integer.valueOf(el1.elementTextTrim("startPosition"))
						));
			}
			sortPlanets();
		} catch (DocumentException e) {
			Log.e(this.getClass().getSimpleName(), "Impossible to configure helioroom");
		}
	}

	

	private void configureWindows(String windowsXML) {
		// reset the windows
		phenWindows.clear();
		// create new windows
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(windowsXML);
			@SuppressWarnings("unchecked")
			List<Element>windows = doc.getRootElement().elements();
			for(Element e: windows) {
				if(e.attributeValue("type").equals("client")) {
					phenWindows.add(new HelioroomWindow(
							e.attributeValue("id"),
							Integer.valueOf(e.elementTextTrim("viewAngleBegin")),
							Integer.valueOf(e.elementTextTrim("viewAngleEnd"))
					));
				}
			}
		} catch (DocumentException e) {
			Log.e(this.getClass().getSimpleName(), "Impossible to configure helioroom windows");
		}
	}
	
	
	synchronized public void setNtcf(long ntcf) {
		this.ntcf = ntcf;
	}
	
	
	synchronized public String getInstanceId() {
		return instanceId;
	}
	
	synchronized public String getPlanetRepresentation() {
		return planetRepresentation;
	}
	
	synchronized public String getPlanetNames() {
		return planetNames;
	}	
	
	synchronized public Long getStartTime() {
		return startTime;
	}

	synchronized public List<Planet> getPlanets() {
		List<Planet> planetCopy = new ArrayList<Planet>(planets);
		return planetCopy;
	}
	
	synchronized public List<HelioroomWindow> getWindows() {
		List<HelioroomWindow> winsCopy = new ArrayList<HelioroomWindow>(phenWindows);
		return winsCopy;
	}
	
	synchronized public long getNtcf() {
		return ntcf;
	}

		
	private void sortPlanets() {
		Collections.sort(planets, new Comparator<Planet>() {
			@Override
			public int compare(Planet p1, Planet p2) {
				return p1.getClassOrbitalTime() - p2.getClassOrbitalTime();
			}
			
		});
	}
	
	
	synchronized public void markAsChanged() {
		this.setChanged();
		this.notifyObservers(this);
	}

}
