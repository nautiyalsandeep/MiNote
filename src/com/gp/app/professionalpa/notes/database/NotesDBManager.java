package com.gp.app.professionalpa.notes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.gp.app.professionalpa.calendar.events.Event;
import com.gp.app.professionalpa.calendar.events.database.CalendarDBManager;
import com.gp.app.professionalpa.calendar.interfaces.DBChangeListener;
import com.gp.app.professionalpa.data.NoteListItem;
import com.gp.app.professionalpa.data.ProfessionalPANote;
import com.gp.app.professionalpa.util.ProfessionalPAParameters;
import com.gp.app.professionalpa.views.listeners.NoteItemLongClickListener;

public class NotesDBManager extends SQLiteOpenHelper implements ProfessionalPADBConstants
{
	private static final UriMatcher uriMatcher;
	
    private static NotesDBManager instance;
	
    private static final String[] PROJECTION_FOR_NOTE = 
		{
        ProfessionalPANote.NOTE_ID,
        ProfessionalPANote.NOTE_TYPE,
        ProfessionalPANote.NOTE_COLOR,
        ProfessionalPANote.NOTE_CREATION_TIME,
        ProfessionalPANote.NOTE_MODIFIED_TIME,
    };
    
    private static final String[] PROJECTION_FOR_NOTE_ITEM =  
		{
		NoteListItem.TEXT_COLOR,
		NoteListItem.IMAGE_NAME,
		NoteListItem.DATA,
    };

    
	private NotesDBManager() 
    {
        super(ProfessionalPAParameters.getApplicationContext(), ProfessionalPADBConstants.DATABASE_NAME, null, ProfessionalPADBConstants.DATABASE_VERSION);
    }

