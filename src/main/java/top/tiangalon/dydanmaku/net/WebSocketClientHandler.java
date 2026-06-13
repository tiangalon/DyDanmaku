package top.tiangalon.dydanmaku.net;

import com.google.protobuf.InvalidProtocolBufferException;
import top.tiangalon.dydanmaku.config.ConfigManager;
import top.tiangalon.dydanmaku.douyin.Douyin;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
//import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
//import net.minecraft.text.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static top.tiangalon.dydanmaku.client.DyDanmakuClient.ConfigDirPath;
import static top.tiangalon.dydanmaku.client.DyDanmakuClient.LOGGER;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private Long LogId = 0L;
    private com.google.protobuf.ByteString Payload = null;
    Timer AckTimer = new Timer();
    public StringBuffer DanmakuList = new StringBuffer();

    private CommandSourceStack source = null;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, CommandSourceStack source) {
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
                PayLoadUpdate(MsgFrame.getLogId(), Msg.getInternalExtBytes());

                for (Douyin.Message SingleMsg : Msg.getMessagesListList()) {
                    String method = SingleMsg.getMethod();
                    if (!ConfigManager.getMethodVisibilityConfig(ConfigDirPath).isMethodEnabled(method)) {
                        continue;
                    }
                    ConfigManager.TemplateConfig templates = ConfigManager.getTemplateConfig(ConfigDirPath);
                    switch (method) {
                        //聊天消息
                        case "WebcastChatMessage": {
                            Douyin.ChatMessage ChatMessage = Douyin.ChatMessage.parseFrom(SingleMsg.getPayload());
                            Douyin.User chatUser = ChatMessage.hasUser() ? ChatMessage.getUser() : null;
                            if (!shouldDisplayUser(chatUser)) {
                                break;
                            }
                            Map<String, String> vars = new HashMap<>();
                            putUserVars(vars, chatUser);
                            vars.put("content", ChatMessage.getContent());
                            String msg = applyTemplate(templates, "WebcastChatMessage", vars,
                                    "§b[聊天]§f${nickname}：${content}");
                            outputFiltered(msg);
                            break;
                        }

                        //进入直播间消息
                        case "WebcastMemberMessage": {
                            Douyin.MemberMessage MemberMessage = Douyin.MemberMessage.parseFrom(SingleMsg.getPayload());
                            Douyin.User memberUser = MemberMessage.hasUser() ? MemberMessage.getUser() : null;
                            Map<String, String> vars = new HashMap<>();
                            putUserVars(vars, memberUser);
                            vars.put("memberCount", String.valueOf(MemberMessage.getMemberCount()));
                            vars.put("actionDescription", MemberMessage.getActionDescription());
                            vars.put("userId", String.valueOf(MemberMessage.getUserId()));
                            String msg = applyTemplate(templates, "WebcastMemberMessage", vars,
                                    "§e[入场]§f${nickname} 进入了直播间");
                            outputFiltered(msg);
                            break;
                        }

                        //直播间统计消息
                        case "WebcastRoomUserSeqMessage": {
                            Douyin.RoomUserSeqMessage RoomUserSeqMessage = Douyin.RoomUserSeqMessage.parseFrom(SingleMsg.getPayload());
                            Map<String, String> vars = new HashMap<>();
                            vars.put("totalStr", RoomUserSeqMessage.getTotalStr());
                            vars.put("totalPvForAnchor", String.valueOf(RoomUserSeqMessage.getTotalPvForAnchor()));
                            String msg = applyTemplate(templates, "WebcastRoomUserSeqMessage", vars,
                                    "§9[统计]§f当前观看人数：${totalStr}，累计观看人数：${totalPvForAnchor}");
                            outputFiltered(msg);
                            break;
                        }

                        //点赞消息
                        case "WebcastLikeMessage": {
                            Douyin.LikeMessage LikeMessage = Douyin.LikeMessage.parseFrom(SingleMsg.getPayload());
                            Douyin.User likeUser = LikeMessage.hasUser() ? LikeMessage.getUser() : null;
                            if (!shouldDisplayUser(likeUser)) {
                                break;
                            }
                            Map<String, String> vars = new HashMap<>();
                            putUserVars(vars, likeUser);
                            vars.put("count", String.valueOf(LikeMessage.getCount()));
                            String msg = applyTemplate(templates, "WebcastLikeMessage", vars,
                                    "§d[点赞]§f${nickname} 点了${count}个赞");
                            outputFiltered(msg);
                            break;
                        }

                        //礼物消息
                        case "WebcastGiftMessage": {
                            Douyin.GiftMessage GiftMessage = Douyin.GiftMessage.parseFrom(SingleMsg.getPayload());
                            Douyin.User giftUser = GiftMessage.hasUser() ? GiftMessage.getUser() : null;
                            if (!shouldDisplayUser(giftUser)) {
                                break;
                            }
                            Douyin.GiftStruct gift = GiftMessage.getGift();
                            Map<String, String> vars = new HashMap<>();
                            putUserVars(vars, giftUser);
                            vars.put("giftName", gift.getName());
                            vars.put("giftCombo", gift.getCombo() ? "x" + GiftMessage.getComboCount() : "");
                            vars.put("comboCount", String.valueOf(GiftMessage.getComboCount()));
                            vars.put("repeatCount", String.valueOf(GiftMessage.getRepeatCount()));
                            vars.put("giftId", String.valueOf(GiftMessage.getGiftId()));
                            vars.put("giftDescribe", gift.getDescribe());
                            vars.put("giftDiamondCount", String.valueOf(gift.getDiamondCount()));
                            vars.put("giftType", String.valueOf(gift.getType()));
                            String msg = applyTemplate(templates, "WebcastGiftMessage", vars,
                                    "§a[礼物]§f${nickname} 送出了${giftName}${giftCombo}");
                            outputFiltered(msg);
                            break;
                        }

                        //粉丝团消息
                        case "WebcastFansclubMessage": {
                            Douyin.FansclubMessage FansclubMessage = Douyin.FansclubMessage.parseFrom(SingleMsg.getPayload());
                            String content = FansclubMessage.getContent();
                            if (content == null || content.isEmpty()) {
                                break;
                            }
                            Map<String, String> vars = new HashMap<>();
                            vars.put("content", content);
                            String msg = applyTemplate(templates, "WebcastFansclubMessage", vars,
                                    "§6[粉丝团]§f${content}");
                            outputFiltered(msg);
                            break;
                        }

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
            source.getPlayer().sendSystemMessage(Component.literal(msg));
        } else {
            source.getPlayer().sendSystemMessage(Component.literal("[DyDanmaku]输出失败，未获取游戏源"));
        }
    }
    public void DanmakuAppend(StringBuffer DanmakuList, String msg){
        if (DanmakuList.length() > 10000){
            DanmakuList.delete(0, DanmakuList.indexOf("\n") + 1);
            DanmakuList.append(msg);
        } else {
            DanmakuList.append(msg);
        }
    }

    /**
     * 检查弹幕消息是否应该通过过滤器显示
     * @param msg 已格式化的弹幕消息（可能包含颜色代码）
     * @return true 如果应该显示，false 如果应该屏蔽
     */
    private boolean shouldDisplay(String msg) {
        ConfigManager.FilterConfig filter = ConfigManager.getFilterConfig(ConfigDirPath);
        if (!filter.isEnabled()) {
            return true; // 过滤器禁用，显示所有消息
        }
        String lowerMsg = msg.toLowerCase();
        boolean matched = false;
        for (String keyword : filter.keywords) {
            if (keyword != null && !keyword.isEmpty() && lowerMsg.contains(keyword.toLowerCase())) {
                matched = true;
                break;
            }
        }
        if ("whitelist".equals(filter.mode)) {
            return matched;  // 白名单模式：匹配才显示
        } else {
            return !matched; // 黑名单模式：匹配则屏蔽
        }
    }

    /**
     * 检查用户属性是否通过过滤器（粉丝团、消费等级）
     * @param user 消息发送者，可能为 null（无用户信息）
     * @return true 如果应该显示，false 如果应该屏蔽
     */
    private boolean shouldDisplayUser(Douyin.User user) {
        ConfigManager.UserFilterConfig filter = ConfigManager.getUserFilterConfig(ConfigDirPath);
        if (!filter.enabled) {
            return true; // 过滤器禁用，显示所有消息
        }
        // 无用户信息且启用了过滤：无法判断属性，默认放行
        if (user == null) {
            return true;
        }
        // 检查粉丝团要求
        if (filter.requireFanClub) {
            if (!user.hasFansClub()) {
                return false;
            }
            if (filter.fanClubMinLevel > 0) {
                if (user.getFansClub().getData().getLevel() < filter.fanClubMinLevel) {
                    return false;
                }
            }
        }
        // 检查消费等级要求
        if (filter.requirePayGrade) {
            if (!user.hasPayGrade()) {
                return false;
            }
            if (filter.payGradeMinLevel > 0) {
                if (user.getPayGrade().getLevel() < filter.payGradeMinLevel) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 向模板变量 map 中填入用户相关字段（昵称、消费等级、粉丝团等级）
     * @param vars 变量 map
     * @param user 用户对象，可能为 null
     */
    private void putUserVars(Map<String, String> vars, Douyin.User user) {
        if (user != null) {
            vars.put("nickname", user.getNickName());
            if (user.hasPayGrade()) {
                vars.put("payGradeLevel", String.valueOf(user.getPayGrade().getLevel()));
            }
            if (user.hasFansClub()) {
                vars.put("fansClubLevel", String.valueOf(user.getFansClub().getData().getLevel()));
            }
        }
    }

    /**
     * 应用模板替换变量占位符，生成最终输出字符串
     * @param templateConfig 模板配置
     * @param method         消息方法名
     * @param vars           变量名到值的映射
     * @param defaultFormat  默认格式（含 ${} 占位符），模板未配置时使用
     * @return 渲染后的消息字符串
     */
    private String applyTemplate(ConfigManager.TemplateConfig templateConfig, String method,
                                  Map<String, String> vars, String defaultFormat) {
        String template = templateConfig.getTemplate(method);
        if (template == null) {
            template = defaultFormat;
        }
        String result = template;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        // 清理未被替换的占位符（移除空值对应的标记）
        result = result.replaceAll("\\$\\{[^}]+\\}", "");
        return result;
    }

    /**
     * 过滤后输出消息到聊天框和弹幕列表（使用 DanmakuAppend）
     */
    private void outputFiltered(String msg) {
        if (!shouldDisplay(msg)) {
            return;
        }
        MsgOutput(msg);
        DanmakuAppend(DanmakuList, msg + "\n");
    }


}