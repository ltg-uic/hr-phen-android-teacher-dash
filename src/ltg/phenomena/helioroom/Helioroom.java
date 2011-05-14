/*
 * Created Oct 29, 2010
 */
package ltg.phenomena.helioroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ltg.phenomena.XMPPThreadObserver;
import android.util.Log;

/**
 * TODO Description
 *
 * @author Gugo
 */
public class Helioroom implements XMPPThreadObserver {
	
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
		Log.i("Helioroom", configXML);
		// filter presence / message
	}


	public void configure(String configXML) {
		// reset the phenomena state
//		planetRepresentation = null;
//		planetNames = null;
//		startTime = -1;
//		planets.clear();
//		// load state from XML
//		Document doc = null;
//		try {
//			doc = DocumentHelper.parseText(configXML);
//			Element el = doc.getRootElement();
//			// Phenomena properties
//			planetRepresentation = el.elementTextTrim("planetRepresentation");
//			planetNames = el.elementTextTrim("planetNames");
//			startTime = Long.parseLong(el.elementTextTrim("startTime"));
//			List<Element> plans = el.element("planets").elements();
//			for (Element el1: plans) {
//				planets.add(new Planet(
//						el1.elementTextTrim("name"), 
//						el1.elementTextTrim("color"),
//						el1.elementTextTrim("colorName"),
//						Integer.valueOf(el1.elementTextTrim("classOrbitalTime")),
//						Integer.valueOf(el1.elementTextTrim("startPosition"))
//						));
//			}
//			sortPlanets();
//			this.setChanged();
//		} catch (DocumentException e) {
//			log.info("Impossible to configure helioroom");
//		}
	}

	

	public void configureWindows(String windowsXML) {
		// reset the windows
		phenWindows .clear();
		// create new windows
//		Document doc = null;
//		try {
//			doc = DocumentHelper.parseText(windowsXML);
//			@SuppressWarnings("unchecked")
//			List<Element>windows = doc.getRootElement().elements();
//			for(Element e: windows) {
//				if(e.attributeValue("type").equals("client")) {
//					phenWindows.add(new HelioroomWindow(
//							e.attributeValue("id"),
//							Integer.valueOf(e.elementTextTrim("viewAngleBegin")),
//							Integer.valueOf(e.elementTextTrim("viewAngleEnd"))
//					));
//				}
//				if(e.attributeValue("type").equals("control")) {
//					phenWindows.add(new HelioroomControlWindow(e.attributeValue("id")));
//				}
//				if(e.attributeValue("type").equals("notifier")) {
//					phenWindows.add(new HelioroomNotifierWindow(e.attributeValue("id")));
//				}
//			}
//		} catch (DocumentException e) {
//			log.info("Impossible to configure helioroom windows");
//		}
	}
	

	public String getPlanetNames() {
		return planetNames;
	}
	
	
	public Long getStartTime() {
		return startTime;
	}


	public List<Planet> getPlanets() {
		return planets;
	}
	
	
	private void sortPlanets() {
		Collections.sort(planets, new Comparator<Planet>() {
			@Override
			public int compare(Planet p1, Planet p2) {
				return p1.getClassOrbitalTime() - p2.getClassOrbitalTime();
			}
			
		});
	}


}
