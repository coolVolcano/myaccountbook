package coolvolcano.myaccountbook.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.activity.MainActivity;
import coolvolcano.myaccountbook.bean.AccountBean;
import coolvolcano.myaccountbook.dao.AccountAdapter;
import coolvolcano.myaccountbook.service.AccountService;
import coolvolcano.myaccountbook.util.AccountBookUtil;

public class KeepAccountsFragment extends Fragment {
	
	private TableLayout tblAccountRecord;
	private Button btnAdd;
	private EditText etxOutcome;
	private EditText etxRemark;
	private Spinner spnOutcomeType;
	private TextView txtSummary;
	private Button btnSendTextMessage;
	private RelativeLayout rl;
	
	private int currentPosition = 0; //标示当点击表格某行时该行的序列号
	private int index = 0;  //标示表格中的第几行
	private String action = "add";
	private final int[] idOfColumns = {R.id.lblOutcomeType,R.id.lblOutcome,R.id.lblTime,R.id.lblRemark};
	private List<AccountBean> currentAccountList = new ArrayList<AccountBean>();
	private AccountService service = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rl= (RelativeLayout)inflater.inflate(R.layout.fragment_keep_accounts, container, false);
		
		tblAccountRecord = (TableLayout)rl.findViewById(R.id.tblAccountRecord);
		btnAdd = (Button)rl.findViewById(R.id.btnAdd);
		etxOutcome = (EditText)rl.findViewById(R.id.etxOutcome);
		etxRemark = (EditText)rl.findViewById(R.id.etxRemark);
		spnOutcomeType = (Spinner)rl.findViewById(R.id.spnOutcomeType);
		txtSummary = (TextView)rl.findViewById(R.id.txtSummary);
		btnSendTextMessage = (Button)rl.findViewById(R.id.btnSendTextMessage);
		
		btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				double outcome = AccountBookUtil.checkOutcome(etxOutcome, getActivity());
				
