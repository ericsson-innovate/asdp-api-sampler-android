package com.idean.atthack;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.idean.atthack.api.ApiSpec;
import com.idean.atthack.api.ApiSpecHelper;
import com.idean.atthack.api.Param;
import com.idean.atthack.network.RequestHelper;
import com.idean.atthack.network.Result;

public class ViewRespActivity extends ActionBarActivity {
	
	public static final String EXTRA_REQ_PARAMS = "EXTRA_REQ_PARAMS";
	private static final String TAG = "ViewResp";
	
	private ProgressBar mProgress;
	private ApiSpec mSpec;

	private EditText mResponseBody;
	private TextView mResponseStatus;
	private RequestHelper mRequestHelper;
	private View mCheckStatusParent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.apiresponse);
		mSpec = getSpec(getIntent());
		Bundle reqParams = getIntent().getBundleExtra(EXTRA_REQ_PARAMS);
		if (mSpec == null || reqParams == null) {
			Log.w(TAG, "Unable to determine api specfrom intent. Req bundle: " + reqParams);
			Toast.makeText(this, R.string.no_api, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// bind name and description
		getSupportActionBar().setTitle("Response: " + mSpec.name);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mResponseBody = (EditText) findViewById(R.id.response_body);
		mResponseStatus = (TextView) findViewById(R.id.response_status);
		mCheckStatusParent = findViewById(R.id.resp_check_status);
		mRequestHelper = new RequestHelper(this);
		
		new ExecApiTask().execute(reqParams, null, null);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
		return super.onOptionsItemSelected(item);
	}


	private ApiSpec getSpec(Intent intent) {
		String specName = intent.getStringExtra(EditReqActivity.EXTRA_SPECID);
		if (specName == null) {
			return null;
		}
		return ApiSpecHelper.SINGLETON.getSpec(specName);
	}

	private class CheckRequestStatus extends AsyncTask<Result, Void, Result> {

		@Override
		protected Result doInBackground(Result... params) {
			Result result = params[0];
			Gson gson = new Gson();
			ResponseBody resp = gson.fromJson(result.body, ResponseBody.class);
			Log.d(TAG, "Got request id " + resp.requestId);
			if (!TextUtils.isEmpty(resp.requestId)) {
				return mRequestHelper.checkRequestStatus(resp.requestId);
			}
			return new Result(400);
		}

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			mCheckStatusParent.setVisibility(View.GONE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Result result) {
			mProgress.setVisibility(View.INVISIBLE);
			Log.d(TAG, "Got resp " + result.prettyJsonBody());
			mResponseBody.setText(result.prettyJsonBody());
			mResponseStatus.setText(result.statusCode + " : "
					+ result.message());
		}

	}

	private class ExecApiTask extends AsyncTask<Bundle, Void, Result> {

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			mCheckStatusParent.setVisibility(View.GONE);
		}

		@Override
		protected void onPostExecute(final Result result) {
			mProgress.setVisibility(View.INVISIBLE);
			Log.d(TAG, "Got resp " + result.prettyJsonBody());
			mResponseBody.setText(result.prettyJsonBody());
			mResponseStatus.setText(result.statusCode + " : "
					+ result.message());
			if (ApiSpecHelper.SINGLETON.isRequireCheckStatus(mSpec)) {
				mCheckStatusParent.setVisibility(View.VISIBLE);
				Button button = (Button) findViewById(R.id.response_button);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new CheckRequestStatus().execute(result, null, null);
					}
				});
			}
			super.onPostExecute(result);
		}

		@Override
		protected Result doInBackground(Bundle... params) {
			if (params == null || params.length == 0){
				Log.w(TAG,"Missing req params");
				return new Result(400);
			}
			RequestHelper helper = new RequestHelper(ViewRespActivity.this);
			Log.d(TAG,
					"Executing " + mSpec.id + " using req params "
							+ Param.toString(params[0]));
			return ApiSpecHelper.SINGLETON.executeApi(mSpec, helper, params[0]);
		}

	}

	// Gson helper
	private class ResponseBody {
		public String requestId;
		public String requestTime;
	}
	

}
