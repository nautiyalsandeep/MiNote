package com.gp.app.professionalpa.data;

import java.io.Serializable;

import com.gp.app.professionalpa.interfaces.ProfessionalPAConstants;

import android.os.Parcel;
import android.os.Parcelable;

public class NoteListItem implements Parcelable 
{
	private static final byte HIGH_IMPORTANCE_INDEX = 0;
	
	private static final byte ALARM_ACTIVE_INDEX = 1;
	
	private String itemText = null;

	private boolean isImportanceHigh = false;

	private boolean isAlarmActive = false;

	public NoteListItem(String textViewData) {
		this.itemText = textViewData;
	}

	public NoteListItem(String textViewData, boolean isImportanceHigh) {

		this.itemText = textViewData;
		
		this.isImportanceHigh = isImportanceHigh;
	}

	public NoteListItem(String textViewData, boolean isImportanceHigh,
			boolean isAlarmActive) {

		this(textViewData, isImportanceHigh);

		this.isAlarmActive = isAlarmActive;
	}

	public NoteListItem() 
	{
	}

	public String getTextViewData() {
		return itemText;
	}

	public void setTextViewData(String textViewData) {
		this.itemText = textViewData;
	}

	public boolean isImportanceHigh() {
		return isImportanceHigh;
	}

	public void setImportanceHigh(boolean isImportanceHigh) {
		this.isImportanceHigh = isImportanceHigh;
	}

	public boolean isAlarmActive() {
		return isAlarmActive;
	}

	public void setAlarmActive(boolean isAlarmActive) {
		this.isAlarmActive = isAlarmActive;
	}

	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(itemText);

		dest.writeBooleanArray(new boolean[] { isImportanceHigh, isAlarmActive });
	}

	public static final Parcelable.Creator<NoteListItem> CREATOR = new Parcelable.Creator<NoteListItem>() {

		@Override
		public NoteListItem createFromParcel(Parcel source) {
			
			String textViewData = source.readString();
			
			boolean [] attributes = new boolean[2];
			
			source.readBooleanArray(attributes);
			
			boolean isImportanceHigh = attributes[HIGH_IMPORTANCE_INDEX];
			
			boolean isAlarmActive = attributes[ALARM_ACTIVE_INDEX];
			
			return new NoteListItem(textViewData, isImportanceHigh, isAlarmActive);
		}

		@Override
		public NoteListItem[] newArray(int size) {
			return new NoteListItem[size];
		}
	};
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("textViewData="+itemText+"\n");
		
		builder.append("isImportant="+isImportanceHigh+"\n");

		builder.append("isAlarm="+isAlarmActive+"\n");

		return builder.toString();
	}
	
	public String convertToString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("ITEM_TEXT="+itemText);
		
		String importanceText = isImportanceHigh ? "true":"false";
		
		sb.append("\n ITEM_IMPORTANCE="+importanceText);
		
		String alarmActiveText = isAlarmActive ? "true":"false";
		
		sb.append("\n ACTIVE_ALARM="+alarmActiveText);
		
		return sb.toString();
	}
}