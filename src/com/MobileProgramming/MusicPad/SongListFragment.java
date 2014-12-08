package com.MobileProgramming.MusicPad;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SongListFragment extends ListFragment {
    private ArrayList<Song> mSongs = new ArrayList<Song>();//initialize into nothing;
    public static final String PREFS_NAME = "SONG_APP";
    public static final String LIST_OF_SONGS = "List_of_Songs";
    MusicService serviceBinder;
    Intent i;
    SaveDataList saveDataList = new SaveDataList();
    
    //private boolean mSubtitleVisible;
    //Keeps track the visibility status of the subtitle. 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("keyHere", "onCreate() called");
        setHasOptionsMenu(true); 
        //FragmentManager is responsible for calling onCreateOptionsMenu()
        //IMPORTANT. Tell FragmentManager that my CrimeListFragment needs to receive options menu callbacks
        // e.g. onCreateOptionsMenu(). 
        
        getActivity().setTitle(R.string.songs_title);
       /* mSongs = saveDataList.getSavedList(getActivity(), PREFS_NAME, LIST_OF_SONGS);
        try {
        	if(mSongs != null)
        		for(int i = 0; i < mSongs.size(); i++)
        			Log.d("keyHere",mSongs.get(i).getTitle());
        } catch(NullPointerException e) {
        	
        }*/
        try {
        	if(saveDataList.getSavedList(getActivity(), PREFS_NAME, LIST_OF_SONGS) != null) {
        		mSongs = saveDataList.getSavedList(getActivity(), PREFS_NAME, LIST_OF_SONGS);
        		SongLab.get(getActivity()).setSongs(mSongs);
        		Log.i("keyHere","mSongs set to sdl");
        		//saveDataList.storeData(getActivity(), SongLab.get(getActivity()).getSongs(), PREFS_NAME, LIST_OF_SONGS);
        	}
        }catch(NullPointerException e) {
        	
        }
        SongAdapter adapter = new SongAdapter(mSongs);
        setListAdapter(adapter);
        
        setRetainInstance(true);
        
        //a variable to keep track of the visibility state of the subtitle. 
        //mSubtitleVisible = false;
        
    }
    
    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {   
        	
        		//If the user rotates the device, we still need to keep the visibility status of the subtitle. 
           // if (mSubtitleVisible) {
           //     getActivity().getActionBar().setSubtitle(R.string.subtitle);
           // }
        }
        
        return v;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        
    	// get the Song from the adapter
        Song s = ((SongAdapter)getListAdapter()).getItem(position);
        
        // start an instance of CrimePagerActivity
        Intent i = new Intent(getActivity(), SongPagerActivity.class);
        i.putExtra(SongFragment.EXTRA_SONG_ID, s.getId());
        startActivityForResult(i, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((SongAdapter)getListAdapter()).notifyDataSetChanged();
    }

    //---- method to handle options menu ---
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	
        super.onCreateOptionsMenu(menu, inflater);
    
        inflater.inflate(R.menu.fragment_song_list, menu);

        //Some special processing on the "showSubtitle" MenuItem 
        //
		//If the user rotates the device, we still need to keep the visibility status of the subtitle, 
        // and set the correct text on the subtitle. 
        //MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
        
       // if (mSubtitleVisible && showSubtitle != null) {
        //    showSubtitle.setTitle(R.string.hide_subtitle);
       // }
    }

    //---- method to handle options menu ---
    @TargetApi(11)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {         
        		//Those menu item IDs are defined in fragment_crime_list.xml file. 
        
        	   case R.id.menu_item_new_song :
                Song song = new Song();
                SongLab.get(getActivity()).addSong(song);
                Log.i("keyHere","New Song Put In");
                
                Intent i = new Intent(getActivity(), SongActivity.class);
                //Here we start the CrimeActivity, not the CrimePagerActivity.
                
                i.putExtra(SongFragment.EXTRA_SONG_ID, song.getId());
                startActivityForResult(i, 0);
                return true;
                
           /* case R.id.menu_item_show_subtitle :
            		//We want to show different menu text, depending on whether subtitle is currently displayed or not. 
            		//
            		//In case the user put the phone in landscape mode, 
            		//we need to keep the subtitle visible if it is visible in vertical mode.
            		if (getActivity().getActionBar().getSubtitle() == null) {
	                    getActivity().getActionBar().setSubtitle(R.string.subtitle);
	                    mSubtitleVisible = true;
	                    item.setTitle(R.string.hide_subtitle);
	            	}  else {
	            		getActivity().getActionBar().setSubtitle(null);
	            		 mSubtitleVisible = false;
	            		item.setTitle(R.string.show_subtitle);
	            	}
                return true;*/
            
            default:
                return super.onOptionsItemSelected(item);
        } 
    }

    private class SongAdapter extends ArrayAdapter<Song> {
        public SongAdapter(ArrayList<Song> songs) {
            super(getActivity(), android.R.layout.simple_list_item_1, songs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (null == convertView) {
                convertView = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_song, null);
            }

            // configure the view for this Song
            Song c = getItem(position);

            TextView titleTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_titleTextView);
            titleTextView.setText(c.getTitle());
            TextView dateTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_dateTextView);
            dateTextView.setText(c.getDate().toString());

            return convertView;
        }
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Log.d("keyHere","onPause() called");
    	//saveDataList.storeData(getActivity(), SongLab.get(getActivity()).getSongs(), PREFS_NAME, LIST_OF_SONGS);
    	/*try{
    	 if(mSongs != null)
         	for(int i = 0; i < mSongs.size(); i++)
         		Log.d("keyHere",mSongs.get(i).getTitle() + "pause");
         } catch(NullPointerException e) {
         	
         }*/
    	
    }
 public void startService(View view) {
        //startService(  new Intent(getBaseContext(), MyService.class)   ); //works
	 getActivity().startService(new Intent(getActivity(), MusicService.class)   ); //works
    }
    
    public void stopService(View view) {
    	getActivity().stopService(new Intent(getActivity().getBaseContext(), MusicService.class));
    }

    

}



