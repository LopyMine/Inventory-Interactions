package net.lopymine.mossy.settings.api;

import com.google.gson.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public class ModrinthDependenciesAPI {

	@NotNull
	public static String getVersion(String modId, String minecraftVersion, String loader) {
		String encodedLoader = URLEncoder.encode("[\"%s\"]".formatted(loader), StandardCharsets.UTF_8);
		String encodedMinecraftVersion = URLEncoder.encode("[\"%s\"]".formatted(minecraftVersion), StandardCharsets.UTF_8);
		String url = "https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s".formatted(modId, encodedLoader, encodedMinecraftVersion);
		JsonElement element;
		try {
			element = JsonHelper.get(url);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("\nFailed to find Modrinth project with id \"%s\"".formatted(modId), e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		JsonArray array = element.getAsJsonArray();
		JsonElement jsonElement = array.get(0);
		JsonObject jsonObject = jsonElement.getAsJsonObject();

		return jsonObject.get("version_number").getAsString();
	}

}
