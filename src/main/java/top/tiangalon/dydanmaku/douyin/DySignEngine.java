package top.tiangalon.dydanmaku.douyin;

import javax.script.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 抖音直播弹幕签名引擎
 * java内模拟js引擎直接套用抖音签名js，无需使用此前nodejs打包的Signature.exe
 * 相关js为从抖音官网获取，逻辑逆向不出来所以只能这样
 */
//根据params获取抖音签名
public class DySignEngine {


    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";


    private static final String BASE_STRING_TEMPLATE =
            "live_id=1,aid=6383,version_code=180800,webcast_sdk_version=1.0.14-beta.0," +
            "room_id=%s,sub_room_id=,sub_channel_id=,did_rule=3,user_unique_id=%s," +
            "device_platform=web,device_type=,ac=,identity=audience";

    //Nashorn JS 引擎
    private static volatile ScriptEngine nashornEngine;
    private static volatile boolean engineInitialized = false;
    private static final Object INIT_LOCK = new Object();

    /**
     * 签名生成
     * @param roomId         直播间ID
     * @param userUniqueId   用户ID
     * @return X-Bogus       签名
     */
    public static String generateSignature(String roomId, String userUniqueId) {
        //计算MD5
        String xMsStub = computeXMsStub(roomId, userUniqueId);
        //JS计算 X-Bogus
        return computeXBogus(xMsStub);
    }

    static String computeXMsStub(String roomId, String userUniqueId) {
        String base = String.format(BASE_STRING_TEMPLATE, roomId, userUniqueId);
        try {
            //计算MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //转十六进制字符串
            byte[] digest = md5.digest(base.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }


    private static String computeXBogus(String xMsStub) {
        ensureEngineInitialized();

        try {
            //调用抖音网页js代码获取签名
            String script = String.format(
                "var __input = {'X-MS-STUB': '%s'};" +
                "var __output = window._0x5c2014(__input);" +
                "__output['X-Bogus'];",
                xMsStub
            );

            Object result = nashornEngine.eval(script);
            if (result == null) {
                throw new RuntimeException("X-Bogus generation returned null");
            }
            return result.toString();

        } catch (ScriptException e) {
            throw new RuntimeException("Failed to compute X-Bogus via Nashorn JS engine", e);
        }
    }


    private static void ensureEngineInitialized() {
        if (engineInitialized) {
            return;
        }

        synchronized (INIT_LOCK) {
            if (engineInitialized) {
                return;
            }

            try {
                nashornEngine = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory()
                        .getScriptEngine("--language=es6");

                setupBrowserStubs();
                loadJsResource("runtime~client-entry.44b556b4.js");
                loadJsResource("webmssdk.es5.js");

                Object hasSignFunc = nashornEngine.eval(
                    "typeof window._0x5c2014 === 'function' " +
                    "&& typeof window._g_ === 'function'"
                );
                if (!Boolean.TRUE.equals(hasSignFunc)) {
                    throw new RuntimeException(
                        "Signature functions not properly loaded. " +
                        "window._0x5c2014 or window._g_ is missing.");
                }
                engineInitialized = true;
                //System.out.println("[DySignEngine] Nashorn JS engine initialized successfully.");

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize DySignEngine", e);
            }
        }
    }


    private static void setupBrowserStubs() throws ScriptException {
        // window 对象
        nashornEngine.eval(
            "var window = {" +
            "  toString: function() { return '[object Window]'; }" +
            "};"
        );

        // self 指向 window
        nashornEngine.eval("var self = window;");

        // document
        nashornEngine.eval(
            "var document = {" +
            "  cookie: ''," +
            "  body: null," +
            "  head: null," +
            "  documentElement: null," +
            "  getElementsByTagName: function() { return []; }," +
            "  createElement: function() {" +
            "    return {" +
            "      style: {}," +
            "      innerHTML: ''," +
            "      offsetWidth: 0," +
            "      offsetHeight: 0," +
            "      getContext: function() { return null; }," +
            "      getAttribute: function() { return ''; }," +
            "      setAttribute: function() {}," +
            "      appendChild: function() {}," +
            "      removeChild: function() {}" +
            "    };" +
            "  }," +
            "  createEvent: function() { return {initEvent: function(){}}; }," +
            "  addEventListener: function() {}," +
            "  removeEventListener: function() {}," +
            "  dispatchEvent: function() {}" +
            "};"
        );

        // navigator
        String userAgentEscaped = escapeForJs(USER_AGENT);
        nashornEngine.eval(
            "var navigator = {" +
            "  userAgent: '" + userAgentEscaped + "'," +
            "  appName: 'Netscape'," +
            "  appVersion: '5.0'," +
            "  platform: 'MacIntel'," +
            "  language: 'zh-CN'," +
            "  languages: ['zh-CN', 'zh']," +
            "  hardwareConcurrency: 8," +
            "  maxTouchPoints: 0," +
            "  cookieEnabled: true," +
            "  onLine: true," +
            "  vendor: 'Google Inc.'," +
            "  vendorSub: ''," +
            "  productSub: '20030107'," +
            "  getBattery: function() { return null; }," +
            "  mediaCapabilities: null," +
            "  bluetooth: null" +
            "};"
        );

        // 定时器
        nashornEngine.eval(
            "function setTimeout(fn, delay) { try { fn(); } catch(e) {} return 0; };" +
            "function clearTimeout(id) {};" +
            "function setInterval(fn, delay) { return 0; };" +
            "function clearInterval(id) {};"
        );

        // XMLHttpRequest
        nashornEngine.eval(
            "function XMLHttpRequest() {" +
            "  this.readyState = 0;" +
            "  this.status = 0;" +
            "  this.responseText = '';" +
            "  this.onreadystatechange = null;" +
            "  this.open = function() {};" +
            "  this.send = function() { this.readyState = 4; this.status = 200; };" +
            "  this.setRequestHeader = function() {};" +
            "  this.getAllResponseHeaders = function() { return ''; };" +
            "};" +
            "var ActiveXObject = null;"
        );

        nashornEngine.eval(
            "var console = { log: function() {}, warn: function() {}, error: function() {} };" +
            "var location = { href: 'https://live.douyin.com/', protocol: 'https:', host: 'live.douyin.com' };" +
            "var screen = { width: 1920, height: 1080, colorDepth: 24, pixelDepth: 24 };" +
            "var performance = { now: function() { return Date.now(); }, timing: {} };" +
            "var localStorage = { getItem: function() { return null; }, setItem: function() {}, removeItem: function() {} };" +
            "var sessionStorage = { getItem: function() { return null; }, setItem: function() {}, removeItem: function() {} };" +
            "var indexedDB = null;" +
            "var RTCPeerConnection = null;" +
            "var mozRTCPeerConnection = null;" +
            "var webkitRTCPeerConnection = null;"
        );
    }

    private static void loadJsResource(String resourceName) throws ScriptException {
        String scriptBody = readResourceAsString(resourceName);
        nashornEngine.eval(scriptBody);
    }


    private static String readResourceAsString(String resourceName) {
        try (InputStream is = DySignEngine.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + resourceName);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource: " + resourceName, e);
        }
    }

    //字符串转义
    private static String escapeForJs(String s) {
        if (s == null) return "null";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
