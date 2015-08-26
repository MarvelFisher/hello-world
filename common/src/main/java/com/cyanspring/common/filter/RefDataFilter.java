package com.cyanspring.common.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataTplLoader;
import com.cyanspring.common.staticdata.fu.IType;

/**
 * 
 * @author alvinxie
 * 
 */
public class RefDataFilter implements IDataObjectFilter {
	
	@Autowired
	RefDataTplLoader refDataTplLoader;
	
	private static final Logger log = LoggerFactory.getLogger(RefDataFilter.class);
	private boolean checkValidContractDate = true;
	private IType[] types;

	public IType[] getTypes() {
		return types;
	}

	public void setTypes(IType[] types) {
		this.types = types;
	}

	/**
	 * Change the value of property "types" of bean "refDataFilter" to filter.
	 * 
	 * DataObjects with matching "iType" and non-duplicate Category will be 
	 * returned as a new DataObject list. 
	 * 
	 * Reference path:
	 * server/conf/fc/fc.xml
	 * 
	 * Key: iType + Symbol
	 * {category}.{exchange} = {refSymbol} means 活躍 (若同類別存在同年月, 活躍優先保存)
	 * 
	 * @param lstDataObj
	 *            The DataObject list to be filtered
	 * @return The filtered DataObject list
	 */
	@Override
	public List<? extends DataObject> filter(List<? extends DataObject> lstDataObj) throws Exception {
		ConcurrentHashMap<String, RefData> mapRefData = new ConcurrentHashMap<String, RefData>();

		ArrayList<String> lstITypes = new ArrayList<String>();
		for (IType iType : getTypes()) {
			lstITypes.add(iType.getValue());
		}

		for (DataObject obj : lstDataObj) {
			RefData refData = (RefData) obj;
			String type = refData.getIType();
			if (type == null || type.isEmpty()) {
				log.error("IType cannot be null or empty.");
				throw new Exception("IType cannot be null or empty.");
			}
			
			if (lstITypes.contains(type)) {
				String symbol = refData.getSymbol();
				if (symbol != null && !symbol.isEmpty()) {
					symbol = symbol.toLowerCase();
				} else {
					log.error("Symbol cannot be null or empty.");
					throw new Exception("Symbol cannot be null or empty.");
				}
				
				String category = refData.getCategory();
				String exchange = refData.getExchange();
				String refSymbol = refData.getRefSymbol();
				
				if (category == null || category.isEmpty()
						|| exchange == null || exchange.isEmpty()
						|| refSymbol == null || refSymbol.isEmpty()) {
					log.error("Category, Exchange, RefSymbol "
							+ "cannot be null or empty.");
					throw new Exception("Category, Exchange, RefSymbol "
							+ "cannot be null or empty.");
				}
				
				String key = type + symbol;
				// If DataObject has duplicate IType+Symbol, exclude the later one unless it's 活躍
				if (mapRefData.containsKey(key)) { 
					if (refSymbol.equals(category + "." + exchange)) {
						// Means current one is 活躍, remove existing one
						mapRefData.remove(key);
					} else {
						continue; // Even Map has unique keys...
					}
				}
				
				mapRefData.put(key, refData);
			}
		}

		List<RefData> lstRefData = new ArrayList<RefData>(mapRefData.values());
		
		lstRefData = excludeNonExistingProducts(lstRefData);
		
		if(isCheckValidContractDate())
			lstRefData = excludeInvalidContractDate(lstRefData);
		
		return lstRefData;
	}
	
	public List<RefData> excludeInvalidContractDate(List<RefData> lstRefData){
		
		List<RefData> newList = new ArrayList<RefData>();
		if(null == lstRefData || lstRefData.isEmpty())
			return newList;
		
		for(RefData data : lstRefData){
			if(isValidContractDate(data)){
				newList.add(data);
			}
		}
		return newList;
	}
	
	private boolean isValidContractDate(RefData refData){		
		if( null == refData)
			return false;
		
		String settlementDate	= null;	
		try {
			settlementDate = refData.getSettlementDate();
			if(!StringUtils.hasText(settlementDate))
				return false;
			
			SimpleDateFormat contractFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar now = Calendar.getInstance();
			int thisYear = now.get(Calendar.YEAR);			
			Calendar contractCal = Calendar.getInstance();
			contractCal.setTime(contractFormat.parse(settlementDate));
			int contractYear = contractCal.get(Calendar.YEAR);

			if((contractYear-thisYear)>=5 || (contractYear-thisYear)<0){
				return false;
			}else{
				return true;
			}
					
		} catch (ParseException e) {
			log.warn("not valid contract date:{},{}",settlementDate,e.getMessage());
		} catch (Exception e){
			log.warn("can't find settlementDate :{},{}",refData.getSymbol(),e.getMessage());
		}

		return false;
	}
	
	private List<RefData> excludeNonExistingProducts(List<RefData> lstRefData) throws Exception {
		if (lstRefData != null && lstRefData.size() > 0) {
			// Compare RefData list from template with the input lstRefData
			// If Category of RefData in the input lstRefData doesn't exist in template, exclude it
			// After filtering, only Category in template will be kept in the returned lstRefData
			List<RefData> lstRefDataTpl = refDataTplLoader.getRefDataList();
			if (lstRefDataTpl != null && lstRefDataTpl.size() > 0) {
				ArrayList<String> lstCategory = new ArrayList<String>();
				for (RefData data : lstRefDataTpl) {
					lstCategory.add(data.getCategory());
				}
				
				Iterator<RefData> itRefData = lstRefData.iterator();
				while (itRefData.hasNext()) {
					RefData data = itRefData.next();
					if (!lstCategory.contains(data.getCategory())) {
						itRefData.remove();
					}
				}
			}
		} else {
			log.error("The given RefData list cannot be null or empty");
			throw new Exception("The given RefData list cannot be null or empty");
		}
		
		return lstRefData;
	}

	public boolean isCheckValidContractDate() {
		return checkValidContractDate;
	}

	public void setCheckValidContractDate(boolean checkValidContractDate) {
		this.checkValidContractDate = checkValidContractDate;
	}
}
