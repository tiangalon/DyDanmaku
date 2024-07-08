package DyDanmaku;

import com.google.protobuf.InvalidProtocolBufferException;
import douyin.Douyin;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.Date;

import static top.tiangalon.dydanmaku.DyDanmaku.LOGGER;

public class WSListener extends WebSocketListener {
    //计时器
    Date StartTime, EndTime;
    boolean isConnected = false;
    private ServerCommandSource source;

    public WSListener(ServerCommandSource source){
        this.source = source;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response){
        LOGGER.info("connected to server");
        isConnected = true;
        StartTime = new Date();
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason){
        super.onClosed(webSocket, code, reason);
        LOGGER.info("connection closed");
        EndTime = new Date();
        long time = (EndTime.getTime() - StartTime.getTime()) / 1000;
        LOGGER.info("duration of the connection：" + time + "s");
        isConnected = false;
//        webSocket.close(1000, "bye");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response){
        source.getPlayer().sendMessage(Text.literal("§c[INFO]§fDyDanmaku连接已关闭"));
        isConnected = false;
        t.printStackTrace();
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes){
        byte[] bytesArray = bytes.toByteArray();
        if (bytesArray == null || bytesArray.length == 0) {
            //System.out.println("收到空消息");
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

                //发送ack
                if (Msg.getNeedAck() == true){
                    Douyin.PushFrame AckFrame = Douyin.PushFrame.newBuilder()
                            .setLogId(MsgFrame.getLogId())
                            .setPayloadType("ack")
                            .setPayload(Msg.getInternalExtBytes())
                            .build();
                    byte[] AckMsg = AckFrame.toByteArray();
                    ByteString AckByteString = ByteString.of(AckMsg);
                    webSocket.send(AckByteString);
                    //System.out.println("已发送ack");
                }

                for (Douyin.Message SingleMsg : Msg.getMessagesListList()){
                    String method = SingleMsg.getMethod();
                    switch (method) {
                        //聊天消息
                        case "WebcastChatMessage":
                            Douyin.ChatMessage ChatMessage = Douyin.ChatMessage.parseFrom(SingleMsg.getPayload());
                            source.getPlayer().sendMessage(Text.literal("§b[消息]§f" + ChatMessage.getUser().getNickName() + "：" + ChatMessage.getContent()));

                            break;

                        /*
                        //进入直播间消息
                        case "WebcastMemberMessage":
                            Douyin.MemberMessage MemberMessage = Douyin.MemberMessage.parseFrom(SingleMsg.getPayload());
                            LOGGER.info("【入场】" + MemberMessage.getUser().getNickName() + "进入了直播间");
                            break;
                        */

                        /*
                        //直播间统计消息
                        case "WebcastRoomUserSeqMessage":
                            Douyin.RoomUserSeqMessage RoomUserSeqMessage = Douyin.RoomUserSeqMessage.parseFrom(SingleMsg.getPayload());
                            source.getPlayer().sendMessage(Text.literal("{\"text\" : \"[统计]\" , \"color\" : \"yellow\", \"bold\" : \"true\"}" + "当前观看人数：" + RoomUserSeqMessage.getTotalStr() + ",累计观看人数：" + RoomUserSeqMessage.getTotalPvForAnchor()));
                            break;
                            */

                        //点赞消息
                        case "WebcastLikeMessage":
                            Douyin.LikeMessage LikeMessage = Douyin.LikeMessage.parseFrom(SingleMsg.getPayload());
                            source.getPlayer().sendMessage(Text.literal("§d[点赞]§f" + LikeMessage.getUser().getNickName() + "点了" + LikeMessage.getCount() + "个赞"));

                            break;

                        //礼物消息
                        case "WebcastGiftMessage":
                            Douyin.GiftMessage GiftMessage = Douyin.GiftMessage.parseFrom(SingleMsg.getPayload());
                            source.getPlayer().sendMessage(Text.literal("§a[礼物]§f" + GiftMessage.getUser().getNickName() + "送出了" + GiftMessage.getGift().getName() + (GiftMessage.getGift().getCombo()? "x" + GiftMessage.getComboCount() : "")));
                            break;

                        //粉丝团消息
                        case "WebcastFansclubMessage":
                            Douyin.FansclubMessage FansclubMessage = Douyin.FansclubMessage.parseFrom(SingleMsg.getPayload());
                            source.getPlayer().sendMessage(Text.literal("§6[粉丝团]§f" + FansclubMessage.getContent()));
                            break;
                        default:
                            //System.out.println("未分类消息: " + method);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
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
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public boolean isConnected() {
        return isConnected;
    }

}
