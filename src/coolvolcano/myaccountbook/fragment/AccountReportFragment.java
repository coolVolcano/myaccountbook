package coolvolcano.myaccountbook.fragment;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.bean.AccountBean;
import coolvolcano.myaccountbook.dao.AccountAdapter;
import coolvolcano.myaccountbook.service.AccountService;
import coolvolcano.myaccountbook.util.AccountBookUtil;

public class AccountReportFragment extends Fragment{

	private Button btnChangeDateOrReportType;
	private String reportType;
	private TextView lblReportTitle;
	private RadioGroup rgReportType;
	private TableLayout tblReport;
	private TableLayout tblReportHeader;
	private TableLayout tblReportSummary;
	private TextView txtSummary;
	private AlertDialog dialog;
	private View dialogView;
	private RadioButton radioDaily;
	private RadioButton radioMonthly;
	private RadioButton radioYearly;
	private AccountBean oldAccount;
	
	private int theYear  = 0;
	private int theMonth = 0;
	private int theDay = 0;
	private AccountService service = null;
	private final int[] idOfColumns = {R.id.headerUserName,R.id.headerOutcomeType,R.id.headerOutcome,R.id.headerTime,R.id.headerRemark};
	private String tagOfTableRow = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		showDateAndReportTypePickerDialog(R.id.radioDailyReport, 0, 0, 0);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		RelativeLayout rl= (RelativeLayout)inflater.inflate(R.layout.fragment_accounts_report, container, false);
		
		tblReport = (TableLayout)rl.findViewById(R.id.tblReport);
		lblReportTitle = (TextView)rl.findViewById(R.id.lblReportTitle);
		tblReportHeader = (TableLayout)rl.findViewById(R.id.tblReportHeader);
		tblReportSummary = (TableLayout)rl.findViewById(R.id.tblReportSummary);
		btnChangeDateOrReportType = (Button)rl.findViewById(R.id.btnChangeDateOrReportType);
		txtSummary = (TextView)rl.findViewById(R.id.txtSummary);
		rgReportType = (RadioGroup)dialogView.findViewById(R.id.rgReportType);
		
