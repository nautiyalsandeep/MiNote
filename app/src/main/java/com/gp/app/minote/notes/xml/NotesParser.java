package com.gp.app.minote.notes.xml;

import com.gp.app.minote.data.NoteItem;
import com.gp.app.minote.data.TextNote;
import com.gp.app.minote.exceptions.MiNoteBaseException;
import com.gp.app.minote.interfaces.XMLEntity;
import com.gp.app.minote.util.MiNoteUtil;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class NotesParser extends DefaultHandler
{
	private List<TextNote> notes = new ArrayList<TextNote>();
	
	private TextNote currentNote = null;
	
	private NoteItem currentNoteItem = null;
	
	private SAXParserFactory factory = null;
	
	private SAXParser saxParser = null;
	
	private boolean note = false;
	private boolean data = false;
	private boolean imageName = false;
	private boolean isImportant;

	private boolean textColor;
	
	public NotesParser() throws MiNoteBaseException
	{
		factory = SAXParserFactory.newInstance();
		
		try
		{
			saxParser = factory.newSAXParser();
		} 
		catch (ParserConfigurationException exception)
		{
		    throw new MiNoteBaseException("UNABLE_TO_READ_PROFESSIOANLPA_NOTES", exception);
		}
		catch (SAXException exception) 
		{
		    throw new MiNoteBaseException("UNABLE_TO_READ_PROFESSIOANLPA_NOTES", exception);
		}
	}
 
	public void startElement(String uri, String localName,String qName, 
                Attributes attributes) throws SAXException
    {
		if (qName.equalsIgnoreCase("note"))
		{
			currentNote = new TextNote();
			
			//Fetching the ID of TownCenter, we use it as a reference to fetch the child nodes.
			String typeOfNote = attributes.getValue("type");
			
            String CreationTime = attributes.getValue("creationTime");//new Date(attributes.getValue("creationTime")).getTime();
			
			String lastEditedTime = attributes.getValue("lastEditedTime"); //new Date(attributes.getValue("lastEditedTime")).getTime();

			String noteId = attributes.getValue("noteId");
			
			currentNote.setNoteId(Integer.valueOf(noteId));
			
			String noteColor = attributes.getValue("noteColor");

			currentNote.setNoteColor(Integer.valueOf(noteColor));

			currentNote.setCreationTime(MiNoteUtil.parseDateAndTimeString(CreationTime, "E yyyy.MM.dd 'at' hh:mm:ss:SSS a zzz"));

			currentNote.setLastEditedTime(MiNoteUtil.parseDateAndTimeString(lastEditedTime, "E yyyy.MM.dd 'at' hh:mm:ss:SSS a zzz"));
			
			currentNote.setTypeOfNote(Byte.valueOf(typeOfNote));
		}
 
		if(qName.equalsIgnoreCase("NoteItem"))
		{
			currentNoteItem = new NoteItem();
		}
		if (qName.equalsIgnoreCase("data")) 
		{
			data = true;
		}
 
		if (qName.equalsIgnoreCase("imageName")) 
		{
			imageName = true;
		}
		
		if (qName.equalsIgnoreCase("textColor")) 
		{
			textColor = true;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException 
    {
		if (qName.equalsIgnoreCase("note"))
		{
			note = false;
		}
 
		if (qName.equalsIgnoreCase("data")) 
		{
			data = false;
		}
 
		if (qName.equalsIgnoreCase("imageName")) 
		{
			imageName = false;
		}
		
		if (qName.equalsIgnoreCase("textColor")) 
		{
			textColor = false;
		}
		
		if(qName.equalsIgnoreCase("NoteItem"))
		{
			currentNote.addNoteItem(currentNoteItem);
		}
		
		if (qName.equalsIgnoreCase("note"))
		{
			notes.add(currentNote);
			
			currentNote.setState(XMLEntity.READ_STATE);
		}
 

	}
 
	public void characters(char ch[], int start, int length) throws SAXException 
	{
		if (data) 
		{
			String data = new String(ch, start, length);
			
			currentNoteItem.setTextViewData(data);
		}
 
		if (imageName) 
		{
			String imageName = new String(ch, start, length);
			
			currentNoteItem.setImageName(imageName);
		}
		
		if(textColor)
		{
            String textColor = new String(ch, start, length);
			
			currentNoteItem.setTextColour(Integer.valueOf(textColor));
		}
	}
	
	public List<TextNote> getNotes()
	{
		return notes;
	}

	public TextNote getNote(int noteId) 
	{
		TextNote result = null;
		
		for(int i = 0; i < notes.size(); i++)
		{
			TextNote note = notes.get(i);
			
			if(note.getId() == noteId)
			{
				result = note;
				
				break;
			}
		}
		
		return result;
	}
}
