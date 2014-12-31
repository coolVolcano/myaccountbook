package coolvolcano.myaccountbook.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import coolvolcano.myaccountbook.R;
import coolvolcano.myaccountbook.footer.FooterCallback;

public class FooterFragment extends Fragment {

	private FooterCallback callbacks = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if(!(activity instanceof FooterCallback)) {
            throw new IllegalStateException("Activity must implements FooterCallback !");
        }
        callbacks = (FooterCallback) activity;
    }
   
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RadioGroup radioGroup = (RadioGroup) inflater.inflate(R.layout.footer, container, false);
        
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				callbacks.onItemSelected(checkedId);
			}
        	
        });
        
        return radioGroup;
    }
    
}
