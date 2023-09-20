package io.openk9.datasource.index.mappings;

public class MappingsKey {
	private final String key;
	private final String hashKey;

	public static MappingsKey of(String key) {
		return new MappingsKey(key);
	}

	public static MappingsKey of(String key, String hashKey) {
		return new MappingsKey(key, hashKey);
	}

	public MappingsKey(String key) {
		this(key, key);
	}

	public MappingsKey(String key, String hashKey) {
		this.key = key;
		this.hashKey = hashKey;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return hashKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MappingsKey) {
			return hashKey.equals(((MappingsKey) obj).hashKey);
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return key;
	}

}