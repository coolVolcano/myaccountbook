package coolvolcano.myaccountbook.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;
import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.bean.AccountBean;

public class AccountXMLAdapter implements AccountAdapter {
	
	private HashMap<Date, AccountBean> accountCache = new HashMap<Date, AccountBean>();

	@Override
	public boolean write(Context context, AccountBean account) {
		boolean flag = false;
		String sdStatus = Environment.getExternalStorageState();
		if(sdStatus.equals(Environment.MEDIA_MOUNTED)){
			flag = writeToSdCard(context, account);
		}else{
			flag = writeToPhoneMemory(context, account);
		}
		
		return flag;
	}
	
	@Override
	public boolean update(Context context, AccountBean oldAccount, AccountBean newAccount){
		boolean flag = false;
//		String sdStatus = Environment.getExternalStorageState();
//		
//		if(sdStatus.equals(Environment.MEDIA_MOUNTED)){
//			flag = updateFileInSdCard(context, account);
//		}else{
//			flag = writeToPhoneMemory(context, account);
//		}
		
		return false;
	}
	
	@Override
	public List<AccountBean> find(Context context, int year, int month, int day){
		return null;
	}
	
	@Override
	public boolean delete(Context context, AccountBean account){
		return false;
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
								.append(context.getResources().getString(R.string.my_data_file_name))
								.toString();
		
		File dataFile = new File(filePath);
		FileOutputStream fos = null;
		
		try {
			if(dataFile.exists()){
				fos = new FileOutputStream(dataFile);
			}else{
				dataFile.createNewFile();
				fos = new FileOutputStream(dataFile);
				writeXML(context, account, fos);
			}
			flag = true;
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		} finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return flag;
	}
	
	private boolean writeToPhoneMemory(Context context, AccountBean account){
		
		
		return false;
	}
	
	private void writeXML(Context context, AccountBean account, FileOutputStream fos){
		XmlSerializer serializer = Xml.newSerializer();  
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(account.getRecordDate());
		try{
			serializer.setOutput(fos,"UTF-8");  
	        serializer.startDocument(null, true);  
	        serializer.startTag(null, "myaccountbook"); 
	        serializer.startTag(null, "user");  
	        serializer.attribute(null, "name", account.getUserName());
	        serializer.startTag(null, "year");  
	        serializer.attribute(null, "value", String.valueOf(calendar.get(Calendar.YEAR)));
	        serializer.startTag(null, "month");  
	        serializer.attribute(null, "value", String.valueOf(calendar.get(Calendar.MONTH)+1));
	        serializer.startTag(null, "day");  
	        serializer.attribute(null, "value", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
	        serializer.startTag(null, "account");  
	        serializer.startTag(null, "outcome_type");  
	        serializer.text(String.valueOf(Arrays.binarySearch(context.getResources().getStringArray(R.array.outcome_types), account.getOutcomeType())));
	        serializer.endTag(null, "outcome_type");
	        serializer.startTag(null, "outcome");  
	        serializer.text(String.valueOf(account.getOutcome()));
	        serializer.endTag(null, "outcome");
	        serializer.startTag(null, "time");  
	        serializer.text(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(account.getRecordDate()));
	        serializer.endTag(null, "time");
	        serializer.startTag(null, "remark");  
	        serializer.text(account.getRemark());
	        serializer.endTag(null, "remark");
	        serializer.endTag(null, "account");
	        serializer.endTag(null, "day");
	        serializer.endTag(null, "month");
	        serializer.endTag(null, "year");
	        serializer.endTag(null, "user");
	        serializer.endTag(null, "myaccountbook");
	        serializer.endDocument();  
	        serializer.flush(); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private boolean updateFileInSdCard(Context context, AccountBean account){
		return false;
	}
	
	private void findInSdCard(Context context){
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder;
	    Document doc;
	    
	    String path = Environment.getExternalStorageDirectory().getPath();
		path = new StringBuilder(path)
					.append("/")
					.append(context.getResources().getString(R.string.root_folder_name))
					.append("/")
					.append(context.getResources().getString(R.string.data_folder_name))
					.append("/")
					.append(context.getResources().getString(R.string.my_data_file_name))
					.toString();
	    
	    try{
		    builder = domFactory.newDocumentBuilder();
		    doc = builder.parse(path);
	
		    XPathFactory factory = XPathFactory.newInstance();
		    XPath xpath = factory.newXPath();
		    XPathExpression expr 
		     = xpath.compile("//year[author='Neal Stephenson']/title/text()");
	
		    Object result = expr.evaluate(doc, XPathConstants.NODESET);
		    NodeList nodes = (NodeList) result;
		    for (int i = 0; i < nodes.getLength(); i++) {
		        System.out.println(nodes.item(i).getNodeValue()); 
		    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}

	@Override
	public String read(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

}
