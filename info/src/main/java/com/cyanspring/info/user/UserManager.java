package com.cyanspring.info.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.ResetAccountReplyEvent;
import com.cyanspring.common.event.account.ResetAccountReplyType;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.event.AsyncEventProcessor;

public class UserManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(UserManager.class);

	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	SessionFactory sessionFactoryCentral;
	
	@Autowired
	ScheduleManager scheduleManager;
	
	@Autowired
	private IRemoteEventManager eventManager;
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {
		@Override
		public void subscribeToEvents() {
			subscribeToEvent(ResetAccountRequestEvent.class, null);
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public void processResetAccountRequestEvent(ResetAccountRequestEvent event) {
		log.info("[processResetAccountRequestEvent] : AccountId :" + event.getAccount() + " Coinid : " + event.getCoinId());
		ResetUser(event);
	}

	private boolean ResetUser(ResetAccountRequestEvent event) {
		Session session = sessionFactory.openSession();
		Session sessionCentral = sessionFactoryCentral.openSession();
		SQLQuery query ;
		Iterator iterator ;
		String strCmd = "";
		int Return ;
		String UserId = event.getUserId();
		String AccountId = event.getAccount();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyyMMddHHmmss");
		Calendar cal = Calendar.getInstance();
		String ddateFormat = "-R" + dateFormat.format(cal.getTime());
		
		ArrayList<String> ContestIdArray = new ArrayList<String>();
		try {
			// Local MYSQL	
			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + AccountId + ddateFormat + "'" +
					 ",USER_ID='" + UserId  + ddateFormat + "' where ACCOUNT_ID='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();			
			strCmd = "update CLOSED_POSITIONS set ACCOUNT_ID='" + AccountId  + ddateFormat + "'" +
					 ",USER_ID='" + UserId  + ddateFormat + "' where ACCOUNT_ID='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + AccountId  + ddateFormat + "'" +
					 ",USER_ID='" + UserId  + ddateFormat + "' where ACCOUNT_ID='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update CHILD_ORDER_AUDIT set ACCOUNT='" + AccountId  + ddateFormat + "'" +
					 ",TRADER='" + UserId  + ddateFormat + "' where ACCOUNT='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update EXECUTIONS set ACCOUNT='" + AccountId  + ddateFormat + "'" +
					 ",TRADER='" + UserId  + ddateFormat + "' where ACCOUNT='" + AccountId + "'" ;
			query = session.createSQLQuery(strCmd);
			Return = query.executeUpdate();

			// Central MYSQL
			strCmd = "update ACCOUNTS_DAILY set ACCOUNT_ID='" + AccountId  + ddateFormat + "'" +
					 ",USER_ID='" + UserId  + ddateFormat + "' where ACCOUNT_ID='" + AccountId + "'" ;
			query = sessionCentral.createSQLQuery(strCmd);
			Return = query.executeUpdate();
			strCmd = "update OPEN_POSITIONS set ACCOUNT_ID='" + AccountId  + ddateFormat + "'" +
					 ",USER_ID='" + UserId  + ddateFormat + "' where ACCOUNT_ID='" + AccountId + "'" ;
			query = sessionCentral.createSQLQuery(strCmd);
			Return = query.executeUpdate();			

			strCmd = "select * from CONTEST;";
			query = sessionCentral.createSQLQuery(strCmd);
			iterator = query.list().iterator();			
			Calendar cal_UTC = Calendar.getInstance();			 
			SimpleDateFormat cdateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.0");
			cdateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String CurTime = cdateFormat.format(cal_UTC.getTime());
			
			while (iterator.hasNext()) {	
				Object[] rows = (Object[]) iterator.next();				
				String StartDate = (String) rows[2].toString();
			    String EndDate = rows[3].toString();
			    
				if(CurTime.compareTo(StartDate) < 0)
				{
					continue;
				}
				if(CurTime.compareTo(EndDate) > 0)
				{
					continue;
				}								
				ContestIdArray.add((String) rows[0]);
			}				
			for (String ContestId : ContestIdArray)
			{				
				strCmd = "delete from "+ ContestId + "_fx where USER_ID='" + UserId + "' and DATE<>'0'";
				query = sessionCentral.createSQLQuery(strCmd);
				Return = query.executeUpdate();
				strCmd = "update "+ ContestId + "_fx set UNIT_PRICE='1' where USER_ID='" + UserId + "' and DATE='0'";
				query = sessionCentral.createSQLQuery(strCmd);
				Return = query.executeUpdate();	
				
			}			
			ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(event.getKey(),event.getSender(), event.getAccount(), event.getTxId(), event.getUserId(), event.getMarket(), event.getCoinId(),ResetAccountReplyType.LTSINFO_USERMANAGER, true,"");
			eventManager.sendRemoteEvent(resetAccountReplyEvent);
			log.info("Reset User Success : " + UserId);
		} catch (Exception e) {
			log.error("["+strCmd+"] "+e.getMessage(), e);
			ResetAccountReplyEvent resetAccountReplyEvent = new ResetAccountReplyEvent(event.getKey(),
					event.getSender(), 
					event.getAccount(), 
					event.getTxId(), 
					event.getUserId(), 
					event.getMarket(), 
					event.getCoinId(), 
					ResetAccountReplyType.LTSINFO_USERMANAGER, 
					false, 
					MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_RESET_ERROR, "[UserManager]: Reset User " + UserId + " fail."));
			try
			{
				eventManager.sendRemoteEvent(resetAccountReplyEvent);
			}
			catch(Exception ee)
			{
				log.error(ee.getMessage());
			}
		}
		finally
		{
			session.close();
			sessionCentral.close();
		}
		return true;
	}
	
	private void SendSQLHeartBeat() {
		Session session = null;
		Session sessionCentral = null ;
		try {
			session = sessionFactory.openSession();
			sessionCentral = sessionFactoryCentral.openSession();
			
			SQLQuery sq = session.createSQLQuery("select 1;");			
			Iterator iterator = sq.list().iterator();			
			sq = sessionCentral.createSQLQuery("select 1;");
			iterator = sq.list().iterator();
			log.info("Send SQLHeartBeat...");
		} catch (Exception e) {
			log.warn("[SendSQLHeartBeat] : " + e.getMessage());
		} finally {
			if (null != session) {
				session.close();
			}
			if (null != sessionCentral)
			{
				sessionCentral.close();
			}
		}
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if (event == timerEvent) {
			try {
				SendSQLHeartBeat();
			}
			catch (Exception e)
			{
				log.warn("[timerEvent] Exception : "
						+ e.getMessage());
			}
		}
	}
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		log.info("Initialising...");

		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("UserManager");
		
		scheduleManager.scheduleRepeatTimerEvent(60000,
				eventProcessor, timerEvent);
		
	}

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
		// TODO Auto-generated method stub
	}
}
