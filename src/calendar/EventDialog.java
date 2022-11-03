package calendar;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;

public class EventDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtEventDescription, txtEventLocation;
	private String eventDescription, eventLocation;
	private LocalDate eventStartDate, eventEndDate;
	private LocalTime eventStartTime, eventEndTime;
	private JSpinner numEndDate, numStartTime, numEndTime, numNotifyTime;
	private int notifyMinutes;
	
	private boolean result = false;
	
	public boolean isResult() {
        return result;
    }
	/**
	 * Create the dialog.
	 */
	public EventDialog(Frame parent, LocalDate selectedDate) {
		super(parent,true);
		
		setEventStartDate(selectedDate);
		
		setTitle("Nowe wydarzenie");
		
		createUI();
	}
	
	
	public EventDialog(Frame parent, String description, String location, LocalDateTime startDateTime, LocalDateTime enddDateTime, int notifyMinutes) {
		super(parent,true);
		
		setEventStartDate(LocalDate.of(startDateTime.getYear(), startDateTime.getMonthValue(), startDateTime.getDayOfMonth()));
		
		setTitle("Modyfikacja wydarzenia");
		
		createUI();
		fillUI(description, location, startDateTime, enddDateTime, notifyMinutes);
	}
	
	private void createUI() {
		setBounds(100, 100, 318, 327);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JLabel lblNewLabel = new JLabel("Krótki opis wydarzenia");
			lblNewLabel.setAlignmentX(LEFT_ALIGNMENT);
			contentPanel.add(lblNewLabel);
		}
		{
			txtEventDescription = new JTextField();
			contentPanel.add(txtEventDescription);
			txtEventDescription.setColumns(10);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("Miejsce wydarzenia");
			contentPanel.add(lblNewLabel_1);
		}
		{
			txtEventLocation = new JTextField();
			contentPanel.add(txtEventLocation);
			txtEventLocation.setColumns(10);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_2 = new JLabel("Początek");
			contentPanel.add(lblNewLabel_2);
		}
		{
			JLabel lblEventStart = new JLabel();
			String actualDate = "" + eventStartDate.getDayOfMonth() + " " + MainWindow.getSelectedMonthYear(eventStartDate);
			lblEventStart.setText(actualDate);
			contentPanel.add(lblEventStart);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_5 = new JLabel("Godzina początku");
			contentPanel.add(lblNewLabel_5);
		}
		{
			SpinnerDateModel model = new SpinnerDateModel();
			numStartTime = new JSpinner(model);
			numStartTime.setEditor(new JSpinner.DateEditor(numStartTime, "HH:mm"));
			contentPanel.add(numStartTime);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_3 = new JLabel("Koniec wydarzenia");
			contentPanel.add(lblNewLabel_3);
		}
		{
			SpinnerDateModel model = new SpinnerDateModel();
			numEndDate = new JSpinner(model);
			numEndDate.setEditor(new JSpinner.DateEditor(numEndDate, "dd/MM/yyyy"));
			contentPanel.add(numEndDate);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_4 = new JLabel("Godzina zakończenia");
			contentPanel.add(lblNewLabel_4);
		}
		{
			SpinnerDateModel model = new SpinnerDateModel();
			numEndTime = new JSpinner(model);
			numEndTime.setEditor(new JSpinner.DateEditor(numEndTime, "HH:mm"));
			contentPanel.add(numEndTime);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_6 = new JLabel("Czas powiadomienia przed początkiem (min)");
			contentPanel.add(lblNewLabel_6);
		}
		{
			SpinnerNumberModel model = new SpinnerNumberModel(60, 1, 4320, 1);
			numNotifyTime = new JSpinner(model);
			contentPanel.add(numNotifyTime);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Zatwierdź");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						getEventTimes();
						
						//No input or input too short
						if (txtEventDescription.getText().length() < 4 || txtEventLocation.getText().length() < 4) {
							JOptionPane.showMessageDialog(null, "Nie podano opisu i/lub miejsca wydarzenai!");
							return;
						}
						
						if (!haveCorrectTime()) {
							JOptionPane.showMessageDialog(null, "Wydarzenie musi się kończyć przynajmniej 5 minut po początku!");
							return;
						}
						
						eventDescription = txtEventDescription.getText();
						eventLocation = txtEventLocation.getText();
						notifyMinutes = (Integer)numNotifyTime.getValue();
						
						result = true;
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Anuluj");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						result = false;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	//Fill interface with values (if we modify event
	private void fillUI(String description, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, int notifyMinutes) {
		txtEventDescription.setText(description);
		txtEventLocation.setText(location);
		
		Calendar cal = Calendar.getInstance();
		
		SpinnerDateModel model = new SpinnerDateModel();
		cal.set(startDateTime.getYear(), startDateTime.getMonthValue(), startDateTime.getDayOfMonth(), startDateTime.getHour(), startDateTime.getMinute());
		model.setValue(cal.getTime());
		numStartTime.setModel(model);
		
		model = new SpinnerDateModel();
		cal.set(endDateTime.getYear(), endDateTime.getMonthValue(), endDateTime.getDayOfMonth(), endDateTime.getHour(), endDateTime.getMinute());
		model.setValue(cal.getTime());
		numEndDate.setModel(model);
		
		model = new SpinnerDateModel();
		cal.set(endDateTime.getYear(), endDateTime.getMonthValue(), endDateTime.getDayOfMonth(), endDateTime.getHour(), endDateTime.getMinute());
		model.setValue(cal.getTime());
		numEndTime.setModel(model);
		
		numNotifyTime.setValue(notifyMinutes);
	}
	
	//Get selected times from JSpinners
	private void getEventTimes() {
		Calendar cal = Calendar.getInstance();
		
		SpinnerDateModel dateModel = (SpinnerDateModel) numEndDate.getModel();
		cal.setTime(dateModel.getDate());
		setEventEndDate(LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
		
		dateModel = (SpinnerDateModel) numEndTime.getModel();
		cal.setTime(dateModel.getDate());
		setEventEndTime(LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
		
		dateModel = (SpinnerDateModel) numStartTime.getModel();
		cal.setTime(dateModel.getDate());
		setEventStartTime(LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
		
		notifyMinutes = (Integer)numNotifyTime.getValue();
	}
	
	//Check if event ends earlier than start or ends too early (less than 5 minutes from event start or in the past)
	private boolean haveCorrectTime() {
		//End date earlier than start date
		if (getEventStartDate().compareTo(getEventEndDate()) > 0)
			return false;
		
		if (getEventStartDate().compareTo(getEventEndDate()) == 0 && getEventStartTime().compareTo(getEventEndTime()) > 0)
			return false;
		
		//Event ends less than 5 minutes from beginning
		if (getEventStartDate().compareTo(getEventEndDate()) == 0 && getEventStartTime().getHour() == getEventEndTime().getHour() && getEventStartTime().getMinute() >= getEventEndTime().getMinute() - 5)
			return false;
		
		return true;
	}
	public LocalDate getEventStartDate() {
		return eventStartDate;
	}
	public void setEventStartDate(LocalDate eventStartDate) {
		this.eventStartDate = eventStartDate;
	}
	public LocalDate getEventEndDate() {
		return eventEndDate;
	}
	public void setEventEndDate(LocalDate eventEndDate) {
		this.eventEndDate = eventEndDate;
	}
	public LocalTime getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(LocalTime eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	public LocalTime getEventEndTime() {
		return eventEndTime;
	}
	public void setEventEndTime(LocalTime eventEndTime) {
		this.eventEndTime = eventEndTime;
	}
	public int getNotifyMinutes() {
		return notifyMinutes;
	}
	public void setNotifyMinutes(int notifyMinutes) {
		this.notifyMinutes = notifyMinutes;
	}
	public String getEventDescription() {
		return eventDescription;
	}
	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}
	public String getEventLocation() {
		return eventLocation;
	}
	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}
}
