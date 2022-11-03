package calendar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class FileManager implements DataInterface {
	
	private String fileLocation;
	private ArrayList<String> dataStrings;
	private ArrayList<String> settingsStrings;
	
	@Override
	public boolean openInterface() {
		File f = new File(fileLocation);
		
		if (!f.canRead())
			return false;
		
		return true;
	}
	
	public boolean saveDataArray(ArrayList<String> dataList, ArrayList<String> settingsList, int dataPortion) {
		try {

		        DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
		        DocumentBuilder build = dFact.newDocumentBuilder();
		        Document doc = build.newDocument();

		        Element root = doc.createElement("Kalendarz");
		        doc.appendChild(root);

		        Element events = doc.createElement("Events");
		        root.appendChild(events);

		        for (int i = 0; i < dataList.size(); i += dataPortion) {
		        	int j = i;
		        	Element event = doc.createElement("Event");
		        	events.appendChild(event);
		        
		        	Element name = doc.createElement("Description");
			        name.appendChild(doc.createTextNode(dataList.get(j)));
			        event.appendChild(name);
			            
			        Element location = doc.createElement("Location");
			        location.appendChild(doc.createTextNode(dataList.get(j+1)));
			        event.appendChild(location);
			            
			        Element startDateTime = doc.createElement("StartDateTime");
			        startDateTime.appendChild(doc.createTextNode(dataList.get(j+2)));
			        event.appendChild(startDateTime);
			            
			        Element endDateTime = doc.createElement("EndDateTime");
			        endDateTime.appendChild(doc.createTextNode(dataList.get(j+3)));
			        event.appendChild(endDateTime);
			            
			        Element notifyMinutes = doc.createElement("NotifyMinutes");
			        notifyMinutes.appendChild(doc.createTextNode(dataList.get(j+4)));
			        event.appendChild(notifyMinutes);
			        
			        Element active = doc.createElement("Active");
			        active.appendChild(doc.createTextNode(dataList.get(j+5)));
			        event.appendChild(active);
		        }
		        
		        Element settings = doc.createElement("Settings");
		        root.appendChild(settings);
		        
		        Element dbServer = doc.createElement("DatabaseServer");
		        dbServer.appendChild(doc.createTextNode(settingsList.get(0)));
			    settings.appendChild(dbServer);
			        
			    Element dbPort = doc.createElement("DatabasePort");
			    dbPort.appendChild(doc.createTextNode(settingsList.get(1)));
			    settings.appendChild(dbPort);
			        
			    Element dbUser = doc.createElement("DatabaseUser");
			    dbUser.appendChild(doc.createTextNode(settingsList.get(2)));
			    settings.appendChild(dbUser);
			        
			    Element dbPassword = doc.createElement("DatabasePassword");
			    dbPassword.appendChild(doc.createTextNode(settingsList.get(3)));
			    settings.appendChild(dbPassword);
			        
			    Element clearOldPeriod = doc.createElement("ClearOldPeriod");
			    clearOldPeriod.appendChild(doc.createTextNode(settingsList.get(4)));
			    settings.appendChild(clearOldPeriod);
			        
			    Element notifyPeriod = doc.createElement("NotifyPeriod");
			    notifyPeriod.appendChild(doc.createTextNode(settingsList.get(5)));
			    settings.appendChild(notifyPeriod);
			    
			    Element notifySound = doc.createElement("NotifySound");
			    notifySound.appendChild(doc.createTextNode(settingsList.get(6)));
			    settings.appendChild(notifySound);

		        // Save the document to the disk file
		        TransformerFactory tranFactory = TransformerFactory.newInstance();
		        Transformer aTransformer = tranFactory.newTransformer();

		        // format the XML nicely
		        aTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		        aTransformer.setOutputProperty(
		                "{http://xml.apache.org/xslt}indent-amount", "4");
		        aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

		        DOMSource source = new DOMSource(doc);
		        try {
		            // location and name of XML file you can change as per need
		            FileWriter fos = new FileWriter(fileLocation);
		            StreamResult result = new StreamResult(fos);
		            aTransformer.transform(source, result);

		        } catch (IOException e) {
		        	return false;
		        }

		    } catch (TransformerException ex) {
		        return false;

		    } catch (ParserConfigurationException ex) {
		        return false;
		    }
		
		return true;
	}
	
	public boolean loadDataArray()
	{	
		dataStrings = new ArrayList<String>();
		settingsStrings = new ArrayList<String>();
		
		try   
		{  
			//creating a constructor of file class and parsing an XML file  
			File file = new File(fileLocation);  
			//an instance of factory that gives a document builder  
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
			//an instance of builder to parse the specified xml file  
			DocumentBuilder db = dbf.newDocumentBuilder();  
			Document doc = db.parse(file);  
			doc.getDocumentElement().normalize();  
			NodeList nodeList = doc.getElementsByTagName("Event");  
		
			// nodeList is not iterable, so we are using for loop  
			for (int itr = 0; itr < nodeList.getLength(); itr++) {  
				Node node = nodeList.item(itr);  
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {  
					Element eElement = (Element) node;  
					dataStrings.add(eElement.getElementsByTagName("Description").item(0).getTextContent());
					dataStrings.add(eElement.getElementsByTagName("Location").item(0).getTextContent());
					dataStrings.add(eElement.getElementsByTagName("StartDateTime").item(0).getTextContent());
					dataStrings.add(eElement.getElementsByTagName("EndDateTime").item(0).getTextContent());
					dataStrings.add(eElement.getElementsByTagName("NotifyMinutes").item(0).getTextContent());
					dataStrings.add(eElement.getElementsByTagName("Active").item(0).getTextContent());
				}  
			}
			
			nodeList = doc.getElementsByTagName("Settings");
			
			for (int itr = 0; itr < nodeList.getLength(); itr++) {  
				Node node = nodeList.item(itr);  
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {  
					Element eElement = (Element) node;  
					settingsStrings.add(eElement.getElementsByTagName("DatabaseServer").item(0).getTextContent());
					settingsStrings.add(eElement.getElementsByTagName("DatabasePort").item(0).getTextContent());
					settingsStrings.add(eElement.getElementsByTagName("DatabaseUser").item(0).getTextContent());
					settingsStrings.add(eElement.getElementsByTagName("DatabasePassword").item(0).getTextContent());
					settingsStrings.add(eElement.getElementsByTagName("ClearOldPeriod").item(0).getTextContent());
					settingsStrings.add(eElement.getElementsByTagName("NotifyPeriod").item(0).getTextContent());
					settingsStrings.add(eElement.getElementsByTagName("NotifySound").item(0).getTextContent());
				}  
			}
		}   
		catch (Exception e) {  
			return false;  
		}  
		
		return true;
	}
	
	private String generateRandomString() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		
		String generatedString = buffer.toString();
		
		return generatedString;
	}
	
	public boolean exportCalendar(ArrayList<String> dataList, int dataPortion) {
		PrintWriter outFile;
		try {
			outFile = new PrintWriter(fileLocation);
			outFile.print("BEGIN:VCALENDAR\r\n");
			outFile.print("VERSION:2.0\r\n");
			outFile.print("PRODID:-//10264//Kalendarz Kalendarz 1.0//EN\r\n");
			
			for (int i = 0; i < dataList.size(); i += dataPortion) {
	        	int j = i;
	        	
	        	outFile.print("BEGIN:VEVENT\r\n");
	        	outFile.print("UID:"+generateRandomString()+"\r\n");
	        	outFile.print("DTSTAMP:"+dataList.get(j) + "\r\n");
	        	outFile.print("DTSTART:"+dataList.get(j+1) + "\r\n");
	        	outFile.print("DTEND:"+dataList.get(j+2) + "\r\n");
	        	outFile.print("SUMMARY:"+dataList.get(j+3) + "\r\n");
	        	outFile.print("BEGIN:VALARM\r\n");
	        	outFile.print("TRIGGER:"+dataList.get(j+4) + "\r\n");
	        	outFile.print("ACTION:DISPLAY\r\n");
	        	outFile.print("DESCRIPTION:Reminder\r\n");
	        	outFile.print("END:VALARM\r\n");
	        	outFile.print("LOCATION:"+dataList.get(j+5) + "\r\n");
	        	outFile.print("END:VEVENT\r\n");
	        }
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		outFile.print("END:VCALENDAR\r\n");
		outFile.close();
		
		return true;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public ArrayList<String> getDataStrings() {
		return dataStrings;
	}

	public ArrayList<String> getSettingsStrings() {
		return settingsStrings;
	}
}