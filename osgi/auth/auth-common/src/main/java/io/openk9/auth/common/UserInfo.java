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

package io.openk9.auth.common;

import java.util.List;
import java.util.Map;

public interface UserInfo {

	long getExp();

	long getIat();

	String getJti();

	String getIss();

	String getAud();

	String getSub();

	String getTyp();

	String getAzp();

	String getSessionState();

	String getName();

	String getGivenName();

	String getFamilyName();

	String getPreferredUsername();

	String getEmail();

	boolean isEmailVerified();

	String getAcr();

	Map<String, List<String>> getRealmAccess();

	Map<String, Map<String, List<String>>> getResourceAccess();

	String getScope();

	String getClientId();

	String getUsername();

	boolean isActive();

}
