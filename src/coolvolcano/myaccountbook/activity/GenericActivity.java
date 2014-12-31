package coolvolcano.myaccountbook.activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import coolvolcano.myaccountbook.footer.FooterCallback;

public class GenericActivity extends FragmentActivity implements FooterCallback {

	@Override
	public void onItemSelected(int item) {
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("tab_type", item);
		startActivityForResult(intent, 9002);
        
	}

}
