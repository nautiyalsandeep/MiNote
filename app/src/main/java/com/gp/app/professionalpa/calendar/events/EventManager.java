package com.gp.app.professionalpa.calendar.events;

import java.util.List;

import com.gp.app.professionalpa.calendar.events.database.CalendarDBManager;
import com.gp.app.professionalpa.data.Event;
import com.gp.app.professionalpa.notes.fragments.NotesManager;
import com.gp.app.professionalpa.notes.operations.NotesOperationManager;
import com.gp.app.professionalpa.notification.service.AlarmRequestCreator;


public class EventManager
{
	
    public static List<Event> getEvents(String startDay)
    {
    	return CalendarDBManager.getInstance().readEvents(startDay);
    }
    
    public static void deleteEvent(int day, int month, int year, int eventId)
    {
    	
    }
    
    public static void deleteEvents(int day, int month, int year)
    {
    	
    }
    
	public static void addEvent(Event event, boolean isEventEditMode)
	{
		if(isEventEditMode)
		{
			CalendarDBManager.getInstance().updateEventInDatabase(event);
		}
		else
		{
			CalendarDBManager.getInstance().saveEventToDatabase(event);
			
			NotesOperationManager.getInstance().createEventNote(event);
		}
		
		AlarmRequestCreator.createAlarmRequest(event.getStartDate(), event.getStartTime());
	}
}
