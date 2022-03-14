package io.openk9.resources;

public final class Release {

	public static String getProductName() {
		return "${product.name}";
	}

	public static String getVersion() {
		return "${product.version}";
	}

	public static void printRelease() {
		String output =
			Logo.getLogo() +
			Logo.getEmptySpace() +
			String.format("%s (%s)", getProductName(), getVersion()) +
			Logo.getEmptySpace();

		System.out.println(output);
	}

	static {
		printRelease();
	}
	
}