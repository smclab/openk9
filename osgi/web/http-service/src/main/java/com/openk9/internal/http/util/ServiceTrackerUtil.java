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

package com.openk9.internal.http.util;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServiceTrackerUtil {

	public static <T1, T2> ServiceTracker<T1, T2> open(
		BundleContext bundleContext, Class<T1> clazz,
		ServiceTrackerCustomizer<T1, T2> customizer) {

		ServiceTracker<T1, T2> st =
			new ServiceTracker<>(bundleContext, clazz, customizer);

		st.open(true);

		return st;

	}

	public static <T1, T2> ServiceTracker<T1, T2> open(
		BundleContext bundleContext, Class<T1> clazz) {

		return open(bundleContext, clazz, null);

	}

}
