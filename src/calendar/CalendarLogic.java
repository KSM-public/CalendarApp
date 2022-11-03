package calendar;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JOptionPane;

public class CalendarLogic {
	private ArrayList<CalendarEvent> eventsList;
	private SettingsManager settingsManager;
	private boolean connectedToDB;
	private DatabaseManager databaseManager;
	private FileManager fileManager;
	private Timer timer;
	private PeriodicTasks periodicTasks;
	
	public CalendarLogic(String[] args) {
		eventsList = new ArrayList<CalendarEvent>();
		settingsManager = new SettingsManager();
		
		//Database settings in commandline
		if (args.length == 4) {
			setDatabaseSettings(args[0], args[1], args[2], Integer.parseInt(args[3]));
		}
		
		connectedToDB = false;
		
		databaseManager = new DatabaseManager(settingsManager.getDatabaseIP(), settingsManager.getDatabaseUser(), settingsManager.getDatabasePassword(), settingsManager.getDatabasePort());
		connectedToDB = databaseManager.openInterface();
		
		fileManager = new FileManager();
		
		timer = new Timer();
		periodicTasks = new PeriodicTasks(this);
		
		//Get all data and settings from DB
		if (connectedToDB) {
			loadDataFromDatabase();
			loadSettingsFromDB();
		}
		
		timer.schedule(periodicTasks, 0, settingsManager.getNotifyPeriod()*60*1000);
	}
	
	public ArrayList<CalendarEvent> getEventsList() {
		return eventsList;
	}
	
	//Return start date time of event
	public LocalDateTime searchEvent(String description) {
		for (CalendarEvent ev : eventsList) 
			if (ev.getDescription().toLowerCase().contains(description.toLowerCase())) //Both strings converted to lower case so searching will be case insensitive
				return ev.getStartDateTime();
			
		return LocalDateTime.of(1970, 1, 1, 1, 1);
	}
	
	//Set database settings
	public void setDatabaseSettings(String ip, String userName, String userPassword, int port) {
		settingsManager.setDatabaseIP(ip);
		settingsManager.setDatabasePort(port);
		
		settingsManager.setDatabaseUser(userName);
		settingsManager.setDatabasePassword(userPassword);
	}
	
	public void setPeriods(int notifyPeriod, int clearOldPeriod) {
		settingsManager.setNotifyPeriod(notifyPeriod);
		settingsManager.setClearOldPeriod(clearOldPeriod);
		
		periodicTasks.cancel();
		timer.cancel();
		
		timer.purge();
		periodicTasks = null;
		
		System.gc();
		
		timer = new Timer();
		periodicTasks = new PeriodicTasks(this);
		
		timer.schedule(periodicTasks, 0, settingsManager.getNotifyPeriod()*60*1000);
	}
	
	public void setNotifySound(boolean notifySound) {
		settingsManager.setNotifySound(notifySound);
	}
	
