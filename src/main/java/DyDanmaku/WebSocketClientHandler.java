package DyDanmaku;

import com.google.protobuf.InvalidProtocolBufferException;
import douyin.Douyin;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import static top.tiangalon.dydanmaku.DyDanmaku.LOGGER;

@Sharable
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private ServerCommandSource source;
    public boolean isConnected = false;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public void setSource(ServerCommandSource source) {
        this.source = source;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        //WebSocketClientHandshaker handshaker = this.handshaker;

        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            // 握手成功
            isConnected = true;
            handshakeFuture.setSuccess();
            return;
        }


        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (status=" + response.status() +
                            ", content=" + response.content().toString(io.netty.util.CharsetUtil.UTF_8) + ')');
        }


        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            byte[] byteArray = new byte[binaryFrame.content().readableBytes()];
            binaryFrame.content().readBytes(byteArray);

            if (byteArray == null || byteArray.length == 0) {
                LOGGER.info("[DyDanmaku]收到空消息");
            } else {
                Douyin.PushFrame MsgFrame = null;
                try {
                    MsgFrame = new Douyin.PushFrame().parseFrom(byteArray);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
                byte[] uncompressedBytes = uncompress(MsgFrame.getPayload().toByteArray());
                Douyin.Response Msg = null;

                try {
                    Msg = Douyin.Response.parseFrom(uncompressedBytes);

                    //发送ack
                    if (Msg.getNeedAck() == true){
                        Douyin.PushFrame AckFrame = Douyin.PushFrame.newBuilder()
                                .setLogId(MsgFrame.getLogId())
                                .setPayloadType("ack")
                                .setPayload(Msg.getInternalExtBytes())
                                .build();
                        byte[] AckMsg = AckFrame.toByteArray();
                        ch.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(AckMsg)));
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
        } else if (frame instanceof CloseWebSocketFrame) {
            LOGGER.info("接收到关闭消息");
            ch.close();
            isConnected = false;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
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
}

