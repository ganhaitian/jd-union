package com.tiantian.jd;

import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdException;
import jd.union.open.goods.jingfen.query.request.JFGoodsReq;
import jd.union.open.goods.jingfen.query.request.UnionOpenGoodsJingfenQueryRequest;
import jd.union.open.goods.jingfen.query.response.UnionOpenGoodsJingfenQueryResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author haitian.gan
 */
public class JdUnionRobot {

    private final static String SERVER_URL = "https://router.jd.com/api";
    private final static String APP_KEY    = "0cb0d224e96e918189ab3c9bcd147d39";
    private final static String APP_SECRET = "447eba325001490a9778f1055d6adb2e";

    public void getSelectionGoods() {
        var client  = new DefaultJdClient(SERVER_URL, null, APP_KEY, APP_SECRET);
        var request = new UnionOpenGoodsJingfenQueryRequest();

        var goodsReq = new JFGoodsReq();
        goodsReq.setPageSize(100);
        goodsReq.setPageIndex(0);
        request.setGoodsReq(goodsReq);

        try {
            UnionOpenGoodsJingfenQueryResponse response = client.execute(request);
            System.out.println(response);
        } catch (JdException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //new JdUnionRobot().getSelectionGoods();
        try {
            System.out.println(new JdUnionRobot().buildSign(
                    "2019-10-28 11:15:18",
                    "1.0", "md5", "json",
                    "jd.union.open.goods.promotiongoodsinfo.query",
                    "{\"goodsReq\":{}}",
                    null, APP_KEY, APP_SECRET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildSign(String timestamp,
                             String version,
                             String signMethod,
                             String format,
                             String method,
                             String paramJson,
                             String accessToken,
                             String appKey,
                             String appSecret) throws Exception {

        //第一步，按照顺序填充参数
        Map<String, String> map = new TreeMap<>();
        map.put("timestamp", timestamp);
        map.put("v", version);
        map.put("sign_method", signMethod);
        map.put("format", format);
        map.put("method", method);

        //param_json为空的时候需要写成 "{}"
        map.put("param_json", paramJson);
        map.put("access_token", accessToken);
        map.put("app_key", appKey);
        StringBuilder sb = new StringBuilder(appSecret);

        //按照规则拼成字符串
        for (Map.Entry entry : map.entrySet()) {
            String name  = (String) entry.getKey();
            String value = (String) entry.getValue();
            //检测参数是否为空
            if (areNotEmpty(new String[]{name, value})) {
                sb.append(name).append(value);
            }

        }

        sb.append(appSecret);
        //MD5
        return md5(sb.toString());
    }

    private static String md5(String source) throws Exception {
        var    md    = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(source.getBytes(StandardCharsets.UTF_8));
        return byte2hex(bytes);
    }

    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }

        return sign.toString();
    }

    private static boolean areNotEmpty(String[] values) {
        boolean result = true;
        if ((values == null) || (values.length == 0))
            result = false;

        else {
            for (String value : values) {
                result &= !isEmpty(value);
            }

        }

        return result;
    }

    private static boolean isEmpty(String value) {
        int strLen;
        if ((value == null) || ((strLen = value.length()) == 0))
            return true;

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }

}
