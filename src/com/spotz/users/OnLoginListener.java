package com.spotz.users;

public interface OnLoginListener {

	public void onLoginSuccess(String json);
	
	public void onLoginError(String error);
}