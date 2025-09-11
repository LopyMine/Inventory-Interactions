package net.lopymine.mossy.testrender.argument;

public class GlobalArgumentType extends StringRenderArgument {

	public static final String ID = "GLA_";
	private final int index;

	private GlobalArgumentType(int index, Object... globalArgs) {
		super(getArg(index, globalArgs));
		this.index = index;
	}

	public static GlobalArgumentType getGlobalArg(String s, Object... globalArgs) {
		return new GlobalArgumentType(Integer.parseInt(s.trim().replace(ID, "")), globalArgs);
	}

	public static boolean isGlobalArg(String s) {
		return s.startsWith(ID);
	}

	public void update(Object... globalArgs) {
		this.setArg(getArg(this.index, globalArgs));
	}

	private static String getArg(int index, Object[] globalArgs) {
		return String.valueOf(globalArgs[index]);
	}
}