	public boolean saveCalendarDataToFile(String location) {
		fileManager.setFileLocation(location);
		
		if (!fileManager.openInterface())
			return false;
		
		ArrayList<String> eventStrings = new ArrayList<String>();
		ArrayList<String> settingsStrings = new ArrayList<String>();
		String eventActive, eventStartDateTime, eventEndDateTime, notifySound, monthFormatted, hourFormatted, minuteFormatted;
		
		for (CalendarEvent event : eventsList) {
		
			if (event.isActive())
				eventActive = "1";
			else 
				eventActive = "0";
			
			//Need to add 0 before month value
			if (event.getStartDateTime().getMonthValue() < 10)
				monthFormatted = "0" + event.getStartDateTime().getMonthValue();
			else
				monthFormatted = ""+event.getStartDateTime().getMonthValue();
			
			//Same as above for hour
			if (event.getStartDateTime().getHour() < 10)
				hourFormatted = "0" + event.getStartDateTime().getHour();
			else
				hourFormatted = "" + event.getStartDateTime().getHour();
			
			//Same as above for minute
			if (event.getStartDateTime().getMinute() < 10)
				minuteFormatted = "0" + event.getStartDateTime().getMinute();
			else
				minuteFormatted = "" + event.getStartDateTime().getMinute();
				
			eventStartDateTime = event.getStartDateTime().getYear() + "-" + monthFormatted + "-" + event.getStartDateTime().getDayOfMonth() +
					" " + hourFormatted + ":" + minuteFormatted;
			
			//Need to add 0 before month value
			if (event.getEndDateTime().getMonthValue() < 10)
				monthFormatted = "0" + event.getEndDateTime().getMonthValue();
			else
				monthFormatted = ""+event.getEndDateTime().getMonthValue();
			
			//Same as above for hour
			if (event.getEndDateTime().getHour() < 10)
				hourFormatted = "0" + event.getEndDateTime().getHour();
			else
				hourFormatted = "" + event.getEndDateTime().getHour();
			
			//Same as above for minute
			if (event.getEndDateTime().getMinute() < 10)
				minuteFormatted = "0" + event.getStartDateTime().getMinute();
			else
				minuteFormatted = "" + event.getEndDateTime().getMinute();
			
			eventEndDateTime = event.getEndDateTime().getYear() + "-" + monthFormatted + "-" + event.getEndDateTime().getDayOfMonth() +
					" " + hourFormatted + ":" + minuteFormatted;
			
			eventStrings.add(event.getDescription());
			eventStrings.add(event.getLocation());
			eventStrings.add(eventStartDateTime);
			eventStrings.add(eventEndDateTime);
			eventStrings.add(""+event.getNotifyMinutes());
			eventStrings.add(eventActive);
		}
		
		if (settingsManager.isNotifySound())
			notifySound = "1";
		else
			notifySound = "0";
		
		settingsStrings.add(settingsManager.getDatabaseIP());
		settingsStrings.add(""+settingsManager.getDatabasePort());
		settingsStrings.add(settingsManager.getDatabaseUser());
		settingsStrings.add(settingsManager.getDatabasePassword());
		settingsStrings.add(""+settingsManager.getClearOldPeriod());
		settingsStrings.add(""+settingsManager.getNotifyPeriod());
		settingsStrings.add(notifySound);
		
		return fileManager.saveDataArray(eventStrings, settingsStrings, 6);
	}
	
	public boolean loadCalendarDataFromFile(String filename) {
		fileManager.setFileLocation(filename);
		
		if (!fileManager.openInterface())
			return false;
		
		fileManager.loadDataArray();
		
		//Erase all data from DB if any
		if (connectedToDB)
			databaseManager.eraseAll();
		
		eventsList.clear();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		
		for (int i = 0; i < fileManager.getDataStrings().size(); i += 6) {
			LocalDateTime eventStart = LocalDateTime.parse(fileManager.getDataStrings().get(i + 2), formatter);
			LocalDateTime eventEnd = LocalDateTime.parse(fileManager.getDataStrings().get(i + 3), formatter);
			boolean eventActive;
			
			if (Integer.parseInt(fileManager.getDataStrings().get(i + 5)) == 0)
				eventActive = false;
			else
				eventActive = true;
			
			CalendarEvent event = new CalendarEvent(fileManager.getDataStrings().get(i), fileManager.getDataStrings().get(i+1), eventStart, eventEnd, Integer.parseInt(fileManager.getDataStrings().get(i + 4)));
			event.setActive(eventActive);
			
			eventsList.add(event);
		}
		
		//Also add loaded events to database
		if (eventsList.size() > 0 && connectedToDB)
			for (CalendarEvent ev : eventsList)
				addEventToDatabase(ev);
		
		if (fileManager.getSettingsStrings().size() < 7)
			return false;
		
		settingsManager.setDatabaseIP(fileManager.getSettingsStrings().get(0));
		settingsManager.setDatabasePort(Integer.parseInt(fileManager.getSettingsStrings().get(1)));
		settingsManager.setDatabaseUser(fileManager.getSettingsStrings().get(2));
		settingsManager.setDatabasePassword(fileManager.getSettingsStrings().get(3));
		settingsManager.setClearOldPeriod(Integer.parseInt(fileManager.getSettingsStrings().get(4)));
		settingsManager.setNotifyPeriod(Integer.parseInt(fileManager.getSettingsStrings().get(5)));
		
		if (fileManager.getSettingsStrings().get(6) == "1")
			settingsManager.setNotifySound(true);
		else
			settingsManager.setNotifySound(false);
		
		//Also add loaded settings to database
		if (connectedToDB)
			saveSettingsInDatabase();
		
		return true;
	}
	
