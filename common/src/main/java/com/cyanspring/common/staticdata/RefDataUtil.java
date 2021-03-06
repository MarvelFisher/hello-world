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
package com.cyanspring.common.staticdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.marketsession.ITradeDate;
import com.cyanspring.common.marketsession.IndexSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.util.TimeUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataUtil {
	private static final Logger log = LoggerFactory
			.getLogger(RefDataUtil.class);

	public static MarketSessionUtil marketSessionUtil;

	private static Calendar truncateDate(Calendar cal) {
		cal.set(Calendar.DATE, cal.getMinimum(Calendar.DATE));
		cal.set(Calendar.HOUR,cal.getMaximum(Calendar.HOUR));
		cal.set(Calendar.MINUTE,cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.SECOND,cal.getMaximum(Calendar.SECOND));
		return cal;
	}
	
	public static ITradeDate getTradeManager(String category) {
		if (!StringUtils.hasText(category)) {
			return null;
		}

		if (marketSessionUtil == null) {
			log.info("marketSessionUtil is null");
			return null;
		}

		return marketSessionUtil.getTradeDateManager(category);
	}

	public static Calendar parseSettlementDate(String date)
			throws ParseException {
		Date nowDate = TimeUtil.parseDate(date, "yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		return cal;
	}

	public static String formatSettlementDate(Date date) throws ParseException {
		return TimeUtil.formatDate(date, "yyyy-MM-dd");
	}

	public static String calSettlementDateByDay(RefData refData, Calendar cal,
			int dayInMonth) {

		String category = refData.getCategory();
		if (!StringUtils.hasText(category)) {
			return null;
		}

		Calendar calDate = Calendar.getInstance();
		calDate.setTime(cal.getTime());
		truncateDate(calDate);

		ITradeDate tradeDateManager = getTradeManager(category);

		if (null == tradeDateManager) {
			log.warn("category:{} can't find tradeDateManager!", category);
			return null;
		}

		calDate.set(Calendar.DAY_OF_MONTH, dayInMonth);
		while (tradeDateManager.isHoliday(calDate.getTime())) {
			calDate.add(Calendar.DAY_OF_MONTH, 1);
		}

		String settlementDate = null;

		try {
			settlementDate = formatSettlementDate(calDate.getTime());
		} catch (ParseException e) {
			log.warn(e.getMessage(), e);
		}
		return settlementDate;
	}

	public static String calSettlementDateByTradeDate(RefData refData,
			Calendar cal, int tradeDateInMonth) {

		String category = refData.getCategory();

		if (!StringUtils.hasText(category)) {
			return null;
		}

		Calendar calDate = Calendar.getInstance();
		calDate.setTime(cal.getTime());
		truncateDate(calDate);

		ITradeDate tradeDateManager = getTradeManager(category);
		Date date = calDate.getTime();
		if (null == tradeDateManager) {
			log.warn("category:{} can't find tradeDateManager!", category);
			return null;
		}

		if (tradeDateInMonth > 0) {
			date = tradeDateManager.preTradeDate(date);
			for (int i = 0; i < tradeDateInMonth; i++) {
				date = tradeDateManager.nextTradeDate(date);
			}
		} else {
			int count = Math.abs(tradeDateInMonth);
			for (int i = 0; i < count; i++) {
				date = tradeDateManager.preTradeDate(date);
			}
		}

		String settlementDate = null;

		try {
			settlementDate = formatSettlementDate(date);
		} catch (ParseException e) {
			log.warn(e.getMessage(), e);
		}

		return settlementDate;
	}

	public static String calSettlementDateByWeekDay(RefData refData,
			Calendar cal, int weeks, int daysInWeek) {

		String category = refData.getCategory();

		if (!StringUtils.hasText(category)) {
			return null;
		}

		Calendar calDate = Calendar.getInstance();
		calDate.setTime(cal.getTime());
		truncateDate(calDate);

		// Get the first desired DAY_OF_WEEK in month
		while (calDate.get(Calendar.DAY_OF_WEEK) != daysInWeek) {
			calDate.add(Calendar.DAY_OF_MONTH, 1);
		}
		// Get the desired n-th DAY_OF_WEEK in month
		calDate.add(Calendar.DAY_OF_MONTH, 7 * (weeks - 1));

		ITradeDate tradeDateManager = getTradeManager(category);

		if (null == tradeDateManager) {
			log.warn("category:{} can't find tradeDateManager!", category);
			return null;
		}

		while (tradeDateManager.isHoliday(calDate.getTime())) {
			calDate.add(Calendar.DAY_OF_YEAR, 1);
		}

		String settlementDate = null;

		try {
			settlementDate = formatSettlementDate(calDate.getTime());
		} catch (ParseException e) {
			log.warn(e.getMessage(), e);
		}

		return settlementDate;
	}

	public static String getSearchIndex(RefData refData) {
		String index = null;
		String indexType = refData.getIndexSessionType();
		if (indexType != null) {
			if (indexType.equals(IndexSessionType.SETTLEMENT.toString())) {
				index = refData.getSymbol();
			} else if (indexType.equals(IndexSessionType.SPOT.toString())) {
				index = refData.getCategory();
			} else if (indexType.equals(IndexSessionType.EXCHANGE.toString())) {
				index = refData.getExchange();
			}
		}
		return index;
	}

	// private static ArrayList<Double> getVolProfile() {
	// ArrayList<Double> volProfile;
	// volProfile = new ArrayList<Double>();
	// volProfile.add(10.0);
	// volProfile.add(18.0);
	// volProfile.add(25.0);
	// volProfile.add(27.0);
	// volProfile.add(31.0);
	// volProfile.add(36.0);
	// volProfile.add(41.0);
	// volProfile.add(45.0);
	// volProfile.add(51.0);
	// volProfile.add(57.0);
	//
	// volProfile.add(61.0);
	// volProfile.add(63.0);
	// volProfile.add(66.0);
	// volProfile.add(69.0);
	// volProfile.add(72.0);
	// volProfile.add(76.0);
	// volProfile.add(78.0);
	// volProfile.add(83.0);
	// volProfile.add(87.0);
	// volProfile.add(100.0);
	//
	// return volProfile;
	// }

	public MarketSessionUtil getMarketSessionUtil() {
		return marketSessionUtil;
	}

	@Autowired(required = true)
	public void setMarketSessionUtil(MarketSessionUtil marketSessionUtil) {
		RefDataUtil.marketSessionUtil = marketSessionUtil;
	}

	public static void main(String args[]) {
		
		XStream xstream = new XStream(new DomDriver());
		ArrayList<RefData> list;

		list = new ArrayList<RefData>();
		RefData refData;

		refData = new RefData();
		refData.put(RefDataField.SYMBOL.value(), "0005.HK");
		refData.put(RefDataField.LOT_SIZE.value(), 400);
		list.add(refData);

		refData = new RefData();
		refData.put(RefDataField.SYMBOL.value(), "0016.HK");
		refData.put(RefDataField.LOT_SIZE.value(), 1000);
		list.add(refData);

		refData = new RefData();
		refData.put(RefDataField.SYMBOL.value(), "1398.HK");
		refData.put(RefDataField.LOT_SIZE.value(), 1000);
		list.add(refData);

		File file = new File("refdata/refDataSample.xml");
		try {
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(list, os);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
