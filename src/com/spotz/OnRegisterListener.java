package com.spotz;

public interface OnRegisterListener {

	public void onRegisterSuccess(String json);
	
	public void onRegisterError(String error);
}