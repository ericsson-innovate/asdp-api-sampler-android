package com.idean.atthack;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.idean.atthack.api.ApiSpecHelper;
import com.idean.atthack.api.Param;
import com.idean.atthack.network.RequestHelper;
import com.idean.atthack.network.Result;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
	private static final String TAG = "Login";

	// UI references.
	private EditText mUsernameView;
	private EditText mVinView;
	private EditText mPinView;
	private ProgressBar mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Pref.USERNAME.contains(this)) {
			startActivity(new Intent(this, ListCategoriesActivity.class));
			finish();
			return;
		}
		setContentView(R.layout.activity_login);
		
		mProgress = (ProgressBar)findViewById(R.id.login_progress);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);
		mPinView = (EditText) findViewById(R.id.pin);
		mVinView = (EditText) findViewById(R.id.vin);

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
	}

	public void onSkipLogin(View view) {
		Log.d(TAG,"Skipping login");
		Pref.USERNAME.set(LoginActivity.this, RequestHelper.DEFAULT_USERNAME);
		Pref.PIN.set(LoginActivity.this, RequestHelper.DEFAULT_PASSWORD);
		Pref.VIN.set(LoginActivity.this, RequestHelper.DEFAULT_VIN);
		String base = Pref.SERVER.get(LoginActivity.this);
		if (TextUtils.isEmpty(base)) {
			base = RequestHelper.DEFAULT_FALLBACK_BASE;
		}
		Pref.SERVER.set(LoginActivity.this, base);
			
		startListCategoriesActivity();
	}
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {

		// Reset errors.
		mUsernameView.setError(null);
		mPinView.setError(null);
		mVinView.setError(null);

		// Store values at the time of the login attempt.
		String user = mUsernameView.getText().toString();
		String pin = mPinView.getText().toString();
		String vin = mVinView.getText().toString();		

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (TextUtils.isEmpty(user)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}
		if (TextUtils.isEmpty(pin)) {
			mPinView.setError(getString(R.string.error_field_required));
			focusView = mPinView;
			cancel = true;
		}
		if (TextUtils.isEmpty(vin)) {
			mVinView.setError(getString(R.string.error_field_required));
			focusView = mVinView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
			return;
		}
		new LoginTask().execute(null, null, null);
	}
	
	private void startListCategoriesActivity() {
		Log.d(TAG,"Stored username " + Pref.USERNAME.get(LoginActivity.this)
				 + "Pin " + Pref.PIN.get(LoginActivity.this) + ", Server " + Pref.SERVER.get(LoginActivity.this));
		
		finish();
		startActivity(new Intent(LoginActivity.this, ListCategoriesActivity.class));
	}

	private class LoginTask extends AsyncTask<Void, Void, Boolean> {		

		@Override
		protected Boolean doInBackground(Void... params) {
			// Load API's from server
			ApiSpecHelper.SINGLETON.loadSpecs(LoginActivity.this);
			
			RequestHelper h = new RequestHelper(LoginActivity.this);	
				
			Bundle param = new Bundle();
			param.putString(Param.username.name(), mUsernameView.getText().toString());
			param.putString(Param.pin.name(), mPinView.getText().toString());
			param.putString(Param.vin.name(), mVinView.getText().toString());
			Result result = h.login(param);
			return result.isSuccess();
		}

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean success) {
			mProgress.setVisibility(View.INVISIBLE);
			if (success) {
				// Store stuff in preferences
				String user = mUsernameView.getText().toString();
				String pin = mPinView.getText().toString();
				String vin = mVinView.getText().toString();		

				Pref.USERNAME.set(LoginActivity.this, user);
				Pref.PIN.set(LoginActivity.this, pin);
				Pref.VIN.set(LoginActivity.this, vin);
				String base = Pref.SERVER.get(LoginActivity.this);
				if (TextUtils.isEmpty(base)) {
					base = RequestHelper.DEFAULT_FALLBACK_BASE;
				}
				Pref.SERVER.set(LoginActivity.this, base);
				
				startListCategoriesActivity();
				return;
			}
			
			// failure
			mUsernameView.setError(LoginActivity.this.getString(R.string.error_bad_login));
			mUsernameView.requestFocus();
			
			super.onPostExecute(success);
		}
		
	}

}