	public boolean exportICalendar(String filename) {
		/*
		 * DTSTAMP:19970714T170000Z - Today
		 * DTSTART:ASABOVE
		 * DTEND:ASABOVE
		 * SUMMARY:DESCRIPTION
		 * TRIGGER:-P15M - 15 min prior to event
		 */
		String dtStamp, dtStart, dtEnd, notTrigger, monthFormatted, hourFormatted, minuteFormatted;
		ArrayList<String> eventICalStrings = new ArrayList<String>();
		
		if (LocalDate.now().getMonthValue() < 10)
			monthFormatted = "0"+LocalDate.now().getMonthValue();
		else
			monthFormatted = ""+LocalDate.now().getMonthValue();
		
		if (LocalTime.now().getHour() < 10)
			hourFormatted = "0"+LocalTime.now().getHour();
		else
			hourFormatted = ""+LocalTime.now().getHour();
		
		if (LocalTime.now().getMinute() < 10)
			minuteFormatted = "0"+LocalTime.now().getMinute();
		else
			minuteFormatted = ""+LocalTime.now().getMinute();
		
		dtStamp = LocalDate.now().getYear() + monthFormatted + LocalDate.now().getDayOfMonth() + "T" +  hourFormatted + minuteFormatted + "00Z";
		
		for (CalendarEvent eve : eventsList) {
			eventICalStrings.add(dtStamp);
			
			if (eve.getStartDateTime().getMonthValue() < 10)
				monthFormatted = "0"+eve.getStartDateTime().getMonthValue();
			else
				monthFormatted = ""+eve.getStartDateTime().getMonthValue();
			
			if (eve.getStartDateTime().getHour() < 10)
				hourFormatted = "0"+eve.getStartDateTime().getHour();
			else
				hourFormatted = ""+eve.getStartDateTime().getHour();
			
			if (eve.getStartDateTime().getMinute() < 10)
				minuteFormatted = "0"+eve.getStartDateTime().getMinute();
			else
				minuteFormatted = ""+eve.getStartDateTime().getMinute();
			
			eventICalStrings.add(eve.getStartDateTime().getYear()+ monthFormatted + eve.getStartDateTime().getDayOfMonth() + "T" + hourFormatted + minuteFormatted + "00Z");
			
			
			if (eve.getEndDateTime().getMonthValue() < 10)
				monthFormatted = "0"+eve.getEndDateTime().getMonthValue();
			else
				monthFormatted = ""+eve.getEndDateTime().getMonthValue();
			
			if (eve.getEndDateTime().getHour() < 10)
				hourFormatted = "0"+eve.getEndDateTime().getHour();
			else
				hourFormatted = ""+eve.getEndDateTime().getHour();
			
			if (eve.getEndDateTime().getMinute() < 10)
				minuteFormatted = "0"+eve.getEndDateTime().getMinute();
			else
				minuteFormatted = ""+eve.getEndDateTime().getMinute();
			
			eventICalStrings.add(eve.getEndDateTime().getYear()+ monthFormatted + eve.getEndDateTime().getDayOfMonth() + "T"+ hourFormatted + minuteFormatted + "00Z");
			
			eventICalStrings.add(eve.getDescription());
			
			eventICalStrings.add("-P" + eve.getNotifyMinutes() + "M");
			
			eventICalStrings.add(eve.getLocation());
		}
		
		fileManager.setFileLocation(filename);
		
		return fileManager.exportCalendar(eventICalStrings, 6);
	}
	
	public void saveSettings() {
		if (connectedToDB)
			saveSettingsInDatabase();
	}
	
