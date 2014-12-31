package coolvolcano.myaccountbook.bean;

import java.util.Date;

public class AccountBean {

	private String userName;
	
	private Date recordDate;
	
	private String outcomeType;
	
	private double outcome;
	
	private String remark;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

	public String getOutcomeType() {
		return outcomeType;
	}

	public void setOutcomeType(String outcomeType) {
		this.outcomeType = outcomeType;
	}

	public double getOutcome() {
		return outcome;
	}

	public void setOutcome(double outcome) {
		this.outcome = outcome;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public boolean equals(Object o) {
		if(!o.getClass().getName().equals(this.getClass().getName())){
			return false;
		}
		else{
			AccountBean obj = (AccountBean)o;
			if(this.getOutcome()==obj.getOutcome()){
				if(this.getUserName().equals(obj.getUserName())){
					if(this.getOutcomeType().equals(obj.getOutcomeType())){
						if(this.getRemark().equals(obj.getRemark())){
							if(this.getRecordDate().equals(obj.getRecordDate())){
								return true;
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
}
