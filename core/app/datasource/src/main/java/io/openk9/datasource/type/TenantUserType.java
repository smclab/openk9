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

package io.openk9.datasource.type;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class TenantUserType implements UserType<String> {

	@Override
	public int getSqlType() {
		return SqlTypes.BIGINT;
	}

	@Override
	public Class<String> returnedClass() {
		return String.class;
	}

	@Override
	public boolean equals(String x, String y) {
		return x.equals(y);
	}

	@Override
	public int hashCode(String x) {
		return Objects.hashCode(x);
	}

	@Override
	public String nullSafeGet(
		ResultSet rs,
		int position,
		SharedSessionContractImplementor session,
		Object owner) throws SQLException {

		return session.getTenantIdentifier();
	}

	@Override
	public void nullSafeSet(
		PreparedStatement st,
		String value,
		int index,
		SharedSessionContractImplementor session) throws SQLException {

	}

	@Override
	public String deepCopy(String value) {
		return "";
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(String value) {
		return null;
	}

	@Override
	public String assemble(Serializable cached, Object owner) {
		return "";
	}

}
