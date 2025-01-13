package DyDanmaku;

import net.minecraft.server.command.ServerCommandSource;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

import java.io.*;
import java.net.URL;
import java.util.Map;

import static top.tiangalon.dydanmaku.DyDanmaku.*;


public class WebSocketClient {
    private WSListener Listener = new WSListener();
    private WebSocket ws;

    /**
     * websocket连接
     * @param params         包含直播间信息的map
     */
    /*
    public void connect(Map<String, String> params, String signature) {
        connect(params, signature, false);
    }

     */

    public void connect(Map<String, String> params, ServerCommandSource source) {
        String signature;
        String roomId = params.get("roomId");
        String user_unique_id = params.get("user_unique_id");
        String ttwid = params.get("ttwid");
        Listener.setSource(source);


        try {
            signature = sign(roomId, user_unique_id);
        } catch (IOException e) {
            LOGGER.info("[DyDanmaku]无法连接房间：获取签名失败");
            return;
        }

        String useragent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
        String wss_url = "wss://webcast5-ws-web-hl.douyin.com/webcast/im/push/v2/?" +
                "app_name=douyin_web&" +
                "version_code=180800&" +
                "webcast_sdk_version=1.0.14-beta.0&" +
                "update_version_code=1.0.14-beta.0&" +
                "compress=gzip&" +
                "device_platform=web&" +
                "cookie_enabled=true&" +
                "screen_width=2560&" +
                "screen_height=1440&" +
                "browser_language=zh-CN&" +
                "browser_platform=Win32&" +
                "browser_name=Mozilla&browser_version=5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/131.0.0.0%20Safari/537.36&" +
                "browser_online=true&" +
                "tz_name=Asia/Hong_Kong&" +
                "cursor=t-1732882891133_r-1_d-1_u-1_h-7442675243345155072&" +
                "internal_ext=internal_src:dim|wss_push_room_id:" + roomId + "|wss_push_did:" + user_unique_id + "|first_req_ms:1732882891041|" +
                "fetch_time:1732882891133|seq:1|wss_info:0-1732882891133-0-0|wrds_v:7442675340347970811&" +
                "host=https://live.douyin.com&" +
                "aid=6383&" +
                "live_id=1&" +
                "did_rule=3&" +
                "endpoint=live_pc&support_wrds=1&" +
                "user_unique_id=" + user_unique_id + "&" +
                "im_path=/webcast/im/fetch/&" +
                "identity=audience&need_persist_msg_count=15&" +
                "insert_task_id=&" +
                "live_reason=&" +
                "room_id=" + roomId + "&" +
                "heartbeatDuration=0&" +
                "signature=" + signature;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(wss_url)
            .header("User-Agent", useragent)
            .header("Cookie", "ttwid="+ ttwid)
            .build();
        ws = client.newWebSocket(request, Listener);


    }

    /**
     * websocket关闭
     */
    public void close() {
        if (ws != null) {
            ws.close(1000, "close");
            Listener.onClosing(ws, 1000, "close");
            Listener.onClosed(ws, 1000, "close");
        }
    }

    public static String sign(String roomId, String user_unique_id) throws IOException {
        String command = "";
        if (isRunInJar()) {
            //在jar中运行时
            command = ConfigDirPath + "/Signature.exe "+ roomId + " " + user_unique_id;
        } else {
            //在IDE中运行时
            command = WebSocketClient.class.getClassLoader().getResource("./Signature.exe").getPath() + " " + roomId + " " + user_unique_id;
        }

        Process process = null;
        String signature = "";
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            signature = br.readLine();
            return signature;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (process!= null) {
                process.destroy();
            }
        }
        return signature;
    }

    public static void getSignFile(String SignFilePath) throws IOException {
        InputStream SignFile = WebSocketClient.class.getClassLoader().getResourceAsStream("Signature.exe");
        if (SignFile == null) {
            LOGGER.info("Signature.exe not found in resources");
        }else{
            int index;
            byte[] bytes = new byte[1024];
            FileOutputStream downloadFile = new FileOutputStream(SignFilePath);
            while ((index = SignFile.read(bytes)) != -1) {
                downloadFile.write(bytes, 0, index);
                downloadFile.flush();
            }
            downloadFile.close();
            SignFile.close();
        }
    }

    public static String getPath() {
        URL url = WebSocketClient.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = "";
        try {
            filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
            filePath = filePath.substring(0, filePath.lastIndexOf("/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public boolean isConnected() {
        return Listener.isConnected;
    }

}