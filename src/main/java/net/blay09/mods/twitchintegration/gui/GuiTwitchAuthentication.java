package net.blay09.mods.twitchintegration.gui;

import net.blay09.mods.twitchintegration.TwitchIntegrationConfig;
import net.blay09.mods.twitchintegration.util.TwitchAPI;
import net.blay09.mods.twitchintegration.TwitchIntegration;
import net.blay09.mods.chattweaks.ChatTweaks;
import net.blay09.mods.chattweaks.auth.TokenPair;
import net.blay09.mods.chattweaks.balyware.gui.GuiPasswordField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiCheckBox;

import javax.annotation.Nullable;
import java.io.IOException;

public class GuiTwitchAuthentication extends GuiScreen {

    private static final ResourceLocation twitchLogo = new ResourceLocation(TwitchIntegration.MOD_ID, "twitch_logo.png");

    private final GuiScreen parentScreen;

    private GuiButton btnGetToken;
    private GuiPasswordField txtToken;
    private GuiButton btnConnect;

    public GuiTwitchAuthentication(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        super.initGui();

        btnGetToken = new GuiButton(0, width / 2 - 100, height / 2 - 25, 200, 20, TextFormatting.GREEN + I18n.format(TwitchIntegration.MOD_ID + ":gui.authentication.generateToken"));
        buttonList.add(btnGetToken);

        txtToken = new GuiPasswordField(1, mc, width / 2 - 100, height / 2 + 20, 200, 15);
        TokenPair tokenPair = ChatTweaks.getAuthManager().getToken(TwitchIntegration.MOD_ID);
        if (tokenPair != null) {
            txtToken.setText(tokenPair.getToken());
        }
        txtToken.setEnabled(!TwitchIntegrationConfig.useAnonymousLogin);

        GuiCheckBox chkAnonymous = new GuiCheckBox(2, width / 2 - 100, height / 2 + 45, I18n.format(TwitchIntegration.MOD_ID + ":gui.authentication.anonymousLogin"), TwitchIntegrationConfig.useAnonymousLogin) {
            @Override
            public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                if (super.mousePressed(mc, mouseX, mouseY)) {
                    txtToken.setEnabled(!isChecked());
                    TwitchIntegrationConfig.useAnonymousLogin = isChecked();
                    return true;
                }
                return false;
            }
        };
        buttonList.add(chkAnonymous);

        btnConnect = new GuiButton(3, width / 2, height / 2 + 65, 100, 20, I18n.format(TwitchIntegration.MOD_ID + ":gui.authentication.connect"));
        if (TwitchIntegration.getTwitchManager().isConnected()) {
            btnConnect.displayString = I18n.format(TwitchIntegration.MOD_ID + ":gui.authentication.disconnect");
        }
        buttonList.add(btnConnect);
    }

    @Override
    public void actionPerformed(@Nullable GuiButton button) throws IOException {
        if (button == btnConnect) {
            TokenPair tokenPair = ChatTweaks.getAuthManager().getToken(TwitchIntegration.MOD_ID);
            if (!TwitchIntegrationConfig.useAnonymousLogin && (tokenPair == null || !tokenPair.getToken().equals(txtToken.getText()) || tokenPair.getUsername() == null)) {
                mc.displayGuiScreen(new GuiTwitchWaitingForUsername());
                TwitchAPI.requestUsername(txtToken.getText(), () -> TwitchIntegration.getTwitchManager().connect());
            } else {
                if (TwitchIntegration.getTwitchManager().isConnected()) {
                    TwitchIntegration.getTwitchManager().disconnect();
                } else {
                    TwitchIntegration.getTwitchManager().connect();
                }
                mc.displayGuiScreen(parentScreen);
            }
        } else if (button == btnGetToken) {
            mc.displayGuiScreen(new GuiTwitchOpenToken(this, 0, TwitchAPI.getAuthenticationURL()));
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        this.txtToken.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        txtToken.mouseClicked(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        txtToken.updateCursorCounter();
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        super.confirmClicked(result, id);
        if (result) {
            if (id == 0) {
                if (TwitchAPI.listenForToken(() -> mc.displayGuiScreen(new GuiTwitchAuthentication(parentScreen)))) {
                    mc.displayGuiScreen(new GuiTwitchWaitingForToken());
                } else {
                    if (mc.currentScreen instanceof GuiTwitchOpenToken) {
                        ((GuiTwitchOpenToken) mc.currentScreen).enableBrowseErrorHints();
                    }
                }
            }
        } else {
            mc.displayGuiScreen(this);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final int windowHalfWidth = 150;
        final int windowHalfHeight = 100;
        drawRect(width / 2 - windowHalfWidth, height / 2 - windowHalfHeight, width / 2 + windowHalfWidth, height / 2 + windowHalfHeight, 0xDD000000);
        drawHorizontalLine(width / 2 - windowHalfWidth - 1, width / 2 + windowHalfWidth, height / 2 - windowHalfHeight - 1, 0xFFFFFFFF);
        drawHorizontalLine(width / 2 - windowHalfWidth - 1, width / 2 + windowHalfWidth, height / 2 + windowHalfHeight, 0xFFFFFFFF);
        drawVerticalLine(width / 2 - windowHalfWidth - 1, height / 2 - windowHalfHeight - 1, height / 2 + windowHalfHeight, 0xFFFFFFFF);
        drawVerticalLine(width / 2 + windowHalfWidth, height / 2 - windowHalfHeight - 1, height / 2 + windowHalfHeight, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(twitchLogo);
        drawModalRectWithCustomSizedTexture(width / 2 - 64, height / 2 - 80, 0, 0, 128, 43, 128, 43);
        drawString(mc.fontRenderer, I18n.format(TwitchIntegration.MOD_ID + ":gui.authentication.chatToken"), width / 2 - 100, height / 2 + 5, 0xFFFFFF);
        txtToken.drawTextBox();
    }

}
