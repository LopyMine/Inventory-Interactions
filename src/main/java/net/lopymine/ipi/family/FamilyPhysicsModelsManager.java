package net.lopymine.ipi.family;

import java.util.*;
import java.util.regex.Pattern;
import net.lopymine.ip.client.command.tags.TagsCommand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

public class FamilyPhysicsModelsManager {

	private static final Pattern KEYS_SEPARATOR = Pattern.compile("[/_]+");

	public static List<FamilyPhysicsModelConfig> get(Item item) {
		List<FamilyPhysicsModelConfig> data = getFamiliesByItemData(item);
		data.add(FamilyPhysicsModelsConfigsManager.getInstance().getFallbackConfig());
		return data;
	}

	@NotNull
	private static List<FamilyPhysicsModelConfig> getFamiliesByItemData(Item item) {
		Identifier id = BuiltInRegistries.ITEM.getKey(item);

		ArrayList<FamilyPhysicsModelConfig> configs = new ArrayList<>();
		for (FamilyPhysicsModelConfig config : FamilyPhysicsModelsConfigsManager.getInstance().getRegisteredConfigs()) {
			if (matchSpecialMarkers(item, config.getSpecialMarkers().getBlacklist())) {
				continue;
			}
			if (matchKeywords(id, config.getKeywords().getBlacklist())) {
				continue;
			}
			if (matchTags(id, config.getTags().getBlacklist())) {
				continue;
			}
			if (matchNamespaces(id, config.getNamespaces().getBlacklist())) {
				continue;
			}

			if (matchSpecialMarkers(item, config.getSpecialMarkers().getWhitelist())
					|| matchKeywords(id, config.getKeywords().getWhitelist())
					|| matchTags(id, config.getTags().getWhitelist())
					|| matchNamespaces(id, config.getNamespaces().getWhitelist())) {
				configs.add(config);
			}
		}

		return configs;
	}

	private static boolean matchSpecialMarkers(Item item, ArrayList<String> list) {
		for (String marker : list) {
			if (marker.equals("block")) {
				return item instanceof BlockItem;
			}
		}
		return false;
	}

	private static boolean matchKeywords(Identifier itemId, ArrayList<String> list) {
		String path = itemId.getPath();
		String[] keys = KEYS_SEPARATOR.split(path);

		for (String key : keys) {
			if (list.contains(key)) {
				return true;
			}
		}

		for (String keyword : list) {
			if (keyword.startsWith("@") && path.contains(keyword.substring(1))) {
				return true;
			}
		}

		return false;
	}

	private static boolean matchTags(Identifier itemId, ArrayList<String> list) {
		List<String> tags = TagsCommand.getTags(itemId);
		if (tags == null) {
			return false;
		}

		for (String tag : tags) {
			if (list.contains(tag)) {
				return true;
			}
		}

		for (String entry : list) {
			if (!entry.startsWith("@")) {
				continue;
			}
			String part = entry.substring(1);
			for (String tag : tags) {
				if (tag.contains(part)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean matchNamespaces(Identifier itemId, ArrayList<String> list) {
		String namespace = itemId.getNamespace();

		if (list.contains(namespace)) {
			return true;
		}

		for (String n : list) {
			if (n.startsWith("@") && namespace.contains(n.substring(1))) {
				return true;
			}
		}

		return false;
	}

}
