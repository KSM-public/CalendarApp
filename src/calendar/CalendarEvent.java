package calendar;

import java.time.Duration;
import java.time.LocalDateTime;

public class CalendarEvent {
	private LocalDateTime startDateTime, endDateTime;
	private String description, location;
	private boolean isActive;
	private int notifyMinutes;
	
	public CalendarEvent() {
		setActive(true);
		notifyMinutes = 0;
	}
	
	public CalendarEvent(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes) {
		this.setDescription(description);
		this.setLocation(location);
		
		this.setStartDateTime(startDateTime);
		this.setEndDateTime(endDateTime);
		
		this.setNotifyMinutes(notifyMinutes);
		
		setActive(true);
	}
	
	//Return how many hours was left to the start of event from actual date
	public long getHoursToEndOfEvent() {
		Duration duration = Duration.between(LocalDateTime.now(), getEndDateTime());
		
		long seconds = duration.getSeconds();
		
		return seconds / 60;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public int getNotifyMinutes() {
		return notifyMinutes;
	}

	public void setNotifyMinutes(int notifyMinutes) {
		this.notifyMinutes = notifyMinutes;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		CalendarEvent calendarEvent = (CalendarEvent)obj;
		
		if (description == null) 
            if (calendarEvent.getDescription() != null)
                return false;
            
        if (location == null) 
            if (calendarEvent.getLocation() != null)
                return false;
            
        if (startDateTime == null)
            if (calendarEvent.getStartDateTime() != null)
                return false;
        
        if (endDateTime == null)
            if (calendarEvent.getEndDateTime() != null)
                return false;
        
        if (notifyMinutes == 0)
            if (calendarEvent.getNotifyMinutes() != 0)
                return false;
        
        if (!description.equals(calendarEvent.getDescription()))
        	return false;
        
        if (!location.equals(calendarEvent.getLocation()))
        	return false;
        
        if (!startDateTime.equals(calendarEvent.getStartDateTime()))
        	return false;
        
        if (!endDateTime.equals(calendarEvent.getEndDateTime()))
        	return false;
        
        if (notifyMinutes != calendarEvent.getNotifyMinutes())
        	return false;
        
        return true;
	}
}
