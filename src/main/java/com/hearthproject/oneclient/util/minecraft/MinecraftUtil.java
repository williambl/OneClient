package com.hearthproject.oneclient.util.minecraft;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.minecraft.GameVersion;
import com.hearthproject.oneclient.json.models.minecraft.launcher.LauncherProfile;
import com.hearthproject.oneclient.util.forge.ForgeUtils;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MinecraftUtil {

	// http://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.jar
	// https://launcher.mojang.com/download/MinecraftInstaller.msi

	private static GameVersion version = null;

	public static GameVersion loadGameVersion() throws Exception {
		if (version == null) {
			String data = IOUtils.toString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), StandardCharsets.UTF_8);
			version = JsonUtil.GSON.fromJson(data, GameVersion.class);
			return version;
		}
		return version;
	}

	public static void loadMC(Instance instance) throws Throwable {
		File launcher = new File(Constants.RUNDIR, "launcher.jar");
		File mcDir = new File(Constants.RUNDIR, "minecraft");
		if (!launcher.exists()) { //TODO check hash
			OneClientLogging.log("Downloading minecraft launcher");
			FileUtils.copyURLToFile(new URL("http://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.jar"), launcher);
		}

		OneClientLogging.log("Creating launcher json");
		String versionID = instance.minecraftVersion;
		if (instance.modLoaderVersion != null && !instance.modLoaderVersion.isEmpty()) {
			versionID = versionID + "-" + instance.modLoader.toLowerCase() + instance.minecraftVersion + "-" + instance.modLoaderVersion;
		}
		checkLauncherProfiles(mcDir, new LauncherProfile.Profile(instance.name, versionID));

		if (instance.modLoaderVersion != null && !instance.modLoaderVersion.isEmpty()) {
			OneClientLogging.log("Downloading forge installer");
			ForgeUtils.installForge(mcDir, instance.minecraftVersion + "-" + instance.modLoaderVersion);
		}

		new Thread(() -> {
			try {
				ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", launcher.getAbsolutePath(), "--workDir", mcDir.getAbsolutePath());
				Process process = processBuilder.start();

				BufferedReader reader =
					new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					OneClientLogging.log(line);
				}
			} catch (Throwable throwable) {
				OneClientLogging.log(throwable);
			}
		}).start();

	}

	public static void checkLauncherProfiles(File mcDir, LauncherProfile.Profile profile) throws Exception {
		File launcherProfiles = new File(mcDir, "launcher_profiles.json");

		LauncherProfile launcherProfile;
		if (launcherProfiles.exists()) {
			launcherProfile = JsonUtil.GSON.fromJson(FileUtils.readFileToString(launcherProfiles, StandardCharsets.UTF_8), LauncherProfile.class);
		} else {
			launcherProfile = new LauncherProfile();
		}

		if (launcherProfile.profiles == null) {
			launcherProfile.profiles = new HashMap<>();
		}
		if (launcherProfile.profiles.containsKey(profile.name)) {
			launcherProfile.profiles.get(profile.name).lastVersionId = profile.lastVersionId;
		} else {
			launcherProfile.profiles.put(profile.name, profile);
		}

		launcherProfile.selectedProfile = profile.name;

		FileUtils.writeStringToFile(launcherProfiles, JsonUtil.GSON.toJson(launcherProfile));
	}

}