	//Return array of database settings
	public String[] getDatabaseSettings() {
		return new String[] { settingsManager.getDatabaseIP(), ""+settingsManager.getDatabasePort(), settingsManager.getDatabaseUser(), settingsManager.getDatabasePassword() };
	}
	
	public int[] getPeriods() {
		return new int[] { settingsManager.getClearOldPeriod(), settingsManager.getNotifyPeriod() };
	}
	
	public boolean isNotifySound() {
		return settingsManager.isNotifySound();
	}
	
	public boolean isConnectedToDatabase() {
		return connectedToDB;
	}
	
	public void disconnectDatabase() {
		connectedToDB = false;
	}
	
	public boolean connectDatabase() {
		databaseManager.setConnectionDetails(settingsManager.getDatabaseIP(), settingsManager.getDatabaseUser(), settingsManager.getDatabasePassword(), settingsManager.getDatabasePort());
		connectedToDB = databaseManager.openInterface();
		
		if (!connectedToDB) {
			JOptionPane.showMessageDialog(null, "Połączenie z bazą nie udane. Sprawdź dane serwera w ustawienaich i spróbuj ponownie.");
			return false;
		}
		
		loadDataFromDatabase();
		loadSettingsFromDB();
		
		return true;
	}
	
	//Return false if event couldn't be added (e.g. user try to add same event as existing)
	public boolean addNewEvent(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes) {

		
		CalendarEvent calendarEvent = new CalendarEvent(description, location, startDateTime, endDateTime, notifyMinutes);
		
		for(CalendarEvent calEv : eventsList) {
			if (calEv.equals(calendarEvent))
				return false;
		}
		
		eventsList.add(calendarEvent);
		
		//Add also to database if we have connection
		if (connectedToDB)
			addEventToDatabase(calendarEvent);
		
		return true;
 	}
	
	public boolean modifyEvent(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes,
					String newDescription, String newLocation, LocalDateTime newStartDateTime, LocalDateTime newEndDateTime, int newNotifyMinutes) {
		int index = getIndexOfElement(description, location, startDateTime, endDateTime, notifyMinutes);
		
		//Can't find event to modify
		if (index < 0)
			return false;
		
		CalendarEvent calendarEvent = eventsList.get(index);
		
		calendarEvent.setDescription(newDescription);
		calendarEvent.setLocation(newLocation);
		calendarEvent.setStartDateTime(newStartDateTime);
		calendarEvent.setEndDateTime(newEndDateTime);
		calendarEvent.setNotifyMinutes(newNotifyMinutes);
		
		if (connectedToDB) {
			CalendarEvent oldCalendarEvent = new CalendarEvent(description, location, startDateTime, endDateTime, notifyMinutes);
			
			modifyEventInDatabase(oldCalendarEvent, calendarEvent);
		}
		
		return true;
	}
	
	public boolean removeEvent(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes) {
		int index = getIndexOfElement(description, location, startDateTime, endDateTime, notifyMinutes);
		
		if (index < 0)
			return false;
		
		if (connectedToDB)
			removeEventFromDatabase(eventsList.get(index));
		
		eventsList.remove(index);
		
		return true;
	}
	
	public boolean endEvent(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes) {
		int index = getIndexOfElement(description, location, startDateTime, endDateTime, notifyMinutes);
		
		if (index < 0)
			return false;
		
		CalendarEvent event = new CalendarEvent(description, location, startDateTime, endDateTime, notifyMinutes);
		eventsList.get(index).setActive(false);
		
		if (connectedToDB)
			modifyEventInDatabase(event, eventsList.get(index));
		
		return true;
	}
	
	//Active inactive event (only if its not ended)
	public boolean activeEvent(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes) {
		int index = getIndexOfElement(description, location, startDateTime, endDateTime, notifyMinutes);
		
		if (index < 0)
			return false;
		
		//Try to active ended event
		if (LocalDateTime.now().compareTo(endDateTime) >= 0)
			return false;
		
		CalendarEvent event = new CalendarEvent(description, location, startDateTime, endDateTime, notifyMinutes);
		event.setActive(false);
		eventsList.get(index).setActive(true);
		
		if (connectedToDB)
			modifyEventInDatabase(event, eventsList.get(index));
		
		return true;
	}
	
