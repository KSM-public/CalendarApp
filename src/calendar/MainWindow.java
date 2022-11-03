package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

public class MainWindow extends JFrame {

	private JPanel contentPane;
	private CalendarView calendarView;
	private LocalDate selectedDate;
	private JLabel lblActualDate, lblSelectedDay;
	private JTable eventsTable;
	private CalendarLogic calendarLogic;
	private JTextField txtEventSearcher;
	
	public static void showMessageBox(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	
	/**
	 * Create the frame.
	 */
	public MainWindow(CalendarLogic calendarLogic) {
		this.calendarLogic = calendarLogic;
		
		if (calendarLogic.isConnectedToDatabase())
			setTitle("Kalendarz 10264 - Online");
		else
			setTitle("Kalendarz 10264 - Offline");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 952, 521);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		selectedDate = LocalDate.now();
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnuFile = new JMenu("Plik");
		menuBar.add(mnuFile);
		
		JMenuItem mitLoadFile = new JMenuItem("Otwórz z pliku");
		mitLoadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int dialogResult = JOptionPane.showConfirmDialog (null, "Czy napewno chcesz załadować dane z pliku? Dane w bazie (jeżeli nawiązano połączenie) zostaną nadpisane!", "Ostrzeżenie", JOptionPane.YES_NO_OPTION);
				
				if(dialogResult == JOptionPane.YES_OPTION){
					loadFIle();
				}
			}
		});
		mnuFile.add(mitLoadFile);
		
		JMenuItem mitSaveFile = new JMenuItem("Zapisz do pliku");
		mitSaveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveFile();
			}
		});
		mnuFile.add(mitSaveFile);
		
		JMenuItem mitConnectDB = new JMenuItem("Połącz z bazą danych");
		mitConnectDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (calendarLogic.isConnectedToDatabase()) {
					JOptionPane.showMessageDialog(null, "Jesteś już połączony z bazą danych.");
					return;
				}
				
				int dialogResult = JOptionPane.showConfirmDialog (null, "Czy napewno chcesz połączyć się z bazą danych? Obecnie wprowadzone zmiany zostaną nadpisane!", "Pytanie", JOptionPane.YES_NO_OPTION);
				
				if(dialogResult == JOptionPane.YES_OPTION){
					calendarLogic.connectDatabase();
					
					if (!calendarLogic.isConnectedToDatabase()) 
						return;
					else 
						setTitle("Kalendarz 10264 - Online");
					
					calendarView.clearAllEvents();
					refreshList();
				}
			}
		});
		mnuFile.add(mitConnectDB);
		
		JMenuItem mitDisconnectDB = new JMenuItem("Rozłącz z bazą danych");
		mitDisconnectDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!calendarLogic.isConnectedToDatabase()) {
					JOptionPane.showMessageDialog(null, "Nie jesteś połączony z bazą danych.");
					return;
				}
				
				int dialogResult = JOptionPane.showConfirmDialog (null, "Czy napewno chcesz rozłączyć się z bazą danych? Zmiany wprowadzone po rozłączeniu nie będą zapisywane w bazie!", "Ostrzeżenie", JOptionPane.YES_NO_OPTION);
				
				if(dialogResult == JOptionPane.YES_OPTION){
					calendarLogic.disconnectDatabase();
					setTitle("Kalendarz 10264 - Offline");
				}
			}
		});
		mnuFile.add(mitDisconnectDB);
		
		JMenuItem mitExport = new JMenuItem("Eksportuj");
		mitExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportICalendar();
			}
		});
		mnuFile.add(mitExport);
		
		JMenuItem mitSettings = new JMenuItem("Ustawienia");
		mitSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showSettings();
			}
		});
		mnuFile.add(mitSettings);
		
		JMenuItem mitExit = new JMenuItem("Zakończ");
		mitExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		mnuFile.add(mitExit);
		
		JMenu mnuHelp = new JMenu("Pomoc");
		menuBar.add(mnuHelp);
		
		JFrame mainFrame = this;
		JMenuItem mitAbout = new JMenuItem("O programie");
		mitAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showAboutDialog();
			}
		});
		mnuHelp.add(mitAbout);
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		
		JButton btnMonthPrevious = new JButton("<");
		btnMonthPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (selectedDate.getMonthValue() > 1)
					selectedDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue() - 1, 1);
				else 
					selectedDate = LocalDate.of(selectedDate.getYear() - 1, 12, 1);

				lblActualDate.setText(getSelectedMonthYear(selectedDate));
				lblSelectedDay.setText("Wydarzenia w dniu: " + getSelectedDateLong());
				
				calendarView.changeSelectedDate(selectedDate);
				refreshList();
			}
		});
		panel.add(btnMonthPrevious);
		
		lblActualDate = new JLabel();
		lblActualDate.setText(getSelectedMonthYear(selectedDate));
		panel.add(lblActualDate);
		
		JButton btnMonthNext = new JButton(">");
		btnMonthNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (selectedDate.getMonthValue() < 12)
					selectedDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue() + 1, 1);
				else 
					selectedDate = LocalDate.of(selectedDate.getYear() + 1, 1, 1);

				lblActualDate.setText(getSelectedMonthYear(selectedDate));
				lblSelectedDay.setText("Wydarzenia w dniu: " + getSelectedDateLong());
				
				calendarView.changeSelectedDate(selectedDate);
				refreshList();
			}
		});
		panel.add(btnMonthNext);
		
		calendarView = new CalendarView(selectedDate);
		calendarView.addCalendarViewListener(() -> {
		    //JOptionPane.showMessageDialog(null, "You selected day: " + calendarView.getSelectedDate());
		    selectedDate = calendarView.getSelectedDate();
		    lblSelectedDay.setText("Wydarzenia w dniu: " + getSelectedDateLong());
		    refreshList();
		});
		contentPane.add(calendarView);
		
		
		//If list was loaded then show loaded events on calendar
		if (calendarLogic.getEventsList().size() > 0)
			for(CalendarEvent calEv : calendarLogic.getEventsList()) {
	        	//Highlight day in actual month
	        	if (calEv.getStartDateTime().getYear() == selectedDate.getYear() && calEv.getStartDateTime().getMonthValue() == selectedDate.getMonthValue())
	        		calendarView.setEventOnDay(calEv.getStartDateTime().getDayOfMonth());
	        }
		
		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3);
		
		txtEventSearcher = new JTextField();
		panel_3.add(txtEventSearcher);
		txtEventSearcher.setColumns(40);
		
		JButton btnSearchEvent = new JButton("Szukaj wydarzenia");
		btnSearchEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				searchEvent();
			}
		});
		panel_3.add(btnSearchEvent);
			
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1);
		
		lblSelectedDay = new JLabel();
		lblSelectedDay.setText("Wydarzenia w dniu: " + getSelectedDateLong());
		panel_1.add(lblSelectedDay);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2);
		
		JButton btnNewEditEvent = new JButton("Dodaj wydarzenie");
		btnNewEditEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (selectedDate.compareTo(LocalDate.now()) < 0) {
					JOptionPane.showMessageDialog(null, "Nie można dodać wydarzenia w przeszłości!");
		            return;
				}
				
				addNewEventDialog();
			}
		});
		panel_2.add(btnNewEditEvent);
		
		JButton btnRemoveEvent = new JButton("Usuń wydarzenie");
		btnRemoveEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(eventsTable.getSelectedRow() < 0) {
		            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego elementu!");
		            return;
		        }
				
				int dialogResult = JOptionPane.showConfirmDialog (null, "Czy napewno chcesz usunąć wybrane wydarzenie? Nie można tego cofnąć!","Ostrzeżenie",JOptionPane.YES_NO_OPTION);
				
				if(dialogResult == JOptionPane.YES_OPTION){
					removeEvent();
				}
			}
		});
		
		JButton btnModifyEvent = new JButton("Modyfikuj wydarzenie");
		btnModifyEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(eventsTable.getSelectedRow() < 0) {
		            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego elementu!");
		            return;
		        }
				
				modifyEventDialog();
			}
		});
		panel_2.add(btnModifyEvent);
		panel_2.add(btnRemoveEvent);
		
		JButton btnEndEvent = new JButton("Zakończ/Wznów wydarzenie");
		btnEndEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(eventsTable.getSelectedRow() < 0) {
		            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego elementu!");
		            return;
		        }
				
				int dialogResult = JOptionPane.showConfirmDialog (null, "Czy chcesz zakończyć/wznowić wybrane wydarzenie? Nieaktywne wydarzenie nie generuje powiadomień.","Pytanie",JOptionPane.YES_NO_OPTION);
				
				if(dialogResult == JOptionPane.YES_OPTION){
				  endEvent();
				}
			}
		});
		panel_2.add(btnEndEvent);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane);
		
		eventsTable = new JTable();
		eventsTable.setFillsViewportHeight(true);
		eventsTable.setModel(new javax.swing.table.DefaultTableModel(
		            new Object [][] {
		                {null, null, null, null, null, null},
		                {null, null, null, null, null, null},
		                {null, null, null, null, null, null},
		                {null, null, null, null, null, null},
		                {null, null, null, null, null, null},
		                {null, null, null, null, null, null},
		                {null, null, null, null, null, null}
		            },
		            new String [] {
		                "Opis wydarzenia", "Aktywne", "Miejsce", "Początek", "Koniec", "Czas przypomnienia (min)"
		            }
		        ));
		eventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(eventsTable);
		
		refreshList();
	}
	
	public static String getSelectedMonthYear(LocalDate selectedDate) {
		String labelString;
		
		switch (selectedDate.getMonthValue()) {
			case 1:
				labelString = "Styczeń";
				break;
			
			case 2:
				labelString = "Luty";
				break;
				
			case 3:
				labelString = "Marzec";
				break;
				
			case 4:
				labelString = "Kwiecień";
				break;
				
			case 5:
				labelString = "Maj";
				break;
				
			case 6:
				labelString = "Czerwiec";
				break;
				
			case 7:
				labelString = "Lipiec";
				break;
				
			case 8:
				labelString = "Sierpień";
				break;
				
			case 9:
				labelString = "Wrzesień";
				break;
				
			case 10:
				labelString = "Październik";
				break;
				
			case 11:
				labelString = "Listopad";
				break;
				
			case 12:
				labelString = "Grudzień";
				break;
				
			default:
				labelString = "??";
				break;
		}
		
		labelString += " ";
		labelString += selectedDate.getYear();
		
		return labelString;
	}
	
	private String getSelectedDateLong() {
		String selectedDateString;
		
		selectedDateString = "" + selectedDate.getDayOfMonth() + " ";
		
		selectedDateString += getSelectedMonthYear(selectedDate);
		
		return selectedDateString;
	}
	
	private void showAboutDialog() {
		AboutDialog aboutDialog = new AboutDialog(this);
		aboutDialog.setVisible(true);
	}
	
	private void addNewEventDialog() {
		
		EventDialog eventDialog = new EventDialog(this, selectedDate);
		eventDialog.setVisible(true);
		
		//Dialog ended successfully
		if (eventDialog.isResult()) {
			
			//Can event be created?
			if (!calendarLogic.addNewEvent(eventDialog.getEventDescription(), eventDialog.getEventLocation(), LocalDateTime.of(eventDialog.getEventStartDate(),  eventDialog.getEventStartTime()), 
					LocalDateTime.of(eventDialog.getEventEndDate(), eventDialog.getEventEndTime()), eventDialog.getNotifyMinutes())) {
				
				JOptionPane.showMessageDialog(null, "Nie udało się stworzyć wydarzenia - identyczne już istnieje.");
				return;
			}				
		}
		
		refreshList();
	}
	
	private void modifyEventDialog() {
		int selectedRow = eventsTable.getSelectedRow();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		String selectedEventDescritpion = (String)eventsTable.getValueAt(selectedRow, 0);
		
		boolean selectedEventActive;
		if (((String)eventsTable.getValueAt(selectedRow, 1)).equals("Tak"))
			selectedEventActive = true;
		else 
			selectedEventActive = false;
		
		
		String selectedEventLocation = (String)eventsTable.getValueAt(selectedRow, 2);
		
		LocalDateTime selectedEventStart = LocalDateTime.parse((String)eventsTable.getValueAt(selectedRow, 3), formatter);
		
		LocalDateTime selectedEventEnd = LocalDateTime.parse((String)eventsTable.getValueAt(selectedRow, 4), formatter);
		
		int selectedEventNotifyTime = (Integer)eventsTable.getValueAt(selectedRow, 5);
		
		//Event nie jest aktywny
		if (!selectedEventActive) {
			JOptionPane.showMessageDialog(null, "Nie można modyfikować zakończonych (nie aktywnych) wydarzeń");
            return;
		}
		
		EventDialog eventDialog = new EventDialog(this, selectedEventDescritpion, selectedEventLocation, selectedEventStart, selectedEventEnd, selectedEventNotifyTime);
		
		eventDialog.setVisible(true);
		
		//Dialog ended successfully
		if (eventDialog.isResult()) {
					
			//Can event be modified?
			if (!calendarLogic.modifyEvent(selectedEventDescritpion,selectedEventLocation, selectedEventStart, selectedEventEnd, selectedEventNotifyTime,
					eventDialog.getEventDescription(), eventDialog.getEventLocation(), LocalDateTime.of(eventDialog.getEventStartDate(),  eventDialog.getEventStartTime()), 
					LocalDateTime.of(eventDialog.getEventEndDate(), eventDialog.getEventEndTime()), eventDialog.getNotifyMinutes())) {
						
					JOptionPane.showMessageDialog(null, "Nie udało się zmodyfikować wydarzenia.");
					return;
					}				
			}
				
			refreshList();
	}
	
	private void removeEvent() {
		int selectedRow = eventsTable.getSelectedRow();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		String selectedEventDescritpion = (String)eventsTable.getValueAt(selectedRow, 0);	
		
		String selectedEventLocation = (String)eventsTable.getValueAt(selectedRow, 2);
		
		LocalDateTime selectedEventStart = LocalDateTime.parse((String)eventsTable.getValueAt(selectedRow, 3), formatter);
		
		LocalDateTime selectedEventEnd = LocalDateTime.parse((String)eventsTable.getValueAt(selectedRow, 4), formatter);
		
		int selectedEventNotifyTime = (Integer)eventsTable.getValueAt(selectedRow, 5);
		
		if (!calendarLogic.removeEvent(selectedEventDescritpion, selectedEventLocation, selectedEventStart, selectedEventEnd, selectedEventNotifyTime)) {
			JOptionPane.showMessageDialog(null, "Nie udało się usunąć wydarzenia.");
			
			return;
		}
		
		refreshList();
	}
	
	private void endEvent() {
		int selectedRow = eventsTable.getSelectedRow();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		String selectedEventDescritpion = (String)eventsTable.getValueAt(selectedRow, 0);
		
		boolean selectedEventActive;
		if (((String)eventsTable.getValueAt(selectedRow, 1)).equals("Tak"))
			selectedEventActive = true;
		else 
			selectedEventActive = false;
		
		String selectedEventLocation = (String)eventsTable.getValueAt(selectedRow, 2);
		
		LocalDateTime selectedEventStart = LocalDateTime.parse((String)eventsTable.getValueAt(selectedRow, 3), formatter);
		
		LocalDateTime selectedEventEnd = LocalDateTime.parse((String)eventsTable.getValueAt(selectedRow, 4), formatter);
		
		int selectedEventNotifyTime = (Integer)eventsTable.getValueAt(selectedRow, 5);
		
		//Selected event active so end it. If inactive then mark it active again
		if (selectedEventActive) {		
			if (!calendarLogic.endEvent(selectedEventDescritpion, selectedEventLocation, selectedEventStart, selectedEventEnd, selectedEventNotifyTime)) {
				JOptionPane.showMessageDialog(null, "Nie zakończyć wydarzenia.");
				return;
			}
		}
		else {
			if (!calendarLogic.activeEvent(selectedEventDescritpion, selectedEventLocation, selectedEventStart, selectedEventEnd, selectedEventNotifyTime)) {
				JOptionPane.showMessageDialog(null, "Nie można wznowić wydarzenia, gdyż wydarzenie dobiegło już końca.");
				return;
			}
		}
		
		refreshList();
	}
	
	private void showSettings() {
		String[] databaseSetting = calendarLogic.getDatabaseSettings();
		int[] periods = calendarLogic.getPeriods();
		SettingsDialog settingsDialog = new SettingsDialog(this, databaseSetting[0], Integer.parseInt(databaseSetting[1]), databaseSetting[2], databaseSetting[3], 
														periods[0], periods[1], calendarLogic.isNotifySound());
			
		settingsDialog.setVisible(true);
			
		if(!settingsDialog.isResult())
			return;
			
		calendarLogic.setDatabaseSettings(settingsDialog.getDatabaseServer(), settingsDialog.getDatabaseUser(), settingsDialog.getDatabasePassword(), settingsDialog.getDatabasePort());
		calendarLogic.setPeriods(settingsDialog.getNotifyPeriod(), settingsDialog.getClearOldPeriod());
		calendarLogic.setNotifySound(settingsDialog.isNotifySound());
		calendarLogic.saveSettings();
	}
	
	private void saveFile() {
		 
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Zapisz do pliku");
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Plik XML", "xml");
		fileChooser.setFileFilter(filter);
		 
		int userSelection = fileChooser.showSaveDialog(this);
		 
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    File fileToSave = fileChooser.getSelectedFile();
		    
		    if (!calendarLogic.saveCalendarDataToFile(fileToSave.getAbsolutePath())) {
		    	MainWindow.showMessageBox("Błąd zapisu do pliku. Sprawdź czy masz uprawnienia do zapisu w danym miejscu i spróbuj ponownie.");
		    	return;
		    }
		    
		}
	}
	
	private void loadFIle() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Otwórz z pliku");
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Plik XML", "xml");
		fileChooser.setFileFilter(filter);
		 
		int userSelection = fileChooser.showOpenDialog(this);
		 
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    File fileToLoad = fileChooser.getSelectedFile();
		    
		    if (!calendarLogic.loadCalendarDataFromFile(fileToLoad.getAbsolutePath())) {
		    	MainWindow.showMessageBox("Błąd odczytu z pliku. Sprawdź uprawnienia oraz poprawność pliku i spróbuj ponownie.");
		    	return;
		    }
		    
		    calendarView.clearAllEvents();
		    refreshList();
		}
	}
	
	private void exportICalendar() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Eksport wydarzeń");
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Plik formatu iCalendar", "ical");
		fileChooser.setFileFilter(filter);
		 
		int userSelection = fileChooser.showSaveDialog(this);
		 
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    File fileToSave = fileChooser.getSelectedFile();
		    
		    if (!calendarLogic.exportICalendar(fileToSave.getAbsolutePath())) {
		    	MainWindow.showMessageBox("Błąd eksportu wydarzeń. Sprawdź czy masz uprawnienia do zapisu w danym miejscu i spróbuj ponownie.");
		    	return;
		    }
		    
		}
	}
	
	private void searchEvent() {
		LocalDateTime eventDateTime = calendarLogic.searchEvent(txtEventSearcher.getText());
		
		//Not found
		if (eventDateTime.compareTo(LocalDateTime.of(1970, 1, 1, 1, 1)) == 0) {
			MainWindow.showMessageBox("Nie udało się znaleźć podanego wydarzenia.");
			return;
		}
		
		selectedDate = eventDateTime.toLocalDate();
		calendarView.changeSelectedDate(selectedDate);
		refreshList();
	}
	
	private void refreshList() {
		DefaultTableModel model = (DefaultTableModel) eventsTable.getModel();
        model.setRowCount(0);
        
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String activeString;
        
        for(CalendarEvent calEv : calendarLogic.getEventsList()) {
        	//Show events on list only if they are in same date as selected
        	if (calEv.isActive())
        		activeString = "Tak";
        	else 
        		activeString = "Nie";
        	
        	if (calEv.getStartDateTime().getYear() == selectedDate.getYear() && calEv.getStartDateTime().getMonthValue() == selectedDate.getMonthValue() && calEv.getStartDateTime().getDayOfMonth() == selectedDate.getDayOfMonth())
        		model.addRow(new Object[]{calEv.getDescription(), activeString, calEv.getLocation(), calEv.getStartDateTime().format(format), calEv.getEndDateTime().format(format), calEv.getNotifyMinutes()});
        	
        	//Highlight day in actual month
        	if (calEv.getStartDateTime().getYear() == selectedDate.getYear() && calEv.getStartDateTime().getMonthValue() == selectedDate.getMonthValue())
        		calendarView.setEventOnDay(calEv.getStartDateTime().getDayOfMonth());
        }
	}
}
