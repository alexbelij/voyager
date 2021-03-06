package com.app.voyager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.app.voyager.AsyncTask.CallAPI;
import com.app.voyager.Util.Util;
import com.app.voyager.dialogs.ProgressDialogView;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotpasswordActivity extends ParentActivity implements View.OnClickListener {
    TextView headername;
    ImageView ic_back;
    EditText edt_emailid;
    RadioGroup radio;
    private RadioButton radioButton;
    private int mail,phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        progressdialog = new ProgressDialogView(ForgotpasswordActivity.this, "");
        BindView(null, savedInstanceState);
    }

    @Override
    public void BindView(View view, Bundle savedInstanceState) {
        headername = (TextView) findViewById(R.id.headername);
        ic_back = (ImageView) findViewById(R.id.ic_back);
        edt_emailid = (EditText) findViewById(R.id.edt_emailid);
        radio= (RadioGroup) findViewById(R.id.radio);
        phone=1;
        mail=0;
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedId = radio.getCheckedRadioButtonId();
                Log.i("aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaa" + checkedId);
                radioButton = (RadioButton) findViewById(selectedId);
                switch (checkedId) {
                    case R.id.mail:
                        mail=1;
                        phone=0;
                        break;

                    case R.id.phone:
                        mail=0;
                        phone=1;
                        break;
                }
            }
        });

        headername.setText("Forgot Password");
        SetOnclicklistener();
        super.BindView(view, savedInstanceState);
    }

    @Override
    public void SetOnclicklistener() {
        ic_back.setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_emailid.getText().toString().trim().length() == 0) {
                    Util.ShowToast(ForgotpasswordActivity.this, "Enter email id");
                } else if (!Util.isEmailValid(edt_emailid.getText().toString().trim())) {
                    Util.ShowToast(ForgotpasswordActivity.this, "Enter valid email id");
                } else {
                    try {
                        JSONObject jsonObject_main = new JSONObject();
                        JSONObject jsonObject = new JSONObject();

                        jsonObject_main = getCommontHeaderParams();
                        jsonObject.put("email", edt_emailid.getText().toString().trim());
                        jsonObject.put("sendEmail", mail);
                        jsonObject.put("sendMobile", phone);

                        jsonObject_main.put("body", jsonObject);
                        CallAPI(jsonObject_main);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                finish();
                break;
        }
    }

    public void CallAPI(JSONObject params) {
        if (Util.isNetworkConnected(ForgotpasswordActivity.this)) {
            try {
                if (progressdialog.isShowing())
                    progressdialog.dismiss();
                progressdialog.show();
                new CallAPI(FORGOTPASSWORD, "FORGOTPASSWORD", params, ForgotpasswordActivity.this, GetDetails_Handler, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            progressdialog.dismissanimation(ProgressDialogView.ERROR);
            Util.ShowToast(ForgotpasswordActivity.this, getString(R.string.nointernetmessage));
        }
    }

    Handler GetDetails_Handler = new Handler() {
        public void handleMessage(Message msg) {

            PrintMessage("Handler " + msg.getData().toString());
            if (msg.getData().getBoolean("flag")) {
                if (msg.getData().getInt("code") == SUCCESS) {
                    progressdialog.dismissanimation(ProgressDialogView.ERROR);
                    Intent intent = new Intent(ForgotpasswordActivity.this, ForgotPasswordPin.class);

                    intent.putExtra("email", edt_emailid.getText().toString().trim());

                    //   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else if (msg.getData().getInt("code") == FROMGENERATETOKEN) {
                    ParseSessionDetails(msg.getData().getString("responce"));
                    try {
                        CallAPI(new JSONObject(msg.getData()
                                .getString("mExtraParam")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (msg.getData().getInt("code") == SESSIONEXPIRE) {
                    if (Util.isNetworkConnected(ForgotpasswordActivity.this)) {
                        CallSessionID(GetDetails_Handler, msg.getData()
                                .getString("mExtraParam"));
                    } else {
                        progressdialog.dismissanimation(ProgressDialogView.ERROR);
                        Util.ShowToast(ForgotpasswordActivity.this, getString(R.string.nointernetmessage));
                    }
                } else {
                    progressdialog.dismissanimation(ProgressDialogView.ERROR);
                    Util.ShowToast(ForgotpasswordActivity.this, msg.getData().getString("msg"));
                }
            } else {
                progressdialog.dismissanimation(ProgressDialogView.ERROR);
                Util.ShowToast(ForgotpasswordActivity.this, msg.getData().getString("msg"));
            }
        }
    };
}
