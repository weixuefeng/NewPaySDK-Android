package com.newmall;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.newmall.entity.BaseTransaction;
import com.newmall.network.BaseResponse;
import com.newmall.network.HttpService;
import com.squareup.picasso.Picasso;

import org.newtonproject.newpay.android.sdk.NewPaySDK;
import org.newtonproject.newpay.android.sdk.bean.ConfirmedPayment;
import org.newtonproject.newpay.android.sdk.bean.ConfirmedProof;
import org.newtonproject.newpay.android.sdk.bean.ConfirmedSign;
import org.newtonproject.newpay.android.sdk.bean.HepProfile;
import org.newtonproject.newpay.android.sdk.bean.NewAuthLogin;
import org.newtonproject.newpay.android.sdk.bean.NewAuthPay;
import org.newtonproject.newpay.android.sdk.bean.NewAuthProof;
import org.newtonproject.newpay.android.sdk.constant.Constant;
import org.newtonproject.newpay.android.sdk.constant.Environment;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout profileLinearLayout;
    private TextView nameTextView;
    private TextView cellphoneTextView;
    private TextView newidTextView;
    private ImageView imageView;
    private String TAG = "Activity";

    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";


    TextView dev;
    TextView beta;
    TextView testnet;
    TextView mainnet;
    TextView evn;

    //Profile key
    private static final String SIGNED_PROFILE = "SIGNED_PROFILE";
    private static final String SIGNED_PROOF = "SIGNED_PROOF";
    private static final String SIGNED_PAY = "SIGNED_PAY";
    private static final String SIGNED_SIGN_MESSAGE = "SIGNED_SIGN_MESSAGE";
    private static final String SIGNED_SIGN_TRANSACTION = "SIGNED_SIGN_TRANSACTION";

    private static final String dappId = "5b796b9b48f74f28b96bcd3ea42f9aaf";
    private HepProfile profileInfo;

    private static final int REQUEST_CODE_NEWPAY = 1000;
    Gson gson = new Gson();
    private Button request20Bt;
    private Button single;
    private Button multiple;
    private MainActivity context;
    private Button requestprofile;
    private Button directSend;
    private Button requestSignTransaction;
    private Button requestSignMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        NewPaySDK.init(getApplication());
        context = this;
    }

    private void initView() {
        profileLinearLayout = findViewById(R.id.profileLayout);
        nameTextView = findViewById(R.id.nameTextView);
        cellphoneTextView = findViewById(R.id.cellphoneTextView);
        newidTextView = findViewById(R.id.newidTextView);
        imageView = findViewById(R.id.avatarImageView);
        request20Bt = findViewById(R.id.request20Bt);
        requestprofile = findViewById(R.id.requestprofile);
        directSend = findViewById(R.id.directSend);
        requestSignMessage = findViewById(R.id.requestSignMessage);
        requestSignTransaction = findViewById(R.id.requestSignTransaction);

        dev = findViewById(R.id.dev);
        beta = findViewById(R.id.beta);
        testnet = findViewById(R.id.testnet);
        mainnet = findViewById(R.id.mainnet);
        evn = findViewById(R.id.env);

        profileLinearLayout.setOnClickListener(this);
        request20Bt.setOnClickListener(this);
        requestprofile.setOnClickListener(this);
        directSend.setOnClickListener(this);
        requestSignTransaction.setOnClickListener(this);
        requestSignMessage.setOnClickListener(this);

        dev.setOnClickListener(this);
        beta.setOnClickListener(this);
        mainnet.setOnClickListener(this);
        testnet.setOnClickListener(this);
        testnet.callOnClick();
        single = findViewById(R.id.pushSingle);
        multiple = findViewById(R.id.pushMultiple);
        single.setOnClickListener(this);
        multiple.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.requestprofile:
            case R.id.profileLayout:
                HttpService
                        .getInstance()
                        .getNewAuthLogin()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<BaseResponse<NewAuthLogin>>() {
                                    @Override
                                    public void accept(BaseResponse<NewAuthLogin> response) throws Exception {
                                        Log.i("request profile:", response.toString());
                                        if(response.errorCode == 1) {
                                            NewPaySDK.requestProfile(context, response.result);
                                        }
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                break;
            case R.id.request20Bt:
                if(profileInfo == null || TextUtils.isEmpty(profileInfo.newid)) {
                    Toast.makeText(this, "Please get profile first", Toast.LENGTH_SHORT).show();
                    return;
                }
                HttpService
                        .getInstance()
                        .getNewAuthPay(profileInfo.newid)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<BaseResponse<NewAuthPay>>() {
                                    @Override
                                    public void accept(BaseResponse<NewAuthPay> response) throws Exception {
                                        if(response.errorCode == 1) {
                                            NewPaySDK.pay(context, response.result);
                                        }
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                break;
            case R.id.pushMultiple:
                //pushSingle();
                break;
            case R.id.pushSingle:
                pushSingle();
                break;
            case R.id.dev:
                evn.setText("Dev");
                NewPaySDK.init(getApplication(), Environment.DEVNET);
                break;
            case R.id.beta:
                evn.setText("Beta");
                NewPaySDK.init(getApplication(), Environment.BETANET);

                break;
            case R.id.testnet:
                evn.setText("testnet");
                NewPaySDK.init(getApplication(), Environment.TESTNET);

                break;
            case R.id.mainnet:
                evn.setText("main");
                NewPaySDK.init(getApplication(), Environment.MAINNET);
                break;
            case R.id.directSend:
                directSendToNewPay();
                break;
            case R.id.requestSignMessage:
                Intent intent = new Intent(this,SignActivity.class);
                intent.putExtra(SignActivity.SIGN_TYPE,SignActivity.SIGN_MESSAGE);
                startActivity(intent);
                break;
            case R.id.requestSignTransaction:
                Intent intent1 = new Intent(this,SignActivity.class);
                intent1.putExtra(SignActivity.SIGN_TYPE,SignActivity.SIGN_TRANSACTION);
                startActivity(intent1);
                break;
            default:
                break;

        }
    }

    private void directSendToNewPay() {
        String authPayDev = "newpay://org.newtonproject.newpay.android.dev.pay";
        String authPayTest = "newpay://org.newtonproject.newpay.android.pay";
        String authPayRelease = "newpay://org.newtonproject.newpay.android.release.pay";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authPayDev));
        // address have to match the network.
        intent.putExtra("ADDRESS", "NEW17xNFGLDpUgTY9QkvRrMXWb8ZeZCeiEFAhk5");
        intent.putExtra("AMOUNT", "20");
        intent.putExtra("EXTRA_MSG", "orderNumber"); // 备注信息
        intent.putExtra("REQUEST_PAY_SOURCE", getPackageName());
        boolean isSafe = checkNewPay(intent);
        if(isSafe) {
            startActivityForResult(intent, 1008);
        } else {
            Log.e("Error:", "No instance");
        }
    }

    private boolean checkNewPay(Intent intent) {
        PackageManager packageManager = getApplication().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }

    private void pushSingle() {
        if(profileInfo == null || TextUtils.isEmpty(profileInfo.newid)) {
            Toast.makeText(context, "Please get profile newid first", Toast.LENGTH_SHORT).show();
            return;
        }
        Disposable subscribe = HttpService.getInstance().getNewAuthProof(profileInfo.newid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseResponse<NewAuthProof>>() {
                               @Override
                               public void accept(BaseResponse<NewAuthProof> newAuthProofBaseResponse) throws Exception {
                                    if(newAuthProofBaseResponse.errorCode == 1) {
                                        NewAuthProof proof = newAuthProofBaseResponse.result;
                                        NewPaySDK.placeOrder(context , proof);
                                    } else {
                                        Toast.makeText(context, newAuthProofBaseResponse.errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
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
            if(requestCode == NewPaySDK.REQUEST_CODE_NEWPAY) {

                String profile = data.getStringExtra(SIGNED_PROFILE);

                if(!TextUtils.isEmpty(profile)){
                    profileInfo = gson.fromJson(profile, HepProfile.class);
                    cellphoneTextView.setText(profileInfo.cellphone);
                    nameTextView.setText(profileInfo.name);
                    newidTextView.setText(profileInfo.newid);
                    Log.e(TAG, "Profile:" + profileInfo);
                    if(!TextUtils.isEmpty(profileInfo.avatarPath)) {
                        Picasso.get().load(profileInfo.avatarPath).into(imageView);
                    }
                }
            }

            // hep pay result
            if(requestCode == NewPaySDK.REQUEST_CODE_NEWPAY_PAY){
                String res = data.getStringExtra(SIGNED_PAY);
                ConfirmedPayment payment = gson.fromJson(res, ConfirmedPayment.class);
                Toast.makeText(this, "提交订单成功 txid is:" + payment.txid, Toast.LENGTH_SHORT).show();
            }
            if(requestCode == 1008) {
                String res = data.getStringExtra("txid");
                Toast.makeText(this, "提交订单成功 txid is:" + res, Toast.LENGTH_SHORT).show();

            }

            if(requestCode == NewPaySDK.REQUEST_CODE_PUSH_ORDER) {
                String res = data.getStringExtra(SIGNED_PROOF);
                ConfirmedProof proof = gson.fromJson(res, ConfirmedProof.class);
                Toast.makeText(this, "上链成功: proof hash is" + proof.proofHash, Toast.LENGTH_SHORT).show();
            }
            // on request sign message
            if(requestCode == NewPaySDK.REQUEST_CODE_SIGN_MESSAGE) {
                String res = data.getStringExtra(SIGNED_SIGN_MESSAGE);
                ConfirmedSign confirmedSign = gson.fromJson(res, ConfirmedSign.class);
                Toast.makeText(this, "信息签名成功 is:" + confirmedSign.getSignature(), Toast.LENGTH_SHORT).show();
            }
            // on request sign transaction
            if(requestCode == NewPaySDK.REQUEST_CODE_SIGN_TRANSACTION) {
                String res = data.getStringExtra(SIGNED_SIGN_TRANSACTION);
                ConfirmedSign confirmedSign = gson.fromJson(res, ConfirmedSign.class);
                Toast.makeText(this, "交易签名成功 is:" + confirmedSign.getSignature(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public class ErrorCode {
        static final int SUCCESS = 1; // 成功

        static final int CANCEL = 2;   // 取消，用户主动操作

        static final int NO_NEWPAY = 100;  // 没有安装 NewPay
        static final int NO_PROFILE = 101; // 没有用户信息，即没有 NewID
        static final int NO_BUNDLE_SOURCE = 102; // 注册的包名不匹配， ios 为 bundle Id
        static final int SIGNATURE_ERROR = 103;  // 签名认证失败，一般为 secp256r1 签名认证
        static final int SELLER_NEWID_ERROR = 104; // 商家的 NEWID 验证失败
        static final int PROTOCOL_VERSION_LOW = 105;  // 协议版本过低，建议升级 sdk 
        static final int NO_ACTION = 106;   // 没有传递Action， 意外情况错误
        static final int APPID_ERROR = 107;  // dapp Id 校验失败
        static final int NO_ORDER_INFO = 108;  // 没有订单信息
        static final int NEWID_ERROR = 109;  // newid 验证失败
        static final int NO_WALLET = 110;    // 没有 newpay 钱包
        static final int UNKNOWN_ERROR = 1000;   // 一般为网络错误，服务端会返回验证错误信息
    }
}
