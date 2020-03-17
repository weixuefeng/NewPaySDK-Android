package com.newmall;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.newmall.entity.BaseTransaction;
import com.newmall.network.HttpService;
import com.squareup.picasso.Picasso;

import org.newtonproject.newpay.android.sdk.NewPaySDK;
import org.newtonproject.newpay.android.sdk.bean.ConfirmedPayment;
import org.newtonproject.newpay.android.sdk.bean.ConfirmedProof;
import org.newtonproject.newpay.android.sdk.bean.ConfirmedSign;
import org.newtonproject.newpay.android.sdk.bean.HepProfile;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SignActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editMessage, editAmount, editAddressFrom, editAddressTo, editCount, editGasLimit, editGasPrice, editData;
    private Button btnMessage, btnTransaction;
    private TextView tvSignResult;
    private LinearLayout lLayoutSignMessage,lLayoutSignTransaction;

    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    private static final String SIGNED_SIGN_MESSAGE = "SIGNED_SIGN_MESSAGE";
    private static final String SIGNED_SIGN_TRANSACTION = "SIGNED_SIGN_TRANSACTION";

    Gson gson = new Gson();

    public static final int SIGN_MESSAGE = 1;
    public static final int SIGN_TRANSACTION = 2;
    public static final String SIGN_TYPE = "signType";

    private String TAG = "SignActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        initView();
        NewPaySDK.init(getApplication());
    }

    public void initView() {
        lLayoutSignMessage = findViewById(R.id.LLayout_sign_message);
        lLayoutSignTransaction = findViewById(R.id.LLayout_sign_transaction);
        editMessage = findViewById(R.id.edit_message);
        editAmount = findViewById(R.id.edit_amount);
        editAddressFrom = findViewById(R.id.edit_address_from);
        editAddressTo = findViewById(R.id.edit_address_to);
        editCount = findViewById(R.id.edit_count);
        editGasLimit = findViewById(R.id.edit_gas_limit);
        editGasPrice = findViewById(R.id.edit_gas_price);
        editData = findViewById(R.id.edit_data);
        btnMessage = findViewById(R.id.btn_message);
        btnTransaction = findViewById(R.id.btn_transaction);
        tvSignResult = findViewById(R.id.tv_sign_result);
        btnMessage.setOnClickListener(this);
        btnTransaction.setOnClickListener(this);

        int intent = getIntent().getIntExtra(SIGN_TYPE,-1);
        String newAddress = getIntent().getStringExtra("newAddress");
        switch (intent){
            case SIGN_MESSAGE:
                lLayoutSignMessage.setVisibility(View.VISIBLE);
                lLayoutSignTransaction.setVisibility(View.GONE);
                break;
            case SIGN_TRANSACTION:
                lLayoutSignMessage.setVisibility(View.GONE);
                lLayoutSignTransaction.setVisibility(View.VISIBLE);
                break;
            default:
        }
        if(newAddress!=null){
            editAddressFrom.setText(newAddress);
            editAddressTo.setText(newAddress);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_message:
                requestSignMessage();
                break;
            case R.id.btn_transaction:
                requestSignTransaction();
                break;
            default:
        }
    }

    private void requestSignMessage() {
        Disposable message = HttpService.getInstance().getSignMessage(editMessage.getText().toString().trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        next-> {
                            Log.i("requestSignMessage", next.toString());
                            NewPaySDK.requestSignMessage(this, next.result);
                        },
                        error-> {
                            Log.e("requestSignMessage", error.toString());
                        }
                );
    }

    private void requestSignTransaction() {
        BaseTransaction transaction = new BaseTransaction(
                editAmount.getText().toString().trim(),
                editAddressFrom.getText().toString().trim(),
                editAddressTo.getText().toString().trim(),
                editCount.getText().toString().trim(),
                editGasPrice.getText().toString().trim(),
                editGasLimit.getText().toString().trim(),
                editData.getText().toString().trim());
        Disposable message = HttpService.getInstance().getSignTransaction(transaction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        next-> {
                            Log.i("requestSignTransaction", next.toString());
                            NewPaySDK.requestSignTransaction(this, next.result);
                        },
                        error-> {
                            Log.e("requestSignTransaction", error.toString());
                        }
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        if(resultCode == RESULT_OK) {
            int errorCode = data.getIntExtra(ERROR_CODE, 0);
            String errorMessage = data.getStringExtra(ERROR_MESSAGE);
            if(errorCode != 1) {
                Log.e(TAG, "error_code is: " + errorCode);
                Log.e(TAG, "ErrorMessage is:" + errorMessage);
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            // on request sign message
            if(requestCode == NewPaySDK.REQUEST_CODE_SIGN_MESSAGE) {
                String res = data.getStringExtra(SIGNED_SIGN_MESSAGE);
                if(!TextUtils.isEmpty(res)){
                    ConfirmedSign confirmedSign = gson.fromJson(res, ConfirmedSign.class);
                    tvSignResult.setText( confirmedSign.getSignature());
                    Toast.makeText(this, "信息签名成功 is:" + confirmedSign.getSignature(), Toast.LENGTH_SHORT).show();
                }
            }
            // on request sign transaction
            if(requestCode == NewPaySDK.REQUEST_CODE_SIGN_TRANSACTION) {
                String res = data.getStringExtra(SIGNED_SIGN_TRANSACTION);
                if(!TextUtils.isEmpty(res)){
                    ConfirmedSign confirmedSign = gson.fromJson(res, ConfirmedSign.class);
                    tvSignResult.setText( confirmedSign.getSignature());
                    Toast.makeText(this, "交易签名成功 is:" + confirmedSign.getSignature(), Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
