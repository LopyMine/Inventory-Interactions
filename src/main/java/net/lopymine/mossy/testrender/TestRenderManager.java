package net.lopymine.mossy.testrender;

import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import net.lopymine.mossy.testrender.action.*;
import net.lopymine.mossy.testrender.argument.*;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.*;

@SuppressWarnings("unused")
public class TestRenderManager {

	private static final Map<String, IRenderAction> REGISTERED_ACTIONS = new HashMap<>();
	private static final Map<String, IDynamicRenderArgument> REGISTERED_DYNAMIC_ARGUMENTS = new HashMap<>();

	public static final Logger LOGGER = LoggerFactory.getLogger("TestRenderManager");

	private final Path renderFilePath;
	private String lastRenderFileText = "";
	private String lastReport = "";

	private RenderPass[] renderPasses = new RenderPass[0];

	private TestRenderManager(Path renderFilePath) {
		this.renderFilePath = renderFilePath;
	}

	public static void registerAction(IRenderAction action) {
		REGISTERED_ACTIONS.put(action.getId(), action);
	}

	public static void registerArgument(IDynamicRenderArgument argument) {
		REGISTERED_DYNAMIC_ARGUMENTS.put(argument.getId(), argument);
	}

	public static TestRenderManager load(Path renderFile) {
		boolean exists = renderFile.toFile().exists();
		if (!exists) {
			throw new RuntimeException("[TestRenderManager] Render file not found: " + renderFile);
		}
		return new TestRenderManager(renderFile);
	}

	public void render(Runnable mainRender, MatrixStack matrices, Immediate immediate, DiffuseLighting lighting, Object... globalArgs) {
		this.updateActions(globalArgs);

		for (RenderPass pass: this.renderPasses) {
			IRenderAction action = pass.action();
			IRenderArgument[] args = pass.args();
			for (IRenderArgument arg : args) {
				if (arg instanceof GlobalArgumentType type) {
					type.update(globalArgs);
				}
			}
			try {
				action.render(new RenderContext(matrices, immediate, lighting, mainRender), args);
			} catch (Exception e) {
				this.report("Exception occurred of render action \"%s\":".formatted(action.getId()), e);
			}
		}
	}

	private void updateActions(Object... globalArgs) {
		String currentLine = "[not started]";
		try {
			List<String> lines = Files.readAllLines(this.renderFilePath)
					.stream()
					.map(String::trim)
					.filter((s) -> !s.isEmpty())
					.toList();

			String renderFileText = String.join("\n", lines);
			boolean sameText = this.lastRenderFileText.equals(renderFileText);
			boolean emptyText = renderFileText.isEmpty();
			if (sameText || emptyText) {
				return;
			}

			RenderPass[] renderPasses = new RenderPass[lines.size()];

			int foundPushes = 0;
			int foundPopes = 0;
			int createdPasses = 0;

			for (String line : lines) {
				currentLine = line;
				IRenderAction action = line.equals("RENDER") ? new HotRenderAction() : this.getRenderAction(line);
				if (action == null) {
					continue;
				}
				if (action instanceof PushRenderAction) {
					foundPushes++;
				}
				if (action instanceof PopRenderAction) {
					foundPopes++;
				}
				IRenderArgument[] args = this.getRenderActionArgs(line, globalArgs);

				renderPasses[createdPasses] = new RenderPass(action, args);
				createdPasses++;
			}

			if (renderPasses.length != createdPasses) {
				renderPasses = Arrays.copyOf(renderPasses, createdPasses);
			}

			if (foundPushes != foundPopes) {
				return;
			}

			this.renderPasses       = renderPasses;
			this.lastRenderFileText = renderFileText;
		} catch (Exception e) {
			this.report("Failed to load actions from render file at line \"%s\":".formatted(currentLine), e);
		}
	}

	private IRenderArgument[] getRenderActionArgs(String line, Object... globalArgs) {
		String trimmed = line.trim();
		if (!trimmed.contains(" ")) {
			return new IRenderArgument[0];
		}
		return Arrays.stream(trimmed.substring(trimmed.indexOf(" ") + 1).split(" "))
				.map((s) -> {
					if (GlobalArgumentType.isGlobalArg(s)) {
						return GlobalArgumentType.getGlobalArg(s, globalArgs);
					}

					IRenderArgument argument = REGISTERED_DYNAMIC_ARGUMENTS.get(s);
					if (argument != null) {
						return argument;
					}

					return new StringRenderArgument(s);
				}).toArray(IRenderArgument[]::new);
	}

	@Nullable
	private IRenderAction getRenderAction(String line) {
		for (Entry<String, IRenderAction> entry : REGISTERED_ACTIONS.entrySet()) {
			if (!line.startsWith(entry.getKey())) {
				continue;
			}
			return entry.getValue();
		}
		return null;
	}

	private void report(String text, Object... args) {
		if (this.lastReport.equals(text)) {
			return;
		}
		LOGGER.error(text, args);
		this.lastReport = text;
	}

	static {
		registerAction(new PushRenderAction());
		registerAction(new PopRenderAction());
		registerAction(new TranslateRenderAction());
		registerAction(new ScaleRenderAction());
		registerAction(new RotateRenderAction());
		registerAction(new SetupLightRenderAction());
		registerAction(new DrawRenderAction());

		registerArgument(new DynamicRotationRenderArgument());
	}

	public record RenderPass(IRenderAction action, IRenderArgument... args) {

	}

}
