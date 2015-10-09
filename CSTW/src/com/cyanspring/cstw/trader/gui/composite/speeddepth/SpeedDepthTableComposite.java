package com.cyanspring.cstw.trader.gui.composite.speeddepth;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.ResourceManager;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.model.SpeedDepthModel;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.provider.SpeedDepthContentProvider;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.provider.SpeedDepthLabelProvider;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.service.SpeedDepthService;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthTableComposite extends Composite {

	private SpeedDepthService speedDepthService;

	private Quote currentQuote;

	private Table table;
	private TableViewer tableViewer;
	private ToolItem lockPriceItem;

	private boolean isLock = false;

	private SpeedDepthContentProvider speedDepthContentProvider;

	private SpeedDepthMainComposite mainComposite;
	private ToolItem lastPriceItem;
	private TableColumn tblclmnAskVol;
	private TableColumn tblBidsVol;
	private ToolItem cancelItem;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SpeedDepthTableComposite(SpeedDepthMainComposite mainComposite,
			int style) {
		super(mainComposite, style);
		this.mainComposite = mainComposite;
		speedDepthService = new SpeedDepthService();
		initComponent();
		initProvider();
		initListener();
	}

	private void initComponent() {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		lastPriceItem = new ToolItem(toolBar, SWT.NONE);
		lastPriceItem.setImage(ResourceManager.getPluginImage(
				"com.cyanspring.cstw", "icons/start.png"));
		lastPriceItem.setToolTipText("Last Price");

		lockPriceItem = new ToolItem(toolBar, SWT.NONE);
		lockPriceItem.setImage(ResourceManager.getPluginImage(
				"com.cyanspring.cstw", "icons/stop.png"));
		lockPriceItem.setToolTipText("Lock Price");

		cancelItem = new ToolItem(toolBar, SWT.NONE);
		cancelItem.setImage(ResourceManager.getPluginImage(
				"com.cyanspring.cstw", "icons/cancel.png"));

		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBounds(0, 0, 85, 85);

		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(100);

		tblclmnAskVol = new TableColumn(table, SWT.NONE);
		tblclmnAskVol.setWidth(100);
		tblclmnAskVol.setText("Volume");

		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(100);
		tblclmnNewColumn_2.setText("Ask/Bid");

		tblBidsVol = new TableColumn(table, SWT.NONE);
		tblBidsVol.setWidth(100);
		tblBidsVol.setText("Volume");

		TableColumn tblclmnNewColumn_4 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_4.setWidth(100);
	}

	private void initProvider() {
		speedDepthContentProvider = new SpeedDepthContentProvider();
		tableViewer.setContentProvider(speedDepthContentProvider);
		tableViewer.setLabelProvider(new SpeedDepthLabelProvider());
	}

	private void initListener() {
		lockPriceItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tableViewer.getInput() == null) {
					return;
				}
				isLock = true;
				tableViewer.setInput(speedDepthService.getSpeedDepthList(
						currentQuote, isLock));
			}
		});
		lastPriceItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isLock = false;
				tableViewer.setInput(speedDepthService.getSpeedDepthList(
						currentQuote, isLock));
			}
		});

		cancelItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentQuote == null) {
					return;
				}
				speedDepthService.cancelOrder(currentQuote.getSymbol());
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				ViewerCell cell = tableViewer.getCell(new Point(e.x, e.y));
				if (cell.getElement() instanceof SpeedDepthModel) {
					SpeedDepthModel model = (SpeedDepthModel) cell.getElement();
					int columnIndex = cell.getColumnIndex();
					// ask
					if (columnIndex == 1) {
						speedDepthService.quickEnterOrder(model, "Buy",
								mainComposite.getDefaultQuantityText()
										.getText());
					}
					// bids
					else if (columnIndex == 3) {
						speedDepthService.quickEnterOrder(model, "Sell",
								mainComposite.getDefaultQuantityText()
										.getText());
					}
				}
			}
		});
	}

	public void setQuote(Quote quote) {
		currentQuote = quote;
		tableViewer.setInput(speedDepthService.getSpeedDepthList(currentQuote,
				isLock));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