	//Get index of choosen element
	private int getIndexOfElement(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes) {
		for(int i = 0; i < eventsList.size(); i++) {
			CalendarEvent calEv = eventsList.get(i);
			
			if (calEv.getDescription().equals(description) && calEv.getLocation().equals(location) && calEv.getStartDateTime().equals(startDateTime) 
					&& calEv.getEndDateTime().equals(endDateTime) && calEv.getNotifyMinutes() == notifyMinutes)
				return i;
		}
		
		return -1;
	}
	
	//Check minutes between two dates
	public static int checkPeriodInMinutes(LocalDateTime startDate, LocalDateTime endDate) {
		Duration duration = Duration.between(startDate, endDate);
		
		long secDuration = duration.getSeconds();
		
		int minutes = (int) (secDuration/60);
		
		return minutes;
	}
	
	//Clean old events (if user want to, and make inactive ended events)
	public void cleanEventData() {
		for(CalendarEvent calEv : eventsList) {
			//Event ended
			if (calEv.isActive() && checkPeriodInMinutes(LocalDateTime.now(), calEv.getEndDateTime()) <= 0) {
				calEv.setActive(false);
				
				if (connectedToDB) {
					CalendarEvent event = new CalendarEvent(calEv.getDescription(), calEv.getLocation(), calEv.getStartDateTime(), calEv.getEndDateTime(), calEv.getNotifyMinutes());
					modifyEventInDatabase(event, calEv);
				}
			}
			
			//Event older than value set by user
			if (calEv.isActive() && settingsManager.getClearOldPeriod() > 0 && checkPeriodInMinutes(calEv.getEndDateTime(), LocalDateTime.now()) >= settingsManager.getClearOldPeriod()*24*60) {
				if (connectedToDB) 
					removeEventFromDatabase(calEv);
				
				eventsList.remove(calEv);
			}
		}
	}
	
	//Check if some events await notify 
	public void notifyAboutEvents() {
		for(CalendarEvent calEv : eventsList) {
			if (calEv.isActive() && checkPeriodInMinutes(LocalDateTime.now(), calEv.getStartDateTime()) <= calEv.getNotifyMinutes() 
					&& checkPeriodInMinutes(LocalDateTime.now(), calEv.getStartDateTime()) > 0){
				notifyEvent(calEv);
			}
		}
	}
	
	private void notifyEvent(CalendarEvent event) {
		if (settingsManager.isNotifySound())
			playNotifySound();
		
		MainWindow.showMessageBox("Wydarzenie " + event.getDescription() + " w miejscu: " + 
				event.getLocation() + " rozpocznie się za " + (checkPeriodInMinutes(LocalDateTime.now(), event.getStartDateTime()) + 1) + " minut(y).");
	}
	
	private void playNotifySound () {
		try {
		    File yourFile = new File("notify.wav");
		    AudioInputStream stream;
		    AudioFormat format;
		    DataLine.Info info;
		    Clip clip;

		    stream = AudioSystem.getAudioInputStream(yourFile);
		    format = stream.getFormat();
		    info = new DataLine.Info(Clip.class, format);
		    clip = (Clip) AudioSystem.getLine(info);
		    clip.open(stream);
		    clip.start();
		}
		catch (Exception e) {
			MainWindow.showMessageBox("Błąd otwierania pliku dźwiękowego powiadomienia.");
		}
	}
	
