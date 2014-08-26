package com.idean.atthack;

import android.app.Service;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.idean.atthack.api.ApiSpecRaw.ReqParam;
import com.idean.atthack.api.Param;

public enum ParamHelper {
	SINGLETON;
	
	public View createRouteParamWidget(Context context, Param param, String routeParam) {
		LayoutInflater inf = (LayoutInflater) context
				.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		View v = inf.inflate(R.layout.request_param, null);
		((TextView) v.findViewById(R.id.param_name))
				.setText(param.name());
		((TextView) v.findViewById(R.id.param_description))
			.setText("This is a parameter set in the URL");

		v.findViewById(R.id.param_required).setVisibility(View.VISIBLE);

		EditText edit = (EditText) v.findViewById(R.id.param_val);
		edit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		edit.setRawInputType(InputType.TYPE_CLASS_TEXT); 
		return v;
	}

	public View createParamWidget(Context context, ReqParam param) {
		LayoutInflater inf = (LayoutInflater) context
				.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		View v = inf.inflate(R.layout.request_param, null);
		((TextView) v.findViewById(R.id.param_name))
				.setText(param.key().name());
		((TextView) v.findViewById(R.id.param_description))
				.setText(param.description + " (" + param.type() + ")");

		v.findViewById(R.id.param_required).setVisibility(
				param.required ? View.VISIBLE : View.GONE);

		EditText edit = (EditText) v.findViewById(R.id.param_val);
		edit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		switch (param.type()) {
		case STRING:
			edit.setRawInputType(InputType.TYPE_CLASS_TEXT);
			break;
		case INTEGER:
			edit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			edit.setFilters(new InputFilter[] { new DigitsKeyListener() });
			break;
		case FLOAT:
			edit.setRawInputType(InputType.TYPE_CLASS_NUMBER
					| InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		case BOOLEAN:
			break;
		default:
			break;
		}
		
		if (!TextUtils.isEmpty(param.defaultVal)) {
			// insert the default val into the EditText
			edit.setText(param.defaultVal);
		}
		
		return v;
	}

	private static final LinearLayout.LayoutParams sLayout = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);

	public void addWidget(View view, ViewGroup parent) {
		parent.addView(view, sLayout);
	}

}
