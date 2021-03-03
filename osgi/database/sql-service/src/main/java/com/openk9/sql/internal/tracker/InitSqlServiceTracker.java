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

package com.openk9.sql.internal.tracker;

import com.openk9.sql.api.InitSql;
import io.r2dbc.spi.ConnectionFactory;
import io.vavr.CheckedFunction0;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;

public class InitSqlServiceTracker
	implements ServiceTrackerCustomizer<InitSql, InitSql> {

	public InitSqlServiceTracker(
		BundleContext bundleContext,
		ConnectionFactory connectionFactory) {
		_bundleContext = bundleContext;
		_connectionFactory = connectionFactory;
	}

	@Override
	public InitSql addingService(
		ServiceReference<InitSql> reference) {

		InitSql service = _bundleContext.getService(reference);

		Bundle bundle = reference.getBundle();

		URL initSqlResource = bundle.getResource(service.initSqlFile());

		if (initSqlResource == null) {
			_log.error(
				"file: "
				+ service.initSqlFile() + " not found in bundle: "
				+ bundle.getSymbolicName());
			return null;
		}

		String initSql = _inputStringToString(
			CheckedFunction0.narrow(initSqlResource::openStream).unchecked());

		executeQuery(initSql)
			.then(Mono.fromRunnable(service::onAfterCreate))
			.subscribe();

		if (service.executeDataSqlQuery()) {
			URL dataSqlFile = bundle.getResource(
				service.dataSqlFile() == null ? "" : service.dataSqlFile());

			if (dataSqlFile == null) {

				_log.error(
					"file: "
					+ service.dataSqlFile() + " not found in bundle: "
					+ bundle.getSymbolicName());
				return null;
			}

			String dataSql = _inputStringToString(
				CheckedFunction0.narrow(
					dataSqlFile::openStream).unchecked());

			executeQuery(dataSql).subscribe();

		}

		return null;
	}

	@Override
	public void modifiedService(
		ServiceReference<InitSql> reference, InitSql service) {

		removedService(reference, service);

		addingService(reference);

	}

	@Override
	public void removedService(
		ServiceReference<InitSql> reference, InitSql service) {

		_bundleContext.ungetService(reference);

	}

	private Mono<Void> executeQuery(String query) {
		return Mono.from(_connectionFactory.create())
			.flatMap(
				connection ->
					Mono.from(
						connection
							.createStatement(query)
							.execute())
						.flatMap(
							ignore -> Mono.from(connection.close()))
			);
	}

	private String _inputStringToString(Supplier<InputStream> inputStream) {

		try (InputStream is = inputStream.get();) {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}

			return result.toString("UTF-8");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final BundleContext _bundleContext;
	private final ConnectionFactory _connectionFactory;

	private static final Logger _log =
		LoggerFactory.getLogger(InitSqlServiceTracker.class);

}
