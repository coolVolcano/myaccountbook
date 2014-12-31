package coolvolcano.myaccountbook.fragment;

import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.util.AccountBookUtil;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class SettingsFragment extends Fragment {

	private RelativeLayout rl;
	private Button btnSetSMSReceiver;
	private Button btnDataIntegration;
	private Button btnExit;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rl= (RelativeLayout)inflater.inflate(R.layout.fragment_settings, container, false);
		
		btnSetSMSReceiver = (Button)rl.findViewById(R.id.btnSetSMSReceiver);
		btnDataIntegration = (Button)rl.findViewById(R.id.btnDataIntegration);
		btnExit = (Button)rl.findViewById(R.id.btnExit);
		
		btnSetSMSReceiver.setOnClickListener(new BtnSetSMSReceiverClickListener());
		btnDataIntegration.setOnClickListener(new BtnDataIntegrationClickListener());
		btnExit.setOnClickListener(new BtnExitClickListener());
		
		return rl;
	}
	
	private class BtnSetSMSReceiverClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			LayoutInflater factory = LayoutInflater.from(getActivity());
			View view = factory.inflate(R.layout.dialog_input_sms_receiver_number, null);
			
			final EditText etxReceiverPhoneNumber = (EditText)view.findViewById(R.id.etxReceiverPhoneNumber);
			etxReceiverPhoneNumber.setText(AccountBookUtil.getUserNameOrSMSReceiver(getActivity(), "phoneNumber"));
			
			new AlertDialog.Builder(getActivity()).setIconAttribute(android.R.attr.alertDialogIcon)
			.setTitle(R.string.please_input_sms_receiver_phone_number)
			.setView(view)
			.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					dialog.dismiss();
				}
				
			})
			.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					AccountBookUtil.saveUserNameOrSMSReceiver(getActivity(),etxReceiverPhoneNumber.getText().toString(), "phoneNumber");
					dialog.dismiss();
				}
				
			}).create().show();
		}
		
	}
	
	private class BtnDataIntegrationClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
		}
		
	}
	
	private class BtnExitClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			getActivity().finish();
			System.exit(0);
		}
		
	}
	
}
