package DyDanmaku;

import douyin.Douyin;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.minecraft.server.command.ServerCommandSource;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static top.tiangalon.dydanmaku.DyDanmaku.*;


public class WebSocketClientNetty {

    private String url;
    private String ttwid;
    private String roomId;
    private String user_unique_id;
    private final String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
    private ServerCommandSource source;
    private EventLoopGroup group = new NioEventLoopGroup();
    public boolean isConnected = false;

    public void init(String roomId, String user_unique_id, String ttwid, ServerCommandSource source) throws IOException {
        this.source = source;
        this.roomId = roomId;
        this.user_unique_id = user_unique_id;
        this.ttwid = ttwid;

        String signature = sign(roomId, user_unique_id);

        url = "wss://webcast5-ws-web-lf.douyin.com/webcast/im/push/v2/?" +
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
                "browser_name=Mozilla&browser_version=5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)" +
                "%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/126.0.0.0%20Safari/537.36&" +
                "browser_online=true&" +
                "tz_name=Asia/Hong_Kong&" +
                "cursor=t-1720704550702_r-1_d-1_u-1_h-7385824323388707875&" +
                URLEncoder.encode("internal_ext=internal_src:dim|wss_push_room_id:" + roomId + "|wss_push_did:" + user_unique_id + "|first_req_ms:1720704550478|" +
                "fetch_time:1720704550602|seq:1|wss_info:0-1720704550602-0-0|wrds_v:7390369768328402246&", StandardCharsets.UTF_8) +
                "host=https://live.douyin.com&" +
                "aid=6383&" +
                "live_id=1&" +
                "did_rule=3&" +
                "endpoint=live_pc&support_wrds=1&" +
                "user_unique_id=" + user_unique_id + "&" +
                "im_path=/webcast/im/fetch/&" +
                "identity=audience&" +
                "need_persist_msg_count=15&" +
                "insert_task_id=&" +
                "live_reason=&" +
                "room_id=" + roomId + "&" +
                "heartbeatDuration=0&" +
                "signature=" + signature;

    }

    public void run() throws Exception {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
            final SslContext sslCtx;
            if ("wss".equalsIgnoreCase(scheme)) {
                sslCtx = SslContextBuilder.forClient()
                        //.sslProvider(SslProvider.JDK)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
            } else {
                sslCtx = null;
            }
            HttpHeaders headers = new DefaultHttpHeaders()
                    .add("User-Agent", UserAgent)
                    .add("Cookie", "ttwid=" + ttwid);
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(
                            uri, WebSocketVersion.V13, null, true, headers));
            handler.setSource(source);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (sslCtx != null) {
                                pipeline.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
                            }
                            pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(Douyin.MemberMessage.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());

                        }
                    });

            Channel ch = bootstrap.connect(uri.getHost(), 443).sync().channel();
            handler.handshakeFuture().sync();
            isConnected = handler.isConnected;

            // 在这里添加自定义处理程序以处理WebSocket消息

            //ch.closeFuture().sync();
        } catch (Exception e) {
            LOGGER.info(e.toString());
        }
//        finally {
//            group.shutdownGracefully();
//        }
    }

    public void close() {
        group.shutdownGracefully();
        isConnected = false;
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
            filePath = filePath.substring(0, filePath.lastIndexOf("/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }



}
