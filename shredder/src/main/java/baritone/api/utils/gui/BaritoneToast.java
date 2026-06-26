/*
 * This file is part of Baritone.
 */

package baritone.api.utils.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public class BaritoneToast implements Toast {

    private final long totalShowTime;
    private Visibility visibility = Visibility.SHOW;

    public BaritoneToast(String title, String subtitle, long totalShowTime) {
        this.totalShowTime = totalShowTime;
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager toastManager, long time) {
        this.visibility = time >= this.totalShowTime * toastManager.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE
                : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, Font font, long time) {
    }

    public static void addOrUpdate(ToastManager toast, String title, String subtitle, long totalShowTime) {
        toast.addToast(new BaritoneToast(title, subtitle, totalShowTime));
    }

    public static void addOrUpdate(Component title, Component subtitle) {
        ToastManager toast = net.minecraft.client.Minecraft.getInstance().gui.toastManager();
        addOrUpdate(toast, title.getString(), subtitle.getString(), baritone.api.BaritoneAPI.getSettings().toastTimer.value);
    }
}
