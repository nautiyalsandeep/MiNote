package com.gp.app.professionalpa.calendar.interfaces;

import com.gp.app.professionalpa.calendar.events.Event;

public interface DBchangePublisher 
{
    void addDataChangeListener(DBChangeListener listener);
    
	void notifyAllListeners(Event event);
}