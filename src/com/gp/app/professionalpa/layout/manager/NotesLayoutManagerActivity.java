package com.gp.app.professionalpa.layout.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseIntArray;
import android.view.FrameStats;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.gp.app.professionalpa.R;
import com.gp.app.professionalpa.calendar.events.database.CalendarDBManager;
import com.gp.app.professionalpa.calendar.ui.ProfessionalPACalendarView;
import com.gp.app.professionalpa.colorpicker.ColourPickerChangeListener;
import com.gp.app.professionalpa.data.NoteListItem;
import com.gp.app.professionalpa.data.ProfessionalPANote;
import com.gp.app.professionalpa.exceptions.ProfessionalPABaseException;
import com.gp.app.professionalpa.export.ProfessionalPANotesExporter;
import com.gp.app.professionalpa.interfaces.ProfessionalPAConstants;
import com.gp.app.professionalpa.notes.database.NotesDBManager;
import com.gp.app.professionalpa.notes.fragments.FragmentCreationManager;
import com.gp.app.professionalpa.notes.fragments.NotesManager;
import com.gp.app.professionalpa.notes.fragments.ProfessionalPANoteFragment;
import com.gp.app.professionalpa.notes.operations.NotesOperationManager;
import com.gp.app.professionalpa.util.ProfessionalPAParameters;

//TODO create notes for calendar events also
public class NotesLayoutManagerActivity extends Activity implements ColourPickerChangeListener, OnQueryTextListener 
{
	private static final String NUMBER_OF_LINEAR_LAYOUTS = "NUMBER_OF_LINEAR_LAYOUTS";

	private static final int LIST_ACTIVITY_RESULT_CREATED = 1;
	
	private static final int PARAGRAPH_ACTIVITY_RESULT_CREATED = 2;

	private static final byte NUMBER_OF_LINEAR_LAYOUT_FOR_SMALL_SCREEN_PORTRAIT = 1;

	private static final byte NUMBER_OF_LINEAR_LAYOUT_FOR_SMALL_SCREEN_LANDSCAPE = 2;

	private static final short NUMBER_OF_LINEAR_LAYOUT_FOR_NORMAL_SCREEN_PORTRAIT = 2;

	private static final short NUMBER_OF_LINEAR_LAYOUT_FOR_NORMAL_SCREEN_LANDSCAPE = 3;

	private static final short NUMBER_OF_LINEAR_LAYOUT_FOR_LARGE_SCREEN_PORTRAIT = 3;

	private static final short NUMBER_OF_LINEAR_LAYOUT_FOR_LARGE_SCREEN_LANDSCAPE = 4;

	private static final short NUMBER_OF_LINEAR_LAYOUT_FOR_EXTRA_LARGE_SCREEN_PORTRAIT = 4;

	private static final short NUMBER_OF_LINEAR_LAYOUT_FOR_EXTRA_LARGE_SCREEN_LANDSCAPE = 5;

	private SparseIntArray linearLayoutOccupancy = new SparseIntArray(2);
	
	private Map<Integer, FrameLayout> childFrames = new LinkedHashMap<Integer, FrameLayout>();

	private List<LinearLayout> linearLayouts = new ArrayList<LinearLayout>();

	private ImageLocationPathManager imageCaptureManager = null;
	
	private NotesDBManager.NotesSearchManager notesSearchManager = null;
	
	public NotesLayoutManagerActivity() 
	{
		super();
		
		imageCaptureManager = ImageLocationPathManager.getInstance();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ScrollView scrollView = (ScrollView) getLayoutInflater().inflate(
				R.layout.activity_notes_layout_manager, null);

		setContentView(scrollView);

		System.out.println("onCreate() called");
		
		int numberOfLinearLayouts = getNumberOfLinearLayouts();
		
		reInitializeLinearLayoutOccupancy(numberOfLinearLayouts);

		
//		linearLayoutAndIndexSelector = new LinearLayoutAndIndexSelector(
//				numberOfLinearLayouts);

		fillLinearLayoutList();

		NotesManager.getInstance().deleteAllNotes();

		try 
		{
			createNotes();
		} 
		catch (ProfessionalPABaseException e) 
		{
			//TODO improve
		}

		ActionBar actionBar = getActionBar();

		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#7F7CD9")));

		ProfessionalPAParameters.setNotesActivity(this);
		
		handleIntent(getIntent());
	}

