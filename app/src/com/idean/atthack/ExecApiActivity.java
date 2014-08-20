package com.idean.atthack;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.idean.atthack.api.ApiSpec;
import com.idean.atthack.api.ApiSpecHelper;
import com.idean.atthack.api.ApiSpecRaw.ReqParam;
import com.idean.atthack.api.Param;
import com.idean.atthack.network.RequestHelper;
import com.idean.atthack.network.Result;

public class ExecApiActivity extends ActionBarActivity {

	private static final String TAG = "ExecApi";
	public static final String EXTRA_SPECID = "EXTRA_SPECID";
	private ProgressBar mProgress;
	private ApiSpec mSpec;
	private Map<Param, EditText> mParam2Field;
	private boolean mIsSending;
	private TextView mResponseBody;
	private TextView mResponseStatus;
	private Button mResponseButton;
	private RequestHelper mRequestHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mParam2Field = new HashMap<Param, EditText>();

		setContentView(R.layout.apidetail);
		ApiSpec spec = getSpec(getIntent());
		if (spec == null) {
			Log.w(TAG, "Unable to determine api specfrom intent");
			Toast.makeText(this, R.string.no_api, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// bind name and description
		getSupportActionBar().setTitle(spec.name);
		setText(R.id.description, "[" + spec.docNumber + "] "
				+ spec.description);

		// bind request params
		bindRequestParams(spec, mParam2Field);

		mProgress = (ProgressBar) findViewById(R.id.progress);
		mSpec = spec;
		mResponseBody = (TextView) findViewById(R.id.response_body);
		mResponseStatus = (TextView) findViewById(R.id.response_status);
		findViewById(R.id.description).requestFocus();

		mResponseButton = (Button) findViewById(R.id.response_button);
		mResponseButton.setVisibility(View.INVISIBLE);
		mRequestHelper = new RequestHelper(this);
	}

	private void bindRequestParams(ApiSpec spec,
			Map<Param, EditText> param2Field) {
		ReqParam[] params = spec.requestParams();
		if (params == null) {
			findViewById(R.id.request_empty).setVisibility(View.VISIBLE);
			return;
		}
		findViewById(R.id.request_empty).setVisibility(View.GONE);
		ViewGroup parent = (ViewGroup) findViewById(R.id.requestParent);
		for (ReqParam param : params) {
			View view = ParamHelper.SINGLETON.createParamWidget(this, param);
			ParamHelper.SINGLETON.addWidget(view, parent);

			// Map param key to the editText that contains the value of the
			// parameter we're sending
			EditText edit = (EditText) view.findViewById(R.id.param_val);
			Param paramKey = param.key();
			if (edit != null && paramKey != null) {
				param2Field.put(paramKey, edit);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.exec_api, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_send) {
			new ExecApiTask().execute(null, null, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.action_send);
		if (item != null) {
			item.setEnabled(!mIsSending);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	private void setText(int viewId, String text) {
		((TextView) findViewById(viewId)).setText(text);
	}

	private ApiSpec getSpec(Intent intent) {
		String specName = intent.getStringExtra(EXTRA_SPECID);
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
			// Force #onPrepareOptions to be called
			supportInvalidateOptionsMenu();
			mResponseButton.setVisibility(View.INVISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Result result) {
			mProgress.setVisibility(View.INVISIBLE);
			mIsSending = false;
			supportInvalidateOptionsMenu();
			Log.d(TAG, "Got resp " + result.prettyJsonBody());
			mResponseBody.setText(result.prettyJsonBody());
			mResponseStatus.setText(result.statusCode + " : "
					+ result.message());
		}

	}

	private class ExecApiTask extends AsyncTask<Void, Void, Result> {

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			mIsSending = true;
			// Force #onPrepareOptions to be called
			supportInvalidateOptionsMenu();
			mResponseButton.setVisibility(View.INVISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(final Result result) {
			mProgress.setVisibility(View.INVISIBLE);
			mIsSending = false;
			supportInvalidateOptionsMenu();
			Log.d(TAG, "Got resp " + result.prettyJsonBody());
			mResponseBody.setText(result.prettyJsonBody());
			mResponseStatus.setText(result.statusCode + " : "
					+ result.message());
			if (ApiSpecHelper.SINGLETON.isRequireCheckStatus(mSpec)) {
				mResponseButton.setVisibility(View.VISIBLE);
				mResponseButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG,"OnClick of button");
						new CheckRequestStatus().execute(result, null, null);
					}
				});
			}
			super.onPostExecute(result);
		}

		@Override
		protected Result doInBackground(Void... params) {

			RequestHelper helper = new RequestHelper(ExecApiActivity.this);
			// Extract info from EditText UI fields
			Bundle bundle = new Bundle();
			Set<Entry<Param, EditText>> entries = mParam2Field.entrySet();
			for (Entry<Param, EditText> entry : entries) {
				if (entry.getKey() != null && entry.getValue() != null) {
					EditText edit = entry.getValue();
					String val = edit.getText().toString();
					Param param = entry.getKey();
					param.putBundleAsTypedVal(bundle, val);
				}
			}
			Log.d(TAG,
					"Executing " + mSpec.id + " using req params "
							+ Param.toString(bundle));
			return ApiSpecHelper.SINGLETON.executeApi(mSpec, helper, bundle);
		}

	}

	// Gson helper
	private class ResponseBody {
		public String requestId;
		public String requestTime;
	}

}
