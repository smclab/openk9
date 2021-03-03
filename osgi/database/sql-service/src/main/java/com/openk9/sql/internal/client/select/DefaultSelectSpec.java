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

package com.openk9.sql.internal.client.select;

import io.r2dbc.spi.ConnectionFactory;
import com.openk9.sql.api.client.CriteriaDefinition;
import com.openk9.sql.api.client.Page;
import com.openk9.sql.api.client.Sort;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public abstract class DefaultSelectSpec<T extends DefaultSelectSpec<T>> {

	public DefaultSelectSpec(
		ConnectionFactory connectionFactory, String table) {
		_connectionFactory = connectionFactory;
		_table = table;
		_sorts = Collections.emptySet();
		_page = Page.DEFAULT;
		_columns = Collections.emptyList();
		_criteriaDefinition = CriteriaDefinition.empty();
	}

	public DefaultSelectSpec(
		ConnectionFactory connectionFactory,
		String table, Collection<Sort> sorts, Page page,
		Collection<String> columns, CriteriaDefinition criteriaDefinition) {
		_connectionFactory = connectionFactory;
		_table = table;
		_sorts = sorts;
		_page = page;
		_columns = columns;
		_criteriaDefinition = criteriaDefinition;
	}

	public T orderBy(Sort sort) {
		return createInstance(
			_connectionFactory, _table, Collections.singleton(sort), _page,
			_columns, _criteriaDefinition);
	}

	public T orderBy(Sort...sorts) {
		return createInstance(
			_connectionFactory, _table, new HashSet<>(Arrays.asList(sorts)),
			_page, _columns, _criteriaDefinition);
	}

	public T page(Page page) {
		return createInstance(
			_connectionFactory, _table, _sorts, page, _columns,
			_criteriaDefinition);
	}

	public T project(String column) {
		return createInstance(
			_connectionFactory, _table, _sorts, _page,
			Collections.singleton(column), _criteriaDefinition);
	}

	public T project(String...columns) {
		return createInstance(
			_connectionFactory, _table, _sorts, _page,
			new HashSet<>(Arrays.asList(columns)), _criteriaDefinition);
	}

	public T project(Collection<String> columns) {
		return createInstance(
			_connectionFactory, _table, _sorts, _page, columns,
			_criteriaDefinition);
	}

	public T matching(CriteriaDefinition criteria) {
		return createInstance(
			_connectionFactory, _table, _sorts, _page, _columns, criteria);
	}

	protected abstract T createInstance(
		ConnectionFactory connectionFactory, String table,
		Collection<Sort> sort, Page pag, Collection<String> columns,
		CriteriaDefinition criteriaDefinition);

	protected final Collection<Sort> _sorts;
	protected final Page _page;
	protected final ConnectionFactory _connectionFactory;
	protected final String _table;
	protected final Collection<String> _columns;
	protected final CriteriaDefinition _criteriaDefinition;

}
