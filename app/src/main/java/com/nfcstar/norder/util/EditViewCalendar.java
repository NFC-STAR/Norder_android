package com.nfcstar.norder.util;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class EditViewCalendar {
	Calendar 			cCal;
	EditText 			cTxt;
	DatePickerDialog	cDlg;
	
	public EditViewCalendar(Context context, EditText pTxt, int pDay){
		cTxt = pTxt;
		cCal = Calendar.getInstance();
		cCal.add(Calendar.DAY_OF_MONTH, pDay);
		
		int cYear  = cCal.get(Calendar.YEAR);
    	int cMonth = cCal.get(Calendar.MONTH);
    	int cDay   = cCal.get(Calendar.DAY_OF_MONTH);
    	
    	cDlg	= new DatePickerDialog(context, lsnr_date, cYear, cMonth, cDay);
    	
    	cMonth++;
    	
    	String cYEAR	= String.valueOf(cYear);
		String sMONTH 	= cMonth>9?String.valueOf(cMonth):"0"+cMonth;
		String sDAY 	= cDay>9?String.valueOf(cDay):"0"+cDay;
		
		cTxt.setText(cYEAR+"-"+sMONTH+"-"+sDAY);
		
		cTxt.setInputType(0);
		cTxt.setOnTouchListener(new View.OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				cDlg.show();
				return false;
			}
        });
	}
	
	
	
	private OnDateSetListener lsnr_date = new OnDateSetListener(){
    	public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
    		
    		monthOfYear++;
    		
			String cYEAR	= String.valueOf(year);
			String sMONTH 	= monthOfYear>9?String.valueOf(monthOfYear):"0"+monthOfYear;
			String sDAY 	= dayOfMonth>9?String.valueOf(dayOfMonth):"0"+dayOfMonth;
			cTxt.setText(cYEAR+"-"+sMONTH+"-"+sDAY);
			reflash();
		}
	};
	
	public void reflash(){
	}
}
