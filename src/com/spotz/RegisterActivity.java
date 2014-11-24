package com.spotz;

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
import android.app.Activity;
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


public class RegisterActivity extends Activity implements OnRegisterListener{
	
    // Debugging
    public static final String TAG = "Register";
    
    //Login Layout InnerViews
    TextView txtUsername, txtEmail, txtPassword, txtRepeatpassword, txtMessage;
    Button 	register_btn;
    ProgressBar	progbar;
    
    String mUsername,mEmail, mRepeatedpassword, mPassword;
    public final String credential_type = "com.spotz";
    public static RegisterActivity instance;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Const.v(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.register);
        
        instance = this;
        MessageManager.setOnRegisterListener(this);
        
        
        
        txtUsername			= (TextView) findViewById(R.id.register_edtusername);
        txtEmail			= (TextView) findViewById(R.id.register_edtemail);
        txtPassword			= (TextView) findViewById(R.id.register_edtpassword);
        txtRepeatpassword	= (TextView) findViewById(R.id.register_edtrepeatpassword);
        txtMessage 			= (TextView) findViewById(R.id.reg_infomessage);
        
        txtRepeatpassword.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				tryRegister();				
				return false;
			}
        });
        
        progbar = (ProgressBar) findViewById(R.id.register_progressbar);
        
        register_btn = (Button) findViewById(R.id.register_btn);
        register_btn.setOnClickListener(new OnClickListener() {
            @Override
                public void onClick(View v) {
            		Const.i(TAG,"12Change - CANCEL");
           	 		tryRegister();
                }
            });
        
    }    
    
	
	private void buttonVisibility(final boolean visible){
		RegisterActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				if(visible){
					register_btn.setVisibility(View.VISIBLE);
		    		progbar.setVisibility(View.GONE);
				}
				else{
					register_btn.setVisibility(View.GONE);
		    		progbar.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private void tryRegister(){
		if(register_btn.getVisibility() == View.VISIBLE)
			try {
				
	        	mUsername 			= txtUsername.getText().toString();
				mEmail 				= txtEmail.getText().toString();
				mPassword 			= txtPassword.getText().toString();
				mRepeatedpassword 	= txtRepeatpassword.getText().toString();
				
	        	if(mUsername.equals("")){
	        		Toast.makeText(getApplicationContext(), getText(R.string.please_input)+" "+getText(R.string.username), 
                            Toast.LENGTH_SHORT).show();
	        	}else if(mEmail.equals("")){
	        		Toast.makeText(getApplicationContext(), getText(R.string.please_input)+" "+getText(R.string.email), 
                            Toast.LENGTH_SHORT).show();
	        	}else if( mRepeatedpassword.equals("")){
	        		Toast.makeText(getApplicationContext(), getText(R.string.please_input)+" "+getText(R.string.password), 
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
	        			
	        			if(!mEmail.matches(Utils.EMAIL_REGEX)){
	        				Toast.makeText(getApplicationContext(), getText(R.string.invalid_email), 
		                            Toast.LENGTH_SHORT).show();
	        			}else if(mRepeatedpassword.equals(mPassword)){
	        				buttonVisibility(false);
			        		MessageManager.sendRegister(mUsername, mEmail, mPassword, mRepeatedpassword);
	        			}
	        			
	        			
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
	public void onRegisterSuccess(String json) {
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
		Intent returnIntent = new Intent(RegisterActivity.this,CameraActivity.class);
		setResult(RESULT_OK, returnIntent);
		finish();
		startActivity(returnIntent);
		//finish this activity
		
		/*
		Intent cameraIntent= new Intent(LoginActivity.this, CameraActivity.class);
    	cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	//openMainActivity.setFlags(Intent.);
        startActivity(cameraIntent); 
		  
		 * */
	}


	@Override
	public void onRegisterError(String error) {
		
		if(error.toLowerCase().contains("failed to connect")){
			error = getString(R.string.connection_problem);
		}
		
		if (error.startsWith("http://") ||
				error.startsWith("https://") ||
				error.startsWith("failed to connect"))
			setMessage(getString(R.string.server_down));
			
			//Toast.makeText(getApplicationContext(), getString(R.string.server_down), 
            //        Toast.LENGTH_SHORT).show();
		else
			setMessage(error);
			//txtMessage.setText(error);
			//Toast.makeText(getApplicationContext(), error, 
            //        Toast.LENGTH_SHORT).show();
		
		Const.e(TAG,"Login Error - "+error);
		
		
		Account[] acs = AccountManager.get(this).getAccounts();
        for (Account a: acs)
			Log.d(TAG,a.name+" - "+a.type+" * ");
        
        Const.v(TAG,"********* Adding new account **************");
        //final Account account = new Account(mUsername, credential_type);
        //AccountManager.get(this).addAccountExplicitly(account, mPassword, null);
        buttonVisibility(true);
	}

	public void setMessage(final String message){
		RegisterActivity.this.runOnUiThread(new Runnable() {
			     public void run() {    
		             try{
		            	 RegisterActivity.this.txtMessage.setText(message);
		             }
		             catch(Exception e){}
		     }
		});
		
		//Thread.currentThread().stop();
	}
}