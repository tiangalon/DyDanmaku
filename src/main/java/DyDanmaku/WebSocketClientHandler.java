package DyDanmaku;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import douyin.Douyin;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static top.tiangalon.dydanmaku.DyDanmaku.LOGGER;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private Long LogId = 0L;
    private com.google.protobuf.ByteString Payload = null;
    Timer AckTimer = new Timer();

    private ServerCommandSource source = null;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, ServerCommandSource source) {
        this.handshaker = handshaker;
        this.source = source;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
        //System.out.println("WebSocket Client connected!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        AckTimer.cancel();
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object frame) throws Exception {
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) frame);
            } catch (WebSocketHandshakeException e) {
                System.err.println("WebSocket连接失败");
                handshakeFuture.setFailure(e);
                ctx.close();
                return;
            }
            System.out.println("WebSocket连接成功");
            AckTimer.schedule(new AckTimerTask(), 3000, 5000);
            handshakeFuture.setSuccess();
            return;
        }

        BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
        int length = binaryFrame.content().readableBytes();
        byte[] bytesArray  = new byte[length];
        binaryFrame.content().getBytes(0, bytesArray);
        //byte[] bytesArray = frame.array();
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
                PayLoadUpdate(MsgFrame.getLogId(), Msg.getInternalExtBytes());

                for (Douyin.Message SingleMsg : Msg.getMessagesListList()) {
                    String method = SingleMsg.getMethod();
                    switch (method) {
                        //聊天消息
                        case "WebcastChatMessage":
                            Douyin.ChatMessage ChatMessage = Douyin.ChatMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("§b[消息]§f" + ChatMessage.getUser().getNickName() + "：" + ChatMessage.getContent());
                            //LOGGER.info("[消息]" + ChatMessage.getUser().getNickName() + "：" + ChatMessage.getContent());
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
                            //LOGGER.info("[点赞]" + LikeMessage.getUser().getNickName() + "点了" + LikeMessage.getCount() + "个赞");
                            break;

                        //礼物消息
                        case "WebcastGiftMessage":
                            Douyin.GiftMessage GiftMessage = Douyin.GiftMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("§a[礼物]§f" + GiftMessage.getUser().getNickName() + "送出了" + GiftMessage.getGift().getName() + (GiftMessage.getGift().getCombo() ? "x" + GiftMessage.getComboCount() : ""));
                            //LOGGER.info("[礼物]" + GiftMessage.getUser().getNickName() + "送出了" + GiftMessage.getGift().getName() + (GiftMessage.getGift().getCombo() ? "x" + GiftMessage.getComboCount() : ""));
                            break;

                        //粉丝团消息
                        case "WebcastFansclubMessage":
                            Douyin.FansclubMessage FansclubMessage = Douyin.FansclubMessage.parseFrom(SingleMsg.getPayload());
                            MsgOutput("§6[粉丝团]§f" + FansclubMessage.getContent());
                            //LOGGER.info("[粉丝团]" + FansclubMessage.getContent());
                            break;

                        default:
                            //System.out.println("未分类消息: " + method);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                //this.onClosed(webSocket, 1000, "error");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
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

    public void PayLoadUpdate(Long LogId, com.google.protobuf.ByteString Payload) {
        this.LogId = LogId;
        this.Payload = Payload;
    }

    class AckTimerTask extends TimerTask {
        public void run() {
            if (LogId == 0L || Payload == null) {
                return;
            }
            Douyin.PushFrame AckFrame = Douyin.PushFrame.newBuilder()
                    .setLogId(LogId)
                    .setPayloadType("ack")
                    .setPayload(Payload)
                    .build();
            byte[] AckMsg = AckFrame.toByteArray();
            ByteBuf AckByteBuf = copiedBuffer(AckMsg);
            //webSocket.send(AckByteString);
            handshakeFuture.channel().writeAndFlush(new BinaryWebSocketFrame(AckByteBuf));
            //LOGGER.info("发送心跳包");
        }
    }

    public void MsgOutput(String msg) {
        if (source != null) {
            source.getPlayer().sendMessage(Text.literal(msg));
        } else {
            source.getPlayer().sendMessage(Text.literal("[DyDanmaku]输出失败，未获取游戏源"));
        }
    }


}