	private void reInitializeLinearLayoutOccupancy(int numberOfLinearLayouts) 
	{
		for(int i = 0; i < numberOfLinearLayouts; i++)
		{
			linearLayoutOccupancy.append(i, 0);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{

		// outState.putStringArrayList(FRAGMENT_TAGS,
		// (ArrayList<String>)fragmentTags);

		outState.putByte(NUMBER_OF_LINEAR_LAYOUTS, (byte)linearLayoutOccupancy.size());

		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		// super.onRestoreInstanceState(savedInstanceState);
		//
		int numberOfLinearLayouts = (byte) savedInstanceState
				.getByte(NUMBER_OF_LINEAR_LAYOUTS);
		
		reInitializeLinearLayoutOccupancy(numberOfLinearLayouts);
		
		//
		// fragmentTags.addAll(savedInstanceState.getStringArrayList(FRAGMENT_TAGS));
		//
		// if(fragmentTags.size() > 0)
		// {
		createActivityLayoutInCaseOfConfigurationChange();
		// }
	}

	private void createActivityLayoutInCaseOfConfigurationChange()
	{
		updateNumberOfLinearLayoutsOnScreenChange(getResources()
				.getConfiguration());

		fillLinearLayoutList();

		//TODO to be removed if everything works fine in case of screen orientation change.
//		try 
//		{
//			System.out.println("createActivityLayoutInCaseOfConfigurationChange ->");
//			
//			createNotes();
//		} 
//		catch (ProfessionalPABaseException e) 
//		{
//			// TODO improve
//			e.printStackTrace();
//		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.notes_layout_manager_menu, menu);

		// Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    
	    SearchView searchView =
	            (SearchView) menu.findItem(R.id.actionSearch).getActionView();

	    searchView.setSearchableInfo(
	            searchManager.getSearchableInfo(getComponentName()));
	    
	    searchView.setOnQueryTextListener(this);
	    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();

		if (id == R.id.action_settings)
		{
			return true;
		} 
		else if (id == R.id.action_create_list_view) 
		{
			Intent intent = new Intent(getApplicationContext(),
					ListItemCreatorActivity.class);

			startActivityForResult(intent, LIST_ACTIVITY_RESULT_CREATED);
		} 
		else if (id == R.id.action_create_paragraph_view)
		{
			Intent intent = new Intent(getApplicationContext(),
					ParagraphNoteCreatorActivity.class);

			startActivityForResult(intent, LIST_ACTIVITY_RESULT_CREATED);
		}
		else if (id == R.id.export_notes) 
		{
			try 
			{
				ProfessionalPANotesExporter.export();
			}
			catch (ProfessionalPABaseException exception) 
			{
				// TODO
			}
		} 
		else if (id == R.id.import_notes) 
		{
			List<ProfessionalPANote> notes;

////			try
////			{
////				//TODO improve import functionality hampered
//////				notes = NotesDBManager.getInstance().readNotes(true);
//////
//////				ProfessionalPAParameters.getProfessionalPANotesWriter()
//////						.writeNotes(notes);
////			} 
////			catch (ProfessionalPABaseException e) 
////			{
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//
		} 
		else if(id == R.id.actionSearch)
		{
			notesSearchManager =  NotesDBManager.getInstance().new  NotesSearchManager();
		}
		else if (id == R.id.action_click_photo) 
		{
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(cameraIntent,
					ProfessionalPAConstants.TAKE_PHOTO_CODE);
		}
		else if(id == R.id.calendar)
		{
			Dialog dialog = new Dialog(this);
	        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        dialog.setContentView(new ProfessionalPACalendarView(this));
	        dialog.setCancelable(true);
	        dialog.show();
		}

		return super.onOptionsItemSelected(item);
	}

	public void updateNumberOfLinearLayoutsOnScreenChange(Configuration newConfig) 
	{
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 
		{
			reInitializeLinearLayoutOccupancy(linearLayoutOccupancy.size() + 1);
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			reInitializeLinearLayoutOccupancy(linearLayoutOccupancy.size() - 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		ProfessionalPANote note = null;

		if (requestCode == ProfessionalPAConstants.TAKE_PHOTO_CODE && resultCode == RESULT_OK) 
		{
			Bitmap photo = (Bitmap) data.getExtras().get("data");

			ImageLocationPathManager.getInstance().createAndSaveImage(photo);

			note = createProfessionalPANoteFromImage(imageCaptureManager
					.getMostRecentImageFilePath());

			NotesDBManager.getInstance().saveNotes(Arrays.asList(note));
		} 
		else
		{
			if (data != null) 
			{
				note = data.getParcelableExtra(ProfessionalPAConstants.NOTE_DATA);
			}
		}

		if (note != null)
		{
			createFragmentForNote(note);
		}
	}

	private ProfessionalPANote createProfessionalPANoteFromImage(String imagePath)
	{
		ProfessionalPANote note;

		ArrayList<NoteListItem> items = new ArrayList<NoteListItem>();

		items.add(new NoteListItem(null, ImageLocationPathManager.getInstance()
				.getImageName(imagePath)));

		note = new ProfessionalPANote(NotesManager.getInstance().getNextFreeNoteId(),
				ProfessionalPAConstants.IMAGE_NOTE, items);

		long creationTime = Long.valueOf(imageCaptureManager
				.getImageName(imagePath));

		note.setCreationTime(creationTime);

		note.setLastEditedTime(creationTime);

		note.setTypeOfNote(ProfessionalPAConstants.IMAGE_NOTE);

		note.setLastEditedTime(System.currentTimeMillis());
		return note;
	}

	private void createFragmentForNote(ProfessionalPANote note) 
	{
		if (note != null)
		{
			System.out.println("createFragmentForNote -> note ="+note);
			
			Fragment fragment = FragmentCreationManager.createFragment(note);

			NotesManager.getInstance().addNote(note.getNoteId(), note);

			if (fragment != null) 
			{
				createActivityLayout(fragment);
			}
		}
	}

	private void createActivityLayout(Fragment fragment) 
	{
        final FrameLayout frameLayout =  (FrameLayout)getLayoutInflater().inflate(R.layout.professional_pa_frame_layout, null, false);
		
		int noteId = ((ProfessionalPANoteFragment)fragment).getFragmentNoteId();

		System.out.println("createActivityLayout -> noteId="+noteId);
		
		int fragmentLength = ((ProfessionalPANoteFragment)fragment).getFragmentLength();
		
		System.out.println("createActivityLayout -> fragmentLength="+fragmentLength);

		frameLayout.setClickable(true);
		
//		frameLayout.setOnTouchListener(touchListener);
		
		int id = ProfessionalPAParameters.getId();
			
		frameLayout.setId(id);

		String tag = fragment.getTag() != null ? fragment.getTag() : "Tag-"
				.concat(Integer.toString(ProfessionalPAParameters.getId()));

		getFragmentManager().beginTransaction().add(id, fragment, tag).commit();

		childFrames.put(noteId, frameLayout);
		
		System.out.println("createActivityLayout -> frameLayout="+frameLayout+" fragmentLength="+fragmentLength);
		updateActivityView(frameLayout, fragmentLength);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		NotesManager.getInstance().deleteAllNotes();
	}

	public byte getNumberOfLinearLayouts() {

		byte numberOfLinearLayouts = -1;

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_EXTRA_LARGE_SCREEN_PORTRAIT;
			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_EXTRA_LARGE_SCREEN_LANDSCAPE;
			}
		}
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_LARGE_SCREEN_PORTRAIT;
			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_LARGE_SCREEN_LANDSCAPE;
			}
		} else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_NORMAL_SCREEN_PORTRAIT;
			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_NORMAL_SCREEN_LANDSCAPE;
			}
		} else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_SMALL_SCREEN_PORTRAIT;
			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				numberOfLinearLayouts = NUMBER_OF_LINEAR_LAYOUT_FOR_SMALL_SCREEN_LANDSCAPE;
			}
		} else {
		}

		return numberOfLinearLayouts;
	}

	private void updateActivityView(FrameLayout frameLayout, int fragmentLength)
	{
		int minimumOccupiedIndex = getMinimumOccupiedLayoutIndex();
		
		System.out.println("updateActivityView -> minimumOccupiedIndex="+minimumOccupiedIndex);

		int occupancy = linearLayoutOccupancy.get(minimumOccupiedIndex);
		
		occupancy = occupancy + fragmentLength;
		
		linearLayoutOccupancy.put(minimumOccupiedIndex, occupancy);
		
		LinearLayout linearLayout = linearLayouts.get(minimumOccupiedIndex);

		LinearLayout parentView = (LinearLayout) frameLayout.getParent();

		if (parentView != null) 
		{
			parentView.removeView(frameLayout);
		}

		System.out.println("updateActivityView -> frameLayout="+frameLayout);

		linearLayout.addView(frameLayout, 0);
	}

	private void fillLinearLayoutList() 
	{
		LinearLayout layout = null;

		LinearLayoutOnClickListener clickListener = new LinearLayoutOnClickListener();

		switch (linearLayoutOccupancy.size()) 
		{
		case 5:
			layout = (LinearLayout) findViewById(R.id.linearLayout5);
			layout.setOnClickListener(clickListener);
			linearLayouts.add(layout);
		case 4:
			layout = (LinearLayout) findViewById(R.id.linearLayout4);
			layout.setOnClickListener(clickListener);
			linearLayouts.add(layout);
		case 3:
			layout = (LinearLayout) findViewById(R.id.linearLayout3);
			layout.setOnClickListener(clickListener);
			linearLayouts.add(layout);
		case 2:
			layout = (LinearLayout) findViewById(R.id.linearLayout2);
			layout.setOnClickListener(clickListener);
			linearLayouts.add(layout);
		case 1:
			layout = (LinearLayout) findViewById(R.id.linearLayout1);
			layout.setOnClickListener(clickListener);
			linearLayouts.add(layout);
			break;
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
	}

	private void createNotes() throws ProfessionalPABaseException
	{
		List<ProfessionalPANote> parsedNotes = NotesDBManager.getInstance().readNotes();

		for (int i = 0, size = parsedNotes == null ? 0 : parsedNotes.size(); i < size; i++)
		{
			ProfessionalPANote note = parsedNotes.get(i);

			if (note != null)
			{
				createFragmentForNote(note);
			}
		}
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
	}

	public void deleteNote(int noteId)
	{
		FrameLayout layout = childFrames.get(noteId);

		if (layout != null) 
		{
			for (int i = 0; i < linearLayouts.size(); i++)
			{
				LinearLayout linearLayout = linearLayouts.get(i);

				linearLayout.removeView(layout);
			}
		}
	}

	public void addNote(int noteId)
	{
		ProfessionalPANote note = NotesManager.getInstance().getNote(noteId);

		if (note != null) {
			createFragmentForNote(note);
		}
	}

	class LinearLayoutOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) 
		{
			NotesOperationManager.getInstance().copyNote();
		}

	}

	public void openNoteInEditMode(int noteId) 
	{
		ProfessionalPANote note = NotesManager.getInstance().getNote(noteId);
		
		if(note.isListNote())
		{
			Intent intent = new Intent(getApplicationContext(),
					ListItemCreatorActivity.class);

			intent.putExtra(ProfessionalPAConstants.NOTE_ID, noteId);
			
			startActivityForResult(intent, LIST_ACTIVITY_RESULT_CREATED);
		}
		else
		{
			Intent intent = new Intent(getApplicationContext(),
					ParagraphNoteCreatorActivity.class);

			intent.putExtra(ProfessionalPAConstants.NOTE_ID, noteId);
			
			startActivityForResult(intent, PARAGRAPH_ACTIVITY_RESULT_CREATED);
		}
		
//		startActivity(intent);
	}

	@Override
	public void changeColour(int noteColor) 
	{
		final int selectedNoteId = NotesOperationManager.getInstance().getSelectedNoteId();
		
		FrameLayout frameLayout = childFrames.get(selectedNoteId);

		setFrameLayoutColour(noteColor, selectedNoteId, frameLayout);
	}

	private void setFrameLayoutColour(int noteColor, final int selectedNoteId,
			FrameLayout frameLayout)
	{
		if(frameLayout != null)
		{
			View view = frameLayout.getChildAt(0);
			
			if(view != null)
			{
				view.setBackgroundColor(noteColor);
				
				ProfessionalPANote note = NotesManager.getInstance().getNote(selectedNoteId);
				
				if(note != null)
				{
					note.setNoteColor(noteColor);

					NotesDBManager.getInstance().setNoteColorAttribute(selectedNoteId, noteColor);
				}
			}
		}
	}
	
	private int getMinimumOccupiedLayoutIndex() 
	{
		int minimumOccupiedLayoutIndex = 0;
		
		int minimumOccupancy = linearLayoutOccupancy.get(0);

		for(int linearLayoutIndex = 1; linearLayoutIndex < linearLayoutOccupancy.size(); linearLayoutIndex++)
		{
			int layoutOccupancy = linearLayoutOccupancy.get(linearLayoutIndex);
			
			if (layoutOccupancy < minimumOccupancy)
			{
				minimumOccupancy = layoutOccupancy;
				
				minimumOccupiedLayoutIndex = linearLayoutIndex;
			}
		}
		
		System.out.println("getMinimumOccupiedLayoutIndex -> minimumOccupiedLayoutIndex="+minimumOccupiedLayoutIndex
				+"minimumOccupancy="+minimumOccupancy);
		
		return minimumOccupiedLayoutIndex;
	}
	
	public void filterNotes(Set<Integer> noteIds) 
	{
		Set<Integer> childFrameIds = childFrames.keySet();
		
		if(childFrameIds != null)
		{
			for(int id : childFrameIds)
			{
				boolean isGone = false;
				
				if(!noteIds.contains(id))
				{
					isGone = true;
				}
				
				FrameLayout frameLayout = childFrames.get(id);
				
				if(frameLayout != null)
				{
					if(isGone)
					{
						frameLayout.setVisibility(View.GONE);
					}
					else
					{
						frameLayout.setVisibility(View.VISIBLE);
					}
				}
				
			}
		}
	}
	
	@Override
    protected void onNewIntent(Intent intent) 
	{
    	System.out.println("onNewIntent ->");

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) 
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) 
        {
        	System.out.println("handleIntent -> intent.getAction()="+intent.getAction());
        	
        	getActionBar().setDisplayShowHomeEnabled(false);
        	
            getActionBar().setDisplayShowTitleEnabled(false);
            
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }
    
    @Override
	public boolean onQueryTextChange(String query) 
	{
		System.out.println("onQueryTextChange -> query="+query);
		
		Set<Integer> noteIds = notesSearchManager.getMatchingNoteIds(query);
		
		System.out.println("onQueryTextChange -> noteIds="+noteIds);

		filterNotes(noteIds);
		
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}
}