	public static NotesDBManager getInstance()
	{
		if(instance == null)
		{
			instance = new NotesDBManager();
		}
		
		return instance;
	}
	
    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                          int newVersion) 
    {
        db.execSQL("DROP TABLE IF EXISTS "+ProfessionalPANote.NOTE_TABLE_NAME);
        
        onCreate(db);
    }
    
	private void createTables(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " + ProfessionalPANote.NOTE_TABLE_NAME + "(" + ProfessionalPANote.NOTE_ID +  " INTEGER, " +
				ProfessionalPANote.NOTE_TYPE + " INTEGER, " + ProfessionalPANote.NOTE_COLOR + " INTEGER, " + ProfessionalPANote.NOTE_CREATION_TIME + " INTEGER, "
				+ ProfessionalPANote.NOTE_MODIFIED_TIME + " INTEGER);");
		
		db.execSQL("CREATE TABLE " + NoteListItem.NOTE_ITEM_TABLE_NAME + "(" + NoteListItem.NOTE_ID +  " INTEGER, " +
				NoteListItem.DATA + " TEXT, " + NoteListItem.IMAGE_NAME + " TEXT, " + NoteListItem.TEXT_COLOR + " INTEGER, "+
				" FOREIGN KEY ("+NoteListItem.NOTE_ID+") REFERENCES "+ProfessionalPANote.NOTE_TABLE_NAME+" ("+ProfessionalPANote.NOTE_ID+"));");
	}

	static 
	{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(ProfessionalPADBConstants.AUTHORITY, ProfessionalPANote.NOTE_TABLE_NAME, 1);
		uriMatcher.addURI(ProfessionalPADBConstants.AUTHORITY, ProfessionalPANote.NOTE_TABLE_NAME + "/#", 2);
		uriMatcher.addURI(ProfessionalPADBConstants.AUTHORITY, ProfessionalPANote.NOTE_TABLE_NAME + "/#/#", 3);
	
		uriMatcher.addURI(ProfessionalPADBConstants.AUTHORITY, NoteListItem.NOTE_ITEM_TABLE_NAME, 1);
		uriMatcher.addURI(ProfessionalPADBConstants.AUTHORITY, NoteListItem.NOTE_ITEM_TABLE_NAME + "/#", 2);
		uriMatcher.addURI(ProfessionalPADBConstants.AUTHORITY, NoteListItem.NOTE_ITEM_TABLE_NAME + "/#/#", 3);
	}
	
	public void saveNotes(List<ProfessionalPANote> notes) 
	{
	    for(int i = 0; i < notes.size(); i++)
	    {
	    	ProfessionalPANote note = notes.get(i);
	    	
	    	saveNote(note);
	    }
	}
	
	public void saveNote(ProfessionalPANote note) 
	{
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues noteValues = new ContentValues();
		noteValues.put(ProfessionalPANote.NOTE_ID, note.getNoteId());
		noteValues.put(ProfessionalPANote.NOTE_TYPE, note.getNoteType());
		noteValues.put(ProfessionalPANote.NOTE_COLOR, note.getNoteColor());
		noteValues.put(ProfessionalPANote.NOTE_CREATION_TIME, note.getCreationTime());
		noteValues.put(ProfessionalPANote.NOTE_MODIFIED_TIME, note.getLastEditedTime());
		
		db.insert(
				 ProfessionalPANote.NOTE_TABLE_NAME, null,
				 noteValues);
		
		List<NoteListItem> noteItems = note.getNoteItems();
		
		for(int i = 0; i < noteItems.size(); i++)
		{
			NoteListItem item = noteItems.get(i);
			ContentValues noteItemValues = new ContentValues();
			noteItemValues.put(NoteListItem.NOTE_ID, note.getNoteId());
			noteItemValues.put(NoteListItem.TEXT_COLOR, item.getTextColour());
			noteItemValues.put(NoteListItem.IMAGE_NAME, item.getImageName());
			noteItemValues.put(NoteListItem.DATA, item.getTextViewData());
			
			db.insert(
					 NoteListItem.NOTE_ITEM_TABLE_NAME,null,
					 noteItemValues);
		}
	}
	
	public List<ProfessionalPANote> readNotes()
	{
		List<ProfessionalPANote> notes = readNotes(null);
		
		return notes;
	}
	
	public ProfessionalPANote readNote(int noteId)
	{
		List<ProfessionalPANote> notes = readNotes(Integer.toString(noteId));
		
		return notes.get(0);
	}
	
	private List<ProfessionalPANote> readNotes(String noteId) 
	{
		SQLiteDatabase db = getReadableDatabase();

		List<ProfessionalPANote> notes = new ArrayList<ProfessionalPANote>();

    	String sortOrder =
    			ProfessionalPANote.NOTE_ID + " DESC";

    	Cursor cursor = null;
    	
    	if(noteId == null)
    	{
    		cursor = db.rawQuery("select * from "+ProfessionalPANote.NOTE_TABLE_NAME, null);
    	}
    	else
    	{
    		String where = NoteListItem.NOTE_ID+"=?";

        	String [] whereArguments = new String []{noteId};
        	
        	cursor = db.query(
        		ProfessionalPANote.NOTE_TABLE_NAME,  // The table to query
        		PROJECTION_FOR_NOTE,                               // The columns to return
        	    where,                                // The columns for the WHERE clause
        	    whereArguments,       // The values for the WHERE clause
        	    null,                                     // don't group the rows
        	    null,                                     // don't filter by row groups
        	    sortOrder                                 // The sort order
        	    );
    	}
    	
    	cursor.moveToFirst();
    	
    	while (cursor.isAfterLast() == false)
    	{
    		int readNoteId = (int)cursor.getInt(cursor.getColumnIndexOrThrow(ProfessionalPANote.NOTE_ID));
        	byte noteType = (byte)cursor.getInt(cursor.getColumnIndexOrThrow(ProfessionalPANote.NOTE_TYPE));
        	byte noteColor = (byte)cursor.getInt(cursor.getColumnIndexOrThrow(ProfessionalPANote.NOTE_COLOR));
        	long creationTime = cursor.getLong(cursor.getColumnIndexOrThrow(ProfessionalPANote.NOTE_CREATION_TIME));
        	long lastEditedTime = cursor.getLong(cursor.getColumnIndexOrThrow(ProfessionalPANote.NOTE_MODIFIED_TIME));
        	ProfessionalPANote note = new ProfessionalPANote(readNoteId, noteType, noteColor, creationTime, lastEditedTime);
    	    notes.add(note);
        	List<NoteListItem> noteItems = readNoteItems(readNoteId);
        	note.setNoteItems(noteItems);
        	cursor.moveToNext();
    	}
    	
		System.out.println("readNotes <- notes="+notes);

    	return notes;
	}

	private List<NoteListItem> readNoteItems(int noteId) 
	{
        List<NoteListItem> noteItems = new ArrayList<NoteListItem>();
		
		SQLiteDatabase db = getReadableDatabase();

    	String sortOrder =
    			NoteListItem.NOTE_ID + " DESC";

    	String where = NoteListItem.NOTE_ID+"=?";

    	Cursor cursor = db.query(
    		NoteListItem.NOTE_ITEM_TABLE_NAME,  // The table to query
    	    PROJECTION_FOR_NOTE_ITEM,                               // The columns to return
    	    where,                                // The columns for the WHERE clause
    	    new String []{Integer.toString(noteId)},                            // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    sortOrder                                 // The sort order
    	    );
    	
    	System.out.println("readNoteItems -> cursor ="+cursor.getCount());
    	
    	cursor.moveToFirst();
    	
    	NoteListItem item = null;
    	
    	while (cursor.isAfterLast() == false)
    	{
        	int noteColor = cursor.getInt(cursor.getColumnIndexOrThrow(NoteListItem.TEXT_COLOR));
        	String imageName = cursor.getString(cursor.getColumnIndexOrThrow(NoteListItem.IMAGE_NAME));
        	String itemData = cursor.getString(cursor.getColumnIndexOrThrow(NoteListItem.DATA));
        	item = new NoteListItem(itemData, imageName);
        	item.setTextColour(noteColor);
            noteItems.add(item);
        	cursor.moveToNext();
    	}
    	
    	return noteItems;
	}

	public void deleteNote(int noteId) 
	{
		SQLiteDatabase db = getWritableDatabase();

		int result = db.delete(ProfessionalPANote.NOTE_TABLE_NAME, ProfessionalPANote.NOTE_ID + "=?", new String[]{Integer.toString(noteId)});
	
		if(result > 0)
		{
			db.delete(NoteListItem.NOTE_ITEM_TABLE_NAME, NoteListItem.NOTE_ID + "=?", new String[]{Integer.toString(noteId)});
		}
	}

	public void updateNote(ProfessionalPANote note)
	{
        SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ProfessionalPANote.NOTE_COLOR, note.getNoteColor());
		values.put(ProfessionalPANote.NOTE_TYPE, note.getNoteType());
		values.put(ProfessionalPANote.NOTE_CREATION_TIME, note.getCreationTime());
		values.put(ProfessionalPANote.NOTE_MODIFIED_TIME, note.getLastEditedTime());
		
    	String where = ProfessionalPANote.NOTE_ID+"=?";

		int numberOfEffectedRows = db.update(ProfessionalPANote.NOTE_TABLE_NAME, values, where, new String[]{Integer.toString(note.getNoteId())});
    	
		if(numberOfEffectedRows > 0)
		{
			List<NoteListItem> items = note.getNoteItems();
			
	        db.delete(NoteListItem.NOTE_ITEM_TABLE_NAME, NoteListItem.NOTE_ID + "=?", new String[]{Integer.toString(note.getNoteId())});
	        
			for(int i = 0; i < items.size(); i++)
			{
				NoteListItem item = items.get(i);
				ContentValues noteItemValues = new ContentValues();
				noteItemValues.put(NoteListItem.NOTE_ID, note.getNoteId());
				noteItemValues.put(NoteListItem.TEXT_COLOR, item.getTextColour());
				noteItemValues.put(NoteListItem.IMAGE_NAME, item.getImageName());
				noteItemValues.put(NoteListItem.DATA, item.getTextViewData());
				
				db.insert(
						 NoteListItem.NOTE_ITEM_TABLE_NAME, null, noteItemValues);
			}
		}
	}

	public void setNoteColorAttribute(int noteId, int noteColor)
	{
        SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ProfessionalPANote.NOTE_COLOR, noteColor);
		
    	String where = ProfessionalPANote.NOTE_ID+"=?";

		int numberOfEffectedRows = db.update(ProfessionalPANote.NOTE_TABLE_NAME, values, where, new String[]{Integer.toString(noteId)});
	}
}
