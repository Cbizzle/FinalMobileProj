package com.MobileProgramming.MusicPad;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.MobileProgramming.MusicPad.MusicService.MusicBinder;
//import android.media.AudioTrack;

public class SongFragment extends Fragment {
	private static final String LOG_KEY = "keyHere";
	private static final String TAG = "audioStuff";
    public static final String EXTRA_SONG_ID = "MusicPad.SONG_ID";
    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_DATE = 0;
    public static final String PREFS_NAME = "SONG_APP";
    public static final String LIST_OF_SONGS = "List_of_Songs";
    private static final int RECORDER_SAMPLERATE =22050;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    int sampleSeek = 22050;
   // private static final int PLAYBACK_CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
   // private static final int PLAYBACK_SAMPLERATE = 44100;
    private AudioRecord recorder=null;
   // private AudioTrack player=null;
    private Thread recordingThread = null;
   // private Thread playbackThread = null;
    private boolean isRecording = false;
   // private boolean isPlaying = false;
	private boolean musicBound=false;
    private SeekBar seekBar;
    
    MusicService musicSrv;
    Intent i;
    
    SaveDataList saveDataList = new SaveDataList();
    String mAudioPath;
    Song mSong;
    EditText mTitleField;
    Button mDateButton;
    Button mRecordButton;
    Button mStopButton;
    Button mPlayButton;
    
  
    

    public static SongFragment newInstance(UUID songId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SONG_ID, songId);

