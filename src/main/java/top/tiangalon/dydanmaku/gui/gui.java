package top.tiangalon.dydanmaku.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.Font;
//import net.minecraft.client.font.TextRenderer;



import net.minecraft.client.gui.screens.Screen;

import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.widget.ButtonWidget;

//import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.components.EditBox;

import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;

//import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

//? if >= 1.21.2 && < 1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
//? if >=1.21.5 {
//?}
//? if >= 1.21.6 {
    //import net.minecraft.client.gl.RenderPipelines;
    import net.minecraft.client.renderer.RenderPipelines;
    //? if >= 1.21.9 {
        /*//import net.minecraft.client.input.KeyInput;
        import net.minecraft.client.input.KeyEvent;

        import net.minecraft.client.input.MouseButtonEvent;
        //import net.minecraft.client.gui.Click;

        import net.minecraft.client.input.CharacterEvent;
        //import net.minecraft.client.input.CharInput;
        //? if >= 26.1 {
        /^import net.minecraft.client.gui.GuiGraphicsExtractor;
        ^///?}
    *///?}
//?}

//? if < 1.21.11{
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.Identifier;
*///?}
//? if < 26.1{
//import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.GuiGraphics;
//?}

import org.jetbrains.annotations.UnknownNullability;
import top.tiangalon.dydanmaku.net.WebSocketClientNetty;

