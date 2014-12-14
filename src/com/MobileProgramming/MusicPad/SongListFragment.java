package com.MobileProgramming.MusicPad;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SongListFragment extends ListFragment {
    private ArrayList<Song> mSongs = new ArrayList<Song>();//initialize into nothing;
    public static final String PREFS_NAME = "SONG_APP";
    public static final String LIST_OF_SONGS = "List_of_Songs";
    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter mNdefExchangeFilters[];
    // Flag to indicate that Android Beam is available
    boolean mAndroidBeamAvailable  = false;

    MusicService serviceBinder;
    Intent i;
    

    private static final String LOG_KEY = "keyHere";
    SaveDataList saveDataList = new SaveDataList(); //used to save list in sharedpref

    
    
    @SuppressLint("NewApi") @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_KEY, "onCreate() called");
        setHasOptionsMenu(true); 
        //Thanks to Professor for this code
        //FragmentManager is responsible for calling onCreateOptionsMenu()
        //IMPORTANT. Tell FragmentManager that my SongListFragment needs to receive options menu callbacks
        // e.g. onCreateOptionsMenu(). 
        getActivity().setTitle(R.string.songs_title);
        //
        try {
        	mSongs = saveDataList.getSavedList(getActivity(), PREFS_NAME, LIST_OF_SONGS);
        	SongLab.get(getActivity()).setSongs(mSongs);
        	Log.i(LOG_KEY,"mSongs set to sdl");
        		
        }catch(NullPointerException e) {
        	
        }
        //update list in listview adapter
        if(mSongs!=null){
        SongAdapter adapter = new SongAdapter(mSongs);
        	setListAdapter(adapter);
        }
        else{
        	mSongs = SongLab.get(getActivity()).getSongs();
        	SongAdapter adapter = new SongAdapter(mSongs);
        	setListAdapter(adapter);
        }
        setRetainInstance(true);
        
        NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (mAdapter == null) {
            return;
        }
 
        if (!mAdapter.isEnabled()) {
            Toast.makeText(getActivity(), "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
        }
 
        mAdapter.setNdefPushMessageCallback(new CreateNdefMessageCallback(){

			@Override
			public NdefMessage createNdefMessage(NfcEvent event) {
				String message = "Hello World";
			     NdefRecord ndefRecord = NdefRecord.createMime("text/plain", message.getBytes());
			     NdefMessage ndefMessage = new NdefMessage(ndefRecord);
			     return ndefMessage;
			}}, getActivity(), getActivity());
     
        
   } 
    
    
    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState); 
        return v;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        
    	// get the Song from the adapter
        Song s = ((SongAdapter)getListAdapter()).getItem(position);
        
        // start SongPager
        Intent i = new Intent(getActivity(), SongPagerActivity.class);
        i.putExtra(SongFragment.EXTRA_SONG_ID, s.getId());
        startActivityForResult(i, 0);
    }

    //on back check if data changed
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((SongAdapter)getListAdapter()).notifyDataSetChanged();
    }

    //Options Menu Creation (content next time?)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_song_list, menu);
    }

    //Options Menu Method
    @TargetApi(11)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {         
        		//Those menu item IDs are defined in fragment_song_list.xml file. 
        
        	//addition of new song
			case R.id.menu_item_new_song:
				Song song = new Song();
				SongLab.get(getActivity()).addSong(song);
				Log.i(LOG_KEY, "New Song Put In");
			
				Intent i = new Intent(getActivity(), SongActivity.class);
				//open songactivity to create songfragment
				i.putExtra(SongFragment.EXTRA_SONG_ID, song.getId());
				startActivityForResult(i, 0);
				return true;
				
                
		    //deletion of song(s)
			case R.id.menu_item_delete_song:
				SongAdapter adapter = new SongAdapter(mSongs);
				//iterate through to check which are checked
				Iterator<Song> it = SongLab.get(getActivity()).getSongs().iterator();
				while(it.hasNext()) {
					Song s = it.next();
					if (s.isChecked()) { //delete if checked
						//delete song file if it exists
						if(s.getAudioPath() != null && !s.getAudioPath().isEmpty()){
							File file = new File(s.getAudioPath());
							if(file.exists())
								file.delete();
						}
						it.remove(); //remove from song list	
					}
				}
				
				//save data
				saveDataList.storeData(getActivity(),SongLab.get(getActivity()).getSongs(), PREFS_NAME,LIST_OF_SONGS);

				try {
					mSongs = saveDataList.getSavedList(getActivity(),PREFS_NAME, LIST_OF_SONGS);
					SongLab.get(getActivity()).setSongs(mSongs);
					Log.i(LOG_KEY, "mSongs set to sdl");

				} catch (NullPointerException e) {
					// nothing yet
				}
				
				//refresh list 
				adapter = new SongAdapter(mSongs);
				setListAdapter(adapter);
				adapter.notifyDataSetChanged();

				return true;
				
			
				
			case R.id.share:
				 Intent intent = getActivity().getIntent();
			     if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			         Parcelable[] rawMessages = intent.getParcelableArrayExtra(
			                 NfcAdapter.EXTRA_NDEF_MESSAGES);
			 
			         NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
			         Toast.makeText(getActivity(), new String(message.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
			 
			     } else
			         Toast.makeText(getActivity(), "Waiting for NDEF Message", Toast.LENGTH_LONG).show();
			     return true;
			 
			
              
            
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
            final Song c = getItem(position);

            TextView titleTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_titleTextView);
            titleTextView.setText(c.getTitle());
            TextView dateTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_dateTextView);
            dateTextView.setText(c.getDate().toString());
            
           
            CheckBox selectBox = 
            		(CheckBox)convertView.findViewById(R.id.selectCheck);
            selectBox.setChecked(false); //start off always unchecked
            selectBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					//set checked if checked
					SongLab.get(getActivity()).getSong(c.getId()).setChecked(isChecked);
					Log.i(LOG_KEY, "is checked: " + c.isChecked());
				}
            });
            
            
            return convertView;
        }
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Log.d(LOG_KEY,"onPause() called");
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

    
    @SuppressLint("NewApi") 
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String message = "test";
        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", message.getBytes());
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);
        return ndefMessage;
    }

}



