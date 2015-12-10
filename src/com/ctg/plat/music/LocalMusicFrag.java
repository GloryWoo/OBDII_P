package com.ctg.plat.music;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LocalMusicFrag extends Fragment implements View.OnClickListener{

	
    @Override 
    public void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);         
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return container;  

    }
    
    @Override
 	public void onResume() {  
         super.onResume();  

     }  
   
     public void onDestroy(){
     	super.onDestroy();
     }
     
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
