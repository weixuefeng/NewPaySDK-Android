package org.newtonproject.newpay.android.sdk.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author weixuefeng@lubangame.com
 * @version $
 */
public class ConfirmedSign {

    @SerializedName("signature")
    public String signature;

    public String getSignature() {
        return signature;
    }
}
