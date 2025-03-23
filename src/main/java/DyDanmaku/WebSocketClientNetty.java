package DyDanmaku;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static top.tiangalon.dydanmaku.DyDanmaku.*;

public class WebSocketClientNetty {

    private String uri;
    private ChannelPromise handshakePromise;
    private String useragent = "Mozilla&browser_version=5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/131.0.0.0%20Safari/537.36";
    private String ttwid = null;
    private Channel channel;
    private static int port = 443;
    public Map<String, String> params = null;

    private ServerCommandSource source = null;

    public void init(Map<String, String> params, ServerCommandSource source) throws URISyntaxException, SSLException, InterruptedException {
        this.source = source;
        String signature;
        this.params = params;
        String roomId = params.get("roomId");
        String user_unique_id = params.get("user_unique_id");
        this.ttwid = params.get("ttwid");
        //Listener.setSource(source);


        try {
            signature = sign(roomId, user_unique_id);
        } catch (IOException e) {
            LOGGER.info("[DyDanmaku]无法连接房间：获取签名失败");
            return;
        }

        this.uri = "wss://webcast5-ws-web-hl.douyin.com/webcast/im/push/v2/?" +
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
                URLEncoder.encode("internal_ext=internal_src:dim|wss_push_room_id:" + roomId + "|wss_push_did:" + user_unique_id + "|first_req_ms:1732882891041|" +
                        "fetch_time:1732882891133|seq:1|wss_info:0-1732882891133-0-0|wrds_v:7442675340347970811&") +
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
    }

    public void run() throws URISyntaxException, SSLException, InterruptedException {
        URI uri = new URI(this.uri);

        Bootstrap b = new Bootstrap();
        SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        EventLoopGroup group = new NioEventLoopGroup();
        HttpHeaders headers = new DefaultHttpHeaders();
        WebSocketClientHandler handler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, headers), source);
        headers.add("User-Agent", useragent);
        headers.add("Cookie", "ttwid="+ ttwid);
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                sslCtx.newHandler(ch.alloc(), uri.getHost(), port),
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                handler
                        );
                    }
                });

        channel = b.connect(uri.getHost(), port).sync().channel();
        handler.handshakeFuture().sync();

    }

    public static String sign(String roomId, String user_unique_id) throws IOException {
        String command = "";

        if (isRunInJar()) {
            //在jar中运行时
            command = ConfigDirPath + "/Signature.exe "+ roomId + " " + user_unique_id;
        } else {
            //在IDE中运行时
            command = WebSocketClientNetty.class.getClassLoader().getResource("./Signature.exe").getPath() + " " + roomId + " " + user_unique_id;
        }


        //command = ConfigDirPath + "/Signature.exe "+ roomId + " " + user_unique_id;

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
        InputStream SignFile = WebSocketClientNetty.class.getClassLoader().getResourceAsStream("Signature.exe");
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
        URL url = WebSocketClientNetty.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = "";
        try {
            filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
            for (int i = 0; i < 2; i++) {
                filePath = filePath.substring(0, filePath.lastIndexOf("/"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public void close() throws InterruptedException {
        if (this.isConnected()) {
            channel.writeAndFlush(new CloseWebSocketFrame());
            channel.closeFuture().sync();
        }
    }

    public void MsgOutput(String msg) {
        if (source != null) {
            source.getPlayer().sendMessage(Text.literal(msg));
        } else {
            source.getPlayer().sendMessage(Text.literal("[DyDanmaku]输出失败，未获取游戏源"));
        }
    }


    public void LiveStatusOutput() {
        if (params == null) {
            MsgOutput("[DyDanmaku]未获取到直播间参数");
            return;
        }
        String live_status_str = "§4●未开播§f";
        MsgOutput("—————————————§bDYDANMAKU§f—————————————");
        if (Objects.equals(params.get("live_status"), "2")) {
            live_status_str = "§a●直播中§f";
        }
        MsgOutput("已经连接到直播间   状态：" + live_status_str);
        MsgOutput("直播间标题：" + params.get("live_title"));
        MsgOutput("主播：" + params.get("nickname"));
        MsgOutput("—————————————————————————————————");
    }
}
