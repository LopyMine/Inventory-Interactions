package net.lopymine.ipi.family.cache;

import java.util.*;
import lombok.*;
import net.lopymine.ip.family.generation.batch.RenderedItemImages;
import net.lopymine.ipi.family.*;
import net.minecraft.world.item.Item;

@Getter
@Setter
public class FamilyLinkCache {

	private final Map<Item, List<FamilyPhysicsModelConfig>> resolvedFamilyConfigs = new IdentityHashMap<>();
	private RenderedItemImages images = new RenderedItemImages();

	public void putResolvedFamilyConfigs(Item item, List<FamilyPhysicsModelConfig> family) {
		this.resolvedFamilyConfigs.put(item, family);
	}

	public List<FamilyPhysicsModelConfig> getResolvedFamilyConfigs(Item item) {
		List<FamilyPhysicsModelConfig> family = this.resolvedFamilyConfigs.get(item);
		return family != null ? family : FamilyPhysicsModelsManager.get(item);
	}

	public void closeAndClear() {
		this.images.closeAndClear();
		this.resolvedFamilyConfigs.clear();
	}
}