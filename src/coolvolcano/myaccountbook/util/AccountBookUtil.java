package coolvolcano.myaccountbook.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
import android.os.Environment;
import android.util.Xml;
import android.widget.EditText;
import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.bean.AccountBean;

public class AccountBookUtil {
	
	public static String getUserNameOrSMSReceiver(Context context, String type){
		String value = null;
		FileInputStream fis = null;
		
		String sdStatus = Environment.getExternalStorageState();
		
		if(sdStatus.equals(Environment.MEDIA_MOUNTED)){
			StringBuilder path = new StringBuilder(Environment.getExternalStorageDirectory().getPath())
							.append("/")
							.append(context.getResources().getString(R.string.root_folder_name))
							.append("/")
							.append(context.getResources().getString(R.string.config_folder_name))
							.append("/");
			
			if(type.equals("userName")){
				path.append(context.getResources().getString(R.string.config_file_name));
			}else if(type.equals("phoneNumber")){
				path.append(context.getResources().getString(R.string.sms_receiver_phone_number_file_name));
			}
			
			try {
				fis = new FileInputStream(path.toString());
				value = readXML(fis);
			} catch (Exception e) {
				
				e.printStackTrace();
			}finally{
				try {
					if(fis!=null)
						fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			value = readUserNameOrSMSReceiverFromPhoneMemory(context, type);
		}
		
		return value;
	}
	
	public static void saveUserNameOrSMSReceiver(Context context, String value, String type){
		
		String sdStatus = Environment.getExternalStorageState();
		
		if(sdStatus.equals(Environment.MEDIA_MOUNTED)){
			//mnt/sdcard/
			String p = Environment.getExternalStorageDirectory().getPath();
			StringBuilder path = new StringBuilder(p)
						.append("/")
						.append(context.getResources().getString(R.string.root_folder_name))
						.append("/")
						.append(context.getResources().getString(R.string.config_folder_name));
			
			File file = new File(path.toString());

			if(!file.exists()){
				file.mkdirs();
			}
			
			path.append("/");
			if(type.equals("userName")){
				path.append(context.getResources().getString(R.string.config_file_name));
			}else if(type.equals("phoneNumber")){
				path.append(context.getResources().getString(R.string.sms_receiver_phone_number_file_name));
			}
			
			File configFile = new File(path.toString());
			FileOutputStream fos = null;
			
			try {
				configFile.createNewFile();
				fos = new FileOutputStream(configFile);
				writeXML(value, fos, type);
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			writeUserNameOrSMSReceiverToPhoneMemory(context, value, type);
		}
	}
	
	private static String readUserNameOrSMSReceiverFromPhoneMemory(Context context, String type){
		FileInputStream fis = null;
		String userName = null;
	
		try {
			if(type.equals("userName")){
				fis = context.openFileInput(context.getResources().getString(R.string.config_file_name));
			}else if(type.equals("phoneNumber")){
				fis = context.openFileInput(context.getResources().getString(R.string.sms_receiver_phone_number_file_name));
			}
			
			userName = readXML(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} finally{
			try {
				if(fis!=null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return userName;
	}
	
	private static void writeUserNameOrSMSReceiverToPhoneMemory(Context context, String value, String type){
		FileOutputStream fos = null;
		try {
			//将来做数据合并功能时，可能需要修改这块
			if(type.equals("userName")){
				fos = context.openFileOutput(context.getResources().getString(R.string.config_file_name), Context.MODE_PRIVATE);
			}else if(type.equals("phoneNumber")){
				fos = context.openFileOutput(context.getResources().getString(R.string.sms_receiver_phone_number_file_name), Context.MODE_PRIVATE);
			}
			writeXML(value, fos, type);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (NotFoundException e) {
			
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void writeXML(String value, FileOutputStream fos, String type){
		
		XmlSerializer serializer = Xml.newSerializer();  
		try{
			serializer.setOutput(fos,"UTF-8");  
	        serializer.startDocument(null, true);  
	        serializer.startTag(null, "myaccountbook"); 
	        
	        if(type.equals("userName")){
	        	serializer.startTag(null, "username");  
		        serializer.text(value);  
		        serializer.endTag(null, "username");
	        }else if(type.equals("phoneNumber")){
	        	serializer.startTag(null, "phonenumber");  
		        serializer.text(value);  
		        serializer.endTag(null, "phonenumber");
	        }
	        
	        serializer.endTag(null, "myaccountbook");
	        serializer.endDocument();
	        serializer.flush(); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	private static String readXML(FileInputStream fis){
		
		String value = null;
		
		try{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
			factory.setNamespaceAware(true); 
			XmlPullParser xpp = factory.newPullParser(); 
			xpp.setInput(fis, "UTF-8");
			
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName=xpp.getName();
	        	switch (eventType) {
	        	case XmlPullParser.START_TAG:
	        		if(nodeName.equals("username")){
	        			value = xpp.nextText();
	        		}else if(nodeName.equals("phonenumber")){
	        			value = xpp.nextText(); 
	        		}
	        		break;
	        	default:
	        		break;
	        	}
	        	eventType = xpp.next();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return value;
	}
	
	public static double[] getStasticsData(Context context, List<AccountBean> accounts){
		
		String[] types = context.getResources().getStringArray(R.array.outcome_types);
		List<String> typesList = Arrays.asList(types);
		double[] total = new double[types.length];
		for(int i=0;i<total.length;i++){
			total[i] = 0;
		}
		
		for(AccountBean accountBean : accounts){
			
			int pos = typesList.indexOf(accountBean.getOutcomeType());
			
			total[pos] += accountBean.getOutcome();
		}
		
		return total;
	}

	public static double checkOutcome(EditText etx, Context context){
		
		double outcome = -1;
		String outcomeValue = etx.getText().toString();
		
		final EditText theEditText = etx;
		
		if(outcomeValue!=null && outcomeValue.length()>0){
			outcome = Double.parseDouble(outcomeValue);
		}
		
		if(outcome<0){
			new AlertDialog.Builder(context)
	        .setIconAttribute(android.R.attr.alertDialogIcon)
	        .setTitle(R.string.alert_outcome_required)
	        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	theEditText.requestFocus();
	            }
	        })
	        .create().show();
		}
		
		return outcome;
		
	}
	
}
