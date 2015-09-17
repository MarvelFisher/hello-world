package com.cyanspring.cstw.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.User;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AllUserSnapshotReplyEvent;
import com.cyanspring.common.event.account.UserUpdateEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.SetPriceDialog.Mode;
import com.cyanspring.cstw.gui.command.auth.AuthMenuManager;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;

public class QuoteView extends ViewPart implements IAsyncEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(QuoteView.class);
	public static final String ID = "com.cyanspring.cstw.gui.QuoteViewer";
	private Composite parentComposite = null;
	private ImageRegistry imageRegistry;
	private Composite topComposite;
	private Button btnSubscribe;
	private Text textSymbol;
	private DynamicTableViewer quoteViewer;
	private String receiverId = IdGenerator.getInstance().getNextID();
	private ConcurrentMap<String, Quote> quoteMap = new ConcurrentHashMap<String,Quote>();
	private List<String>subList = new ArrayList<String>();
	private AsyncTimerEvent refreshEvent = new AsyncTimerEvent();
	private long maxRefreshInterval = 1000;
	private boolean columnCreated = false;
	private Menu menu;
	private Action popDeleteSymbol;
	private final String MENU_ID_DELETESYMBOL = "POPUP_DELETE_SYMBOL";

	@Override
	public void createPartControl(Composite parent) {
		parentComposite = parent;
		imageRegistry = Activator.getDefault().getImageRegistry();
		// create views
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		{
			SashForm sashForm = new SashForm(container,SWT.SMOOTH | SWT.VERTICAL);
			{
				Composite composite = new Composite(sashForm, SWT.NONE);
				GridLayout gl_composite = new GridLayout(3, false);
				composite.setLayout(gl_composite);
				{
					Label lblSymbol = new Label(composite, SWT.NONE);
					lblSymbol.setSize(43, 15);
					lblSymbol.setText("Symbol :");
				}
				{
					textSymbol = new Text(composite, SWT.BORDER);
					GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
					gd_text.widthHint = 171;
					textSymbol.setLayoutData(gd_text);
					textSymbol.setSize(149, 468);
				}
				{
					btnSubscribe = new Button(composite, SWT.NONE);
					btnSubscribe.setSize(148, 468);
					btnSubscribe.setText("Subscribe");
					btnSubscribe.addSelectionListener(new SelectionListener() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							final String symbol = textSymbol.getText();
							if(!StringUtils.hasText(symbol)){
								showMessageBox("Symbol is empty" , parentComposite);
								return;
							}

							if(subList.contains(symbol)){
								showMessageBox(symbol+" already subscribed" , parentComposite);
								return;
							}

							if(!quoteMap.containsKey(symbol)){
								log.info("send sub event:{},{}",textSymbol.getText(),receiverId);
								QuoteSubEvent subEvent = new QuoteSubEvent(receiverId, Business.getInstance().getFirstServer(), symbol);
								sendRemoteEvent(subEvent);
								parentComposite.getDisplay().asyncExec(new Runnable(){

									@Override
									public void run() {
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										if(!quoteMap.containsKey(symbol))
											showMessageBox("this symbol doesn't exist or hasn't any quote yet" , parentComposite);
										else{
											subList.add(symbol);
											refreshQuote();
										}
									}	
								});
							}else{
								subList.add(symbol);
								refreshQuote();
							}
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {

						}
					});		
				}
			}
			{
				Composite bottomComposite = new Composite(sashForm, SWT.NONE);
				bottomComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
				createQuoteViewer(bottomComposite);
				createMenu(bottomComposite);

			}
			sashForm.setWeights(new int[] {1,4});
		}
		
		subEvent(QuoteEvent.class);
		scheduleJob(refreshEvent, maxRefreshInterval);
	}
	
	private void createQuoteViewer(Composite parent) {
		String strFile = Business.getInstance().getConfigPath() + "QuoteTable.xml";
		quoteViewer = new DynamicTableViewer(parent,
				SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, Business.getInstance().getXstream(),
				strFile, BeanHolder.getInstance().getDataConverter());
		quoteViewer.init();
	}
	
	
	@Override
	public void setFocus() {		
	}
	
	private void createMenu(Composite parent) {

		AuthMenuManager menuMgr = AuthMenuManager.newInstance(this.getPartName());
		menuMgr.add(createPopDeleteSymbol(parent));
		menu = menuMgr.createContextMenu(quoteViewer.getTable());
		quoteViewer.setBodyMenu(menu);
	}
	
	private Action createPopDeleteSymbol(final Composite parent) {
		popDeleteSymbol = new StyledAction("", org.eclipse.jface.action.IAction.AS_PUSH_BUTTON) {
			public void run() {

				try {
					Table table = quoteViewer.getTable();				
					TableItem items[] = table.getSelection();
					for (TableItem item : items) {
						Object obj = item.getData();
						if (obj instanceof Quote) {
							subList.remove(((Quote) obj).getSymbol());
							refreshQuote();
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		};

		popDeleteSymbol.setId(MENU_ID_DELETESYMBOL);
		popDeleteSymbol.setText("Delete Symbol");
		popDeleteSymbol.setToolTipText("Delete Symbol");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.MANUAL_CLOSE_ICON.toString());
		popDeleteSymbol.setImageDescriptor(imageDesc);
		return popDeleteSymbol;
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof QuoteEvent){
			QuoteEvent e = (QuoteEvent) event;			
			log.info("e.getQuote().getSymbol():{},{}",e.getQuote().getSymbol());
			if(!PriceUtils.isZero(e.getQuote().getAsk()) && !PriceUtils.isZero(e.getQuote().getBid()))
				quoteMap.put(e.getQuote().getSymbol(), e.getQuote());
		}else if(event instanceof AsyncTimerEvent){
			refreshQuote();
		}
	}
	
	private void refreshQuote() {
		quoteViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (quoteViewer) {
					if (quoteViewer.isViewClosing())
						return;
								
					if( null != quoteViewer.getTable() && null != quoteViewer.getTable().getMenu() && quoteViewer.getTable().getMenu().isVisible()){
						log.info("menu is visible dont refresh");
						return;
					}
					
					List <Quote> tempList = collectQuote();
					if (!columnCreated) {
								
						if(tempList.isEmpty())
							return;
						
						Object obj = tempList.get(0);
						List<ColumnProperty> properties = quoteViewer
								.setObjectColumnProperties(obj);
						quoteViewer.setSmartColumnProperties(obj.getClass().getName(),
								properties);
						
						columnCreated = true;
						quoteViewer.setInput(tempList);
					}else{
						quoteViewer.setInput(tempList);
					}

					quoteViewer.refresh();
				}
			}
		});		
	}

	private List<Quote> collectQuote() {
		List <Quote> list = new ArrayList<Quote>();
		for(String symbol : subList){
			Quote quote = quoteMap.get(symbol);
			if(null == quote){
				quote = new Quote(symbol,null,null);
			}
			list.add(quote);
		}
		
		
		return list;
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz, this);
	}
	
	private void unSubEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().unsubscribe(clazz,ID, this);		
	}
	
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void scheduleJob(AsyncTimerEvent timerEvent,
			long maxRefreshInterval) {
		Business.getInstance().getScheduleManager().scheduleRepeatTimerEvent(maxRefreshInterval,
				QuoteView.this, timerEvent);
	}

	private void cancelScheduleJob(AsyncTimerEvent timerEvent) {
		Business.getInstance().getScheduleManager().cancelTimerEvent(timerEvent);		
	}
	
	private void showMessageBox(final String msg, Composite parent){
		parent.getDisplay().asyncExec(new Runnable(){

			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(parentComposite.getShell(),
						SWT.ICON_INFORMATION);
				messageBox.setText("Info");
				messageBox.setMessage(msg);
				messageBox.open();				
			}
			
		});
	}
	
	@Override
	public void dispose() {
		super.dispose();
		unSubEvent(QuoteEvent.class);
		cancelScheduleJob(refreshEvent);
	}
	
}
