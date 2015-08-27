package com.cyanspring.common.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INFO/spring/CategoryFilterTest.xml" })
public class TestCategoryFilter {

	@Autowired
	@Qualifier("categoryFilter")
	IRefDataFilter iDataFilter;
	
	RefData refData1;
	RefData refData2;
	RefData refData3;
	RefData refData4;
	List<RefData> lstRefData;
	
	@Before
	public void before() {
		lstRefData = new ArrayList<RefData>();
	}
	
	@Test
	public void testRefDataFilter() throws Exception {
		refData1 = new RefData();
		refData1.setIType(IType.FUTURES_CX.getValue());
		refData1.setSymbol("IF1502");
		refData1.setCategory("AG");
		refData1.setExchange("SHF");
		refData1.setRefSymbol("AG12.SHF");
		refData1.setCommodity("F");

		// This record doesn't exist in FcRefDataTemplate thus will be excluded.
		refData2 = new RefData();
		refData2.setIType(IType.FUTURES.getValue());
		refData2.setSymbol("ag1511.SHF");
		refData2.setCategory("BG");
		refData2.setExchange("SHF");
		refData2.setRefSymbol("AG11.SHF");
		refData2.setCommodity("F");

		refData3 = new RefData();
		refData3.setIType(IType.FUTURES_CX.getValue());
		refData3.setSymbol("IF1502");
		refData3.setCategory("AG");
		refData3.setExchange("SHF");
		refData3.setRefSymbol("AG.SHF");
		refData3.setCommodity("F");
		
		// Commodity is not "F", won't be filtered out
		refData4 = new RefData();
		refData4.setIType(IType.FUTURES_CX.getValue());
		refData4.setSymbol("IF1502");
		refData4.setCategory("AG");
		refData4.setExchange("SHF");
		refData4.setRefSymbol("AG.SHF");
		refData4.setCommodity("A");

		lstRefData.add(refData1);
		lstRefData.add(refData2);
		lstRefData.add(refData3);
		lstRefData.add(refData4);
		assertEquals(4, lstRefData.size());

		List<RefData> lstFilteredRefData = (List<RefData>) iDataFilter.filter(lstRefData);
		assertEquals(3, lstFilteredRefData.size());
	}
	
}