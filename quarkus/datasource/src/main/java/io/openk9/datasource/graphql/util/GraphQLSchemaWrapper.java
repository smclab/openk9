/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.graphql.util;

import graphql.com.google.common.collect.ImmutableMap;
import graphql.language.SchemaDefinition;
import graphql.language.SchemaExtensionDefinition;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.visibility.GraphqlFieldVisibility;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class GraphQLSchemaWrapper extends GraphQLSchema {

	private final GraphQLSchema schema;

	public GraphQLSchemaWrapper(GraphQLSchema graphQLSchema) {
		super(graphQLSchema, graphQLSchema.getCodeRegistry(), ImmutableMap.of(), ImmutableMap.of());
		this.schema = graphQLSchema;
	}

	public GraphQLCodeRegistry getCodeRegistry() {
		return this.schema.getCodeRegistry();
	}

	public GraphQLFieldDefinition getIntrospectionSchemaFieldDefinition() {
		return this.schema.getIntrospectionSchemaFieldDefinition();
	}

	public GraphQLFieldDefinition getIntrospectionTypeFieldDefinition() {
		return this.schema.getIntrospectionTypeFieldDefinition();
	}

	public GraphQLFieldDefinition getIntrospectionTypenameFieldDefinition() {
		return this.schema.getIntrospectionTypenameFieldDefinition();
	}

	public GraphQLObjectType getIntrospectionSchemaType() {
		return this.schema.getIntrospectionSchemaType();
	}

	public Set<GraphQLType> getAdditionalTypes() {
		return this.schema.getAdditionalTypes();
	}

	public GraphQLType getType(
		String typeName) {
		return this.schema.getType(typeName);
	}

	public <T extends GraphQLType> List<T> getTypes(
		Collection<String> typeNames) {
		return this.schema.getTypes(typeNames);
	}

	public <T extends GraphQLType> T getTypeAs(String typeName) {
		return this.schema.getTypeAs(typeName);
	}

	public boolean containsType(String typeName) {
		return this.schema.containsType(typeName);
	}

	public GraphQLObjectType getObjectType(String typeName) {
		return this.schema.getObjectType(typeName);
	}

	public GraphQLFieldDefinition getFieldDefinition(
		FieldCoordinates fieldCoordinates) {
		return this.schema.getFieldDefinition(fieldCoordinates);
	}

	public Map<String, GraphQLNamedType> getTypeMap() {
		return this.schema.getTypeMap();
	}

	public List<GraphQLNamedType> getAllTypesAsList() {
		return this.schema.getAllTypesAsList();
	}

	public List<GraphQLNamedSchemaElement> getAllElementsAsList() {
		return this.schema.getAllElementsAsList();
	}

	public List<GraphQLObjectType> getImplementations(
		GraphQLInterfaceType type) {
		return this.schema.getImplementations(type);
	}

	public boolean isPossibleType(
		GraphQLNamedType abstractType, GraphQLObjectType concreteType) {
		return this.schema.isPossibleType(abstractType, concreteType);
	}

	public GraphQLObjectType getQueryType() {
		return this.schema.getQueryType();
	}

	public GraphQLObjectType getMutationType() {
		return this.schema.getMutationType();
	}

	public GraphQLObjectType getSubscriptionType() {
		return this.schema.getSubscriptionType();
	}

	public GraphqlFieldVisibility getFieldVisibility() {
		return this.schema.getFieldVisibility();
	}

	public List<GraphQLDirective> getDirectives() {
		return this.schema.getDirectives();
	}

	public Map<String, GraphQLDirective> getDirectivesByName() {
		return this.schema.getDirectivesByName();
	}

	public GraphQLDirective getDirective(String directiveName) {
		return this.schema.getDirective(directiveName);
	}

	public List<GraphQLDirective> getSchemaDirectives() {
		return this.schema.getSchemaDirectives();
	}

	public Map<String, GraphQLDirective> getSchemaDirectiveByName() {
		return this.schema.getSchemaDirectiveByName();
	}

	public Map<String, List<GraphQLDirective>> getAllSchemaDirectivesByName() {
		return this.schema.getAllSchemaDirectivesByName();
	}

	public GraphQLDirective getSchemaDirective(String directiveName) {
		return this.schema.getSchemaDirective(directiveName);
	}

	public List<GraphQLDirective> getSchemaDirectives(String directiveName) {
		return this.schema.getSchemaDirectives(directiveName);
	}

	public List<GraphQLAppliedDirective> getSchemaAppliedDirectives() {
		return this.schema.getSchemaAppliedDirectives();
	}

	public Map<String, List<GraphQLAppliedDirective>> getAllSchemaAppliedDirectivesByName() {
		return this.schema.getAllSchemaAppliedDirectivesByName();
	}

	public GraphQLAppliedDirective getSchemaAppliedDirective(
		String directiveName) {
		return this.schema.getSchemaAppliedDirective(directiveName);
	}

	public List<GraphQLAppliedDirective> getSchemaAppliedDirectives(
		String directiveName) {
		return this.schema.getSchemaAppliedDirectives(directiveName);
	}

	public SchemaDefinition getDefinition() {
		return this.schema.getDefinition();
	}

	public List<SchemaExtensionDefinition> getExtensionDefinitions() {
		return this.schema.getExtensionDefinitions();
	}

	public boolean isSupportingMutations() {
		return this.schema.isSupportingMutations();
	}

	public boolean isSupportingSubscriptions() {
		return this.schema.isSupportingSubscriptions();
	}

	public String getDescription() {
		return this.schema.getDescription();
	}

	public GraphQLSchema transform(
		Consumer<Builder> builderConsumer) {
		return this.schema.transform(builderConsumer);
	}

	public GraphQLSchema transformWithoutTypes(
		Consumer<BuilderWithoutTypes> builderConsumer) {
		return this.schema.transformWithoutTypes(builderConsumer);
	}
}