        SongFragment fragment = new SongFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_KEY, "song fragment created");
        
        UUID songId = (UUID)getArguments().getSerializable(EXTRA_SONG_ID);
        SongLab.get(getActivity()).getSong(songId).setChecked(false); //return to default
        mSong = SongLab.get(getActivity()).getSong(songId);
        Log.i(LOG_KEY,"Frag isChecked? :" + mSong.isChecked());
        
        setHasOptionsMenu(true);
        //Tell fragment manager that this fragment should receive a call to onOptionsItemSelected(...)
        // on behalf of the hosting activity when OS does callback on this method. 
        
        
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	saveDataList.storeData(getActivity(), SongLab.get(getActivity()).getSongs(), PREFS_NAME, LIST_OF_SONGS);
    }
    public void updateDate() {
        mDateButton.setText(mSong.getDate().toString());
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_song, parent, false);
  
        //Enable the app icon as an up button, and display < 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        		if (NavUtils.getParentActivityName( getActivity( ) )  != null)  
        			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }   
        //enable the home-as-up button.
        //This enabled icon is treated as an existing option menu item. 
        
        
        //mAudioPath=mSong.getAudioPath(); Not needed
        mTitleField = (EditText)v.findViewById(R.id.song_title);
        mTitleField.setText(mSong.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mSong.setTitle(c.toString());
                mAudioPath=Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+c.toString()
                        + ".pcm";
                Log.i(TAG, c.toString());
                mSong.setAudioPath(mAudioPath);
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });
        
        mDateButton = (Button)v.findViewById(R.id.song_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                    .newInstance(mSong.getDate());
                dialog.setTargetFragment(SongFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });
        
        mRecordButton = (Button)v.findViewById(R.id.recordButton);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi") public void onClick(View v) {
				if(mSong.getAudioPath()!=null){
					if(!isRecording){
						try {
							recordAudio(v);
						} catch (IOException e) {
							e.printStackTrace();
						}
					
						mRecordButton.setBackground(getResources().getDrawable(R.drawable.recorddusheddisabled));
					}
					else if (isRecording) {
						stopClicked(v);
						mRecordButton.setBackground(getResources().getDrawable(R.drawable.recordnormal));
					}
				}
				else{
					Toast.makeText(getActivity(), "Please Enter Song Name First", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
       
        mPlayButton =(Button)v.findViewById(R.id.playButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
        	
        	public void onClick(View v) {
        		musicBound=true;
        		try {
					playAudio(v);
				} catch (IOException e) {
					e.printStackTrace();
				}
        		
        		
        	}
        	
        });
        
        seekBar = (SeekBar) v.findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

        	          int progress = 0;
        	          @Override
        	          public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
        	              progress = progresValue;
        	           
        	              playbackrate(progress+11000);
        	          }
        	          		
        	          @Override
        	          public void onStartTrackingTouch(SeekBar seekBar) {
        	          
        	        	  
        	          }
        	          @Override
        	          public void onStopTrackingTouch(SeekBar seekBar) {
        	        	  playbackrate(progress+11000);
        	       
        	          }
        	       });
        seekBar.setEnabled(true);
    	
    	
   
       
        return v; 
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_DATE) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mSong.setDate(date);
            updateDate();
        }
    }

    //option menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Thanks to professor for this code
        	// We don't need to define or inflate the app icon menu item in an XML file. 
        	//It comes with a ready-made resource ID: android.R.id.home
        	case android.R.id.home :
          	
        		if (NavUtils.getParentActivityName(getActivity())!=null )
        			NavUtils.navigateUpFromSameTask(getActivity());
                //Set parent in androidmanifest.xml
                
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } 
    }
    
    private byte[] short2byte(short[] sData) {
    	//converts audio from type short to type byte
    	int shortArrsize= sData.length;
    	byte[] bytes = new byte [shortArrsize *2];
    	for (int i = 0; i < shortArrsize; i++) {
    		bytes [i*2] = (byte) (sData[i] & 0x00FF);
    		bytes[(i*2)+1] = (byte) (sData[i] >>8);
    		sData[i] = 0;		
    	}
    	return bytes;
    }
    
    int BufferElements2Rec=1024;
    int BytesPerElement = 2;
    public void recordAudio (View view) throws IOException
    {
    			
    	recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, 
    							   RECORDER_AUDIO_ENCODING, 16000 * 30);
    	recorder.startRecording();
    	isRecording=true;
    	recordingThread=new Thread(new Runnable() {
    		public void run(){
    			writeAudioDataToFile();
    		//	wavIO convert;
    			/*convert= new wavIO(mSong.getAudioPath());
    			convert.read();
        		convert.save();*/
    		}
    	}, "AudioRecorder Thread");
    	recordingThread.start();
    }
    
    private void writeAudioDataToFile() {
    	//Writing to File Path
    	String filePath=mSong.getAudioPath();
    	short sData[] = new short[BufferElements2Rec];
    	
    	FileOutputStream os = null;
    	try {
    			os = new FileOutputStream(filePath);
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	}
    	
    	while (isRecording) {
    		//Method for mic to byte conversion
    		
    		recorder.read(sData, 0,BufferElements2Rec);
    		System.out.println("Short writing to file " + sData.toString());
    		try {
    			byte bData[] = short2byte(sData);
    			os.write(bData, 0, BufferElements2Rec * BytesPerElement);
    		} catch(IOException e) {
    			e.printStackTrace();
    		}
    	}
    	try {
    		os.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
   }
    
    public void stopClicked (View view)
    {
    	if(null !=recorder) {
    		isRecording = false;
    		recorder.stop();
    		recorder.release();
    		recorder = null;
    		recordingThread= null;
    		
    	}
    }
    public void playAudio (View view) throws IOException{ 
    	startService(view);
    	
  
    	
    	  }
  //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        MusicBinder binder = (MusicBinder)service;
        //get service
        musicSrv = binder.getService();
        //pass list
        musicBound = true;
        musicSrv.setSong(mSong);
        musicSrv.playbackrate(sampleSeek);
      }
      @Override
      public void onServiceDisconnected(ComponentName name) {
        musicBound = false;
      }
    };
    public void startService(View view) {
        //startService(  new Intent(getBaseContext(), MyService.class)   ); //works
     getActivity().bindService(new Intent(getActivity(), MusicService.class), musicConnection, Context.BIND_AUTO_CREATE); 
	 getActivity().startService(new Intent(getActivity(), MusicService.class)   ); //works
    }
    
    public void stopService(View view) {
    	getActivity().stopService(new Intent(getActivity().getBaseContext(), MusicService.class));
    }
    
    private void playbackrate(int rate) {
    	if (!musicBound){
    		sampleSeek=rate;
    	}
    	else{
    	sampleSeek=rate;
    	musicSrv.playbackrate(rate);
    	}
    }
    


}
 

