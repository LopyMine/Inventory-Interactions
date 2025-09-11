package net.lopymine.mossy.testrender.argument;

import lombok.*;

@AllArgsConstructor
@Setter
public class StringRenderArgument implements IRenderArgument {

	@NonNull
	private String arg;

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String get() {
		return this.arg;
	}

}
