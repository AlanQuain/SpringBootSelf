package com.github.wxpay.sdk;

import com.alibaba.fastjson.JSON;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 拉起微信付款码(后端传前端参数)
 *
 * @author legend
 * @version 1.0
 * @description
 * @date 2021/7/30
 */
public class WXT {


    public static void main(String[] args) {
        WXPayConstants.SignType signType = WXPayConstants.SignType.MD5;
        String signTypeName = WXPayConstants.SignType.MD5.name();
        Map<String, String> map = new LinkedHashMap<>();
        Long timeStamp = WXPayUtil.getCurrentTimestamp();
        String nonceStr = WXPayUtil.generateNonceStr();

        map.put("appId", "XXXX");
        map.put("timeStamp", timeStamp.toString());
        map.put("nonceStr", nonceStr);
        map.put("package", "mch_id=XXXXXX");
        map.put("signType", signTypeName);

        String sign = null;
        try {
            sign = WXPayUtil.generateSignature(map, "XXX", signType);
            map.put("sign", sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("sign：" + sign);

        try {
            boolean flag = WXPayUtil.isSignatureValid(map, "XXX", signType);
            System.out.println(flag);

            map.put("paySign", sign);
            System.out.println(JSON.toJSONString(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
