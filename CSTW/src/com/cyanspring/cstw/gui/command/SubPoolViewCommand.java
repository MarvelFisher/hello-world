package com.cyanspring.cstw.gui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.cstw.ui.views.SubPoolView;

/**
 * @author Junfeng
 * @create 19 Nov 2015
 */
public class SubPoolViewCommand extends AbstractHandler {

	private static final Logger log = LoggerFactory
			.getLogger(SubPoolViewCommand.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(SubPoolView.ID);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
