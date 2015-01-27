package com.cyanspring.info.alert;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.alert.*;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.alert.AlertType;
import com.cyanspring.common.event.alert.PriceAlertReplyEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertReplyEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertRequestEvent;
import com.cyanspring.common.event.alert.QueryPriceAlertRequestEvent;
import com.cyanspring.common.event.alert.SetPriceAlertRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.event.AsyncEventProcessor;

public class AlertManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(AlertManager.class);

	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	ScheduleManager scheduleManager;

	@Autowired
	private IRemoteEventManager eventManagerMD;
	
	@Autowired
	SessionFactory sessionFactory;
	
	private int timeoutSecond ;
	private int createThreadCount ;
	private int maxRetrytimes ;
	private long killTimeoutSecond ;
	private int maxNoOfAlerts;
	
	private boolean tradeAlert;
	private boolean priceAlert;
	private static final String MSG_TYPE_PRICE = "1";
	private static final String MSG_TYPE_ORDER = "2";
	
	private String parseApplicationId;
	private String parseRestApiId;
	
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private int CheckThreadStatusInterval = 60000; // 60 seconds
	
	public ConcurrentLinkedQueue<ParseData> ParseDataQueue ;
	private ArrayList<ParseThread> ParseThreadList ;
	
	private Map<String, ArrayList<BasePriceAlert>> symbolPriceAlerts = new HashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<BasePriceAlert>> userPriceAlerts = new HashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<BasePriceAlert>> userPastPriceAlerts = new HashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<TradeAlert>> userTradeAlerts = new HashMap<String, ArrayList<TradeAlert>>();
	
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(SetPriceAlertRequestEvent.class, null);
			subscribeToEvent(QueryPriceAlertRequestEvent.class, null);
			subscribeToEvent(QueryOrderAlertRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	private AsyncEventProcessor eventProcessorMD = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(ChildOrderUpdateEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}
	};
	
	
	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		Execution execution = event.getExecution();
		if(null == execution)
			return;
		
		log.debug("[processUpdateChildOrderEvent] "+ execution.toString());		
		if(null != ParseDataQueue)
		{
			ParseDataQueue.add(PackTradeAlert(execution));
			receiveChildOrderUpdateEvent(execution) ;
		}
		else
		{
			log.error("ParseDataQueue not ready!!");
			return ;
		}
	}
	
	private void receiveChildOrderUpdateEvent(Execution execution)
	{
		try
		{
			DecimalFormat qtyFormat = new DecimalFormat("#0");		
			String strQty = qtyFormat.format(execution.getQuantity());		
			DecimalFormat priceFormat = new DecimalFormat("#0.#####");
			String strPrice = priceFormat.format(execution.getPrice());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String Datetime = dateFormat.format(execution.get(Date.class, "Created"));
			String tradeMessage = "Trade " + execution.getSymbol() + " " + 
								execution.getSide().toString() + " " + strQty + "@" + strPrice;
			TradeAlert TA = new TradeAlert(execution.getUser(), execution.getSymbol(), null ,execution.getQuantity(), execution.getPrice(),Datetime, tradeMessage);
			//save to Array
			ArrayList<TradeAlert> list ;
			int search;
			list = userTradeAlerts.get(execution.getUser());
			if (null == list)
			{			
				Session session = sessionFactory.openSession();
				Query query = session.getNamedQuery("LoadPastTradeAlert");
				query.setString(0, execution.getUser());
				query.setInteger("maxNoOfAlerts", maxNoOfAlerts);
				Iterator iterator = query.list().iterator();
				list = new ArrayList<TradeAlert>();
				while(iterator.hasNext()) {
					TradeAlert  pastTradeAlert = (TradeAlert) iterator.next();
					list.add(pastTradeAlert);
				}
				if (list.size() == 0)
				{
					list.add(TA);
				}
				else if (list.size() >= 20)
				{
					list.remove(19);
					list.add(0,TA);
				}
				else
				{
					list.add(0,TA);
				}
				userTradeAlerts.put(execution.getUser(),list) ;
			}
			else
			{
				if (list.contains(TA))
				{
					log.warn("[UpdateChildOrderEvent][WARNING] : ChildOrderEvent already exists.");
					return ;
				}
				else
				{
					if (list.size() >= 20)
					{
						list.remove(19);
						list.add(0,TA);
					}
					else
					{
						list.add(0,TA);
					}
				}			
			}
			//save to SQL
			SQLSave(TA);
		}
		catch (Exception e)
		{
			log.warn("[receiveChildOrderUpdateEvent] : " + e.getMessage());
		}
	}
	
	public void processQueryOrderAlertRequestEvent(QueryOrderAlertRequestEvent event)
	{
		AlertType type = event.getType();
		QueryOrderAlertReplyEvent queryorderalertreplyevent = null;
		if (type == AlertType.TRADE_QUERY_PAST)
		{
			ArrayList<TradeAlert> list = userTradeAlerts.get(event.getuserId());
			if (null == list)
			{
				Session session = sessionFactory.openSession();
				Query query = session.getNamedQuery("LoadPastTradeAlert");
				query.setString(0, event.getuserId());
				query.setInteger("maxNoOfAlerts", maxNoOfAlerts);
				Iterator iterator = query.list().iterator();
				list = new ArrayList<TradeAlert>();
				while(iterator.hasNext()) {
					TradeAlert pastTradeAlert = (TradeAlert) iterator.next();
					list.add(pastTradeAlert);
				}
				if (list.size() == 0)
				{
					userTradeAlerts.put(event.getuserId(),list) ;
					log.info("[processQueryOrderAlertRequestEvent] : user OrderAlert list isn't exists.") ;
					//Send orderalert event reply
					queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null, event.getSender(),null,event.getTxId(),false,"userOrderAlert list isn't exists");
				}
				else 
				{
					queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null, event.getSender(),list,event.getTxId(),true,"");
				}
			}
			else
			{
				//Send orderalert event reply
				queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null, event.getSender(),list,event.getTxId(),true,"");
			}
			try {
				eventManager.sendRemoteEvent(queryorderalertreplyevent);
				log.debug("[processQueryOrderAlertRequestEvent] Send Reply User : "+ queryorderalertreplyevent.getUserId());		
			} catch (Exception e) {
				log.warn("[processQueryOrderAlertRequestEvent] : " + e.getMessage());
			}
		}
		else
		{
			log.warn("[receiveQueryOrderAlertRequestEvent] AlertType error." + type.toString());
		}
	}
	
	public void processQuoteEvent(QuoteEvent event) {		
		Quote quote = event.getQuote();
//		log.debug("Quote: " + quote);
		if (quotes.get(quote.getSymbol()) == null)
		{
			quotes.put(quote.getSymbol(), quote);
			return ;
		}
		
		ArrayList<BasePriceAlert> list = symbolPriceAlerts.get(quote.getSymbol());
		ArrayList<BasePriceAlert> UserPriceList;
		if(null == list)
			return;
		else
		{
			for(BasePriceAlert alert: list) {
				if (ComparePriceQuoto(alert, quotes.get(quote.getSymbol()), quote))
				{
					//Add Alert to ParseQueue
					ParseDataQueue.add(PackPriceAlert(alert));
					//Add Alert to PastSQL
					PastPriceAlert pastPriceAlert = new PastPriceAlert(alert.getUserId(),alert.getSymbol(),alert.getPrice(),alert.getDateTime(),alert.getContent());
					pastPriceAlert.setId(alert.getId());
					SQLSave(pastPriceAlert);
					//Add Alert to pastUserPriceAlertList
					UserPriceList = userPastPriceAlerts.get(alert.getUserId());
					if (null == UserPriceList)
					{
						loadPastPriceAlert(alert.getUserId());
						UserPriceList = userPastPriceAlerts.get(alert.getUserId());
						if (UserPriceList.size() >=20)
						{
							UserPriceList.remove(19);
							UserPriceList.add(0, pastPriceAlert);
						}
						else
						{
							UserPriceList.add(0, pastPriceAlert);
						}
					}
					else
					{
						if (UserPriceList.size() >=20)
						{
							UserPriceList.remove(19);
							UserPriceList.add(0, pastPriceAlert);
						}
						else
						{
							UserPriceList.add(0, pastPriceAlert);
						}
					}					
					//Delete Alert from CurSQL
					CurPriceAlert curPriceAlert = new CurPriceAlert(alert.getUserId(),alert.getSymbol(),alert.getPrice(),alert.getDateTime(),alert.getContent());
					curPriceAlert.setId(alert.getId());
					SQLDelete(curPriceAlert);
					//Delete Alert from CurUserPriceAlertList
					UserPriceList = userPriceAlerts.get(alert.getUserId());
					if (null == UserPriceList)
					{
						log.warn("[processQuoteEvent] : userPriceAlerts data didnt match with SQL");
					}
					else
					{
						UserPriceList.remove(alert);
					}
					//Delete Alert from List
					list.remove(alert);					
				}
			}
		}
		quotes.put(quote.getSymbol(), quote);		
	}
	
	private boolean ComparePriceQuoto(BasePriceAlert alert, Quote Previousquoto, Quote quote)
	{
		double alertPrice = alert.getPrice();
		double PreviousPrice = getAlertPrice(Previousquoto);
		double currentPrice = getAlertPrice(quote);
		if (PriceUtils.GreaterThan(alertPrice, PreviousPrice))
		{
			if (PriceUtils.GreaterThan(alertPrice, currentPrice))
			{
				return false ;
			}
			else
			{
				return true ;
			}
		}
		else if (PriceUtils.EqualLessThan(alertPrice, PreviousPrice))
		{
			if (PriceUtils.GreaterThan(alertPrice, currentPrice))
			{
				return true ;
			}
			else
			{
				return false ;
			}
		}
		return false;
	}

	public void processSetPriceAlertRequestEvent(SetPriceAlertRequestEvent event) {
		AlertType type = event.getType();
		log.info("[processSetPriceAlertRequestEvent] "+ event.toString());	
		if (type == AlertType.PRICE_SET_NEW)
		{			
			receiveAddPriceAlert(event);
		}
		else if (type == AlertType.PRICE_SET_MODIFY)
		{
			receiveModifyPriceAlert(event);
		}
		else if (type == AlertType.PRICE_SET_CANCEL)
		{
			receiveCancelPriceAlert(event);
		}
	}
	
	public void processQueryPriceAlertRequestEvent(QueryPriceAlertRequestEvent event) {
		AlertType type = event.getType();
		log.info("[processQueryPriceAlertRequestEvent] "+ event.toString());	
		if (type == AlertType.PRICE_QUERY_CUR)
		{
			receiveQueryCurPriceAlert(event);
		}
		else if (type == AlertType.PRICE_QUERY_PAST)
		{
			receiveQueryPastPriceAlert(event);
		}
	}
	
	private void loadPastPriceAlert(String userId)
	{
		Session session = sessionFactory.openSession();
		ArrayList<BasePriceAlert> BasePriceAlertlist = new ArrayList<BasePriceAlert>();
		Query query = session.getNamedQuery("LoadPastPriceAlert");
		query.setString(0, userId);
		query.setInteger("maxNoOfAlerts", maxNoOfAlerts);
		Iterator iterator = query.list().iterator();
		while(iterator.hasNext()) {
			PastPriceAlert  pastPriceAlert= (PastPriceAlert) iterator.next();
			BasePriceAlertlist.add(pastPriceAlert);			
		}
		userPastPriceAlerts.put(userId, BasePriceAlertlist);
		session.close();
	}
	
	private void receiveAddPriceAlert(SetPriceAlertRequestEvent event) {
		ArrayList<BasePriceAlert> list;
		BasePriceAlert priceAlert = event.getPriceAlert();
		int search;
		PriceAlertReplyEvent pricealertreplyevent = null;
		//Add Alert to List
		list = userPriceAlerts.get(priceAlert.getUserId());
		if(null == list) {
			list = new ArrayList<BasePriceAlert>();
			//do new PriceAlert
			list.add(priceAlert);
			userPriceAlerts.put(priceAlert.getUserId(), list);
			//save to SQL
			CurPriceAlert curPriceAlert = new CurPriceAlert(priceAlert.getUserId(),priceAlert.getSymbol(),priceAlert.getPrice(),priceAlert.getDateTime(),priceAlert.getContent());
			SQLSave(curPriceAlert);
			//SendPriceAlertreplyEvent
			pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);
		}
		else
		{
			if (list.size() >= getMaxNoOfAlerts())
			{
				//reject
				log.debug("[recevieAddPriceAlert] : UserAlert is Greater than maxNoOfAlerts -> reject");
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"UserAlert is Greater than maxNoOfAlerts");
				try {
					eventManager.sendRemoteEvent(pricealertreplyevent);
					log.info("[receiveAddPriceAlert] : send reject User : " + pricealertreplyevent.getUserId());
				} catch (Exception e) {					
					log.debug("[recevieAddPriceAlert] : " + e.getMessage());
				}
				return ;
			}
			else
			{
				if (list.contains(priceAlert))
				{
					log.debug("[recevieAddPriceAlert] : id already exists. -> reject");
					//SendPriceAlertreplyEvent
					pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id already exists.");
					try {
						eventManager.sendRemoteEvent(pricealertreplyevent);
						log.info("[receiveAddPriceAlert] : send reject User : " + pricealertreplyevent.getUserId());
					} catch (Exception e) {					
						log.debug("[recevieAddPriceAlert] : " + e.getMessage());
					}
					return ;
				}
				else
				{
					list.add(priceAlert);
					//save to SQL
					CurPriceAlert curPriceAlert = new CurPriceAlert(priceAlert.getUserId(),priceAlert.getSymbol(),priceAlert.getPrice(),priceAlert.getDateTime(),priceAlert.getContent());
					SQLSave(curPriceAlert);
					//SendPriceAlertreplyEvent
					pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);
				}	
			}
		}
		
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if(null == list) {
			list = new ArrayList<BasePriceAlert>();
			//do new PriceAlert
			list.add(priceAlert);
			symbolPriceAlerts.put(priceAlert.getSymbol(), list);
		}
		else
		{
			search = Collections.binarySearch(list, priceAlert);
			if (search > 0)
			{
				
			}
			else
			{
				list.add(~search,priceAlert);
			}
		}
		try {
			eventManager.sendRemoteEvent(pricealertreplyevent);
			log.info("[receiveAddPriceAlert] : send reply User : " + pricealertreplyevent.getUserId());
		} catch (Exception e) {					
			log.debug("[recevieAddPriceAlert] : " + e.getMessage());
		}
	}
	
	private void receiveModifyPriceAlert(SetPriceAlertRequestEvent event) {			
		ArrayList<BasePriceAlert> list;
		int search;
		BasePriceAlert priceAlert = event.getPriceAlert();
		PriceAlertReplyEvent pricealertreplyevent = null;
		//Add Alert to List
		list = userPriceAlerts.get(priceAlert.getUserId());
		if(null == list) {
			log.debug("[receiveModifyPriceAlert] : id isn't exists. -> reject");
			//SendPriceAlertreplyEvent
			pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
			try {
				eventManager.sendRemoteEvent(pricealertreplyevent);
				log.info("[receiveModifyPriceAlert] : send reject User : " + pricealertreplyevent.getUserId());
			} catch (Exception e) {					
				log.debug("[receiveModifyPriceAlert] : " + e.getMessage());
			}
			return ;
		}
		else
		{			
			if (list.contains(priceAlert))
			{
				for(BasePriceAlert basePriceAlert : list)
				{
					if (basePriceAlert.compareTo(priceAlert) == 0)
					{
						basePriceAlert.modifyPriceAlert(priceAlert);
						//update to SQL
						SQLUpdate(priceAlert);
						//SendPriceAlertreplyEvent
						pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);
					}
				}				
			}
			else
			{
				log.debug("[receiveModifyPriceAlert] : id isn't exists. -> reject");				
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
				try {
					eventManager.sendRemoteEvent(pricealertreplyevent);
					log.info("[receiveModifyPriceAlert] : send reject User : " + pricealertreplyevent.getUserId());
				} catch (Exception e) {					
					log.debug("[receiveModifyPriceAlert] : " + e.getMessage());
				}
				return ;
			}
		}
				
		list = symbolPriceAlerts.get(priceAlert.getSymbol());		
		search = Collections.binarySearch(list, priceAlert);
		if (search > 0)
		{
			list.get(search).modifyPriceAlert(priceAlert);
		}
		try {
			eventManager.sendRemoteEvent(pricealertreplyevent);
			log.info("[receiveModifyPriceAlert] : send reply User : " + pricealertreplyevent.getUserId());
		} catch (Exception e) {					
			log.debug("[receiveModifyPriceAlert] : " + e.getMessage());
		}
	}
	
	private void receiveCancelPriceAlert(SetPriceAlertRequestEvent event) {
		ArrayList<BasePriceAlert> list;
		int search;
		BasePriceAlert priceAlert = event.getPriceAlert();
		PriceAlertReplyEvent pricealertreplyevent = null;
		//Add Alert to List
		list = userPriceAlerts.get(priceAlert.getUserId());
		if(null == list) {
			log.debug("[receiveCancelPriceAlert] : id isn't exists. ->reject");
			//SendPriceAlertreplyEvent
			pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
			try {
				eventManager.sendRemoteEvent(pricealertreplyevent);
				log.info("[receiveCancelPriceAlert] : send reject User : " + pricealertreplyevent.getUserId());
			} catch (Exception e) {					
				log.debug("[receiveCancelPriceAlert] : " + e.getMessage());
			}
			return ;
		}
		else
		{			
			if (list.contains(priceAlert))
			{
				list.remove(priceAlert);				
				//update to SQL
				SQLDelete(priceAlert);
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);
			}
			else
			{
				log.debug("[receiveCancelPriceAlert] : id isn't exists. ->reject");
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
				try {
					eventManager.sendRemoteEvent(pricealertreplyevent);
					log.info("[receiveCancelPriceAlert] : send reject User : " + pricealertreplyevent.getUserId());
				} catch (Exception e) {					
					log.debug("[receiveCancelPriceAlert] : " + e.getMessage());
				}
				return ;
			}
		}		
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		
		search = Collections.binarySearch(list, priceAlert);
		if (search > 0)
		{
			list.remove(priceAlert);
		}
		try {
			eventManager.sendRemoteEvent(pricealertreplyevent);
			log.info("[receiveCancelPriceAlert] : send reply User : " + pricealertreplyevent.getUserId());
		} catch (Exception e) {					
			log.debug("[receiveCancelPriceAlert] : " + e.getMessage());
		}
	}

	private void receiveQueryCurPriceAlert(QueryPriceAlertRequestEvent event)
	{
		try {
			PriceAlertReplyEvent priceAlertReplyEvent;
			ArrayList<BasePriceAlert> list = userPriceAlerts.get(event.getUserId());
			if (null == list)
			{
				log.debug("[receiveQueryCurPriceAlert] : User CurPriceAlert list isn't exists.") ;
				//Send event reply
				priceAlertReplyEvent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),event.getUserId(),event.getType(),null,false,"User CurPriceAlert list isn't exists.");
			}
			else
			{
				//Send event reply
				priceAlertReplyEvent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),event.getUserId(),event.getType(),list,true,null);
			}
			eventManager.sendRemoteEvent(priceAlertReplyEvent);
			log.info("[receiveQueryCurPriceAlert] : send reply User : " + priceAlertReplyEvent.getUserId());
		} catch (Exception e) {
			log.debug("[receiveQueryCurPriceAlert] : " + e.getMessage());
		}
	}
	
	private void receiveQueryPastPriceAlert(QueryPriceAlertRequestEvent event)
	{
		try {
			PriceAlertReplyEvent priceAlertReplyEvent;
			ArrayList<BasePriceAlert> list =  userPastPriceAlerts.get(event.getUserId());
			if (null == list)
			{
				loadPastPriceAlert(event.getUserId());
				list =  userPastPriceAlerts.get(event.getUserId());
				if (list.size() == 0)
				{
					log.debug("[receiveQueryPastPriceAlert] : User PastPriceAlert list isn't exists.") ;
					//Send orderalert event reply
					priceAlertReplyEvent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),event.getUserId(),event.getType(),null,false,"User PastPriceAlert list isn't exists.");
				}
				else
				{
					priceAlertReplyEvent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),event.getUserId(),event.getType(),list,true,null);
				}
			}
			else
			{
				if (list.size() == 0)
				{
					log.debug("[receiveQueryPastPriceAlert] : User PastPriceAlert list isn't exists.") ;
					//Send orderalert event reply
					priceAlertReplyEvent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),event.getUserId(),event.getType(),null,false,"User PastPriceAlert list isn't exists.");
				}
				else
				{
					priceAlertReplyEvent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),event.getUserId(),event.getType(),list,true,null);
				}
			}
			eventManager.sendRemoteEvent(priceAlertReplyEvent);
			log.info("[receiveQueryPastPriceAlert] : send reply User : " + priceAlertReplyEvent.getUserId());
		} catch (Exception e) {
			log.debug("[receiveQueryPastPriceAlert] : " + e.getMessage());
		}
		
	}
	
	public <T> void SQLSave(T object)
	{
		try
		{
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.save(object);
			tx.commit();
			session.close();
		}
		catch (Exception e)
		{
			log.warn("[SQLSave] : " + e.getMessage());
		}
	}
	
	private <T> void SQLUpdate(T object)
	{
		try
		{
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.update(object);
			tx.commit();
			session.close();
		}
		catch (Exception e)
		{
			log.warn("[SQLUpdate] : " + e.getMessage());
		}
	}
	
	private <T> void SQLDelete(T object)
	{
		try
		{
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.delete(object);
			tx.commit();
			session.close();
		}
		catch (Exception e)
		{
			log.warn("[SQLDelete] : " + e.getMessage());
		}
	}
	
	private double getAlertPrice(Quote quote) {
		return (quote.getBid() + quote.getAsk())/2;
	}
	
	public ParseData PackTradeAlert(Execution execution) {
		DecimalFormat qtyFormat = new DecimalFormat("#0");
		String strQty = qtyFormat.format(execution.getQuantity());
		DecimalFormat priceFormat = new DecimalFormat("#0.#####");
		String strPrice = priceFormat.format(execution.getPrice());
		String tradeMessage = "Trade " + execution.getSymbol() + " " + 
				execution.getSide().toString() + " " + strQty + "@" + strPrice;
		String user = execution.getUser();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(Clock.getInstance().now());
		String keyValue = execution.getSymbol() + "," + strPrice + "," + strQty + "," + (execution.getSide().isBuy()?"BOUGHT":"SOLD");
		return new ParseData(user, tradeMessage, user + IdGenerator.getInstance().getNextID(),
				MSG_TYPE_ORDER, strDate, keyValue);
	}

	public ParseData PackPriceAlert(BasePriceAlert priceAlert) {
		DecimalFormat priceFormat = new DecimalFormat("#0.#####");
		String strPrice = priceFormat.format(priceAlert.getPrice());		
		String PriceAlertMessage = priceAlert.getSymbol() + " just reached $" + strPrice;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(Clock.getInstance().now());	
		String keyValue = priceAlert.getSymbol() + "," + strPrice;
		priceAlert.setContent(PriceAlertMessage);
		return new ParseData(priceAlert.getUserId(), PriceAlertMessage, priceAlert.getId(),
				MSG_TYPE_PRICE, strDate, keyValue);
	}

	
	@SuppressWarnings("deprecation")
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		Session session = null;
		if(event == timerEvent) {
			
			try
			{
				log.info("ParseDataQueue Size : " + ParseDataQueue.size());
//				ParseDataQueue.add(PackTradeAlert(new Execution("USDJPY", OrderSide.Buy, 10000.0,
//						123.4, "123-456-789", "111-111-111",
//						"1", "222-222-222",
//						"rickdev", "rickdev-FX", "abcdefg")));
////				
//				TradeAlert TA = new TradeAlert("davidddd", "USDJPY", null ,(long)1000000, (double)120,"2015-01-22 20:45:11", "TEST");
//				SQLSave(TA);
//				CurPriceAlert CP = new CurPriceAlert("d22222222","NZDUSD",(double)888,"2015-01-24 20:45:11","TESTCP");
//				PastPriceAlert PA = new PastPriceAlert("davidddd","USDJPY",(double)120,"2015-01-22 20:45:11","TESTPA");
//				SQLSave(PA);
//				SQLSave(CP);
//				PA.setPrice((double)150);
//				SQLUpdate(PA);
//				ParseDataQueue.add(PackPriceAlert(new BasePriceAlert("David","USDJPT",(double)120,"2015-01-26 11:05:18","")));
//				ArrayList<TradeAlert> list = new ArrayList<TradeAlert>();
//				session = sessionFactory.openSession();
////				Transaction tx = session.beginTransaction();
//				Query query = session.getNamedQuery("LoadPastTradeAlert");
//				query.setString(0, "davidddd");
//				query.setInteger("maxNoOfAlerts", getMaxNoOfAlerts());
//				Iterator iterator = query.list().iterator();
//				
//				while(iterator.hasNext()) {
//					TradeAlert  pastTradeAlert= (TradeAlert) iterator.next();
//					list.add(0, pastTradeAlert);
//				}
				ThreadStatus TS ;				
				for (ParseThread PT : ParseThreadList)
				{
					TS = PT.getThreadStatus() ;		
					if (TS.getThreadState() == ThreadState.IDLE.getState())
					{
						continue ;
					}
					else
					{
						long CurTime = System.currentTimeMillis();
						String Threadid = PT.getThreadId();
						if ((CurTime - TS.getTime()) > killTimeoutSecond)
						{
							log.warn(Threadid + " Timeout , ReOpen Thread.");
							ParseThreadList.remove(PT);
							try
							{
								PT.stop();
							}
							catch (Exception e)
							{
								log.warn("[processAsyncTimerEvent] Exception : " + e.getMessage()) ;
							}
							finally
							{
								PT = new ParseThread(Threadid, ParseDataQueue, timeoutSecond, maxRetrytimes, parseApplicationId,parseRestApiId);
								ParseThreadList.add(PT);
								PT.start();
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				log.warn("[processAsyncTimerEvent] Exception : " + e.getMessage()) ;
			}
			finally
			{
				if (null != session)
				{
					session.close();
				}
			}
		}
	}
	
	private void loadSQLdata()
	{
		log.info("LoadSQLdata...");
		Session session = sessionFactory.openSession();
		try
		{			
			ArrayList<BasePriceAlert> BasePriceAlertlist ;
			Query query = session.getNamedQuery("LoadAllCurPriceAlert");
			Iterator iterator = query.list().iterator();
			int search ;
			while(iterator.hasNext()) {
				CurPriceAlert  curPriceAlert= (CurPriceAlert) iterator.next();
				BasePriceAlertlist = userPriceAlerts.get(curPriceAlert.getUserId());
				if (null == BasePriceAlertlist)
				{
					BasePriceAlertlist = new ArrayList<BasePriceAlert>() ;
					BasePriceAlertlist.add(curPriceAlert);
					userPriceAlerts.put(curPriceAlert.getUserId(), BasePriceAlertlist);					
				}
				else
				{
					BasePriceAlertlist.add(curPriceAlert);
				}
				
				BasePriceAlertlist = symbolPriceAlerts.get(curPriceAlert.getSymbol());
				if (null == BasePriceAlertlist)
				{
					BasePriceAlertlist = new ArrayList<BasePriceAlert>() ;
					BasePriceAlertlist.add(curPriceAlert);
					symbolPriceAlerts.put(curPriceAlert.getSymbol(), BasePriceAlertlist);			
				}
				else
				{
					search = Collections.binarySearch(BasePriceAlertlist, curPriceAlert);
					if (search > 0)
					{
						log.warn("[loadSQLdata] : PriceAlert id repeat warning.");
					}
					else
					{
						BasePriceAlertlist.add(~search, curPriceAlert);
					}					
				}
			}
		}
		catch (Exception e)
		{
			log.warn("[loadSQLdata] : " + e.getMessage());
		}
		finally
		{
			if (null != session)
			{
				session.close();
			}
		}
		return ;
	}
	
	@Override
	public void init() throws Exception {
		String strThreadId = "";
		try
		{
			log.info("Initialising...");

			ParseDataQueue = new ConcurrentLinkedQueue<ParseData>() ;
			ParseThreadList = new ArrayList<ParseThread>() ;

			// subscribe to events
			eventProcessor.setHandler(this);
			eventProcessor.init();
			if(eventProcessor.getThread() != null)
				eventProcessor.getThread().setName("AlertManager");
			
			// subscribe to events
			eventProcessorMD.setHandler(this);
			eventProcessorMD.init();
			if(eventProcessorMD.getThread() != null)
				eventProcessorMD.getThread().setName("AlertManager-MD");
			
			scheduleManager.scheduleRepeatTimerEvent(CheckThreadStatusInterval , eventProcessorMD, timerEvent);
			

			loadSQLdata();
			if (getCreateThreadCount() > 0)
			{
					for (int i = 0; i < getCreateThreadCount() ; i ++)
					{
						strThreadId = "Thread" + String.valueOf(i);
						ParseThread PT = new ParseThread(strThreadId,ParseDataQueue, timeoutSecond, maxRetrytimes, parseApplicationId,parseRestApiId);
						log.info("[" + strThreadId + "] New.") ;
						ParseThreadList.add(PT);
						PT.start();
					}
			}
			else
			{
				log.warn("createThreadCount Setting error : " + String.valueOf(getCreateThreadCount()));
			}
		}
		catch(Exception e)
		{
			log.warn("[" + strThreadId + "] Exception : " + e.getMessage()) ;
		}
	}

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
		eventProcessorMD.uninit();
		for (ParseThread PT : ParseThreadList)
		{
			PT.setstartThread(false) ;
		}
	}
	
	// getters and setters
	public boolean isTradeAlert() {
		return tradeAlert;
	}

	public void setTradeAlert(boolean tradeAlert) {
		this.tradeAlert = tradeAlert;
	}

	public boolean isPriceAlert() {
		return priceAlert;
	}

	public void setPriceAlert(boolean priceAlert) {
		this.priceAlert = priceAlert;
	}

	public int getTimeoutSecond() {
		return timeoutSecond;
	}

	public void setTimeoutSecond(int timeoutSecond) {
		this.timeoutSecond = timeoutSecond;
	}

	public int getMaxRetrytimes() {
		return maxRetrytimes;
	}

	public void setMaxRetrytimes(int maxRetrytimes) {
		this.maxRetrytimes = maxRetrytimes;
	}

	public long getKillTimeoutSecond() {
		return killTimeoutSecond;
	}

	public void setKillTimeoutSecond(long killTimeoutSecond) {
		this.killTimeoutSecond = killTimeoutSecond;
	}
	
	public String getParseApplicationId() {
		return parseApplicationId;
	}

	public void setParseApplicationId(String parseApplicationId) {
		this.parseApplicationId = parseApplicationId;
	}

	public String getParseRestApiId() {
		return parseRestApiId;
	}

	public void setParseRestApiId(String parseRestApiId) {
		this.parseRestApiId = parseRestApiId;
	}

	public int getCreateThreadCount() {
		return createThreadCount;
	}

	public void setCreateThreadCount(int createThreadCount) {
		this.createThreadCount = createThreadCount;
	}

	public int getMaxNoOfAlerts() {
		return maxNoOfAlerts;
	}

	public void setMaxNoOfAlerts(int maxNoOfAlerts) {
		this.maxNoOfAlerts = maxNoOfAlerts;
	}
}
