package DyDanmaku;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class WebSocketClient {
    private ServerCommandSource source;
    WebSocket ws;
    WSListener Listener = null;

    public WebSocketClient(ServerCommandSource source) {
        this.source = source;
        this.Listener = new WSListener(source);
    }

    /**
     * websocket连接
     * @param roomId 直播间网页相应的roomId
     * @param ttwid 添加cookie的ttwid
     * @param useragent 添加请求头的User-Agent
     */
    public void connect(String roomId, String useragent, String ttwid) {
        //拼接url,连接websocket
        String url = "wss://webcast5-ws-web-lq.douyin.com/webcast/im/push/v2/?" +
                "app_name=douyin_web&version_code=180800&webcast_sdk_version=1.0.14-beta.0" +
                "&update_version_code=1.0.14-beta.0&compress=gzip&device_platform=web&cookie_enabled=true&screen_width=1536" +
                "&screen_height=864&browser_language=zh-CN&browser_platform=Win32&browser_name=Mozilla" +
                "&browser_version=5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)" +
                "%20Chrome/126.0.0.0%20Safari/537.36&browser_online=true&tz_name=Asia/Shanghai&" +
                "cursor=r-1_d-1_u-1_fh-7385824323388707875_t-1719646741156&" +
                "internal_ext=internal_src:dim|wss_push_room_id:" + roomId + "|wss_push_did:7311183754668557878" +
                "|first_req_ms:1719646741059|fetch_time:1719646741156|seq:1|wss_info:0-1719646741156-0-0|" +
                "wrds_v:7311183754668557878&host=https://live.douyin.com&aid=6383" +
                "&live_id=1&did_rule=3&endpoint=live_pc&support_wrds=1&" +
                "user_unique_id=7311183754668557878" +
                "&im_path=/webcast/im/fetch/&identity=audience&need_persist_msg_count=15" +
                "&insert_task_id=&live_reason=&room_id=" + roomId + "&heartbeatDuration=0&signature=";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
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

    public boolean isConnected() {
        return Listener.isConnected();
    }
}