package calendar;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

public class Main {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		CalendarLogic calendarLogic = new CalendarLogic(args);
		
		if (!calendarLogic.isConnectedToDatabase()) {
			int dialogResult = JOptionPane.showConfirmDialog (null, "Nie udało się połączyć z bazą danych. Czy chcesz używać programu offline?", 
					"Pytanie", JOptionPane.YES_NO_OPTION);
		
			if(dialogResult == JOptionPane.NO_OPTION)
				return;
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow(calendarLogic);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
