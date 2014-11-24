package com.spotz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.androidhive.R;
import com.spotz.comm.MessageManager;
import com.spotz.users.User;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

/**
 * This is the main Activity that displays the current chat session.
 */
public class LoginActivity extends AccountAuthenticatorActivity implements OnLoginListener {
	
    // Debugging
    public static final String TAG = "LogIN";
    
    //Login Layout InnerViews
    TextView txtUser, txtPass;
    Button 	login_btn, register_btn;
    ProgressBar	progbar;
    
    String mUsername, mPassword;
    public static LoginActivity instance;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Const.v(TAG, "+++ ON CREATE +++");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        
        instance = this;
        MessageManager.setOnLoginListener(this);
        
        txtUser = (TextView) findViewById(R.id.login_edtusername);
        txtPass = (TextView) findViewById(R.id.login_edtpassword);
        txtPass.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				tryLogin();				
				return false;
			}
        	
        });
        
        progbar = (ProgressBar) findViewById(R.id.login_progressbar);
        
        //give functionality to second button
        login_btn = (Button) findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new OnClickListener() {
            @Override
                public void onClick(View v) {
            		Const.i(TAG,"Change - CANCEL");
           	 		tryLogin();
                }
            });
        
        register_btn = (Button) findViewById(R.id.login_register_btn);
        register_btn.setOnClickListener(new OnClickListener() {
            @Override
                public void onClick(View v) {
	            	Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
	    			startActivity(registerIntent);
                }
            });
    }    
    
	
	private void buttonVisibility(final boolean visible){
		LoginActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				if(visible){
					login_btn.setVisibility(View.VISIBLE);
		    		progbar.setVisibility(View.GONE);
				}
				else{
					login_btn.setVisibility(View.GONE);
		    		progbar.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private void tryLogin(){
		if(login_btn.getVisibility() == View.VISIBLE)
			try {
	        	mUsername = txtUser.getText().toString();
	        	mPassword = txtPass.getText().toString();
	        	if(mUsername.equals("")){
	        		Toast.makeText(getApplicationContext(), getText(R.string.please_input)+" "+getText(R.string.usernameoremail), 
                            Toast.LENGTH_SHORT).show();
	        	}else if( mPassword.equals("")){
	        		Toast.makeText(getApplicationContext(), getText(R.string.please_input)+" "+getText(R.string.password), 
                            Toast.LENGTH_SHORT).show();
	        	}
	        	else{
	        		if(mUsername.length() < 6){
	        			Toast.makeText(getApplicationContext(), getText(R.string.username_length), 
	                            Toast.LENGTH_SHORT).show();
	        		}else{
	        			buttonVisibility(false);
		        		MessageManager.sendLogIn(mUsername, mPassword);
	        		}
	        		
	        	}
			} catch (Throwable e) {	if(Const.D) Log.e(TAG,e.getMessage()+" - Click");}
	}

	
    @Override
    public void onStart() {
        super.onStart();
        Const.v(TAG, "++ ON START ++");        
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Const.v(TAG, "+ ON RESUME +");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Const.v(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        Const.v(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Const.v(TAG, "--- ON DESTROY ---");
    }

	@Override
	public void onBackPressed() {
		Const.v(TAG,"***OnBackPressed****");
		//finish parent
		//Main.instance.finishFromChild(this);
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
		//finish this activity
		finish();
	}


	@Override
	public void onLoginSuccess(String json) {
		/*
		Toast.makeText(this, getString(R.string.login_success), 
                Toast.LENGTH_SHORT).show();
		*/
		buttonVisibility(true);
		/// PROCESS JSON
		
		JSONObject res;
		
		try {
			//res = new JSONArray(json);
			res = new JSONObject(json);
			//Log.d("TAG","ACAAA="+res.getJSONArray("info") );
			User.current().initPlayer( res.getJSONArray("info") );
			//Team.current().initTeam( res.getJSONObject("team") );
		} catch (JSONException e) {
			Const.e(TAG, "tronix at onLoginSuccess:");
			e.printStackTrace();
		}
		
		
		
		/////
		Intent returnIntent = new Intent(LoginActivity.this,CameraActivity.class);
		setResult(RESULT_OK, returnIntent);
		//finish this activity
		finish();
		/*
		Intent cameraIntent= new Intent(LoginActivity.this, CameraActivity.class);
    	cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	//openMainActivity.setFlags(Intent.);
        startActivity(cameraIntent); 
		  
		 * */
	}


	@Override
	public void onLoginError(String error) {
		if(error.equals("02")){
			//Toast.makeText(LoginActivity.instance, getString(R.string.not_json_checkdata), 
            //        Toast.LENGTH_SHORT).show();
			
			return;
		}
		/*
		if (error.startsWith("http://") ||
				error.startsWith("https://") ||
				error.startsWith("failed to connect"))
			Toast.makeText(getApplicationContext(), getString(R.string.server_down), 
                    Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getApplicationContext(), error, 
                    Toast.LENGTH_SHORT).show();
		*/
		Const.e(TAG,"Login Error - "+error);
		
		
		Account[] acs = AccountManager.get(this).getAccounts();
        for (Account a: acs)
			Log.d(TAG,a.name+" - "+a.type+" * ");
        
        Const.v(TAG,"********* Adding new account **************");
        //final Account account = new Account(mUsername, credential_type);
        //AccountManager.get(this).addAccountExplicitly(account, mPassword, null);
        buttonVisibility(true);
	}

	

}