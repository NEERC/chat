package matvey.neerc.support.googlecal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Date: 9/23/12:12:01 PM
 *
 * @author Matvey
 */
public class CalGenerator {

	private static final String INPUT_DATE_FORMAT = "yyyy-MM-dd";
    private static DateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");

	public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException {
		if (args.length < 3) {
			System.out.println(String.format("USAGE: %s <source file> <dest file mask> <base date>", CalGenerator.class.getSimpleName()));
			return;
		}
    	String sourceFile = args[0];
    	String destFile = String.format(args[1],"");
    	String destFileFood = String.format(args[1],"-food");
    	String destFileOrg = String.format(args[1],"-org");
    	Date contestDate = new SimpleDateFormat(INPUT_DATE_FORMAT).parse(args[2]);
    	

        try(PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8"));
        		PrintWriter outFood = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destFileFood), "UTF-8"));
        		PrintWriter outOrg = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destFileOrg), "UTF-8"))
        		) {
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(new File(sourceFile));
    		out.println("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Location,Private");
    		outFood.println("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Location,Private");
    		outOrg.println("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Location,Private");
    		
    		NodeList nDays = doc.getElementsByTagName("day");
    		for (int i = 0; i < nDays.getLength(); i++) {
    			Node nDay = nDays.item(i);
    			if (nDay.getNodeType() == Node.ELEMENT_NODE) {
    				Element eDay = (Element)nDay;
    				int shift = Integer.valueOf(eDay.getAttribute("shift"));
    				String where = trim(eDay.getAttribute("where"));
    				String subject = trim(getText(eDay));
    				String date = getDate(contestDate, shift);
    				if (subject.length() > 0) {
    					out.println(String.format("\"%s\",%s,,%s,,TRUE,%s,FALSE", subject, date, date, where));
    				}
    				NodeList nEvents = eDay.getElementsByTagName("event");
    	    		for (int j = 0; j < nEvents.getLength(); j++) {
    	    			Node nEvent = nEvents.item(j);
    	    			if (nEvent.getNodeType() == Node.ELEMENT_NODE) {
    	    				Element eEvent = (Element)nEvent;
    	    				String ewhere = trim(eEvent.getAttribute("where"));
    	    				String esubject = trim(getText(eEvent));
    	    				String start = trim(eEvent.getAttribute("start"));
    	    				String end = trim(eEvent.getAttribute("end"));
    	    				String cal = trim(eEvent.getAttribute("cal"));
    	    				PrintWriter o = out;
    	    				if ("food".equals(cal)) {
    	    					o = outFood;
    	    				} else if ("org".equals(cal)) {
    	    					o = outOrg;
    	    				} 
    	    				if (esubject.length() > 0) {
    	    					o.println(String.format("\"%s\",%s,%s,%s,%s,FALSE,%s,FALSE", esubject, date, start, date, end, ewhere.length() > 0 ? ewhere : where));
    	    				}
    	    			}
    	    		}
    			}
    		}
    		
		}

    }

	private static String getText(Element e) {
		NodeList childNodes = e.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				return child.getTextContent();
			}
		}
		return null;
	}

	private static String getDate(Date contestDate, int shift) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(contestDate);
		cal.add(Calendar.DAY_OF_YEAR, shift);
		return outputDateFormat.format(cal.getTime());
	}

	private static String trim(String str) {
		if (str == null) {
			return "";
		}
		String res = str.trim();
		return res.length() > 0 ? res : "";
	}
}
