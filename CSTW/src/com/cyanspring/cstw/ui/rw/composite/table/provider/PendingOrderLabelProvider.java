/**
 * 
 */
package com.cyanspring.cstw.ui.rw.composite.table.provider;

import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;

/**
 * @author Yu-Junfeng
 * @create 24 Aug 2015
 */
public class PendingOrderLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		RCOrderRecordModel model = (RCOrderRecordModel) element;
		switch (columnIndex) {
		case 0:
			return model.getSubAccount();
		case 1:
			return model.getOrderId();
		case 2:
			return model.getSymbol();
		case 3:
			return model.getSymbolName();
		case 4:
			return model.getSide();
		case 5:
			return model.getPrice().toString();
		case 6:
			return model.getVolume().toString();
		case 7:
			return model.getOrderStatus();
		case 8:
			return model.getCreateTime();
		case 9:
			return model.getTrader();
		default:
			return "";
		}
	}

}