import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private EditBox liveIdInput;
    private Button connectButton;



    // 动态头像纹理相关
    private DynamicTexture avatarTexture = null;
    //? if < 1.21 {
    /*private static final ResourceLocation AVATAR_ID = ResourceLocation.tryParse("dydanmaku:avatar");
    *///?} else if < 1.21.11 {
    private static final ResourceLocation AVATAR_ID = ResourceLocation.parse("dydanmaku:avatar");
    //?} else {
    /*private static final Identifier AVATAR_ID = Identifier.parse("dydanmaku:avatar");
    *///?}
    private boolean avatarRegistered = false;
    private Font font;

    public gui(MutableComponent title , WebSocketClientNetty websocket) {
        super(title);
        this.websocket = websocket;
    }

    @Override
    protected void init() {
        super.init();
        this.font = this.minecraft.font;
        DanmakuScrollBox = new DanmakuWidget(this.font, 40, 110, width - 80, height - 110 - 10, Component.literal("直播间实时弹幕"));
        DanmakuScrollBox.setEditable(true);
        DanmakuScrollBox.isActive();
        //DanmakuScrollBox.setText(websocket.handler.DanmakuList.toString());
        this.addRenderableWidget(DanmakuScrollBox);
        DanmakuScrollBox.visible = false;

        // 创建直播间ID输入框
        liveIdInput = new LiveIdInputWidget(this.font, 40, 70, 120, 20, Component.literal("输入直播间ID"));
        liveIdInput.setMaxLength(20);
        liveIdInput.setEditable(true);
        this.addRenderableWidget(liveIdInput);

        // 创建连接按钮
        connectButton = Button.builder(Component.literal("连接"), btn -> {
            connectButton.setMessage(Component.literal("连接中"));
            if (websocket.isConnected()){
                try {
                    websocket.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                /*
                this.minecraft.getToasts().getToastManager().addToast(
                        SystemToast.multiline(
                                this.minecraft,
                                SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                Component.literal("DyDanmaku"),
                                Component.literal("已断开直播间连接")
                        )
                );*/
                //? if < 1.21.2 {
                /*this.minecraft.getToasts().addToast(
                *///?} else {
                this.minecraft.getToastManager().addToast(
                //?}
                        SystemToast.multiline(
                                this.minecraft,
                                //? if < 1.20.3 {
                                /*SystemToast.SystemToastIds.NARRATOR_TOGGLE,
                                *///?} else {
                                SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                //?}
                                Component.literal("DyDanmaku"),
                                Component.literal("已断开直播间连接")
                        )
                );
            } else {
                // 连接逻辑
                live_id = liveIdInput.getValue().toString();
                LOGGER.info("开始连接直播间: " + live_id);
                //this.minecraft.getServer().getCommandManager().execute(this.minecraft.getServer().getCommandSource(), "/dydanmaku connect " + live_id);

                // 获取命令解析器
                CommandDispatcher<CommandSourceStack> dispatcher = this.minecraft.getSingleplayerServer().getCommands().getDispatcher();

                // 创建 ParseResults 对象
                ParseResults<CommandSourceStack> parseResults = dispatcher.parse(
                        "dydanmaku connect " + live_id,
                        //this.minecraft.getServer().getPlayerManager().getPlayer(this.minecraft.player.getUuid()).getCommandSource()
                        this.minecraft.getSingleplayerServer().getPlayerList().getPlayer(this.minecraft.player.getUUID()).createCommandSourceStack()
                );

                this.minecraft.getSingleplayerServer().getCommands().performCommand(parseResults, "/dydanmaku connect " + live_id);


                // 显示反馈
                /*this.minecraft.getToastManager().addToast(
                        SystemToast.multiline(
                                this.minecraft,
                                SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                Component.literal("DyDanmaku"),
                                Component.literal("开始连接直播间: " + live_id)
                        )
                );*/
                //? if < 1.21.2 {
                /*this.minecraft.getToasts().addToast(
                *///?} else {
                this.minecraft.getToastManager().addToast(
                //?}
                        SystemToast.multiline(
                                this.minecraft,
                                //? if < 1.20.3 {
                                /*SystemToast.SystemToastIds.NARRATOR_TOGGLE,
                                 *///?} else {
                                SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                //?}
                                Component.literal("DyDanmaku"),
                                Component.literal("开始连接直播间: " + live_id)
                        )
                );
            }
        }
        ).bounds(170, 70, 60, 20).build();





        this.addRenderableWidget(connectButton);


    }

    @Override
    //? if < 26.1 {
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
    //?} else {
    /*public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    *///?}

        //GUI标题
        //? if < 26.1 {
        graphics.drawString(this.font, "DyDanmaku", 40, 40 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
        //?} else {
        /*graphics.text(this.font, "DyDanmaku", 40, 40 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
        *///?}

        //弹幕服务器已连接界面
        if (websocket.params != null && websocket.isConnected()) {
            liveIdInput.setX(50 + 40 + 10);
            connectButton.setX(50 + 170 + 10);
            connectButton.setMessage(Component.literal("§a已连接§f"));
            if(connectButton.isHovered()){
                connectButton.setMessage(Component.literal("§4断开§f"));
            }
            //? if < 26.1 {
            graphics.drawString(this.font, "直播间状态: 已连接", 40, 50 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            graphics.drawString(this.font, "直播间标题：" + websocket.params.get("live_title"), 40, 60 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            //?} else {
            /*graphics.text(this.font, "直播间状态: 已连接", 40, 50 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            graphics.text(this.font, "直播间标题：" + websocket.params.get("live_title"), 40, 60 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            *///?}
            //? if < 1.21.11 {
            ResourceLocation avatar_loading = ResourceLocation.tryBuild("dydanmaku", "textures/gui/sprite/loading.png");
            //?} else {
            /*Identifier avatar_loading = Identifier.fromNamespaceAndPath("dydanmaku", "textures/gui/sprite/loading.png");
            *///?}

            // 使用动态纹理方式绘制头像
            if (avatarRegistered) {
                //? if < 1.21.2 {
                /*graphics.blit(AVATAR_ID, 40, 70 - this.font.lineHeight - 10, 0, 0, 50, 50, 50, 50);
                *///?} else if <1.21.6 {
                /*graphics.blit(RenderType::guiTextured, AVATAR_ID, 40, 70 - this.font.lineHeight - 10, 0, 0, 50, 50, 50, 50);
                *///? } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, AVATAR_ID, 40, 70 - this.font.lineHeight - 10, 0, 0, 50, 50, 50, 50);
                //?}
            } else {
                //主播头像加载中的动图
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFrameTime > 33) { // 每30ms切换一帧
                    currentFrame = (currentFrame + 1) % 30;
                    lastFrameTime = currentTime;
                }
                //? if < 1.21.2 {
                /*graphics.blit(avatar_loading, 40, 70 - this.font.lineHeight - 10, 0, (currentFrame - 1) * 50, 50, 50, 50, 1500);
                 *///?} else if <1.21.6 {
                /*graphics.blit(RenderType::guiTextured, avatar_loading, 40, 70 - this.font.lineHeight - 10, 0, (currentFrame - 1) * 50, 50, 50, 50, 1500);
                *///? } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, avatar_loading, 40, 70 - this.font.lineHeight - 10, 0, (currentFrame - 1) * 50, 50, 50, 50, 1500);
                //?}
            }
            //? if < 26.1 {
            graphics.drawString(this.font, "主播：" + websocket.params.get("nickname"), 50 + 40 + 10, 70 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            //? } else {
            /*graphics.text(this.font, "主播：" + websocket.params.get("nickname"), 50 + 40 + 10, 70 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            *///?}
            DanmakuScrollBox.visible = true;
        } else {
            liveIdInput.setX(40);
            connectButton.setX(170);
            connectButton.setMessage(Component.literal("连接"));
            //? if < 26.1 {
            graphics.drawString(this.font, "直播间状态: 未连接", 40, 50 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            //? } else {
            /*graphics.text(this.font, "直播间状态: 未连接", 40, 50 - this.font.lineHeight - 10, 0xFFFFFFFF, true);
            *///?}

            DanmakuScrollBox.visible = false;
        }
    }

    //重复按下热键关闭GUI，按回车连接直播间
    @Override
    //? if < 1.21.9 {
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (DyDanmakuKey.matches(keyCode,scanCode)) {
            onClose();
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
    //?} else {
    /*public boolean keyPressed(KeyEvent keyEvent) {
        int keycode = keyEvent.key();
        //LOGGER.info("[DyDanmaku]keycode: " + keycode);
        if (DyDanmakuKey.matches(keyEvent)) {
            onClose();
            return true;
        }
        if (liveIdInput.isFocused()) {
            if(keycode == GLFW.GLFW_KEY_ENTER) {
                connectButton.onPress(null);
                return true;
            }
            if (liveIdInput.keyPressed(keyEvent)) {
                return true;
            }
        }

        return super.keyPressed(keyEvent);
    }
    *///?}

    @Override
    //? if < 1.21.9 {
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (liveIdInput.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    //?} else {
    /*public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!liveIdInput.isHovered()) {
            liveIdInput.setFocused(false);
        }
        return super.mouseClicked(click, doubled);
    }
    *///?}

    //GUI打开时不暂停
    @Override
    public boolean isPauseScreen(){
        return false;
    }

    // GUI关闭时清理头像资源
//    @Override
//    public void close() {
//        LOGGER.info("[DyDanmaku]已清理头像");
//        cleanupAvatar();
//        super.close();
//    }

    // 自定义滚动组件
    class DanmakuWidget extends EditBox {
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

        public DanmakuWidget(Font font, int x, int y, int width, int height, MutableComponent text) {
            super(font, x, y, width, height, text);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            //this.maxLines = (height - 4) / font.lineHeight;
        }

        private int getMaxScroll() {
            return Math.max(0, lines.size() - getMaxLines());
        }

        private int getMaxScrollOffset() {
            return getMaxScroll() * font.lineHeight;
        }

        private int getMaxLines() {
            return (height - 4) / font.lineHeight; // 留出 2px 的上下边距
        }

        public void setText(String text) {
            lines.clear();
            Collections.addAll(lines, text.split("\n"));
        }

        @Override
        //? if < 1.20.3 {
        /*public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
         *///?} else if < 26.1{
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        //?} else {
        /*public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        *///?}
            //裁剪管理
            graphics.enableScissor(x, y, x + width, y + height);

            // 绘制背景
            graphics.fill(x, y, x + width, y + height, 0x80000000);

            // 计算可见行范围
            setText(websocket.handler.DanmakuList.toString());
            int startLine = lines.size() - (scrollOffset / font.lineHeight);
            int endLine = Math.max((startLine - getMaxLines() - 2), 0);

            // 绘制可见文本行
            for (int i = 1; startLine - i >= endLine; i++) {
                int yPos = y + height - (i * font.lineHeight) + Math.floorMod(scrollOffset, font.lineHeight);
                //? if < 26.1 {
                graphics.drawString(font, lines.get(startLine - i), x + 2, yPos, 0xFFFFFFFF, false);
                //?} else {
                /*graphics.text(font, lines.get(startLine - i), x + 2, yPos, 0xFFFFFFFF, false);
                *///?}
            }

            // 绘制滚动条
            if (lines.size() > getMaxLines()) {
                drawScrollBar(graphics);
            }
            if (isHovered()) {
//                if (this.mouseScrolled(mouseX, mouseY, 0, 1)){
//                    LOGGER.info("滚动");
//                }
                //LOGGER.info("鼠标在弹幕滚动组件上");
            }

            graphics.disableScissor();
        }

        @Override
        //? if < 1.20.2 {
        /*public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        *///?} else {
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            double amount = verticalAmount;
        //?}
            if (this.isHovered()) {
                scrollOffset = Math.max(0, Math.min(scrollOffset + (int) amount * 2, getMaxScrollOffset()));
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

        //? if < 26.1 {
        private void drawScrollBar(GuiGraphics graphics) {
        //?} else {
        /*private void drawScrollBar(@UnknownNullability GuiGraphicsExtractor graphics) {
        *///?}
            int maxScrollOffset = getMaxScrollOffset();
            int scrollBarX = x + width - SCROLL_BAR_WIDTH;

            // 滚动条背景
            graphics.fill(scrollBarX, y, scrollBarX + SCROLL_BAR_WIDTH, y + height, 0x80000000);

            // 计算滑块参数
            thumbHeight = Math.max(10, height * (getMaxLines() / (float) lines.size()));
            float scrollRatio = ((float) scrollOffset / maxScrollOffset);
            thumbY = y + (int) (height - thumbHeight) - (int) (scrollRatio * (height - thumbHeight));

            // 绘制滑块
            graphics.fill(scrollBarX, thumbY,
                    scrollBarX + SCROLL_BAR_WIDTH,
                    thumbY + (int) thumbHeight,
                    0xFF808080);
        }

        @Override
                //? if < 1.21.9 {
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
        //?} else {
        /*public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (isMouseOver(click.x(), click.y())) {
                double mouseX = click.x();
                double mouseY = click.y();
                int scrollBarX = x + width - SCROLL_BAR_WIDTH;
                if (mouseX >= scrollBarX && mouseX <= scrollBarX + SCROLL_BAR_WIDTH) {
                    draggingScrollBar = true;
                    dragStartY = mouseY;
                    dragStartScrollOffset = scrollOffset;
                    return true;
                }
            }
            return super.mouseClicked(click, doubled);
        }
        *///?}

        @Override
        //? if < 1.21.9 {
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (draggingScrollBar) {
                int maxScrollOffset = getMaxScrollOffset();
                if (maxScrollOffset == 0) {
                    return true;
                }


                float deltaYRatio = (float) ((dragStartY - mouseY) / (height - thumbHeight));
                scrollOffset = (int) Math.max(0, Math.min(dragStartScrollOffset + deltaYRatio * maxScrollOffset, maxScrollOffset));
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        //?} else {
        /*public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
            double mouseX = click.x();
            double mouseY = click.y();
            if (draggingScrollBar) {
                int maxScrollOffset = getMaxScrollOffset();
                if (maxScrollOffset == 0) {
                    return true;
                }
                float deltaYRatio = (float) ((dragStartY - mouseY) / (height - thumbHeight));
                scrollOffset = (int) Math.max(0, Math.min(dragStartScrollOffset + deltaYRatio * maxScrollOffset, maxScrollOffset));
            }
            return super.mouseDragged(click, offsetX, offsetY);
        }
        *///?}

        @Override
        //? if < 1.21.9 {
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            draggingScrollBar = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }
        //?} else {
        /*public boolean mouseReleased(MouseButtonEvent click) {
            draggingScrollBar = false;
            return super.mouseReleased(click);
        }
        *///?}
    }

    // 自定义输入框组件
    class LiveIdInputWidget extends EditBox {
        private static final int BLINK_INTERVAL = 300;
        private long lastBlinkTime = 0;
        private boolean showCursor = true;

        public LiveIdInputWidget(Font font, int x, int y, int width, int height, MutableComponent text) {
            super(font, x, y, width, height, text);
        }

        @Override
        //? if < 1.20.3 {
        /*public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        *///?} else if < 26.1{
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        //?} else {
        /*public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        *///?}
            // 更新光标状态
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBlinkTime > BLINK_INTERVAL) {
                showCursor = !showCursor;
                lastBlinkTime = currentTime;
            }

            // 绘制边框
            int borderColor = this.isFocused() ? 0xFFFFFFFF : 0xFF666666;
            graphics.fill(this.getX() -1, this.getY() -1, this.getX() +this.width+1, this.getY() +this.height+1, borderColor);

            // 绘制背景
            graphics.fill(this.getX(), this.getY(), this.getX() +this.width, this.getY() +this.height, 0xFF333333);

            // 绘制提示文字
            if(websocket.isConnected()){
                this.setFocused(false);
                //? if < 26.1 {
                graphics.drawString(
                //?} else {
                /*graphics.text(
                *///?}
                        font,
                        Component.literal(websocket.params.get("live_id")),
                        this.getX() + 4,
                        this.getY() + (this.height - 8) / 2,
                        0xFFA0A0A0
                );
            } else {
                if (this.getValue().isEmpty() && !this.isFocused()) {
                    //? if < 26.1 {
                    graphics.drawString(
                    //?} else {
                    /*graphics.text(
                    *///?}
                            font,
                            this.getMessage(),
                            this.getX() + 4,
                            this.getY() + (this.height - 8) / 2,
                            0xFFA0A0A0
                    );
                }

            }

            // 绘制输入内容
            String visibleText = font.plainSubstrByWidth(
                    this.getValue(),
                    this.width - 8
            );
            int textX = this.getX() + 4;
            int textY = this.getY() + (this.height - 8) / 2;
            //? if < 26.1 {
            graphics.drawString(
            //?} else {
            /*graphics.text(
            *///?}
                    font,
                    visibleText,
                    textX,
                    textY,
                    0xFFFFFFFF
            );

            // 绘制光标（当获得焦点时）
            if (this.isFocused() && showCursor) {
                int cursorX = textX;
                if (!visibleText.isEmpty()) {
                    //cursorX += font.getWidth(visibleText);
                    cursorX += font.width(visibleText.substring(0, Math.min(visibleText.length(), this.getCursorPosition())));
                }
                graphics.fill(
                        cursorX, textY - 1,
                        cursorX + 1, textY + 9,
                        0xFFFFFFFF
                );
            }
        }

        @Override
        //? if < 1.21.9 {
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height) {
                    this.setFocused(true);
                    return true;
            } else {
                this.setFocused(false);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        //? } else {
        /*public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            double mouseX = click.x();
            double mouseY = click.y();
            //LOGGER.info("[DyDanmaku]LiveInputmouseClicked: " + mouseX + " " + mouseY);
            if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height) {
                this.setFocused(true);
                return true;
            } else {
                this.setFocused(false);
            }
            return super.mouseClicked(click, doubled);
        }
        *///? }

    }


    // 在gui类中添加输入处理
    @Override
    //? if < 1.21.9 {
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
        //? } else {
    /*public boolean charTyped(CharacterEvent input) {
        if (liveIdInput.isFocused()) {
            return liveIdInput.charTyped(input);
        }
        return super.charTyped(input);
    }
    *///?}



    public void avatar_register(String path) throws IOException {
        File avatar_File = new File(path);
        NativeImage image = NativeImage.read(new FileInputStream(path));

        // 在主渲染线程中创建纹理
        //MinecraftClient.getInstance().execute(() -> {
        Minecraft.getInstance().execute(() -> {
            // 创建动态纹理
            //Identifier avatarId = Identifier.parse("dydanmaku");
            //DynamicTexture avatarTex = DynamicTexture.save(image, avatarId);
            //? if >= 1.21.5 {
            avatarTexture = new DynamicTexture(() -> "dydanmaku:avatar", image);
            //?} else {
            /*avatarTexture = new DynamicTexture(image);
            *///?}
            //? if >= 1.21.4 {
            Minecraft.getInstance().getTextureManager().register(AVATAR_ID, avatarTexture);
            //?} else {
            /*Minecraft.getInstance().getTextureManager().register("dydanmaku:avatar", avatarTexture);
            *///?}
            avatarRegistered = true;
        });
    }

    // 清理资源
//    public void cleanupAvatar() {
//        if (avatarTexture != null) {
//            avatarTexture.close();
//            avatarTexture = null;
//            avatarRegistered = false;
//        }
//    }
}
