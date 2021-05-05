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

package io.openk9.http.client;

import org.reactivestreams.Publisher;

import java.util.Map;

public interface HttpClient {

	Publisher<byte[]> request(int method, String url);

	Publisher<byte[]> request(int method, String url, String dataRow);

	Publisher<byte[]> request(
		int method, String url, Map<String, String> formDataAttr);

	Publisher<byte[]> request(
		int method, String url, String dataRow,
		Map<String, Object> headers);

	Publisher<byte[]> request(
		int method, String url, Map<String, String> formDataAttr,
		Map<String, Object> headers);
}
