package coolvolcano.myaccountbook.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.bean.AccountBean;

public class AccountJSONAdapter implements AccountAdapter {
	
	private static HashMap<String, List<AccountBean>> cache = new HashMap<String, List<AccountBean>>();
	/**if just it's the first time to use this app after the phone is off or after exit this app, which means cache is empty,  
	 * so if after record some outcome, and then view the report, will only see the newly added account. So to fix this issue,
	 * need to check if cache is empty or not. If empty, then still need to read the json file to get the history.
	 * 
	 */
	private static boolean isCacheEmpty = true;
	
	@Override
	public boolean write(Context context, AccountBean account) {
		boolean flag = false;
		String sdStatus = Environment.getExternalStorageState();
		if(sdStatus.equals(Environment.MEDIA_MOUNTED)){
			flag = writeToSdCard(context, account);
		}else{
			flag = writeToPhoneMemory(context, account);
		}
		isCacheEmpty = false;
		return flag;
	}

	@Override
	public boolean update(Context context, AccountBean oldAccount, AccountBean newAccount) {
		boolean flag = false;
		String content = new StringBuilder("[").append(read(context).replaceFirst(",","")).append("]").toString();
		JSONObject object = account2Json(oldAccount);
		JSONArray array = null;
		FileWriter writer = null;
		
		String key = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(oldAccount.getRecordDate());
		
		try {
			array = new JSONArray(content);
			for(int i=0;i<array.length();i++){
				if(object.getString("user_name").equals(array.getJSONObject(i).getString("user_name"))){
					if(object.getString("outcome_type").equals(array.getJSONObject(i).getString("outcome_type"))){
						if(object.getDouble("outcome")==array.getJSONObject(i).getDouble("outcome")){
							if(object.getString("remark").equals(array.getJSONObject(i).getString("remark"))){
								if(object.getString("record_date").equals(array.getJSONObject(i).getString("record_date"))){
									array.put(i, account2Json(newAccount));
									break;
								}
							}
						}
					}
				
				}
			}
			writer = new FileWriter(new File(getFilePathInSdCard(context)));
			String s = array.toString();
			s = s.substring(1, s.length()-1);
			writer.write(new StringBuilder(",").append(s).toString());
			
			List<AccountBean> accounts = cache.get(key);
			
			accounts.remove(oldAccount);
			accounts.add(newAccount);
			cache.remove(key);
			cache.put(key, accounts);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return flag;
	}

	@Override
	public List<AccountBean> find(Context context, int year, int month, int day) {
		
		JSONArray array = null;
		
		List<AccountBean> accountBeans = new ArrayList<AccountBean>();
		boolean matched = false;
		
		String key = ""; 
		if(month==0 & day==0){
			for(int i=1;i<=12;i++){
				for(int j=1;j<=31;j++){
					key= new StringBuilder().append(year).append("-").append(i<10?"0":"").append(i).append("-").append(j<10?"0":"").append(j).toString();
					if(cache.get(key)!=null){
						accountBeans.addAll(cache.get(key));
					}
				}
				
			}
		}else if(day==0){
			for(int k=1;k<=31;k++){
				key= new StringBuilder().append(year).append("-").append(month<10?"0":"").append(month).append("-").append(k<10?"0":"").append(k).toString();
				if(cache.get(key)!=null){
					accountBeans.addAll(cache.get(key));
				}
			}
		}else{
			key= new StringBuilder().append(year).append("-").append(month<10?"0":"").append(month).append("-").append(day<10?"0":"").append(day).toString();
			if(cache.get(key)!=null){
				accountBeans = cache.get(key);
			}
		}
		
		if(accountBeans.size()<=0 || !isCacheEmpty){
			
			//to delete the duplicated item from cache - the duplicated item was inserted when add the new account record.
			if(!isCacheEmpty){
				accountBeans.clear();
			}
			String content = new StringBuilder("[").append(read(context).replaceFirst(",","")).append("]").toString();
			try{
				array = new JSONArray(content);
				for(int i=0;i<array.length();i++){
					JSONObject object = array.getJSONObject(i);
					String strDate = object.getString("record_date");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
					Date theDate = sdf.parse(strDate);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(theDate);
					
					matched = (year==calendar.get(Calendar.YEAR) && month==0 && day==0) || (year==calendar.get(Calendar.YEAR) && month==calendar.get(Calendar.MONTH)+1 && day==0) 
									|| (year==calendar.get(Calendar.YEAR) && month==calendar.get(Calendar.MONTH)+1 && day==calendar.get(Calendar.DAY_OF_MONTH));
					
					if(matched){
						String theKey = new StringBuilder().append(calendar.get(Calendar.YEAR)).append("-")
												.append(calendar.get(Calendar.MONTH)+1<10?"0"+calendar.get(Calendar.MONTH)+1:calendar.get(Calendar.MONTH)+1).append("-")
												.append(calendar.get(Calendar.DAY_OF_MONTH)<10?"0"+calendar.get(Calendar.DAY_OF_MONTH):calendar.get(Calendar.DAY_OF_MONTH))
												.toString();
						AccountBean accountBean = new AccountBean();
						accountBean.setOutcome(object.getDouble("outcome"));
						accountBean.setOutcomeType(object.getString("outcome_type"));
						accountBean.setUserName(object.getString("user_name"));
						accountBean.setRecordDate(theDate);
						accountBean.setRemark(object.getString("remark"));
						accountBeans.add(accountBean);
						cache.put(theKey, accountBeans);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return accountBeans;
	}

	@Override
	public boolean delete(Context context, AccountBean account) {
		boolean flag = false;
		JSONArray array = null;
		FileWriter writer = null;
		JSONObject object = account2Json(account);
		int i=0;
		String content = new StringBuilder("[").append(read(context).replaceFirst(",","")).append("]").toString();
		
		String key = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(account.getRecordDate());
		List<AccountBean> accountBeans = cache.get(key);
		
		try{
			array = new JSONArray(content);
			for(;i<array.length();i++){
				if(object.getString("user_name").equals(array.getJSONObject(i).getString("user_name"))){
					if(object.getString("outcome_type").equals(array.getJSONObject(i).getString("outcome_type"))){
						if(object.getDouble("outcome")==array.getJSONObject(i).getDouble("outcome")){
							if(object.getString("remark").equals(array.getJSONObject(i).getString("remark"))){
								if(object.getString("record_date").equals(array.getJSONObject(i).getString("record_date"))){
									break;
								}
							}
						}
					}
				
				}
			}
			array.put(i, null);
			writer = new FileWriter(new File(getFilePathInSdCard(context)));
			String s = array.toString();
			s = s.substring(1, s.length()-1);
			
			if(array.length()<=1)
				s = s.replace("null", "");
			else
				s = s.replace(",null", "");
			if(s.length()>0)
				writer.write(new StringBuilder(",").append(s).toString());
			else {
				writer.write("");
			}
			
			if(accountBeans!=null && accountBeans.size()>0){
				accountBeans.remove(account);
				if(accountBeans.size()<=0){
					cache.remove(key);
				}
			}
			flag = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return flag;
	}
	
	@Override
	public String read(Context context){
		String content = "";
		String sdStatus = Environment.getExternalStorageState();
		if(sdStatus.equals(Environment.MEDIA_MOUNTED)){
			content = readFromSdCard(context);
		}else{
			
		}
		
		return content;
	}
	
	private boolean writeToSdCard(Context context, AccountBean account){
		
		boolean flag = false;
		String path = Environment.getExternalStorageDirectory().getPath();
		path = new StringBuilder(path)
					.append("/")
					.append(context.getResources().getString(R.string.root_folder_name))
					.append("/")
					.append(context.getResources().getString(R.string.data_folder_name))
					.toString();
		
		File file = new File(path);

		if(!file.exists()){
			file.mkdirs();
		}
		
		String filePath = new StringBuilder(path)
								.append("/")
								.append(context.getResources().getString(R.string.my_data__json_file_name))
								.toString();
		
		File dataFile = new File(filePath);
		FileWriter writer = null;
		
		try {
			if(!dataFile.exists()){
				dataFile.createNewFile();
				writer = new FileWriter(dataFile);
			}else{
				writer = new FileWriter(dataFile,true);
			}
			
			writeJSON(context, account, writer);
			
			//write cache
			String key = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(account.getRecordDate());
			System.out.println("write-key:"+key);
			if(cache.get(key)!=null){
				List<AccountBean> accounts = cache.get(key);
				cache.remove(key);
				accounts.add(account);
				cache.put(key, accounts);
			}else{
				List<AccountBean> accounts = new ArrayList<AccountBean>();
				accounts.add(account);
				cache.put(key, accounts);
			}
			
			flag = true;
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		} finally{
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return flag;
	}
	
	private boolean writeToPhoneMemory(Context context, AccountBean account){
		
		
		return false;
	}
	
	private void writeJSON(Context context, AccountBean account, FileWriter writer){
		JSONObject jsonObject = account2Json(account);
		try {
			
			String content = new StringBuilder(",").append(jsonObject.toString()).toString();
			
			writer.write(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//因为String有长度上限2^32-1，所以必须得考虑json太长的问题。
	private String readFromSdCard(Context context){
		String content = "";
		BufferedReader reader = null;
		String line;
		
		File file = new File(getFilePathInSdCard(context));
		if(file.exists()){
			try {
				reader = new BufferedReader(new FileReader(file));
				while((line=reader.readLine())!=null){
					content = new StringBuilder(content).append(line).toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				if(reader!=null){
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return content;
	}
	
	private void readFromPhoneMemory(){
		
	}
	
	public JSONObject account2Json(AccountBean account){
		JSONObject jsonObject = new JSONObject();
		try{
			jsonObject.put("outcome_type", account.getOutcomeType());
			jsonObject.put("outcome", account.getOutcome());
			jsonObject.put("record_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(account.getRecordDate()));
			jsonObject.put("remark", account.getRemark());
			jsonObject.put("user_name", account.getUserName());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	private String getFilePathInSdCard(Context context){
		return new StringBuilder(Environment.getExternalStorageDirectory().getPath())
		.append("/")
		.append(context.getResources().getString(R.string.root_folder_name))
		.append("/")
		.append(context.getResources().getString(R.string.data_folder_name))
		.append("/")
		.append(context.getResources().getString(R.string.my_data__json_file_name))
		.toString();
	}

}
