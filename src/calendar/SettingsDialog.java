package calendar;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSeparator;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SettingsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtDBServer;
	private JTextField txtDBUser;
	private JPasswordField txtDBPassword;
	private JSpinner numClearOldPeriod, numDBPort, numNotifyPeriod;
	private JCheckBox chkRemoveOldEvents;
	private JRadioButton rbtMessageBox, rbtSound;
	private ButtonGroup group;
	
	private boolean result;

	/**
	 * Create the dialog.
	 */
	public SettingsDialog(Frame parent, String databaseServer, int databasePort, String databaseUser, String databasePassword,  int clearOldPeriod, int notifyPeriod, boolean isNotifySound) {
		super(parent, true);
		setTitle("Ustawienia programu");
		
		result = false;
		
		setResizable(false);
		setBounds(100, 100, 461, 375);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JLabel lblNewLabel = new JLabel("IP serwera bazy");
			contentPanel.add(lblNewLabel);
		}
		{
			txtDBServer = new JTextField();
			txtDBServer.setText("localhost");
			contentPanel.add(txtDBServer);
			txtDBServer.setColumns(10);
		}
		{
			JLabel lblNewLabel_3 = new JLabel("Port serwera bazy danych");
			contentPanel.add(lblNewLabel_3);
		}
		{
			numDBPort = new JSpinner();
			SpinnerNumberModel model = new SpinnerNumberModel(1433, 0, 65535, 1);
			numDBPort = new JSpinner(model);
			contentPanel.add(numDBPort);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("Nazwa użytkownika bazy");
			contentPanel.add(lblNewLabel_1);
		}
		{
			txtDBUser = new JTextField();
			txtDBUser.setText("sa");
			contentPanel.add(txtDBUser);
			txtDBUser.setColumns(10);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_2 = new JLabel("Hasło użytkownika bazy");
			contentPanel.add(lblNewLabel_2);
		}
		{
			txtDBPassword = new JPasswordField();
			txtDBPassword.setText("sa");
			contentPanel.add(txtDBPassword);
		}
		{
			JSeparator separator = new JSeparator();
			contentPanel.add(separator);
		}
		{
			JLabel lblNewLabel_4 = new JLabel((String) null);
			contentPanel.add(lblNewLabel_4);
		}
		{
			chkRemoveOldEvents = new JCheckBox("Usuwaj stare wydarzenia");
			chkRemoveOldEvents.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (chkRemoveOldEvents.isSelected())
						numClearOldPeriod.setEnabled(true);
					else 
						numClearOldPeriod.setEnabled(false);			
				}
			});
			contentPanel.add(chkRemoveOldEvents);
		}
		
		JLabel lblNewLabel_5 = new JLabel("Starsze niż (dni):");
		contentPanel.add(lblNewLabel_5);
		
		numClearOldPeriod = new JSpinner();
		SpinnerNumberModel model = new SpinnerNumberModel(30, 1, 1000, 1);
		numClearOldPeriod = new JSpinner(model);
		numClearOldPeriod.setEnabled(false);
		contentPanel.add(numClearOldPeriod);
		
		JSeparator separator = new JSeparator();
		contentPanel.add(separator);
		
		JLabel lblNewLabel_6 = new JLabel("Rodzaj powiadomień");
		contentPanel.add(lblNewLabel_6);
		
		JPanel panel = new JPanel();
		contentPanel.add(panel);
		
		rbtMessageBox = new JRadioButton("Komunikat tekstowy");
		rbtMessageBox.setSelected(true);
		panel.add(rbtMessageBox);
		
		rbtSound = new JRadioButton("Komunikat dźwiękowy");
		panel.add(rbtSound);
		group = new ButtonGroup();
		group.add(rbtMessageBox);
		group.add(rbtSound);
		{
			JSeparator separator_1 = new JSeparator();
			contentPanel.add(separator_1);
		}
		{
			JLabel lblNewLabel_7 = new JLabel("Wysyłaj powiadomienia co (minuty): ");
			contentPanel.add(lblNewLabel_7);
		}
		{
			numNotifyPeriod = new JSpinner();
			SpinnerNumberModel model1 = new SpinnerNumberModel(10, 1, 60*24, 1);
			numNotifyPeriod = new JSpinner(model1);
			contentPanel.add(numNotifyPeriod);
		}
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Zapisz");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						result = true;
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				
				JButton cancelButton = new JButton("Anuluj");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						result = false;
						setVisible(false);
					}
				});
				okButton.setActionCommand("CANCEL");
				buttonPane.add(cancelButton);
			}
		}
		
		fillUI(databaseServer, databasePort, databaseUser, databasePassword, clearOldPeriod, notifyPeriod, isNotifySound);
	}
	
	private void fillUI(String databaseServer, int databasePort, String databaseUser, String databasePassword, int clearOldPeriod, int notifyPeriod, boolean isNotifySound) {
		txtDBServer.setText(databaseServer);
		numDBPort.setValue(databasePort);
		txtDBUser.setText(databaseUser);
		txtDBPassword.setText(databasePassword);
		
		if (clearOldPeriod > 0) {
			chkRemoveOldEvents.setSelected(true);
			numClearOldPeriod.setValue(clearOldPeriod);
			numClearOldPeriod.setEnabled(true);
		}
		
		numNotifyPeriod.setValue(notifyPeriod);
		group.clearSelection();
		rbtMessageBox.setSelected(!isNotifySound);
		rbtSound.setSelected(isNotifySound);
	}

	public boolean isResult() {
		return result;
	}
	
	public String getDatabaseServer() {
		return txtDBServer.getText();
	}
	
	public int getDatabasePort() {
		return (Integer)numDBPort.getValue();
	}
	
	public String getDatabaseUser() {
		return txtDBUser.getText();
	}
	
	public String getDatabasePassword() {
		return txtDBPassword.getText();
	}
	
	public int getClearOldPeriod() {
		if (chkRemoveOldEvents.isSelected())
			return (Integer)numClearOldPeriod.getValue();
		else 
			return -1;
	}
	
	public boolean isNotifySound() {
		if (rbtMessageBox.isSelected())
			return false;
		else 
			return true;
	}
	
	public int getNotifyPeriod() {
		return (Integer)numNotifyPeriod.getValue();
	}
}
