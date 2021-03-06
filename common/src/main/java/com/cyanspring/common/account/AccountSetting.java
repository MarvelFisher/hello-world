package com.cyanspring.common.account;

import com.cyanspring.common.Default;
import com.cyanspring.common.data.DataObject;

public class AccountSetting extends DataObject {

	protected AccountSetting() {

	}
	public AccountSetting(String accountId) {
		setId(accountId);
	}

	//!!! very important: you must add entry here whenever you add a setting
	public static AccountSetting createEmptySettings(String accountId) {

		AccountSetting settings = new AccountSetting(accountId);
		settings.setDefaultQty(Default.getOrderQuantity());
		settings.setStopLossValue(Default.getPositionStopLoss());	//user position stop loss value
		settings.setCompanySLValue(Default.getCompanyStopLossValue());							//company position stop loss value
		settings.setMargin(0.0);
		settings.setRoute("");
		settings.setCommission(0.0);
		settings.setLeverageRate(0.0);
		settings.setDailyStopLoss(0.0);								//user daily stop loss value
		settings.setTrailingStop(0.0);
		settings.setStopLossPercent(Default.getStopLossPercent());	//company position stop loss percent
		settings.setFreezePercent(Default.getFreezePercent());		//company daily stop loss percent
		settings.setTerminatePercent(Default.getTerminatePecent());	//company account stop loss percent
		settings.setLiveTrading(Default.isLiveTrading());
		settings.setUserLiveTrading(Default.isUserLiveTrading());
		settings.setLiveTradingType(Default.getLiveTradingType());
		settings.setLiveTradingSettedDate("");
		settings.setFreezeValue(Default.getFreezeValue());			//company daily stop loss value
		settings.setTerminateValue(Default.getTerminateValue());	//company account stop loss percent
		settings.setDefaultQty2("");
		settings.setLtsApiPerm(true);
		return settings;
	}

	public String getId() {
		return get(String.class, AccountSettingType.ID.value());
	}
	public void setId(String id) {
		put(AccountSettingType.ID.value(), id);
	}
	public Double getDefaultQty() {
		Double result = get(double.class, AccountSettingType.DEFAULT_QTY.value());
		return null == result? Default.getOrderQuantity() : result;
	}
	public void setDefaultQty(Double defaultQty) {
		put(AccountSettingType.DEFAULT_QTY.value(), defaultQty);
	}
	public double getStopLossValue() {
		Double result = get(double.class, AccountSettingType.STOP_LOSS_VALUE.value());
		return null == result? Default.getPositionStopLoss() : result;
	}
	public void setStopLossValue(double stopLossValue) {
		put(AccountSettingType.STOP_LOSS_VALUE.value(), stopLossValue);
	}
	public double getCompanySLValue(){
		return get(double.class, AccountSettingType.COMPANY_SL_VALUE.value());
	}
	public void setCompanySLValue(double companySLValue){
		put(AccountSettingType.COMPANY_SL_VALUE.value(), companySLValue);
	}
	public double getMargin(){
		return get(double.class, AccountSettingType.MARGIN.value());
	}
	public void setMargin(double margin){
		put(AccountSettingType.MARGIN.value(), margin);
	}
	public String getRoute() {
		return get(String.class, AccountSettingType.ROUTE.value());
	}
	public void setRoute(String route) {
		put(AccountSettingType.ROUTE.value(), route);
	}
	public double getLeverageRate(){
		return get(double.class, AccountSettingType.LEVERAGE_RATE.value());
	}
	public void setLeverageRate(double lRate){
		put(AccountSettingType.LEVERAGE_RATE.value(), lRate);
	}
	public double getCommission(){
		return get(double.class, AccountSettingType.COMMISSION.value());
	}
	public void setCommission(double commission){
		put(AccountSettingType.COMMISSION.value(), commission);
	}
	public double getDailyStopLoss(){
		return get(double.class, AccountSettingType.DAILY_STOPLOSS.value());
	}
	public void setDailyStopLoss(double dailyStopLoss){
		put(AccountSettingType.DAILY_STOPLOSS.value(), dailyStopLoss);
	}

	public double getTrailingStop() {
		return get(double.class, AccountSettingType.TRAILING_STOP.value());
	}

	public void setTrailingStop(double trailingStop){
		put(AccountSettingType.TRAILING_STOP.value(), trailingStop);
	}

	public double getStopLossPercent() {
		return get(double.class, AccountSettingType.STOP_LOSS_PERCENT.value());
	}
	public void setStopLossPercent(double stopLossPercent) {
		put(AccountSettingType.STOP_LOSS_PERCENT.value(), stopLossPercent);
	}


	public double getFreezePercent() {
		return get(double.class, AccountSettingType.FREEZE_PERCENT.value());
	}
	public void setFreezePercent(double freezePercent) {
		put(AccountSettingType.FREEZE_PERCENT.value(), freezePercent);
	}


	public double getTerminatePercent() {
		return get(double.class, AccountSettingType.TERMINATE_PERCENT.value());
	}
	public void setTerminatePercent(double terminatePercent) {
		put(AccountSettingType.TERMINATE_PERCENT.value(), terminatePercent);
	}

	public boolean isLiveTrading() {
		return get(boolean.class, AccountSettingType.LIVE_TRADING.value());
	}
	public void setLiveTrading(boolean liveTrading) {
		put(AccountSettingType.LIVE_TRADING.value(), liveTrading);
	}

	public boolean isUserLiveTrading() {
		return get(boolean.class, AccountSettingType.USER_LIVE_TRADING.value());
	}
	public void setUserLiveTrading(boolean userLiveTrading) {
		put(AccountSettingType.USER_LIVE_TRADING.value(), userLiveTrading);
	}

	public LiveTradingType getLiveTradingType() {
		return Default.getLiveTradingType();
	}

	public void setLiveTradingType(LiveTradingType liveTradingType) {
	}

	public String getLiveTradingSettedDate() {
		return "";
	}
	public void setLiveTradingSettedDate(String liveTradingSettedDate) {

	}

	public double getFreezeValue() {
		return get(double.class, AccountSettingType.FREEZE_VALUE.value());
	}
	public void setFreezeValue(double freezeValue) {
		put(AccountSettingType.FREEZE_VALUE.value(), freezeValue);
	}


	public double getTerminateValue() {
		return get(double.class, AccountSettingType.TERMINATE_VALUE.value());
	}
	public void setTerminateValue(double terminateValue) {
		put(AccountSettingType.TERMINATE_VALUE.value(), terminateValue);
	}

	public String getDefaultQty2() {
		return get(String.class, AccountSettingType.DEFAULT_QTY2.value());
	}

	public void setDefaultQty2(String defaultQty2) {
		put(AccountSettingType.DEFAULT_QTY2.value(), defaultQty2);
	}

	public boolean isLtsApiPerm() {
		return get(boolean.class, AccountSettingType.LTS_API_PERM.value());
	}

	public void setLtsApiPerm(boolean ltsApiPerm) {
		put(AccountSettingType.LTS_API_PERM.value(), ltsApiPerm);
	}

	public boolean checkLiveTrading(){
		return (isLiveTrading()||isUserLiveTrading());
	}


	public AccountSetting clone() {
		return (AccountSetting)super.clone();
	}
	@Override
	public String toString() {
		return this.getFields().toString();
	}
}
