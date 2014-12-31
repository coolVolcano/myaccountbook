package coolvolcano.myaccountbook.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.footer.FooterCallback;
import coolvolcano.myaccountbook.fragment.AccountReportFragment;
import coolvolcano.myaccountbook.fragment.KeepAccountsFragment;
import coolvolcano.myaccountbook.fragment.SettingsFragment;
import coolvolcano.myaccountbook.util.AccountBookUtil;

public class MainActivity extends FragmentActivity implements FooterCallback {

	private String userName;
	
	private AlertDialog dialog;
	
	private EditText txtUserName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
		
		int tabType = getIntent().getIntExtra("tab_type",R.id.radioKeepAccounts);
		onItemSelected(tabType);
		
		userName = getUserName();
		if(userName==null){
			showUserNameInputDialog();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemSelected(int item) {
		Fragment fragment = null;
        
        switch(item){
        case R.id.radioKeepAccounts: 
        	fragment = new KeepAccountsFragment();
        	break;
        
        case R.id.radioViewReports:
        	fragment = new AccountReportFragment();
        	break;
        
        default:
        	fragment = new SettingsFragment();
           	break;
        }
        
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.main_detail_FrameLayout, fragment).commit();
	}
	
	private void showUserNameInputDialog(){
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(R.layout.dialog_input_username, null);
		txtUserName = (EditText)view.findViewById(R.id.txtUserName);
		
        dialog = new AlertDialog.Builder(this).setIconAttribute(android.R.attr.alertDialogIcon)
        				.setTitle(R.string.please_input_username)
        				.setView(view)
        				.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MainActivity.this.finish();
							}
        					
        				})
        				.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								userName = txtUserName.getText().toString();
								MainActivity.this.saveUserName(userName);
							}
        					
        				}).create();
        dialog.show();
	}
	
	private String getUserName(){
		String userName = AccountBookUtil.getUserNameOrSMSReceiver(this, "userName");
		
		return userName;
	}
	
	private void saveUserName(String userName){
		AccountBookUtil.saveUserNameOrSMSReceiver(this, userName, "userName");
	}

}
