package com.gp.app.professionalpa.data;

import android.os.Parcel;
import android.os.Parcelable;

public class NoteItem implements Parcelable 
{
	public static final String NOTE_ID = "noteId";
	public static final String DATA = "data";
    public static final String	IMAGE_NAME = "imageName";
    public static final String	TEXT_COLOR ="textColor";
    public static final String NOTE_ITEM_TABLE_NAME = "NoteItem";
	public static final String NOTE_ITEM_VIRTUAL_TABLE = "VirtualNoteItems";

	private String itemText = null;
	
	private String imageName = null;

	private int textColour = 0;
	
	public NoteItem(String textViewData)
	{
		this.itemText = textViewData;
	}
	
	public NoteItem(String textViewData, String imageName) 
	{
		this(textViewData);
		
		this.imageName = imageName;
	}
	
	public NoteItem() 
	{
	}

	public String getText() 
	{
		return itemText == null ? "" : itemText;
	}

	public void setTextViewData(String textViewData) 
	{
		this.itemText = textViewData;
	}
	
	public String getImageName() 
	{
		String imageName1 = imageName == null ? "" : imageName;
		
		return imageName1;
	}

	public void setTextColour(int colour)
	{
		textColour = colour;
	}
	
	public int getTextColour()
	{
		return textColour;
	}
	
	public void setImageName(String imagePath) 
	{
		this.imageName = imagePath;
	}

	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeStringArray(new String[]{itemText, imageName});
		
		dest.writeInt(textColour);
		
//		if(imagePath != null)
//		{
//			ByteArrayOutputStream stream = new ByteArrayOutputStream();
//			
//			imagePath.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//			
//			byte[] imageArray = stream.toByteArray();
//
//			dest.writeByteArray(imageArray);
//			
//			dest.writeInt(imageArray.length);
//		}
//		else
//		{
//			dest.writeInt(0);
//		}
	}

	public static final Parcelable.Creator<NoteItem> CREATOR = new Parcelable.Creator<NoteItem>() 
	{
		@Override
		public NoteItem createFromParcel(Parcel source) {
			
			String [] itemProperties = new String[2];
			
			source.readStringArray(itemProperties);
			
			int colour = source.readInt();
			
			NoteItem noteListItem = new NoteItem(itemProperties[0], itemProperties[1]);
			
			noteListItem.setTextColour(colour);
			
			return noteListItem;
		}

		@Override
		public NoteItem[] newArray(int size) 
		{
			return new NoteItem[size];
		}
	};
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("textViewData="+itemText+"\n");
		
		builder.append("imageName="+imageName+"\n");
		
		return builder.toString();
	}
	
	public String convertToString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("ITEM_TEXT="+itemText);
		
		sb.append("IMAGE_NAME="+imageName);
		
		return sb.toString();
	}
	
	public int getLength()
	{
		int textFactor = 0;
		
		if(itemText != null)
		{
			//TODO constant 22 has to be changed according to device size. Ver important- has to be changed.
			textFactor = itemText.length() > 22 ? Math.abs((int)Math.ceil(itemText.length()/22.0)) : 1;
		}
		
		int length =  textFactor + (imageName == null || imageName.trim().length() == 0  ? 0 : 10);

		return length;
	}
}