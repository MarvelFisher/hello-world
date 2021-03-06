package com.cyanspring.cstw.ui.rw.composite.table;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.common.TableType;
import com.cyanspring.cstw.ui.rw.composite.table.provider.RWTradeRecordLabelProvider;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWTradeRecordTableComposite extends BasicTableComposite {

	public RWTradeRecordTableComposite(Composite parent, int style) {
		super(parent, style, TableType.RWTradeRecord);
		
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new RWTradeRecordLabelProvider();
	}

}
