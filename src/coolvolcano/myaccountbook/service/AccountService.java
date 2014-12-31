package coolvolcano.myaccountbook.service;

import java.util.List;

import android.R.integer;
import android.content.Context;
import coolvolcano.myaccountbook.bean.AccountBean;
import coolvolcano.myaccountbook.dao.AccountAdapter;

public class AccountService {

	private AccountAdapter adapter;
	
	private Context context;
	
	private static final AccountService service = new AccountService();
	
	private AccountService(){
		
	}
	
	public static AccountService getInstance(){
		
		return service;
	}

	public AccountAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(AccountAdapter adapter) {
		this.adapter = adapter;
	}
	
	public void setContext(Context context){
		this.context = context;
	}
	
	public boolean write(AccountBean account){
		
		return adapter.write(context, account);
	}
	
	public boolean update(AccountBean oldAccount, AccountBean newAccount){
		return adapter.update(context, oldAccount, newAccount);
	}
	
	public boolean delete(AccountBean account){
		return adapter.delete(context, account);
	}
	
	public List<AccountBean> find(int year, int month, int day){
		return adapter.find(context, year, month, day);
	}
}
