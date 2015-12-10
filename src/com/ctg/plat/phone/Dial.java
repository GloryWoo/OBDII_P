package com.ctg.plat.phone;

import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class Dial extends Fragment implements View.OnClickListener{

	private TextView num1;
	private TextView num2;
	private TextView num3;
	private TextView num4;
	private TextView num5;
	private TextView num6;
	private TextView num7;
	private TextView num8;
	private TextView num9;
	private TextView num0;
	private TextView num_star;
	private TextView num_hash;
	private ImageView dial;
	private TextView numInput;
	private ImageView backSpace;
	String str = "";
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
		View view = inflater.inflate(R.layout.fragment_dial, container, false);
    	
    	View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(v.getId()){
				case R.id.num_1:
					str = numInput.getText() + "1";
			    	numInput.setText(str);
					break;
				case R.id.num_2:
					str = numInput.getText() + "2";
			    	numInput.setText(str);
					break;
				case R.id.num_3:
					str = numInput.getText() + "3";
			    	numInput.setText(str);
					break;
				case R.id.num_4:
					str = numInput.getText() + "4";
			    	numInput.setText(str);
					break;
				case R.id.num_5:
					str = numInput.getText() + "5";
			    	numInput.setText(str);
					break;
				case R.id.num_6:
					str = numInput.getText() + "6";
			    	numInput.setText(str);
					break;
				case R.id.num_7:
					str = numInput.getText() + "7";
			    	numInput.setText(str);
					break;
				case R.id.num_8:
					str = numInput.getText() + "8";
			    	numInput.setText(str);
					break;
				case R.id.num_9:
					str = numInput.getText() + "9";
			    	numInput.setText(str);
					break;
				case R.id.num_0:
					str = numInput.getText() + "0";
			    	numInput.setText(str);
					break;
				case R.id.num_star:
					str = numInput.getText() + "*";
			    	numInput.setText(str);
					break;
				case R.id.num_hash:
					str = numInput.getText() + "#";
			    	numInput.setText(str);
					break;					
				case R.id.imgCall:
					Intent intent = new Intent(Intent.ACTION_CALL , Uri.parse("tel:" +  numInput.getText()));
					getActivity().startActivity(intent);
					Base.OBDApp.callStat = 2;
					str = "";
					numInput.setText(str);
	                break;
				case R.id.backSpace:
					if(str == null || str.length() == 0)
						return;
					StringBuffer sb = new StringBuffer(str);
					sb.deleteCharAt(sb.length()-1);
					numInput.setText(sb);
					str = sb.toString();
					break;
				}
			}
		};
    	
    	num1 = (TextView)view.findViewById(R.id.num_1);
    	num2 = (TextView)view.findViewById(R.id.num_2);
    	num3 = (TextView)view.findViewById(R.id.num_3);
    	num4 = (TextView)view.findViewById(R.id.num_4);
    	num5 = (TextView)view.findViewById(R.id.num_5);
    	num6 = (TextView)view.findViewById(R.id.num_6);
    	num7 = (TextView)view.findViewById(R.id.num_7);
    	num8 = (TextView)view.findViewById(R.id.num_8);
    	num9 = (TextView)view.findViewById(R.id.num_9);
    	num0 = (TextView)view.findViewById(R.id.num_0);
    	num_star = (TextView)view.findViewById(R.id.num_star);
    	num_hash = (TextView)view.findViewById(R.id.num_hash);    	
    	
    	dial = (ImageView)view.findViewById(R.id.imgCall);
    	backSpace = (ImageView)view.findViewById(R.id.backSpace);
    	numInput = (TextView)view.findViewById(R.id.numInput);
    	
    	num1.setOnClickListener(listener);
    	num2.setOnClickListener(listener);
    	num3.setOnClickListener(listener);
    	num4.setOnClickListener(listener);
    	num5.setOnClickListener(listener);
    	num6.setOnClickListener(listener);
    	num7.setOnClickListener(listener);
    	num8.setOnClickListener(listener);
    	num9.setOnClickListener(listener);
    	num0.setOnClickListener(listener);
    	num_star.setOnClickListener(listener);
    	num_hash.setOnClickListener(listener);    	
    	
    	dial.setOnClickListener(listener);
    	backSpace.setOnClickListener(listener);
    	numInput.setText(str);
    	if(!str.equals(""))
    		numInput.setHint("");
    	return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
                  