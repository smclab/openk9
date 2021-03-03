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

package com.openk9.reactor.agent;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.tools.shaded.net.bytebuddy.jar.asm.ClassReader;
import reactor.tools.shaded.net.bytebuddy.jar.asm.ClassVisitor;
import reactor.tools.shaded.net.bytebuddy.jar.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Activator implements BundleActivator {

	private BundleTracker _bundleTracker;

	@Override
	public void start(BundleContext context) throws Exception {
		init();

		_bundleTracker = new BundleTracker(
			context, Bundle.ACTIVE | Bundle.STOPPING,
			new BundleTrackerCustomizer() {

				private final Set<Bundle> _bundles = Collections.newSetFromMap(
					new ConcurrentHashMap<>());

				@Override
				public Object addingBundle(Bundle bundle, BundleEvent event) {

					if (bundle.getState() != Bundle.ACTIVE) {
						removedBundle(bundle, event, null);
						return null;
					}

					if (!bundle.getSymbolicName().startsWith("com.openk9")) {
						return null;
					}

					if (!_bundles.contains(bundle)) {
						BundleWiring adapt = bundle.adapt(BundleWiring.class);

						processExistingClasses(adapt.getClassLoader());

						_bundles.add(bundle);
					}

					return null;
				}

				@Override
				public void modifiedBundle(
					Bundle bundle, BundleEvent event, Object object) {

					removedBundle(bundle, event, object);

					addingBundle(bundle, event);

				}

				@Override
				public void removedBundle(
					Bundle bundle, BundleEvent event, Object object) {
					_bundles.remove(bundle);
				}
			}
		);

		_bundleTracker.open();

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		_bundleTracker.close();
	}

	private static final String INSTALLED_PROPERTY = "reactor.tools.agent.installed";

	private static Instrumentation instrumentation;

	public static synchronized void init() {

		_log.debug("START REACTOR AGENT");

		if (System.getProperty(INSTALLED_PROPERTY) != null) {
			return;
		}

		if (instrumentation != null) {
			return;
		}
		instrumentation = ByteBuddyAgent.install();

		instrument(instrumentation);
	}

	private static void instrument(Instrumentation instrumentation) {
		ClassFileTransformer transformer = new ClassFileTransformer() {
			@Override
			public byte[] transform(
				ClassLoader loader,
				String className,
				Class<?> clazz,
				ProtectionDomain protectionDomain,
				byte[] bytes
			) {
				if (loader == null) {
					return null;
				}

				if (
					className == null ||
					className.startsWith("java/") ||
					className.startsWith("jdk/") ||
					className.startsWith("sun/") ||
					className.startsWith("com/sun/") ||
					className.startsWith("reactor/core/")
				) {
					return null;
				}

				if (
					clazz != null && (
						clazz.isPrimitive() ||
						clazz.isArray() ||
						clazz.isAnnotation() ||
						clazz.isSynthetic()
					)
				) {
					return null;
				}

				ClassReader cr = new ClassReader(bytes);
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

				AtomicBoolean changed = new AtomicBoolean();

				BundleWiring adapt =
					FrameworkUtil
						.getBundle(Activator.class)
						.adapt(BundleWiring.class);

				try {

					ClassVisitor
						classVisitor = _createReactorDebugClassVisitor(
							cw, changed, adapt.getClassLoader());


					cr.accept(classVisitor, 0);
				}
				catch (Throwable e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

				if (!changed.get()) {
					return null;
				}

				return cw.toByteArray();
			}
		};

		instrumentation.addTransformer(transformer, true);
	}

	public static synchronized void processExistingClasses(
		ClassLoader classLoader) {
		if (System.getProperty(INSTALLED_PROPERTY) != null) {
			// processExistingClasses is a NOOP when running as an agent
			return;
		}

		if (instrumentation == null) {
			throw new IllegalStateException("Must be initialized first!");
		}

		try {
			Class[] classes = Stream
				.of(instrumentation.getInitiatedClasses(classLoader))
				.filter(aClass -> {
					try {
						if (aClass.getClassLoader() == null) return false;
						if (aClass.isPrimitive() || aClass.isArray() || aClass.isInterface()) return false;
						if (aClass.isAnnotation() || aClass.isSynthetic()) return false;
						String name = aClass.getName();
						if (name == null) return false;
						if (name.startsWith("[")) return false;
						if (name.startsWith("java.")) return false;
						if (name.startsWith("sun.")) return false;
						if (name.startsWith("com.sun.")) return false;
						if (name.startsWith("jdk.")) return false;
						if (name.startsWith("reactor.core.")) return false;

						// May trigger NoClassDefFoundError, fail fast
						aClass.getConstructors();
					}
					catch (LinkageError e) {
						return false;
					}

					return true;
				})
				.toArray(Class[]::new);

			if (classes.length > 0) {
				instrumentation.retransformClasses(classes);
			}

		}
		catch (Throwable e) {

			if (_log.isDebugEnabled()) {
				_log.debug(e.getMessage(), e);
			}
			// Some classes fail to re-transform (e.g. worker.org.gradle.internal.UncheckedException)
			// See https://bugs.openjdk.java.net/browse/JDK-8014229
		}
	}

	private static ClassVisitor _createReactorDebugClassVisitor(
			ClassWriter cw, AtomicBoolean changed, ClassLoader classLoader)
		throws Exception {

		String className = "reactor.tools.agent.ReactorDebugClassVisitor";

		Class<?> aClass = classLoader.loadClass(className);

		Constructor<?> constructor =
			aClass.getDeclaredConstructor(
				reactor.tools.shaded.net.bytebuddy.jar.asm.ClassVisitor.class,
				AtomicBoolean.class);

		constructor.setAccessible(true);

		return (ClassVisitor)constructor.newInstance(cw, changed);

	}

	private static final Logger _log =
		LoggerFactory.getLogger(Activator.class);

}
