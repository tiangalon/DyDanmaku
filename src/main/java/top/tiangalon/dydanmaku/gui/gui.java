package top.tiangalon.dydanmaku.gui;

import DyDanmaku.DyDanmakuRequest;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import DyDanmaku.WebSocketClientNetty;
import net.minecraft.text.TextContent;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static top.tiangalon.dydanmaku.client.DyDanmakuClient.ConfigDirPath;
import static top.tiangalon.dydanmaku.client.DyDanmakuClient.DyDanmakuKey;
import static top.tiangalon.dydanmaku.client.DyDanmakuClient.LOGGER;

public class gui extends Screen {
    public Boolean isConnected = false;
    public WebSocketClientNetty websocket;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private DanmakuWidget DanmakuScrollBox;
    //private String TestMassage = "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17\n18\n19\n20\n21\n22\n23\n24\n25\n26\n27";

    private String live_id = "";
    private TextFieldWidget liveIdInput;
    private ButtonWidget connectButton;

    public gui(Text title , WebSocketClientNetty websocket) {
        super(title);
        this.websocket = websocket;
    }

    @Override
    protected void init() {
        super.init();

        DanmakuScrollBox = new DanmakuWidget(this.textRenderer, 40, 110, width - 80, height - 110 - 10, Text.of("直播间实时弹幕"));
        DanmakuScrollBox.setEditable(true);
        DanmakuScrollBox.isActive();
        //DanmakuScrollBox.setText(websocket.handler.DanmakuList.toString());
        this.addDrawableChild(DanmakuScrollBox);
        DanmakuScrollBox.visible = false;

        // 创建直播间ID输入框
        liveIdInput = new LiveIdInputWidget(this.textRenderer, 40, 70, 120, 20, Text.of("输入直播间ID"));
        liveIdInput.setMaxLength(20);
        liveIdInput.setEditable(true);
        this.addDrawableChild(liveIdInput);

        // 创建连接按钮
        connectButton = ButtonWidget.builder(Text.of("连接"), btn -> {
            if (websocket.isConnected()){
                try {
                    websocket.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.client.getToastManager().add(
                        SystemToast.create(
                                this.client,
                                SystemToast.Type.NARRATOR_TOGGLE,
                                Text.of("DyDanmaku"),
                                Text.of("已断开直播间连接")
                        )
                );
            } else {
                // 连接逻辑
                live_id = liveIdInput.getText().toString();
                LOGGER.info("开始连接直播间: " + live_id);
                //this.client.getServer().getCommandManager().execute(this.client.getServer().getCommandSource(), "/dydanmaku connect " + live_id);

                // 获取命令解析器
                CommandDispatcher<ServerCommandSource> dispatcher = this.client.getServer().getCommandManager().getDispatcher();

                // 创建 ParseResults 对象
                ParseResults<ServerCommandSource> parseResults = dispatcher.parse(
                        "dydanmaku connect " + live_id,
                        this.client.getServer().getPlayerManager().getPlayer(this.client.player.getUuid()).getCommandSource()
                );

                this.client.getServer().getCommandManager().execute(parseResults, "/dydanmaku connect " + live_id);


                // 显示反馈
                this.client.getToastManager().add(
                        SystemToast.create(
                                this.client,
                                SystemToast.Type.NARRATOR_TOGGLE,
                                Text.of("DyDanmaku"),
                                Text.of("开始连接直播间: " + live_id)
                        )
                );
            }
        }
        ).dimensions(170, 70, 60, 20).build();





        this.addDrawableChild(connectButton);


    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //GUI标题
        context.drawText(this.textRenderer, "DyDanmaku", 40, 40 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);

        //弹幕服务器已连接界面
        if (websocket.params != null && websocket.isConnected()) {
            liveIdInput.setX(50 + 40 + 10);
            connectButton.setX(50 + 170 + 10);
            connectButton.setMessage(Text.of("§a已连接§f"));
            if(connectButton.isHovered()){
                connectButton.setMessage(Text.of("§4断开§f"));
            }
            context.drawText(this.textRenderer, "直播间状态: 已连接", 40, 50 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
            context.drawText(this.textRenderer, "直播间标题：" + websocket.params.get("live_title"), 40, 60 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
            Identifier avatar_loading = Identifier.of("dydanmaku", "textures/gui/sprite/loading.png");

            if (new File(ConfigDirPath + "/" + websocket.params.get("roomId") + "_avatar.png").exists())
            {
                Identifier avatar = Identifier.of("dydanmaku:avatar");
                context.drawTexture(avatar, 40, 70 - this.textRenderer.fontHeight - 10, 0, 0, 50, 50, 50, 50);
            } else {
                //主播头像加载中的动图
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFrameTime > 33) { // 每30ms切换一帧
                    currentFrame = (currentFrame + 1) % 30;
                    lastFrameTime = currentTime;
                }
                context.drawTexture(avatar_loading, 40, 70 - this.textRenderer.fontHeight - 10, 0, (currentFrame - 1) * 50, 50, 50, 50, 1500);
            }
            context.drawText(this.textRenderer, "主播：" + websocket.params.get("nickname"), 50 + 40 + 10, 70 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
            DanmakuScrollBox.visible = true;
        } else {
            liveIdInput.setX(40);
            connectButton.setX(170);
            connectButton.setMessage(Text.of("连接"));
            context.drawText(this.textRenderer, "直播间状态: 未连接", 40, 50 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);

            DanmakuScrollBox.visible = false;
        }
    }

    //重复按下热键关闭GUI
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (DyDanmakuKey.matchesKey(keyCode,scanCode)) {
            close();
            return true;
        }
        if (liveIdInput.keyPressed(keyCode, scanCode, modifiers)) {
            if (liveIdInput.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
                    // 实现光标位置移动逻辑
                    return true;
                }
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    //GUI打开时不暂停
    @Override
    public boolean shouldPause(){
        return false;
    }

    // 自定义滚动组件
    class DanmakuWidget extends TextFieldWidget {
        private final int x, y, width, height;
        private final List<String> lines = new ArrayList<>();
        private int maxLines;
        public int scrollOffset = 0;
        private boolean draggingScrollBar = false;
        private double dragStartY;
        private double dragStartScrollOffset;
        private static final int SCROLL_BAR_WIDTH = 5;
        float thumbHeight;
        int thumbY;

        public DanmakuWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
            super(textRenderer, x, y, width, height, text);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.maxLines = calculateMaxLines();
        }

        private int getMaxScroll() {
            return Math.max(0, lines.size() - maxLines);
        }

        private int getMaxScrollOffset() {
            return getMaxScroll() * textRenderer.fontHeight;
        }

        private int calculateMaxLines() {
            return (height - 4) / textRenderer.fontHeight; // 留出 2px 的上下边距
        }

        public void setText(String text) {
            lines.clear();
            Collections.addAll(lines, text.split("\n"));
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            //裁剪管理
            context.enableScissor(x, y, x + width, y + height);

            // 绘制背景
            context.fill(x, y, x + width, y + height, 0x80000000);

            // 计算可见行范围
            setText(websocket.handler.DanmakuList.toString());
            int startLine =lines.size() - (scrollOffset / textRenderer.fontHeight);
            int endLine = Math.max((startLine - maxLines - 2), 0);

            // 绘制可见文本行
            for (int i = 1; startLine - i >= endLine; i++) {
                int yPos = y + height - (i * textRenderer.fontHeight)  + Math.floorMod(scrollOffset, textRenderer.fontHeight);
                context.drawText(textRenderer, lines.get(startLine - i), x + 2, yPos, 0xFFFFFFFF, false);
            }

            // 绘制滚动条
            if (lines.size() > maxLines) {
                drawScrollBar(context);
            }
            if(isHovered()){
//                if (this.mouseScrolled(mouseX, mouseY, 0, 1)){
//                    LOGGER.info("滚动");
//                }
                //LOGGER.info("鼠标在弹幕滚动组件上");
            }

            context.disableScissor();
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if(this.isHovered()){
                scrollOffset = Math.min(Math.max(scrollOffset + (int) verticalAmount * 2, 0), getMaxScrollOffset());
                //LOGGER.info("scrollOffset:" + scrollOffset);
                return true;
            }
            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }

        private void drawScrollBar(DrawContext context) {
            int maxScrollOffset = getMaxScrollOffset();
            int scrollBarX = x + width - SCROLL_BAR_WIDTH;

            // 滚动条背景
            context.fill(scrollBarX, y, scrollBarX + SCROLL_BAR_WIDTH, y + height, 0x80000000);

            // 计算滑块参数
            thumbHeight = Math.max(10, height * (maxLines / (float) lines.size()));
            float scrollRatio =  ((float)scrollOffset / maxScrollOffset);
            thumbY = y + (int) (height - thumbHeight) - (int) (scrollRatio * (height - thumbHeight));

            // 绘制滑块
            context.fill(scrollBarX, thumbY,
                    scrollBarX + SCROLL_BAR_WIDTH,
                    thumbY + (int) thumbHeight,
                    0xFF808080);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && isMouseOver(mouseX, mouseY)) {
                int scrollBarX = x + width - SCROLL_BAR_WIDTH;
                if (mouseX >= scrollBarX && mouseX <= scrollBarX + SCROLL_BAR_WIDTH) {
                    draggingScrollBar = true;
                    dragStartY = mouseY;
                    dragStartScrollOffset = scrollOffset;
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (draggingScrollBar) {
                int maxScrollOffset = getMaxScrollOffset();
                if (maxScrollOffset == 0) {
                    return true;
                }


                float deltaYRatio = (float) ((dragStartY - mouseY)  / (height - thumbHeight));
                scrollOffset = (int) Math.max(0, Math.min(dragStartScrollOffset + deltaYRatio *  maxScrollOffset, maxScrollOffset));
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            draggingScrollBar = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    // 自定义输入框组件
    class LiveIdInputWidget extends TextFieldWidget {
        private static final int BLINK_INTERVAL = 300;
        private long lastBlinkTime = 0;
        private boolean showCursor = true;

        public LiveIdInputWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
            super(textRenderer, x, y, width, height, text);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // 更新光标状态
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBlinkTime > BLINK_INTERVAL) {
                showCursor = !showCursor;
                lastBlinkTime = currentTime;
            }

            // 绘制边框
            int borderColor = this.isFocused() ? 0xFFFFFFFF : 0xFF666666;
            context.fill(this.getX() -1, this.getY() -1, this.getX() +this.width+1, this.getY() +this.height+1, borderColor);

            // 绘制背景
            context.fill(this.getX(), this.getY(), this.getX() +this.width, this.getY() +this.height, 0xFF333333);

            // 绘制提示文字
            if(websocket.isConnected()){
                this.setFocused(false);
                context.drawTextWithShadow(
                        textRenderer,
                        Text.of(websocket.params.get("live_id")),
                        this.getX() + 4,
                        this.getY() + (this.height - 8) / 2,
                        0xFFA0A0A0
                );
            } else {
                if (this.getText().isEmpty() && !this.isFocused()) {
                    context.drawTextWithShadow(
                            textRenderer,
                            this.getMessage(),
                            this.getX() + 4,
                            this.getY() + (this.height - 8) / 2,
                            0xFFA0A0A0
                    );
                }

            }

            // 绘制输入内容
            String visibleText = textRenderer.trimToWidth(
                    this.getText(),
                    this.width - 8
            );
            int textX = this.getX() + 4;
            int textY = this.getY() + (this.height - 8) / 2;
            context.drawTextWithShadow(
                    textRenderer,
                    visibleText,
                    textX,
                    textY,
                    0xFFFFFFFF
            );

            // 绘制光标（当获得焦点时）
            if (this.isFocused() && showCursor) {
                int cursorX = textX;
                if (!visibleText.isEmpty()) {
                    //cursorX += textRenderer.getWidth(visibleText);
                    cursorX += textRenderer.getWidth(visibleText.substring(0, Math.min(visibleText.length(), this.getCursor())));
                }
                context.fill(
                        cursorX, textY - 1,
                        cursorX + 1, textY + 9,
                        0xFFFFFFFF
                );
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height) {
                    this.setFocused(true);
                    return true;
            } else {
                this.setFocused(false);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

    }


    // 在gui类中添加输入处理
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (liveIdInput.isFocused()) {
            if (chr == 13) { // 回车键确认
                connectButton.onPress();
                return true;
            }
            return liveIdInput.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }


    public void avatar_register(String path) throws IOException {
        File avatar_File = new File(path);
        NativeImage image = NativeImage.read(new FileInputStream(path));

        // 创建动态纹理
        //Identifier avatarId = Identifier.of("dydanmaku");
        //DynamicTexture avatarTex = DynamicTexture.save(image, avatarId);
        NativeImageBackedTexture avatarTexture = new NativeImageBackedTexture(image);
        MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("dydanmaku:avatar"), avatarTexture);
    }
}
