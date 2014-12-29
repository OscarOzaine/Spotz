package com.spotz;



import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.spotz.comm.MessageManager;
import com.spotz.gen.R;
import com.spotz.services.LoginService;
import com.spotz.services.UploadProfilePicService;
import com.spotz.users.OnLoginListener;
import com.spotz.users.User;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import com.facebook.*;
import com.facebook.model.GraphUser;

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
	
    private LoginButton loginButton;
    private GraphUser user;
    private PendingAction pendingAction = PendingAction.NONE;
    private UiLifecycleHelper uiHelper;
    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
    
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	Log.d(TAG, "StatusCallback");
            onSessionStateChange(session, state, exception);
        }
        
    };
 
    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("FBError: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };
    
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	Log.d(TAG, "onSessionStateChange");
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Canceled")
                    .setMessage("Permission not granted")
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
        /*
        if(Session.getActiveSession() != null){
        	Intent cameraIntent= new Intent(LoginActivity.this, LoadingActivity.class);
        	cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        	//openMainActivity.setFlags(Intent.);
            startActivity(cameraIntent);
        }
        */
        //updateUI();
    }

    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_PHOTO:
                //postPhoto();
                break;
            case POST_STATUS_UPDATE:
                //postStatusUpdate();
                break;
        }
    }
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Const.v(TAG, "+++ ON CREATE +++");
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
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
	    			overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
                }
            });
        
        
        loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email","user_friends"));
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
            	if(user != null){
            		fbLogin(user);
            	}else{
            		/*
            		Toast.makeText(getApplicationContext(), "Error in Facebook login", 
                            Toast.LENGTH_SHORT).show();
            		*/
            	}
            }
        });
    }    
    
	private void fbLogin(GraphUser user) {
        Session session = Session.getActiveSession();
        /*
    	Log.d(TAG,"Id = "+user.getId());
    	Log.d(TAG,"FirstName = "+user.getFirstName());
    	Log.d(TAG,"lastname = "+user.getLastName());
    	Log.d(TAG,"Link = "+user.getLink());
    	*/
    	try {
        	JSONObject outerObject = new JSONObject();
        	JSONArray outerArray = new JSONArray();
        	
        	outerArray.put(user.getInnerJSONObject());
        	outerObject.put("event", "facebook_login");
			outerObject.put("info", outerArray);
			Log.d(TAG,"Json = "+outerObject.toString());
			/*
			Log.d(TAG,"Json = "+outerObject.toString());
			Log.d(TAG,"Email = "+user.getInnerJSONObject().getString("email"));
			Log.d(TAG,"Gender = "+user.getInnerJSONObject().getString("gender"));
			Log.d(TAG,"accesstoken = "+session.getAccessToken());
			*/
			
			Intent intentUploadProfilePic = new Intent(LoginActivity.this, LoginService.class);
	        intentUploadProfilePic.putExtra("json", ""+outerObject);
	        intentUploadProfilePic.putExtra("accesstoken", session.getAccessToken());
    		startService(intentUploadProfilePic);
    		
    		Intent cameraIntent= new Intent(LoginActivity.this, LoadingActivity.class);
            startActivity(cameraIntent);
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
            finish();
    		/*
			if(outerArray != null){
				MessageManager.sendFBLogin(outerObject, session.getAccessToken());
			}
			*/
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
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
        //SessionManager.requestLogin(this); 
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Utils.setCurrentLocale(this);
        Const.v(TAG, "+ ON RESUME +");
        uiHelper.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        uiHelper.onPause();
        AppEventsLogger.deactivateApp(this);
        Const.v(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        uiHelper.onStop();
        Const.v(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
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
		Log.d(TAG,"LoginSuccess = "+json);
		JSONObject res;
		
		try {
			//res = new JSONArray(json);
			res = new JSONObject(json);
			Log.d("TAG","ACAAA="+res.getJSONArray("info") );
			User.current().initPlayer( res.getJSONArray("info") );
			//Team.current().initTeam( res.getJSONObject("team") );
		} catch (JSONException e) {
			Const.e(TAG, "tronix at onLoginSuccess:");
			e.printStackTrace();
		}
		
		
		
		Intent cameraIntent= new Intent(LoginActivity.this, CameraActivity.class);
    	cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	//openMainActivity.setFlags(Intent.);
        startActivity(cameraIntent); 
        finish();
		
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

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"activityResult");
        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
    }
	


}