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

package com.openk9.ingestion.api;

import java.util.Date;
import java.util.Map;

public interface BasicProperties {

	String getContentType();

	String getContentEncoding();

	Map<String,Object> getHeaders();

	Integer getDeliveryMode();

	Integer getPriority();

	String getCorrelationId();

	String getReplyTo();

	String getExpiration();

	String getMessageId();

	Date getTimestamp();

	String getType();

	String getUserId();

	String getAppId();

	String getClusterId();

	interface Builder {

		Builder contentType(String contentType);
		Builder contentEncoding(String contentEncoding);
		Builder headers(Map<String,Object> headers);
		Builder deliveryMode(Integer deliveryMode);
		Builder priority(Integer priority);
		Builder correlationId(String correlationId);
		Builder replyTo(String replyTo);
		Builder expiration(String expiration);
		Builder messageId(String messageId);
		Builder timestamp(Date timestamp);
		Builder type(String type);
		Builder userId(String userId);
		Builder appId(String appId);
		Builder clusterId(String clusterId);

		BasicProperties build();
	}


}
