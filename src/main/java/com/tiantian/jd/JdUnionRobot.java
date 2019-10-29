package com.tiantian.jd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdException;
import jd.union.open.goods.jingfen.query.request.JFGoodsReq;
import jd.union.open.goods.jingfen.query.request.UnionOpenGoodsJingfenQueryRequest;
import jd.union.open.goods.jingfen.query.response.UnionOpenGoodsJingfenQueryResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author haitian.gan
 */
public class JdUnionRobot {

    private final static String SERVER_URL = "https://router.jd.com/api";
    private final static String APP_KEY    = "0cb0d224e96e918189ab3c9bcd147d39";
    private final static String APP_SECRET = "447eba325001490a9778f1055d6adb2e";

    // Time out value of getting promotion goods
    private final static int PROMOTION_GOODS_TIMEOUT = 5000;

    // http client
    private HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(PROMOTION_GOODS_TIMEOUT))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

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

    public void getPromotionGoods(int pageNo){
        // 从配置文件中读取cookie
        String cookie = Optional.of(this.getClass())
               .map(Class::getClassLoader)
                .map(clsLoader -> clsLoader.getResourceAsStream("cookie"))
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .map(reader -> {
                    try {
                        return reader.readLine();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .orElse(null);

        if(cookie == null){
            return;
        }

        // 拼传入的参数
        JSONObject mainJson = new JSONObject();
        mainJson.put("pageNo", pageNo);
        mainJson.put("pageSize", 60);
        mainJson.put("searchUUID", "4a5fe0385949423ebcb9e9a7989b69aa");

        JSONObject dataJson = new JSONObject();
        dataJson.put("deliveryType", 0);
        dataJson.put("hasCoupon", 0);
        dataJson.put("isPinGou", 0);
        dataJson.put("isZY", 0);
        dataJson.put("isCare", 0);
        dataJson.put("lock", 0);
        dataJson.put("orientationFlag", 0);
        dataJson.put("searchType", "st3");
        dataJson.put("keywordType", "kt0");
        mainJson.put("data", dataJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://union.jd.com/api/goods/search"))
                .header("Content-Type", "application/json")
                .header("Cookie", cookie)
                .POST(HttpRequest.BodyPublishers.ofString(mainJson.toString()))
                .timeout(Duration.ofMillis(5009))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200){
                return;
            }

            JSON.parseObject(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //new JdUnionRobot().getSelectionGoods();
        /*try {
            System.out.println(new JdUnionRobot().buildSign(
                    "2019-10-28 11:15:18",
                    "1.0", "md5", "json",
                    "jd.union.open.goods.promotiongoodsinfo.query",
                    "{\"goodsReq\":{}}",
                    null, APP_KEY, APP_SECRET));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        new JdUnionRobot().getPromotionGoods(1);
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
