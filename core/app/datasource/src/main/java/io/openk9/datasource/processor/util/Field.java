package io.openk9.datasource.processor.util;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RegisterForReflection
public class Field {

	private Field(String name) {
		this(name, null);
	}

	private Field(String name, String type) {
		this(name, type, Map.of());
	}

	private Field(String name, String type, Map<String, Object> extra) {
		this.name = Objects.requireNonNull(name, "name is null");
		this.type = type;
		this.extra = extra;
	}



	public void addSubFields(Collection<Field> fields) {

		if (subFields == null) {
			subFields = new LinkedList<>();
		}

		this.subFields.addAll(fields);
	}

	public void addSubField(Field field) {

		if (subFields == null) {
			subFields = new LinkedList<>();
		}

		this.subFields.add(field);

	}

	public void addI18NField(Field field) {

		if (i18nFields == null) {
			i18nFields = new LinkedList<>();
		}

		this.i18nFields.add(field);

	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<Field> getSubFields() {
		return subFields == null ? List.of() : subFields;
	}

	public List<Field> getI18NFields() {
		return i18nFields == null ? List.of() : i18nFields;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRoot() {
		return Objects.equals(name, ROOT);
	}

	public static Field of(String name) {
		return new Field(name);
	}

	public static Field of(String name, String type) {
		return new Field(name, type);
	}

	public static Field of(String name, String type, Map<String, Object> extra) {
		return new Field(name, type, extra);
	}

	public static Field createRoot() {
		return new Field(ROOT);
	}

	private final String name;
	private String type;

	private Map<String, Object> extra;
	private List<Field> subFields;
	private List<Field> i18nFields;

	public static final String ROOT = "root-" + UUID.randomUUID();

}
