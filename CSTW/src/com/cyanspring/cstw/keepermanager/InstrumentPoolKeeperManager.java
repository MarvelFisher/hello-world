package com.cyanspring.cstw.keepermanager;

import java.util.List;

import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.account.User;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.pool.AccountPoolsUpdateEvent;
import com.cyanspring.common.event.pool.ExchangeAccountUpdateEvent;
import com.cyanspring.common.event.pool.ExchangeSubAccountUpdateEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordUpdateEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordsUpdateEvent;
import com.cyanspring.common.event.pool.InstrumentPoolUpdateEvent;
import com.cyanspring.common.pool.InstrumentPoolHelper;
import com.cyanspring.common.pool.InstrumentPoolKeeper;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.CSTWEventManager;
import com.cyanspring.cstw.localevent.InstrumentPoolUpdateLocalEvent;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.16
 *
 */
public final class InstrumentPoolKeeperManager {

	private static InstrumentPoolKeeperManager instance;

	private InstrumentPoolKeeper instrumentPoolKeeper;

	private AccountKeeper accountKeeper;
	
	private List<User> riskManagerNGroupUser;

	private IAsyncEventListener listener;

	public static InstrumentPoolKeeperManager getInstance() {
		if (instance == null) {
			instance = new InstrumentPoolKeeperManager();
		}
		return instance;
	}

	public void init() {
		listener = new IAsyncEventListener() {
			@Override
			public void onEvent(AsyncEvent event) {
				if (event instanceof ExchangeAccountUpdateEvent) {
					processExchangeAccountUpdateEvent((ExchangeAccountUpdateEvent) event);
				} else if (event instanceof ExchangeSubAccountUpdateEvent) {
					processExchangeSubAccountUpdateEvent((ExchangeSubAccountUpdateEvent) event);
				} else if (event instanceof InstrumentPoolUpdateEvent) {
					processInstrumentPoolUpdateEvent((InstrumentPoolUpdateEvent) event);
				} else if (event instanceof InstrumentPoolRecordsUpdateEvent) {
					processInstrumentPoolRecordsUpdateEvent((InstrumentPoolRecordsUpdateEvent) event);
				} else if (event instanceof InstrumentPoolRecordUpdateEvent) {
					processInstrumentPoolRecordUpdateEvent((InstrumentPoolRecordUpdateEvent) event);
				} else if (event instanceof AccountPoolsUpdateEvent) {
					processAccountPoolsUpdateEvent((AccountPoolsUpdateEvent) event);
				}
			}
		};
		CSTWEventManager.subscribe(ExchangeAccountUpdateEvent.class, listener);
		CSTWEventManager.subscribe(ExchangeSubAccountUpdateEvent.class,
				listener);
		CSTWEventManager.subscribe(InstrumentPoolUpdateEvent.class, listener);
		CSTWEventManager.subscribe(InstrumentPoolRecordsUpdateEvent.class,
				listener);
		CSTWEventManager.subscribe(InstrumentPoolRecordUpdateEvent.class,
				listener);
		CSTWEventManager.subscribe(AccountPoolsUpdateEvent.class, listener);
	}

	public void processExchangeAccountUpdateEvent(
			ExchangeAccountUpdateEvent event) {
		InstrumentPoolHelper.updateExchangeAccount(instrumentPoolKeeper,
				event.getExchangeAccount(), event.getOperationType());
		sendUpdateEvent();
	}

	public void processExchangeSubAccountUpdateEvent(
			ExchangeSubAccountUpdateEvent event) {
		InstrumentPoolHelper.updateExchangeSubAccount(instrumentPoolKeeper,
				event.getExchangeSubAccount(), event.getOperationType());
		sendUpdateEvent();
	}

	public void processInstrumentPoolUpdateEvent(InstrumentPoolUpdateEvent event) {
		InstrumentPoolHelper.updateInstrumentPool(instrumentPoolKeeper,
				event.getInstrumentPool(), event.getOperationType());
		sendUpdateEvent();
	}

	public void processInstrumentPoolRecordsUpdateEvent(
			InstrumentPoolRecordsUpdateEvent event) {
		InstrumentPoolHelper.updateInstrumentPoolRecords(instrumentPoolKeeper,
				event.getInstrumentPoolRecords(), event.getOperationType());
		sendUpdateEvent();
	}

	public void processInstrumentPoolRecordUpdateEvent(
			InstrumentPoolRecordUpdateEvent event) {
		instrumentPoolKeeper.update(event.getInstrumentPoolRecord());
		sendUpdateEvent();
	}

	public void processAccountPoolsUpdateEvent(AccountPoolsUpdateEvent event) {
		InstrumentPoolHelper.updateAccountPools(null, instrumentPoolKeeper,
				event.getAccountPools(), event.getOperationType());
		sendUpdateEvent();
	}

	public AccountKeeper getAccountKeeper() {
		return accountKeeper;
	}

	public void setAccountKeeper(AccountKeeper accountKeeper) {
		this.accountKeeper = accountKeeper;
	}

	public IInstrumentPoolKeeper getInstrumentPoolKeeper() {
		return instrumentPoolKeeper;
	}

	public void setInstrumentPoolKeeper(
			InstrumentPoolKeeper instrumentPoolKeeper) {
		this.instrumentPoolKeeper = instrumentPoolKeeper;
	}
	
	public List<User> getRiskManagerNGroupUser() {
		return riskManagerNGroupUser;
	}

	public void setRiskManagerNGroupUser(List<User> riskManagerNGroupUser) {
		this.riskManagerNGroupUser = riskManagerNGroupUser;
	}

	private void sendUpdateEvent() {
		InstrumentPoolUpdateLocalEvent event = new InstrumentPoolUpdateLocalEvent(
				IdGenerator.getInstance().getNextID());
		CSTWEventManager.sendEvent(event);

	}

}
