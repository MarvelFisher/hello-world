/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * <p/>
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.staticdata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefDataManager extends RefDataService {

    Map<String, RefData> map = new HashMap<String, RefData>();
    private boolean changeMode = false;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws Exception {
        if(!changeMode) {
            log.info("initialising with " + refDataFile);
            XStream xstream = new XStream(new DomDriver());
            File file = new File(refDataFile);
            List<RefData> list;
            if (file.exists()) {
                list = (List<RefData>) xstream.fromXML(file);
            } else {
                throw new Exception("Missing refdata file: " + refDataFile);
            }
            injectionMap(list);
        }
    }

    public void injectionMap(List<RefData> refDataList){
        for (RefData refData : refDataList) {
            updateMarginRate(refData);
            updateCommission(refData);
            map.put(refData.getSymbol(), refData);
        }
    }

    @Override
    public List<RefData> updateAll(String tradeDate) throws Exception {
        return new ArrayList<RefData>(map.values());
    }

    @Override
    public void uninit() {
        log.info("uninitialising");
        map.clear();
    }

    @Override
    public RefData getRefData(String symbol) {
        return map.get(symbol);
    }

    @Override
    public List<RefData> getRefDataList() {
        return new ArrayList<RefData>(map.values());
    }

    @Override
    public void clearRefData() {
        map.clear();
    }

	@Override
	public List<RefData> update(String index, String tradeDate) throws Exception {
		return new ArrayList<>();
	}

	@Override
	public boolean remove(RefData refData) {
		if (map.get(refData.getSymbol()) != null) {
			map.remove(refData);
			return true;
		}
		return false;
	}

	@Override
	public void setQuoteFile(String quoteFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveRefDataToFile() {
		// TODO Auto-generated method stub
		
	}
}
