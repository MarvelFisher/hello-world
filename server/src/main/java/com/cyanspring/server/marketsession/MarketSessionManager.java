/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.server.marketsession;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.event.marketsession.*;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.marketsession.MarketSessionChecker;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.event.AsyncEventProcessor;

public class MarketSessionManager implements IPlugin, IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(MarketSessionManager.class);
	
	@Autowired
	private ScheduleManager scheduleManager;
	
	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private IRefDataManager refDataManager;

	private Date chkDate;
	private int settlementDelay = 10;
	
	protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	protected long timerInterval = 5*1000;
	
	private MarketSessionType currentSessionType;
	private String currentTradeDate;
	
	private MarketSessionChecker sessionChecker;
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(MarketSessionRequestEvent.class, null);
			subscribeToEvent(TradeDateRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	public void processTradeDateRequestEvent(TradeDateRequestEvent event){
		try{
			String tradeDate = sessionChecker.getTradeDate();
			TradeDateEvent tdEvent = new TradeDateEvent(null, null, tradeDate);
			eventManager.sendEvent(tdEvent);
			this.currentTradeDate = tradeDate;
		}catch(Exception e){
			log.error(e.getMessage(), e);
		} 
	}
	
	public void processMarketSessionRequestEvent(MarketSessionRequestEvent event){
		Date date = Clock.getInstance().now();
		try {
			MarketSessionData sessionData = sessionChecker.getState(date);			
			MarketSessionEvent msEvent = new MarketSessionEvent(null, null, sessionData.getSessionType(), 
					sessionData.getStartDate(), sessionData.getEndDate(), sessionChecker.getTradeDate(), Default.getMarket());
			msEvent.setKey(null);
			msEvent.setReceiver(null);
			if(event.isLocal())
				eventManager.sendEvent(msEvent);
			else
				eventManager.sendRemoteEvent(msEvent);
			currentSessionType = sessionData.getSessionType();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processPmSettlementEvent(PmSettlementEvent event){
		log.info("Receive PmSettlementEvent, symbol: " + event.getEvent().getSymbol());
		eventManager.sendEvent(event.getEvent());
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		Date date = Clock.getInstance().now();
		try {			
			MarketSessionData sessionData = sessionChecker.getState(date);
			if(currentSessionType == null || !currentSessionType.equals(sessionData.getSessionType())){				
				MarketSessionEvent msEvent = new MarketSessionEvent(null, null, sessionData.getSessionType(), 
						sessionData.getStartDate(), sessionData.getEndDate(), sessionChecker.getTradeDate(), Default.getMarket());
				msEvent.setKey(null);
				msEvent.setReceiver(null);
				log.info("Send MarketSessionEvent: " + msEvent);
				eventManager.sendGlobalEvent(msEvent);	
				currentSessionType = sessionData.getSessionType();				
			}
			if(currentTradeDate == null || !currentTradeDate.equals(sessionChecker.getTradeDate())){
				String tradeDate = sessionChecker.getTradeDate();
				TradeDateEvent tdEvent = new TradeDateEvent(null, null, tradeDate);
				log.info("Send TradeDateEvent: " + tradeDate);
				eventManager.sendEvent(tdEvent);
				currentTradeDate = tradeDate;
			}
			
			if(refDataManager.getRefDataList().size() <= 0)
				return;
			if(TimeUtil.sameDate(chkDate, date) || currentSessionType.equals(MarketSessionType.CLOSE))
				return;
			chkDate = date;
			for(RefData refData : refDataManager.getRefDataList()){
				if (refData.getSettlementDate() != null){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date settlementDate = sdf.parse(refData.getSettlementDate());
					if (TimeUtil.sameDate(settlementDate, chkDate)){
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.MINUTE, settlementDelay);
						SettlementEvent sdEvent = new SettlementEvent(null, null, refData.getSymbol());
						PmSettlementEvent pmSDEvent = new PmSettlementEvent(null, null, sdEvent);
						scheduleManager.scheduleTimerEvent(cal.getTime(), eventProcessor, pmSDEvent);
						log.info("Start SettlementEvent after " + settlementDelay + " mins, symbol: " + refData.getSymbol());
					}
				}
			}			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void init() throws Exception {
		log.info("initialising");

		Date date = Clock.getInstance().now();
		sessionChecker.init(date);
		
		chkDate = TimeUtil.getPreviousDay(date);
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("MarketSessionManager");
		
		if(!eventProcessor.isSync())
			scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);	
	}	

	@Override
	public void uninit() {
	}

	public void onEvent(AsyncEvent event) {
		if (event instanceof MarketSessionEvent) {
			currentSessionType = ((MarketSessionEvent)event).getSession();
			eventManager.sendEvent(event);
		} else {
			log.error("unhandled event: " + event);
		}
	}

	public MarketSessionType getCurrentSessionType() {
		return currentSessionType;
	}

	public void setSessionChecker(MarketSessionChecker sessionChecker) {
		this.sessionChecker = sessionChecker;
	}

	public void setSettlementDelay(int settlementDelay) {
		this.settlementDelay = settlementDelay;
	}
}
