package models.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.DateTime;


public class SyncUploadData {
	
	public SyncUploadData(){
		this.formsSync = new ArrayList<FormSet>();
	}

	private List<FormSet> formsSync;
	
	private String deviceId;
	
	private String deviceUserId;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastDeviceSyncDate;

	public List<FormSet> getFormsSets() {
		return formsSync;
	}

	public void setFormsSets(List<FormSet> formsSyncList) {
		this.formsSync = formsSyncList;
	}
	
	public void addFormSet(FormSet formSet) {
		this.formsSync.add(formSet);
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getDeviceUserId() {
		return deviceUserId;
	}

	public void setDeviceUserId(String deviceUserId) {
		this.deviceUserId = deviceUserId;
	}
	
	public Date getLastDeviceSyncDate() {
		return lastDeviceSyncDate;
	}

	public void setLastDeviceSyncDate(Date lastDeviceSyncDate) {
		this.lastDeviceSyncDate = lastDeviceSyncDate;
	}
	
}