	//Change event details format to suitable for database and send data to DatabaseManager
	private void addEventToDatabase(CalendarEvent event) {
		String eventActive, eventStartDateTime, eventEndDateTime;
		
		if (event.isActive())
			eventActive = "1";
		else 
			eventActive = "0";
		
		eventStartDateTime = event.getStartDateTime().getYear() + "-" + event.getStartDateTime().getMonthValue() + "-" + event.getStartDateTime().getDayOfMonth() +
				" " + event.getStartDateTime().getHour() + ":" + event.getStartDateTime().getMinute();
		
		eventEndDateTime = event.getEndDateTime().getYear() + "-" + event.getEndDateTime().getMonthValue() + "-" + event.getEndDateTime().getDayOfMonth() +
				" " + event.getEndDateTime().getHour() + ":" + event.getEndDateTime().getMinute();
		
		String[] eventStrings = {event.getDescription(), event.getLocation(), eventActive, eventStartDateTime, eventEndDateTime, ""+event.getNotifyMinutes()};
		
		if (!databaseManager.addDataToDB(eventStrings))
			MainWindow.showMessageBox("Dodawanie danych do bazy się nie powiodło. Jeżeli problem się powtórzy sprawdź dane serwera i spróbuj połączyć się ponownie.");
	}
	
	//Modify event in database
	private void modifyEventInDatabase(CalendarEvent oldEvent, CalendarEvent newEvent) {
		String oldEventActive, oldEventStartDateTime, oldEventEndDateTime;
		String newEventActive, newEventStartDateTime, newEventEndDateTime;
		
		if (oldEvent.isActive())
			oldEventActive = "1";
		else 
			oldEventActive = "0";
		
		if (newEvent.isActive())
			newEventActive = "1";
		else 
			newEventActive = "0";
		
		oldEventStartDateTime = oldEvent.getStartDateTime().getYear() + "-" + oldEvent.getStartDateTime().getMonthValue() + "-" + oldEvent.getStartDateTime().getDayOfMonth() +
				" " + oldEvent.getStartDateTime().getHour() + ":" + oldEvent.getStartDateTime().getMinute();
		
		oldEventEndDateTime = oldEvent.getEndDateTime().getYear() + "-" + oldEvent.getEndDateTime().getMonthValue() + "-" + oldEvent.getEndDateTime().getDayOfMonth() +
				" " + oldEvent.getEndDateTime().getHour() + ":" + oldEvent.getEndDateTime().getMinute();
		
		newEventStartDateTime = newEvent.getStartDateTime().getYear() + "-" + newEvent.getStartDateTime().getMonthValue() + "-" + newEvent.getStartDateTime().getDayOfMonth() +
				" " + newEvent.getStartDateTime().getHour() + ":" + newEvent.getStartDateTime().getMinute();
		
		newEventEndDateTime = newEvent.getEndDateTime().getYear() + "-" + newEvent.getEndDateTime().getMonthValue() + "-" + newEvent.getEndDateTime().getDayOfMonth() +
				" " + newEvent.getEndDateTime().getHour() + ":" + newEvent.getEndDateTime().getMinute();
		
		String[] oldEventStrings = {oldEvent.getDescription(), oldEvent.getLocation(), oldEventActive, oldEventStartDateTime, oldEventEndDateTime, ""+oldEvent.getNotifyMinutes()};
		String[] newEventStrings = {newEvent.getDescription(), newEvent.getLocation(), newEventActive, newEventStartDateTime, newEventEndDateTime, ""+newEvent.getNotifyMinutes()};
		
		if (!databaseManager.modifyDataInDB(oldEventStrings, newEventStrings))
			MainWindow.showMessageBox("Modyfikacja danych w bazie się nie powiodła. Jeżeli problem się powtórzy sprawdź dane serwera i spróbuj połączyć się ponownie.");
	}
	
	private void removeEventFromDatabase(CalendarEvent event) {
		String eventActive, eventStartDateTime, eventEndDateTime;
		
		if (event.isActive())
			eventActive = "1";
		else 
			eventActive = "0";
		
		eventStartDateTime = event.getStartDateTime().getYear() + "-" + event.getStartDateTime().getMonthValue() + "-" + event.getStartDateTime().getDayOfMonth() +
				" " + event.getStartDateTime().getHour() + ":" + event.getStartDateTime().getMinute();
		
		eventEndDateTime = event.getEndDateTime().getYear() + "-" + event.getEndDateTime().getMonthValue() + "-" + event.getEndDateTime().getDayOfMonth() +
				" " + event.getEndDateTime().getHour() + ":" + event.getEndDateTime().getMinute();
		
		String[] eventStrings = {event.getDescription(), event.getLocation(), eventActive, eventStartDateTime, eventEndDateTime, ""+event.getNotifyMinutes()};
		
		if (!databaseManager.removeDataFromDB(eventStrings))
			MainWindow.showMessageBox("Usuwanie danych z bazy się nie powiodło. Jeżeli problem się powtórzy sprawdź dane serwera i spróbuj połączyć się ponownie.");
	}
	
