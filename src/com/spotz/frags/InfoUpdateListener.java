package com.spotz.frags;

public interface InfoUpdateListener {
	
	/** Refresh the UI  */
	public void refreshUI();	
	
	/** Refresh the UI on a new thread */
	public void refreshUI_newThread();

}