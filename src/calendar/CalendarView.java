package calendar;

import java.awt.Color;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CalendarView extends JPanel {
	
	private LocalDate selectedDate;
	private ArrayList<JButton> dayButtonsList;
	private int previousSelectedDay;
	private JLabel[] daysOfWeek;
	
	private ArrayList<CalendarViewListener> eventListeners;
	private boolean [] containEvent;
	
	public LocalDate getSelectedDate() {
    	return selectedDate;
    }

    public CalendarView(LocalDate selectedDate) {
    	
    	this.eventListeners = new ArrayList<CalendarViewListener>();
    	
    	this.selectedDate = selectedDate;
    	dayButtonsList = new ArrayList<JButton>();
    	previousSelectedDay = selectedDate.getDayOfMonth();
    	
    	containEvent = new boolean[31];
    	
    	for (int i = 0; i < 31; i++)
    		containEvent[i] = false;
    	
        setLayout(new java.awt.GridLayout(7, 7, 5, 5));
        
        daysOfWeek = new JLabel[7];
        
        daysOfWeek[0] = new JLabel();
        daysOfWeek[0].setText("Poniedziałek");
        
        daysOfWeek[1] = new JLabel();
        daysOfWeek[1].setText("Wtorek");
        
        daysOfWeek[2] = new JLabel();
        daysOfWeek[2].setText("Środa");
        
        daysOfWeek[3] = new JLabel();
        daysOfWeek[3].setText("Czwartek");
        
        daysOfWeek[4] = new JLabel();
        daysOfWeek[4].setText("Piątek");
        
        daysOfWeek[5] = new JLabel();
        daysOfWeek[5].setText("Sobota");
        
        daysOfWeek[6] = new JLabel();
        daysOfWeek[6].setText("Niedziela");
        
        drawCalendar();
    }
    
    private void drawCalendar() {
    	previousSelectedDay = selectedDate.getDayOfMonth();
    	
    	//Clear highlighted days
    	for (int i = 0; i < 31; i++)
    		containEvent[i] = false;
    	
    	//Clear previous calendar
    	setLayout(null);
    	removeAll();
    	dayButtonsList.clear();
    	
    	setLayout(new java.awt.GridLayout(7, 7, 5, 5));
    	
    	for (int i = 0; i < 7; i++) {
        	daysOfWeek[i].setHorizontalAlignment(JLabel.CENTER);
        	add(daysOfWeek[i]);
        }
    	
    	LocalDate localDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue(), 1);
        int dayOfWeek = localDate.getDayOfWeek().getValue();
        
        YearMonth yearMonth = YearMonth.of(selectedDate.getYear(), selectedDate.getMonth());
        int daysInMonth = yearMonth.lengthOfMonth();
        
        for (int i = 0; i < dayOfWeek - 1; i++) {
        	JLabel label = new JLabel();
        	add(label);
        }

        for (int i = 1; i <= daysInMonth; i++) {
            JButton b = new JButton(String.valueOf(i));
            
          //Highlight day with event
            if (containEvent[i - 1])
            	b.setBackground(Color.YELLOW);
            
            //Highlight actual day if actual month is displayed
            if (i == LocalDate.now().getDayOfMonth() && isSelectedActualMonth(selectedDate))
            	b.setBackground(Color.GREEN);
            
            //Highlight selected day
            if (i == selectedDate.getDayOfMonth())
            	b.setBackground(Color.CYAN);
            
            //Handle button click
            b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	JButton eventSource = (JButton)e.getSource();
                	
                	//Clear highlight of previous selected day
                	if (previousSelectedDay != 0)
                		dayButtonsList.get(previousSelectedDay - 1).setBackground(null);
                	
                	//Highlight selected date
                	eventSource.setBackground(Color.CYAN);
                	
                	//If previous selected day had event, highlight it yellow again
                	if (containEvent[previousSelectedDay - 1])
                		dayButtonsList.get(previousSelectedDay - 1).setBackground(Color.YELLOW);
                	
                	//If previous selected day was actual day in actual month, highlight it green again
                	if (previousSelectedDay == LocalDate.now().getDayOfMonth() && isSelectedActualMonth(selectedDate))
                		dayButtonsList.get(previousSelectedDay - 1).setBackground(Color.GREEN);
                	
                	previousSelectedDay = Integer.parseInt(eventSource.getText());
                	
                	CalendarView.this.selectedDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue(), Integer.parseInt(eventSource.getText()));
                	eventListeners.forEach((el) -> el.onValueChanged());
                }
            });
            
            dayButtonsList.add(b);
            add(b);
        }
        
        //Fill rest of grid cells with empty labels
        for (int i = 7 + dayOfWeek + daysInMonth; i < 7*7; i++) {
        	JLabel label = new JLabel();
        	add(label);
        }
    }
    
    private boolean isSelectedActualMonth(LocalDate selectedDate) {
    	if (LocalDate.now().getYear() == selectedDate.getYear() && LocalDate.now().getMonthValue() == selectedDate.getMonthValue())
    		return true;
    	else 
			return false;
		
    }
    
    public void addCalendarViewListener(CalendarViewListener eventListener)
    {
        this.eventListeners.add(eventListener);
    }
    
    public void changeSelectedDate(LocalDate date) {
    	selectedDate = date;    	
    	drawCalendar();
    }
    
    //Highlight day with event
    public void setEventOnDay(int day) {
    	dayButtonsList.get(day - 1).setBackground(Color.YELLOW);
    	
    	//If it is selected day mark it cyan again
    	if (day == selectedDate.getDayOfMonth())
    		dayButtonsList.get(day - 1).setBackground(Color.CYAN);
    	
    	//If it was actual day then mark it green again
    	if (day == LocalDate.now().getDayOfMonth() && isSelectedActualMonth(selectedDate))
    		dayButtonsList.get(day - 1).setBackground(Color.GREEN);	
    	
    	containEvent[day - 1] = true;
    }
    
    //Remove highlight from day
    public void removeEventOnDay(int day) {
    	dayButtonsList.get(day - 1).setBackground(null);
    	
    	containEvent[day - 1] = false;
    	
    	//If it is selected day mark it cyan again
    	if (day == selectedDate.getDayOfMonth())
    		dayButtonsList.get(day - 1).setBackground(Color.CYAN);
    	
    	//If it was actual day then mark it green again
    	if (day == LocalDate.now().getDayOfMonth() && isSelectedActualMonth(selectedDate))
    		dayButtonsList.get(day - 1).setBackground(Color.GREEN);		
    }
    
    //Remove all highlights
    public void clearAllEvents() {
    	for (int i = 0; i < 31; i++)
    		removeEventOnDay(i+1);
    }
}
