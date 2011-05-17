/*
 * Created Oct 29, 2010
 */
package ltg.phenomena.helioroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ltg.StringUtilities;
import ltg.phenomena.XMPPThreadObserver;

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
public class Helioroom extends java.util.Observable implements XMPPThreadObserver {
	
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
	private List<Planet> planets = null;
	private List<HelioroomWindow> phenWindows  = null;

	public Helioroom() {
		planets = new ArrayList<Planet>();
		phenWindows = new ArrayList<HelioroomWindow>();
	}
	
	
	public void parse (String configXML) {
		Document doc = null;
		Element firstBodyEl = null;
		// filter presence / message
		try {
			doc = DocumentHelper.parseText(StringUtilities.toJava(configXML));
			if (doc.getRootElement().getName().equals("message")) {
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
			startTime = Long.parseLong(el.elementTextTrim("startTime"));
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
	
	
	public String getInstanceId() {
		return instanceId;
	}
	
	
	public String getPlanetRepresentation() {
		return planetRepresentation;
	}
	
	
	public Long getStartTime() {
		return startTime;
	}


	public List<Planet> getPlanets() {
		return planets;
	}
	
	public List<HelioroomWindow> getWindows() {
		return phenWindows;
	}
	
	public String getPlanetNames() {
		return planetNames;
	}	
	
	private void sortPlanets() {
		Collections.sort(planets, new Comparator<Planet>() {
			@Override
			public int compare(Planet p1, Planet p2) {
				return p1.getClassOrbitalTime() - p2.getClassOrbitalTime();
			}
			
		});
	}
	
	
	public void markAsChanged() {
		this.setChanged();
		this.notifyObservers(this);
	}

}
