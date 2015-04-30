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
package com.cyanspring.common.marketdata;

public interface IMarketDataAdaptor {
	public void init() throws Exception;
	public void uninit();
	public boolean getState();
	public void subscribeMarketDataState(IMarketDataStateListener listener);
	public void unsubscribeMarketDataState(IMarketDataStateListener listener);
	public void subscribeMarketData(String instrument, IMarketDataListener listener) throws MarketDataException;
	public void unsubscribeMarketData(String instrument, IMarketDataListener listener);
	public void subscirbeSymbolData(ISymbolDataListener listener) ;
	public void unsubscribeSymbolData(ISymbolDataListener listener);
	public void refreshSymbolInfo(String market);
	public void processEvent(Object object);
}
