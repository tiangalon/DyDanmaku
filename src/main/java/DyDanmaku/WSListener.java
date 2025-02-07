package DyDanmaku;

import com.google.protobuf.InvalidProtocolBufferException;
import douyin.Douyin;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import static top.tiangalon.dydanmaku.DyDanmaku.LOGGER;


public class WSListener extends WebSocketListener{

    private ServerCommandSource source = null;
    public boolean isConnected = false;
    private Long LogId = 0L;
    private com.google.protobuf.ByteString Payload = null;
    private WebSocket webSocket = null;

    @Override
    public void onOpen(WebSocket webSocket, Response response){
        this.webSocket = webSocket;
        LOGGER.info("已连接至直播间");
        isConnected = true;
        Timer AckTimer = new Timer();
        AckTimer.schedule(new AckTimerTask(), 1000, 5000);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        isConnected = false;
        LOGGER.info("[DuDanmaku]已断开直播间连接,原因：" + reason);
        MsgOutput("[DuDanmaku]已断开直播间连接,原因：" + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response){
        String reason = null;
        if (t.getMessage() == null) {
            reason = "网络连接失败";
        } else {
            reason = t.getMessage();
        }
        this.onClosed(webSocket, 1000, reason);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes){
        byte[] bytesArray = bytes.toByteArray();
        if (bytesArray == null || bytesArray.length == 0) {
            LOGGER.info("收到空消息");
        } else {
            Douyin.PushFrame MsgFrame = null;

            try {
                MsgFrame = new Douyin.PushFrame().parseFrom(bytesArray);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
            byte[] uncompressedBytes = uncompress(MsgFrame.getPayload().toByteArray());
            Douyin.Response Msg = null;

            try {
                Msg = Douyin.Response.parseFrom(uncompressedBytes);
                //JsonMsg = JsonFormat.printer().print(Msg);
                if(LogId == 0L || Payload == null){
                    LogId = MsgFrame.getLogId();
                    Payload = Msg.getInternalExtBytes();
                }

                for (Douyin.Message SingleMsg : Msg.getMessagesListList()){
                    String method = SingleMsg.getMethod();
                    switch (method) {
                        //聊天消息
                        case "WebcastChatMessage":
                            Douyin.ChatMessage ChatMessage = Douyin.ChatMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("§b[消息]§f" + ChatMessage.getUser().getNickName() + "：" + ChatMessage.getContent());
                            break;



                         /*
                        //进入直播间消息
                        case "WebcastMemberMessage":
                            Douyin.MemberMessage MemberMessage = Douyin.MemberMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("【入场】" + MemberMessage.getUser().getNickName() + "进入了直播间");
                            break;
                        */

                         /*
                        //直播间统计消息
                        case "WebcastRoomUserSeqMessage":
                            Douyin.RoomUserSeqMessage RoomUserSeqMessage = Douyin.RoomUserSeqMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("【统计】当前观看人数：" + RoomUserSeqMessage.getTotalStr() + ",累计观看人数：" + RoomUserSeqMessage.getTotalPvForAnchor());
                            break;
                             */

                        //点赞消息
                        case "WebcastLikeMessage":
                            Douyin.LikeMessage LikeMessage = Douyin.LikeMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("§d[点赞]§f" + LikeMessage.getUser().getNickName() + "点了" + LikeMessage.getCount() + "个赞");
                            break;

                        //礼物消息
                        case "WebcastGiftMessage":
                            Douyin.GiftMessage GiftMessage = Douyin.GiftMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("§a[礼物]§f" + GiftMessage.getUser().getNickName() + "送出了" + GiftMessage.getGift().getName() + (GiftMessage.getGift().getCombo()? "x" + GiftMessage.getComboCount() : ""));
                            break;

                        //粉丝团消息
                        case "WebcastFansclubMessage":
                            Douyin.FansclubMessage FansclubMessage = Douyin.FansclubMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("§6[粉丝团]§f" + FansclubMessage.getContent());
                            break;

                        default:
                            //System.out.println("未分类消息: " + method);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.onClosed(webSocket, 1000, "error");
            }
        }

    }


    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to uncompress bytes", e);
            return null;
        }

        return out.toByteArray();
    }

    public void setSource(ServerCommandSource source) {
        this.source = source;
    }

    public void MsgOutput(String msg) {
        if (source != null) {
            source.getPlayer().sendMessage(Text.literal(msg));
        } else {
            source.getPlayer().sendMessage(Text.literal("[DyDanmaku]弹幕输出失败，未获取游戏源"));
        }
    }

    class AckTimerTask extends TimerTask {
        public void run() {
            sendAck(webSocket);
        }
    }

    public void sendAck(WebSocket webSocket) {
        if (LogId == 0L || Payload == null) {
            return;
        }
        Douyin.PushFrame AckFrame = Douyin.PushFrame.newBuilder()
                .setLogId(LogId)
                .setPayloadType("ack")
                .setPayload(Payload)
                .build();
        byte[] AckMsg = AckFrame.toByteArray();
        ByteString AckByteString = ByteString.of(AckMsg);
        webSocket.send(AckByteString);
    }

}
