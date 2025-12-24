package ninja.trek.mute;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowFocusCallback;

public class MutesoundwhenawayClient implements ClientModInitializer {
	private static double savedVolume = -1.0;

	@Override
	public void onInitializeClient() {
		// Wait for game to be ready
		new Thread(() -> {
			try {
				Thread.sleep(1000);
				setupFocusCallback();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void setupFocusCallback() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getWindow() == null) return;

		Window window = client.getWindow();

		// Access the GLFW window handle - try reflection if needed
		try {
			long windowHandle = (long) Window.class.getDeclaredField("window").get(window);

			GLFW.glfwSetWindowFocusCallback(windowHandle, new GLFWWindowFocusCallback() {
				@Override
				public void invoke(long window, boolean focused) {
					if (focused) {
						onWindowGainedFocus();
					} else {
						onWindowLostFocus();
					}
				}
			});
		} catch (Exception e) {
			Mutesoundwhenaway.LOGGER.error("Failed to setup window focus callback", e);
		}
	}

	private void onWindowLostFocus() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.options == null) return;

		OptionInstance<Double> masterVolume = client.options.getSoundSourceOptionInstance(SoundSource.MASTER);
		savedVolume = masterVolume.get();
		masterVolume.set(0.0);
		Mutesoundwhenaway.LOGGER.info("Window lost focus - muting audio (saved volume: {})", savedVolume);
	}

	private void onWindowGainedFocus() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.options == null) return;

		if (savedVolume >= 0.0) {
			OptionInstance<Double> masterVolume = client.options.getSoundSourceOptionInstance(SoundSource.MASTER);
			masterVolume.set(savedVolume);
			Mutesoundwhenaway.LOGGER.info("Window gained focus - restoring audio to volume: {}", savedVolume);
			savedVolume = -1.0;
		}
	}
}