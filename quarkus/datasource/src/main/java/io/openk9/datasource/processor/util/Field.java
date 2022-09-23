package io.openk9.datasource.processor.util;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RegisterForReflection
public class Field {

	private Field(String name) {
		this.name = Objects.requireNonNull(name, "name is null");
	}

	private Field(String name, String type) {
		this(name);
		this.type = type;
	}

	public void addSubFields(Collection<Field> fields) {

		if (subFields == null) {
			subFields = new ArrayList<>();
		}

		this.subFields.addAll(fields);
	}

	public void addSubField(Field field) {

		if (subFields == null) {
			subFields = new ArrayList<>();
		}

		this.subFields.add(field);

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

	public static Field createRoot() {
		return new Field(ROOT);
	}

	private final String name;
	private String type;
	private List<Field> subFields;

	public static final String ROOT = "root-" + UUID.randomUUID();

}
