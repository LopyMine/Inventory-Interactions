package net.lopymine.ipi.utils;

import net.minecraft.util.Identifier;

public class IdentifierUtils {

	public static String getFileName(Identifier identifier) {
		String path = identifier.getPath();
		int i = path.lastIndexOf("/");
		if (i == -1) {
			return path;
		}
		if (i + 1 >= path.length()) {
			return path;
		}
		return path.substring(i + 1);
	}

}
