import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    private static OkHttpClient sOkHttpClient = new OkHttpClient();
    private static String sSessionId;
    private static Gson sGson = new Gson();

    private static Map<String, Object> mapFromJsonString(String data) throws IOException {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map = (Map<String, Object>)sGson.fromJson(data, map.getClass());
//        return map;
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        @SuppressWarnings("unchecked")
        Map <String, Object> map = mapper.readValue(data, Map.class);
        return map;
    }

    private static String JsonStringFromMap(Map map){
        return sGson.toJson(map);
    }

    public static Map<String, Object> getLoginNone() throws IOException {
        String url = "https://passport.ximalaya.com/" + "mobile/nonce/" + System.currentTimeMillis();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        System.out.println(request.url());

        Response response = sOkHttpClient.newCall(request).execute();
        String data = response.body().string();

        System.out.println("onResponse: " + data);
        return mapFromJsonString(data);
    }

    public static Map<String, Object> getH5CaptchaUrl() throws IOException {
        sSessionId = UUID.randomUUID().toString() + System.currentTimeMillis();

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://mobile.ximalaya.com/captcha-web/check/slide/get")
                .newBuilder();
        urlBuilder.addQueryParameter("bpId", "139");
        urlBuilder.addQueryParameter("sessionId", sSessionId);
        urlBuilder.addQueryParameter("requestType", "xmClient");
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.build();

        System.out.println(request.url());

        Response response = sOkHttpClient.newCall(request).execute();
        String data = response.body().string();

        System.out.println("onResponse: " + data);
        return mapFromJsonString(data);
    }

    @NotNull
    public static String webviewLoadH5CaptchaUrl(String h5CaptchaUrl) throws IOException {
        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(h5CaptchaUrl)
                .newBuilder();
        urlBuilder.addQueryParameter("bpId", "139");
        urlBuilder.addQueryParameter("sessionId", sSessionId);
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.build();

        System.out.println(request.url());

        Response response = sOkHttpClient.newCall(request).execute();
        String data = response.body().string();

        System.out.println("onResponse: " + data);
        return data;
    }

    @NotNull
    public static String webviewCheckSlider1_OPTIONS() throws IOException {
        sSessionId = UUID.randomUUID().toString() + System.currentTimeMillis();

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://mobile.ximalaya.com/captcha-web/check/slide/get")
                .newBuilder();
        urlBuilder.addQueryParameter("bpId", "139");
        urlBuilder.addQueryParameter("sessionId", sSessionId);
        reqBuild.url(urlBuilder.build());
        reqBuild.method("OPTIONS", null);
        Request request = reqBuild.build();

        System.out.println(request.url());

        Response response = sOkHttpClient.newCall(request).execute();
        String data = response.body().string();

        System.out.println("onResponse: " + data);
        return data;
    }

    public static Map<String, Object> webviewCheckSlider2_GET() throws IOException {
        sSessionId = UUID.randomUUID().toString() + System.currentTimeMillis();

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://mobile.ximalaya.com/captcha-web/check/slide/get")
                .newBuilder();
        urlBuilder.addQueryParameter("bpId", "139");
        urlBuilder.addQueryParameter("sessionId", sSessionId);
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.build();

        System.out.println(request.url());

        Response response = sOkHttpClient.newCall(request).execute();
        String data = response.body().string();

        System.out.println("onResponse: " + data);
        return mapFromJsonString(data);
    }

    @NotNull
    public static void downloadFile(String url, String filename) throws IOException {
        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url)
                .newBuilder();
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.build();

        System.out.println(request.url());

        Response response = sOkHttpClient.newCall(request).execute();

        InputStream is = response.body().byteStream();
        File file = new File(filename);
        FileOutputStream fos = new FileOutputStream(file);

        long total = response.body().contentLength();
        byte[] buf = new byte[2048];
        int len = 0;
        long sum = 0;
        while ((len = is.read(buf)) != -1) {
            fos.write(buf, 0, len);
            sum += len;
            int progress = (int) (sum * 1.0f / total * 100);
            System.out.println("Downloading " + filename + ": " + progress + "%");
        }
        System.out.println("onResponse: <file> " + filename);
    }

    public static Map<String, Object> webviewValidSlider() throws Exception {

        File fgPng = new File("fg.png");
        BufferedImage fgBI = ImageIO.read(fgPng);
        BufferedImage fg_binaryImage = ImageUtil.binaryImage(fgBI);

        File bgPng = new File("bg.png");
        BufferedImage bgBI = ImageIO.read(bgPng);
        BufferedImage bg_binaryImage = ImageUtil.binaryImage(bgBI);

//        int fgRectRGBSum = ImageUtil.binaryRectRGBSum(fg_binaryImage, 0, 0, fg_binaryImage.getWidth(), fg_binaryImage.getHeight());
        Map<Point, Integer> fgComparedColorPoints = ImageUtil.getComparedColorPointsFromFgBi(fg_binaryImage);
        int targetX = ImageUtil.binaryRectRGBSumMatch(bg_binaryImage, fgComparedColorPoints);
        System.out.format("ValidSlider TargetX: %d\n", targetX);


        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://mobile.ximalaya.com/captcha-web/valid/slider")
                .newBuilder();
        reqBuild.url(urlBuilder.build());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("bpId", "139");
        map.put("sessionId", sSessionId);
        map.put("type", "slider");
        map.put("captchaText", (targetX+40) + ",-41");
        map.put("startX", "83");
        map.put("startY", "457");
        map.put("startTime", System.currentTimeMillis());
        RequestBody requestBody = RequestBody.create(JsonStringFromMap(map), mediaType);
        reqBuild.post(requestBody);

        Request request = reqBuild.build();

        System.out.println(request.url());
        System.out.println(JsonStringFromMap(map));

        Response response = sOkHttpClient.newCall(request).execute();
        String data = Objects.requireNonNull(response.body()).string();

        System.out.println("onResponse: " + data);
        return mapFromJsonString(data);
    }

    public static Map<String, Object> loginByPwd(String account, String password, String nonce, String sliderToken) throws Exception {
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://passport.ximalaya.com/mobile/login/pwd/v3"))
                .newBuilder();
        reqBuild.url(urlBuilder.build());

        Map<String, String> map = new HashMap<String, String>();
        map.put("account", MuxAqhmrDS(account));
        map.put("password", MuxAqhmrDS(password));
        map.put("fdsOtp", sliderToken);
        map.put("nonce", nonce);

        map.put("signature", createLoginParamSign(map));

        RequestBody requestBody = RequestBody.create(JsonStringFromMap(map), mediaType);
        reqBuild.post(requestBody);

        Request request = reqBuild.build();

        System.out.println(request.url());
        System.out.println(JsonStringFromMap(map));

        Response response = sOkHttpClient.newCall(request).execute();
        String data = response.body().string();

        System.out.println("onResponse: " + data);
        return mapFromJsonString(data);
    }

    private static String MuxAqhmrDS(String str) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        String a = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVhaR3Or7suUlwHUl2Ly36uVmboZ3+HhovogDjLgRE9CbaUokS2eqGaVFfbxAUxFThNDuXq/fBD+SdUgppmcZrIw4HMMP4AtE2qJJQH/KxPWmbXH7Lv+9CisNtPYOlvWJ/GHRqf9x3TBKjjeJ2CjuVxlPBDX63+Ecil2JR9klVawIDAQAB";
        byte[] decodedBase64 = Base64.getDecoder().decode(a);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedBase64);
        PublicKey publicKey = rsa.generatePublic(spec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(1, publicKey);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        byte[] resultBytes = cipher.doFinal(strBytes, 0, strBytes.length);
        String result = Base64.getEncoder().encodeToString(resultBytes);
        String[] resultStrarr = result.split("(?<=\\G.{76})");
        result = String.join("\n", resultStrarr) + "\n";
        return result;
    }

    private static String createLoginParamSign(Map<String, String> map) throws NoSuchAlgorithmException {
        TreeMap<String, String> treeMap = new TreeMap<String, String>(map);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            sb.append((String) entry.getKey());
            sb.append("=");
            sb.append((String) entry.getValue());
            sb.append("&");
        }
        return GopXBjsbEg(sb.toString());
    }

    private static String GopXBjsbEg(String str) throws NoSuchAlgorithmException {
        str += "MOBILE-V1-PRODUCT-7D74899B338B4F348E2383970CC09991E8E8D8F2BC744EF0BEE94D76D718C089";
        str = str.toUpperCase();
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(strBytes);
        byte[] sha1ResultBytes = sha1.digest();
        StringBuilder sb = new StringBuilder();

        for (byte b: sha1ResultBytes) {
            String s = Integer.toHexString(b & 0xff);
            if (s.length() == 1) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        String account = "13378899202";
        String password = "aaabbbccc";

        Map<String, Object> map = getLoginNone();
        if (map.get("ret").toString().equals("0")) {
            String nonce = (String) map.get("nonce");
            System.out.println("nonce: " + nonce);

            Map<String, Object> json_getH5CaptchaUrl = getH5CaptchaUrl();
            if (Boolean.parseBoolean((String) json_getH5CaptchaUrl.get("needCaptcha"))) {
                String h5CaptchaUrl = (String) json_getH5CaptchaUrl.get("h5CaptchaUrl");
                if (h5CaptchaUrl == null || h5CaptchaUrl.isEmpty()) {
                    h5CaptchaUrl = "https://static2.test.ximalaya.com/sr012018/captcha-html/1.0.0/dist/index.html";
                }
                webviewLoadH5CaptchaUrl(h5CaptchaUrl);
                webviewCheckSlider1_OPTIONS();
                Map<String, Object> json_webviewCheckSlider2_GET = webviewCheckSlider2_GET();
                Map<String, Object> json_webviewCheckSlider2_GET_data = (Map<String, Object>) json_webviewCheckSlider2_GET.get("data");
                String fgUrl = (String) json_webviewCheckSlider2_GET_data.get("fgUrl");
                String bgUrl = (String) json_webviewCheckSlider2_GET_data.get("bgUrl");
                downloadFile(fgUrl, "fg.png");
                downloadFile(bgUrl, "bg.png");
                Map<String, Object> json_webviewValidSlider = webviewValidSlider();
                String sliderToken = (String) json_webviewValidSlider.get("token");
                System.out.println("sliderToken: " + sliderToken);
                if (sliderToken != null) {
                    loginByPwd(account, password, nonce, sliderToken);
                }
            }
        }
    }

    public static void test() {

//        File fgPng = new File("samples/fg004.png");
//        BufferedImage fgBI = ImageIO.read(fgPng);
//
//        File bgPng = new File("samples/bg004.png");
//        BufferedImage bgBI = ImageIO.read(bgPng);
//
//        BufferedImage fg_binaryImage = ImageUtil.binaryImage(fgBI);
//        BufferedImage bg_binaryImage = ImageUtil.binaryImage(bgBI);

//        Map<Point, Integer> fgComparedColorPoints = ImageUtil.getComparedColorPointsFromFgBi(fgBI);
//        int mostSimilarX = ImageUtil.getMostSimilarXInBg(bgBI, fgComparedColorPoints);
//        System.out.println(mostSimilarX);


//        Map<Point, Integer> fgComparedColorPoints = ImageUtil.getComparedColorPointsFromFgBi(fg_binaryImage);
//        int mostSimilarX = ImageUtil.getMostSimilarXInBg(bg_binaryImage, fgComparedColorPoints);
//        System.out.println(mostSimilarX);

//        int fgRectRGBSum = ImageUtil.rectRGBSum(fg_binaryImage, 0, 0, fg_binaryImage.getWidth(), fg_binaryImage.getHeight());
//        Map<Point, Integer> fgComparedColorPoints = ImageUtil.getComparedColorPointsFromFgBi(fgBI);
//        int mostSimilarX = ImageUtil.RectRGBSumMatch(bgBI, fgComparedColorPoints, fgRectRGBSum);
//        System.out.println(fgRectRGBSum);
//        System.out.println(mostSimilarX);

//        int fgRectRGBSum = ImageUtil.binaryRectRGBSum(fg_binaryImage, 0, 0, fg_binaryImage.getWidth(), fg_binaryImage.getHeight());
//        Map<Point, Integer> fgComparedColorPoints = ImageUtil.getComparedColorPointsFromFgBi(fg_binaryImage);
//        int mostSimilarX = ImageUtil.binaryRectRGBSumMatch(bg_binaryImage, fgComparedColorPoints);
//        System.out.println(fgRectRGBSum);
//        System.out.println(mostSimilarX);


//        File fg_binaryPng = new File("samples/fg_binaryImage.png");
//        if (fg_binaryPng.exists() == false) {
//            fg_binaryPng.createNewFile();
//        }
//        ImageIO.write(fg_binaryImage, "png", fg_binaryPng);
//
//        File bg_binaryPng = new File("samples/bg_binaryImage.png");
//        if (bg_binaryPng.exists() == false) {
//            bg_binaryPng.createNewFile();
//        }
//        ImageIO.write(bg_binaryImage, "png", bg_binaryPng);
    }
}