	private void saveSettingsInDatabase() {
		String notifySound;
		
		if (settingsManager.isNotifySound())
			notifySound = "1";
		else 
			notifySound = "0";
		
		String[] settingsStrings = {""+settingsManager.getClearOldPeriod(), ""+settingsManager.getNotifyPeriod(), notifySound};
		
		if(!databaseManager.saveSettingsInDB(settingsStrings))
			MainWindow.showMessageBox("Zapisanie ustawień w bazie danych nie powiodło się. Jeżeli problem się powtórzy sprawdź dane serwera i spróbuj połączyć się ponownie.");
	}
	
	private void loadDataFromDatabase() {
		if (!databaseManager.getAllDataFromDB()) {
			MainWindow.showMessageBox("Odczytanie danych z bazy nie powiodło się. Jeżeli problem się powtórzy sprawdź dane serwera i spróbuj połączyć się ponownie.");
			connectedToDB = false;
			return;
		}
		
		eventsList.clear();
		
		//No events data in DB
		if (databaseManager.getDataArrayList().size() < 6)
			return;
		
		//Database datetime format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		int k;
		String[] dataStrings = new String[6];
		String eventDescription, eventLocation;
		boolean eventActive;
		LocalDateTime eventStart, eventEnd;
		int eventNotifyMinutes;
		
		for (int i = 0; i < databaseManager.getDataArrayList().size()/6; i++) {
			k = 0;
			
			for (int j = i*6; j < i*6+6; j++) {
				dataStrings[k] = databaseManager.getDataArrayList().get(j);
				k++;
			}
			eventDescription = dataStrings[0];
			eventLocation = dataStrings[1];
			
			if (Integer.parseInt(dataStrings[2]) == 0)
				eventActive = false;
			else
				eventActive = true;
			
			eventStart = LocalDateTime.parse(dataStrings[3].substring(0, dataStrings[3].length() - 2), formatter);
			eventEnd = LocalDateTime.parse(dataStrings[4].substring(0, dataStrings[4].length() - 2), formatter);
			
			eventNotifyMinutes = Integer.parseInt(dataStrings[5]);
			
			CalendarEvent event = new CalendarEvent(eventDescription, eventLocation, eventStart, eventEnd, eventNotifyMinutes);
			event.setActive(eventActive);
			eventsList.add(event);
		}
	}
	
	private void loadSettingsFromDB() {
		if (!databaseManager.getAllSettingsFromDB()) {
			MainWindow.showMessageBox("Odczytanie danych z bazy nie powiodło się. Jeżeli problem się powtórzy sprawdź dane serwera i spróbuj połączyć się ponownie.");
			connectedToDB = false;
			return;
		}
		
		//No settings data in DB
		if (databaseManager.getSettingsArrayList().size() < 3)
			return;
		
		for (int i = 0; i < databaseManager.getDataArrayList().size(); i++) {
			settingsManager.setClearOldPeriod(Integer.parseInt(databaseManager.getSettingsArrayList().get(0)));
			
			settingsManager.setNotifyPeriod(Integer.parseInt(databaseManager.getSettingsArrayList().get(1)));
			
			if (Integer.parseInt(databaseManager.getSettingsArrayList().get(2)) == 0)
				settingsManager.setNotifySound(false);
			else
				settingsManager.setNotifySound(true);
		}
	}
}

class PeriodicTasks extends TimerTask {
	private CalendarLogic calendarLogic;
	
	public PeriodicTasks(CalendarLogic calendarLogic) {
		this.calendarLogic = calendarLogic;
	}
	
	@Override
	public void run() {
		calendarLogic.notifyAboutEvents();
		calendarLogic.cleanEventData();
	}
	
}
