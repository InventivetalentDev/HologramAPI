package de.inventivegames.hologram.touch;

public enum TouchAction {
	RIGHT_CLICK,
	LEFT_CLICK,
	UNKNOWN;

	public static TouchAction fromUseAction(Object useAction) {
		if (useAction == null) { return UNKNOWN; }
		@SuppressWarnings("rawtypes")
		int i = ((Enum) useAction).ordinal();
		switch (i) {
			case 0:
				return RIGHT_CLICK;
			case 1:
				return LEFT_CLICK;
			default:
				break;
		}
		return UNKNOWN;
	}
}