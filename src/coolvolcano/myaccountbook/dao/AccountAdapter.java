package coolvolcano.myaccountbook.dao;

import java.util.List;

import android.content.Context;
import coolvolcano.myaccountbook.bean.AccountBean;

public interface AccountAdapter {
	
	public boolean write(Context context, AccountBean account);
	
	public boolean update(Context context, AccountBean oldAccount, AccountBean newAccount);
	
	public List<AccountBean> find(Context context, int year, int month, int day);
	
	public boolean delete(Context context, AccountBean account);
	
	public String read(Context context);
	
}