				if(outcome>=0){
					AccountBean oldAccount = null;
					if(currentPosition>0)
						oldAccount = currentAccountList.get(currentPosition-1);
					
					AccountBean account = new AccountBean();
					account.setOutcome(outcome);
					account.setOutcomeType(spnOutcomeType.getSelectedItem().toString());
					account.setUserName(AccountBookUtil.getUserNameOrSMSReceiver(getActivity(),"userName"));
					if(action.equals("add")){
						account.setRecordDate(new Date());
					}else {
						account.setRecordDate(oldAccount.getRecordDate());
					}
					account.setRemark(etxRemark.getText().toString());
					
					service = AccountService.getInstance();
					try {
						service.setAdapter((AccountAdapter)Class.forName(getActivity().getResources().getString(R.string.adapter_class_name)).newInstance());
						service.setContext(KeepAccountsFragment.this.getActivity());
						
						boolean flag = false;
						if(action.equals("add")){
							if((flag=service.write(account))){
								index++;
								addRow(account);
								currentAccountList.add(account);
							}
						}else{
							if((flag=service.update(oldAccount, account))){
								currentAccountList.set(currentPosition-1, account);
								updateRow(currentPosition, account);
								
							}
						}
						if(flag){
							setSummaryText();	
							clearForm();
						}
						action = "add";
						btnAdd.setText(getActivity().getResources().getString(R.string.btn_add));
						
					} catch (java.lang.InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (NotFoundException e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		
		btnSendTextMessage.setEnabled(false);

		btnSendTextMessage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String summary = txtSummary.getText().toString();
				SmsManager sms = SmsManager.getDefault();
				
				String phoneNumber = AccountBookUtil.getUserNameOrSMSReceiver(getActivity(), "phoneNumber");
				
				if(phoneNumber==null){
					new AlertDialog.Builder(getActivity())
	                .setIconAttribute(android.R.attr.alertDialogIcon)
	                .setTitle(R.string.please_set_sms_receiver_number)
	                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	dialog.dismiss();
	                    	((MainActivity)getActivity()).onItemSelected(R.id.radioSettings);
	                    }
	                })
	                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	dialog.dismiss();
	                    }
	                })
	                .create().show();
				}
				else{
					sms.sendTextMessage(phoneNumber, null, new StringBuilder("来自[记账簿]用户").append(AccountBookUtil.getUserNameOrSMSReceiver(getActivity(),"userName")).append("的短信：").append(summary).toString(), null, null);
				}
			}
			
		});
		
		return rl;
	}
	
	private void addRow(AccountBean account){
		TableRow tr = createTableRow();
		
		ImageButton imgButton = new ImageButton(getActivity());
		imgButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.delete));
		imgButton.setTag(String.valueOf(index));
		LayoutParams lpForImageButton = new LayoutParams(tblAccountRecord.findViewById(R.id.lblBlank).getWidth()+1,50);
		lpForImageButton.setMargins(0, 1, 1, 0);
		imgButton.setLayoutParams(lpForImageButton);
		imgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int i = Integer.parseInt(v.getTag().toString());
				tblAccountRecord.removeView(tblAccountRecord.findViewWithTag(i));
				AccountBean theAccountBean = currentAccountList.get(i-1);
				if(service.delete(theAccountBean)){
					currentAccountList.set(i-1, null);
					
					setSummaryText();
				}
				
			}
		});
		imgButton.setBackgroundColor(getActivity().getResources().getColor(R.color.grey));
		tr.addView(imgButton);
		
		for(int i=2;i<=5;i++){
			TextView txt = createTextView(idOfColumns[i-2],i);
			switch (i) {
			case 2:
				txt.setText(account.getOutcomeType());
				break;
			case 3:
				txt.setText(new StringBuilder(getActivity().getResources().getString(R.string.currency_unit))
									.append(account.getOutcome()).toString());
				break;
			case 4:
				txt.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(account.getRecordDate()));
				break;
			case 5:
				txt.setText(account.getRemark());
				break;
			default:
				break;
			}
			
			txt.setBackgroundColor(getActivity().getResources().getColor(R.color.grey));
			tr.addView(txt);
			tr.setWeightSum(1f);
		}
		
		tr.setTag(index);
		
		tr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAdd.setText(getActivity().getResources().getString(R.string.btn_save));
				currentPosition = Integer.parseInt(v.getTag().toString());
				
				AccountBean theAccount = currentAccountList.get(currentPosition-1);
				etxOutcome.setText(String.valueOf(theAccount.getOutcome()));
				spnOutcomeType.setSelection(Arrays.asList(getActivity().getResources().getStringArray(R.array.outcome_types)).indexOf(theAccount.getOutcomeType()));
				etxRemark.setText(theAccount.getRemark());
				
				btnAdd.setText(getActivity().getResources().getString(R.string.btn_save));
				action = "edit";
			}
		});
		tblAccountRecord.addView(tr);
	}
	
	private TableRow createTableRow(){
		TableRow tr = new TableRow(getActivity());
		
		//导入LayoutParams要特别注意其所在的包，否则会导致表格不显示
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(params);
		
		return tr;
	}
	
	private TextView createTextView(int id, int i){
		TextView tv = new TextView(getActivity());
		
		LayoutParams params = new LayoutParams(tblAccountRecord.findViewById(id).getWidth()+1, 50);
		
		if(i==5){
			params.setMargins(0, 1, 0, 0);
		}
		else {
			params.setMargins(0, 1, 1, 0);
		}

		tv.setLayoutParams(params);
		tv.setTextSize(12);
		tv.setPadding(1, 1, 1, 1);
		
		return tv;
	}
	
	private void updateRow(int theIndex, AccountBean theAccount){
		
		TableRow tr = (TableRow)tblAccountRecord.findViewWithTag(theIndex);
		
		TextView theTxtType = (TextView)tr.getChildAt(1);
		theTxtType.setText(theAccount.getOutcomeType());
		
		TextView theTxtOutcome = (TextView)tr.getChildAt(2);
		theTxtOutcome.setText(new StringBuilder(getActivity().getResources().getString(R.string.currency_unit))
										.append(theAccount.getOutcome()).toString());
		
		TextView theTxtRemark = (TextView)tr.getChildAt(4);
		theTxtRemark.setText(theAccount.getRemark());
		
		
	}
	
	private void clearForm(){
		etxRemark.setText("");
		etxOutcome.setText("");
	}
	
	private void setSummaryText(){
		int summary = 0;
		for(AccountBean a : currentAccountList){
			if(a!=null){
				summary+=a.getOutcome();
			}
		}
		if(summary>0)
			txtSummary.setText(new StringBuilder(getResources().getString(R.string.you_have_spent))
									.append(getResources().getString(R.string.currency_unit))
									.append(summary)
									.toString());
		else {
			txtSummary.setText("");
		}
		
		if(txtSummary.getText().toString().length()>0){
			btnSendTextMessage.setEnabled(true);
		}
	}
	
	
	
}
