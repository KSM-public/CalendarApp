package calendar;

public class SettingsManager {
	private String databaseIP, databaseUser, databasePassword;
	private int databasePort, clearOldPeriod, notifyPeriod;
	private boolean notifySound;
	
	public SettingsManager() {
		setDatabaseIP("localhost");
		setDatabasePort(1433);
		
		setDatabaseUser("sa");
		setDatabasePassword("sa");
		
		setClearOldPeriod(30);
		setNotifyPeriod(10);
		
		setNotifySound(false);
	}
	
	public SettingsManager(String databaseIP, int databasePort, String databaseUser, String databasePassword, int clearOldPeriod, int notifyPeriod, boolean notifySound) {
		this.setDatabaseIP(databaseIP);
		this.setDatabasePort(databasePort);
		this.setDatabaseUser(databaseUser);
		this.setDatabasePassword(databasePassword);
		
		this.setClearOldPeriod(clearOldPeriod);
		this.setNotifyPeriod(notifyPeriod);
		this.setNotifySound(notifySound);
	}

	public String getDatabaseIP() {
		return databaseIP;
	}

	public void setDatabaseIP(String databaseIP) {
		this.databaseIP = databaseIP;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public int getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(int databasePort) {
		this.databasePort = databasePort;
	}

	public int getClearOldPeriod() {
		return clearOldPeriod;
	}

	public void setClearOldPeriod(int clearOldPeriod) {
		this.clearOldPeriod = clearOldPeriod;
	}

	public int getNotifyPeriod() {
		return notifyPeriod;
	}

	public void setNotifyPeriod(int notifyPeriod) {
		this.notifyPeriod = notifyPeriod;
	}

	public boolean isNotifySound() {
		return notifySound;
	}

	public void setNotifySound(boolean notifySound) {
		this.notifySound = notifySound;
	}
}