		btnChangeDateOrReportType.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				showDateAndReportTypePickerDialog(rgReportType.getCheckedRadioButtonId(), theYear, theMonth, theDay);
			}
		});
		
		return rl;
	}
	
	private void initTable(List<AccountBean> accounts){
		int position = 1;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		for(AccountBean accountBean : accounts){
			TableRow tr = createTableRow();
			for(int i=0;i<5;i++){
				TextView tv = createTextView(tblReportHeader.findViewById(idOfColumns[i]).getWidth()+1, i);
				switch (i) {
				case(0):
					tv.setText(accountBean.getUserName());break;
				case(1):
					tv.setText(accountBean.getOutcomeType());break;
				case(2):
					tv.setText(String.valueOf(accountBean.getOutcome()));break;
				case(3):
					tv.setText(sdf.format(accountBean.getRecordDate()));break;
				case(4):
					tv.setText(accountBean.getRemark());
				default:break;
				}
				tv.setBackgroundColor(getActivity().getResources().getColor(R.color.grey));
				tr.addView(tv);
				tr.setWeightSum(1f);
			
			}
			tr.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					oldAccount = new AccountBean();
					TableRow row = (TableRow)v;
					oldAccount.setUserName(((TextView)row.getChildAt(0)).getText().toString());
					oldAccount.setOutcomeType(((TextView)row.getChildAt(1)).getText().toString());
					oldAccount.setOutcome(Double.parseDouble(((TextView)row.getChildAt(2)).getText().toString()));
					oldAccount.setRemark(((TextView)row.getChildAt(4)).getText().toString());
					try {
						oldAccount.setRecordDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(((TextView)row.getChildAt(3)).getText().toString()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					showUpdateAccountDialog(v.getTag().toString());
				}
			});
			tr.setTag(String.valueOf(position));
			tblReport.addView(tr);
			position++;
		}
	}
	
	private TableRow createTableRow(){
		TableRow tr = new TableRow(getActivity());
		
		//导入LayoutParams要特别注意其所在的包，否则会导致表格不显示
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(params);
		
		return tr;
	}
	
	private TextView createTextView(int width, int i){
		TextView tv = new TextView(getActivity());
		
		LayoutParams params = new LayoutParams(width, 50);
		
		if(i==4){
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
	
	private void showDateAndReportTypePickerDialog(int radioId, int year, int month, int day){
		new AlertDialog.Builder(getActivity())
			.setIconAttribute(android.R.attr.alertDialogIcon).create().show();
		
		int selectedYear = 0;
		int selectedMonth = 0;
		int selectedDay = 0;
		Calendar calendar = Calendar.getInstance();
		
		LayoutInflater factory = getActivity().getLayoutInflater();
		dialogView = factory.inflate(R.layout.dialog_select_report_type_and_date, null);
		radioDaily = (RadioButton)dialogView.findViewById(R.id.radioDailyReport);
		radioMonthly = (RadioButton)dialogView.findViewById(R.id.radioMonthlyReport);
		radioYearly = (RadioButton)dialogView.findViewById(R.id.radioYearlyReport);
		
		final DatePicker datePicker = (DatePicker)dialogView.findViewById(R.id.datePicker);
		rgReportType = (RadioGroup)dialogView.findViewById(R.id.rgReportType);
		
		
		((RadioButton)dialogView.findViewById(radioId)).setChecked(true);
		if(year==0){
			selectedYear = calendar.get(Calendar.YEAR);
		}else{
			selectedYear = year;
		}
		if(month==0){
			selectedMonth = calendar.get(Calendar.MONTH);
		}else{
			selectedMonth = month - 1;
		}
		if(day==0){
			selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
		}else{
			selectedDay = day;
		}
		datePicker.init(selectedYear, selectedMonth, selectedDay, null);
		
		dialog = new AlertDialog.Builder(getActivity()).setIconAttribute(android.R.attr.alertDialogIcon)
		.setTitle(R.string.alert_select_date_and_report_type)
		.setView(dialogView)
		.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
			}
			
		})
		.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				tblReport.removeAllViews();
				
				if(radioDaily.isChecked()){
					reportType = "daily_report";
				}else if(radioMonthly.isChecked()){
					reportType = "monthly_report";
				}
				else if(radioYearly.isChecked()){
					reportType = "yearly_report";
				}
				
				theYear = datePicker.getYear();
				
				if(reportType.equals("monthly_report")){
					theMonth = datePicker.getMonth() + 1;
					theDay = 0;
					lblReportTitle.setText(new StringBuilder().append(theYear).append(getResources().getString(R.string.year)).append(theMonth).append(getResources().getString(R.string.month)).append(getResources().getString(R.string.monthly_report)).toString());
				}else if(reportType.equals("yearly_report")){
					lblReportTitle.setText(new StringBuilder().append(theYear).append(getResources().getString(R.string.year)).append(getResources().getString(R.string.yearly_report)).toString());
					theMonth = 0;
					theDay = 0 ;
				}else if(reportType.equals("daily_report")){
					theMonth = datePicker.getMonth() + 1;
					theDay = datePicker.getDayOfMonth();
					lblReportTitle.setText(new StringBuilder().append(theYear).append(getResources().getString(R.string.year)).append(theMonth).append(getResources().getString(R.string.month)).append(theDay).append(getResources().getString(R.string.day)).append(getResources().getString(R.string.daily_report)).toString());
				}
				
				try{
					service = AccountService.getInstance();
					service.setAdapter((AccountAdapter)Class.forName(getResources().getString(R.string.adapter_class_name)).newInstance());
					service.setContext(AccountReportFragment.this.getActivity());
					List<AccountBean> accounts = service.find(theYear, theMonth, theDay);
					
					if(accounts.size()>0){
						initTable(accounts);
					}
					showStasticsData(accounts);
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}).create();
		
		dialog.show();
	}
	
	private void showStasticsData(List<AccountBean> accounts){
		
		double[] stastics = AccountBookUtil.getStasticsData(getActivity(), accounts);
		TableRow tr = null;
		String[] types = getResources().getStringArray(R.array.outcome_types);
		double total = 0;
		DecimalFormat df = new DecimalFormat("0.00");
		
		if(tblReportSummary.getChildCount()>2){
			tblReportSummary.removeViews(2, (int)Math.ceil(stastics.length/3));
		}
		
		
		for(int i=0;i<stastics.length;i++){
			total+=stastics[i];
			if(i%3==0){
				if(i>0){
					tblReportSummary.addView(tr);
					tr = null;
				}
				tr = createTableRow();
			}
			TextView tv = createStasticsTextView(i, stastics.length);
			tv.setText(new StringBuilder(types[i]).append(":").append(df.format(stastics[i])).toString());
			tr.addView(tv);
			
		}
		
		tblReportSummary.addView(tr);
		txtSummary.setText(new StringBuilder(getResources().getString(R.string.total)).append(":").append(df.format(total)).toString());
		txtSummary.setTextColor(getResources().getColor(R.color.red));
	}
	
	private TextView createStasticsTextView(int i, int size){
		TextView tv = new TextView(getActivity());
		
		LayoutParams params = new LayoutParams(200, 30,.33f);
		
		//come to the last one
		if(i==size-1){
			if(i%3>0){
				params.span = 3 - i%3;
			}
		}
		
		tv.setLayoutParams(params);
		tv.setTextSize(12);
		tv.setPadding(1, 1, 1, 1);
		
		return tv;
	}
	
	private void showUpdateAccountDialog(String tag){
		
		tagOfTableRow = tag;
		LayoutInflater factory = getActivity().getLayoutInflater();
		View v = factory.inflate(R.layout.dialog_update_account, null);
		
		final Spinner spnOutcomeType = (Spinner)v.findViewById(R.id.spnOutcomeType);
		final EditText etxOutcome = (EditText)v.findViewById(R.id.etxOutcome);
		final EditText etxRemark = (EditText)v.findViewById(R.id.etxRemark);
		
		etxOutcome.setText(String.valueOf(oldAccount.getOutcome()));
		spnOutcomeType.setSelection(Arrays.asList(getActivity().getResources().getStringArray(R.array.outcome_types)).indexOf(oldAccount.getOutcomeType()));
		etxRemark.setText(oldAccount.getRemark());
		
		new AlertDialog.Builder(getActivity()).setIconAttribute(android.R.attr.alertDialogIcon)
		.setTitle(R.string.alert_update_account)
		.setView(v)
		.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
			}
			
		})
		.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				
				if(AccountBookUtil.checkOutcome(etxOutcome, getActivity())>=0){
					AccountBean newAccount = new AccountBean();
					newAccount.setUserName(oldAccount.getUserName());
					newAccount.setOutcome(Double.parseDouble(etxOutcome.getText().toString()));
					newAccount.setOutcomeType(spnOutcomeType.getSelectedItem().toString());
					newAccount.setRemark(etxRemark.getText().toString());
					newAccount.setRecordDate(oldAccount.getRecordDate());
					try{
						service = AccountService.getInstance();
						service.setAdapter((AccountAdapter)Class.forName(getResources().getString(R.string.adapter_class_name)).newInstance());
						service.setContext(AccountReportFragment.this.getActivity());
						service.update(oldAccount, newAccount);
						
						List<AccountBean> accounts = service.find(theYear, theMonth, theDay);
						
						if(accounts.size()>0){
							showStasticsData(accounts);
							
							TableRow tr = (TableRow)tblReport.findViewWithTag(tagOfTableRow);
							((TextView)tr.getChildAt(1)).setText(newAccount.getOutcomeType());
							((TextView)tr.getChildAt(2)).setText(String.valueOf(newAccount.getOutcome()));
							((TextView)tr.getChildAt(4)).setText(newAccount.getRemark());
							
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}
			
		}).create().show();
	}
}
