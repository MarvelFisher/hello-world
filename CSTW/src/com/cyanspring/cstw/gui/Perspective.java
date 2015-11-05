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
package com.cyanspring.cstw.gui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.cyanspring.cstw.ui.views.ChildOrderView;
import com.cyanspring.cstw.ui.views.ExecutionView;
import com.cyanspring.cstw.ui.views.MultiInstrumentStrategyView;
import com.cyanspring.cstw.ui.views.PropertyView;
import com.cyanspring.cstw.ui.views.SingleInstrumentStrategyView;
import com.cyanspring.cstw.ui.views.SingleOrderStrategyView;
import com.cyanspring.cstw.ui.views.StrategyLogView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
//		layout.setFixed(true);
//	    layout.addView(SingleOrderStrategyView.ID, IPageLayout.LEFT,
//		        1.0f, layout.getEditorArea());
//	    layout.addView(MultiInstrumentStrategyView.ID, IPageLayout.LEFT,
//		        1.0f, layout.getEditorArea());
//	    layout.addView(ChildOrderView.ID, IPageLayout.LEFT,
//		        1.0f, layout.getEditorArea());
//	    layout.addView(PropertyView.ID, IPageLayout.LEFT,
//		        1.0f, layout.getEditorArea());	
//	    layout.addView(StrategyLogView.ID, IPageLayout.LEFT,
//		        1.0f, layout.getEditorArea());
//	    layout.addView(ExecutionView.ID, IPageLayout.LEFT,
//		        1.0f, layout.getEditorArea());
//	    layout.addView(SignalView.ID, IPageLayout.LEFT,
//		        1.0f, layout.getEditorArea());
	    
	    layout.addFastView(SingleOrderStrategyView.ID);
	    layout.addFastView(SingleInstrumentStrategyView.ID);
	    layout.addFastView(MultiInstrumentStrategyView.ID);
	    layout.addFastView(ChildOrderView.ID);
	    layout.addFastView(PropertyView.ID);	
	    layout.addFastView(StrategyLogView.ID);
	    layout.addFastView(ExecutionView.ID);
		
	}

}
