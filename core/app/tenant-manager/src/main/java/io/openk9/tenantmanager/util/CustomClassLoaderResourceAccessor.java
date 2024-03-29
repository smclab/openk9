package io.openk9.tenantmanager.util;

import liquibase.Scope;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.PathResource;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.URIResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class CustomClassLoaderResourceAccessor extends AbstractResourceAccessor {

	private ClassLoader classLoader;
	private CompositeResourceAccessor additionalResourceAccessors;
	protected SortedSet<String> description;

	public CustomClassLoaderResourceAccessor() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public CustomClassLoaderResourceAccessor(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public List<String> describeLocations() {
		init();

		return additionalResourceAccessors.describeLocations();
	}

	@Override
	public void close() throws Exception {
		if (additionalResourceAccessors != null) {
			additionalResourceAccessors.close();
		}
	}

	/**
	 * Performs the configuration of this resourceAccessor.
	 * Not done in the constructor for performance reasons, but can be called at the beginning of every public method.
	 */
	protected synchronized void init() {
		if (additionalResourceAccessors == null) {
			this.description = new TreeSet<>();
			this.additionalResourceAccessors = new CompositeResourceAccessor();

			configureAdditionalResourceAccessors(classLoader);
		}
	}

	/**
	 * The classloader search logic in {@link #search(String, boolean)} does not handle jar files well.
	 * This method is called by that method to configure an internal {@link ResourceAccessor} with paths to search.
	 */
	protected void configureAdditionalResourceAccessors(ClassLoader classLoader) {
		if (classLoader instanceof URLClassLoader) {
			final URL[] urls = ((URLClassLoader) classLoader).getURLs();
			if (urls != null) {
				PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);

				for (URL url : urls) {
					try {
						if (url.getProtocol().equals("file")) {
							additionalResourceAccessors.addResourceAccessor(pathHandlerFactory.getResourceAccessor(url.toExternalForm()));
						}
					} catch (FileNotFoundException e) {
						//classloaders often have invalid paths specified on purpose. Just log them as fine level.
						Scope.getCurrentScope().getLog(getClass()).fine("Classloader URL " + url.toExternalForm() + " does not exist", e);
					} catch (Throwable e) {
						Scope.getCurrentScope().getLog(getClass()).warning("Cannot handle classloader url " + url.toExternalForm() + ": " + e.getMessage()+". Operations that need to list files from this location may not work as expected", e);
					}
				}
			}
		}

		final ClassLoader parent = classLoader.getParent();
		if (parent != null) {
			configureAdditionalResourceAccessors(parent);
		}
	}
//
//    private void addDescription(URL url) {
//        try {
//            this.description.add(Paths.get(url.toURI()).toString());
//        } catch (Throwable e) {
//            this.description.add(url.toExternalForm());
//        }
//    }

	@Override
	public List<Resource> search(String path, boolean recursive) throws IOException {init();

		final LinkedHashSet<Resource> returnList = new LinkedHashSet<>();
		PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);

		final Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(path);
		} catch (IOException e) {
			throw new IOException("Cannot list resources in path " + path + ": " + e.getMessage(), e);
		}

		while (resources.hasMoreElements()) {
			final URL url = resources.nextElement();

			String urlExternalForm = url.toExternalForm();

			String pathRegex = path;

			if (pathRegex.endsWith("/")) {
				pathRegex = pathRegex.substring(0, path.length() - 1);
			}

			urlExternalForm = urlExternalForm.replaceFirst(Pattern.quote(pathRegex) + "/?$", "");

			try (ResourceAccessor resourceAccessor = pathHandlerFactory.getResourceAccessor(urlExternalForm)) {
				returnList.addAll(resourceAccessor.search(path, recursive));
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}
		}

		returnList.addAll(additionalResourceAccessors.search(path, recursive));


		return new ArrayList<>(returnList);
	}

	@Override
	public List<Resource> getAll(String path) throws IOException {
		//using a hash because sometimes the same resource gets included multiple times.
		LinkedHashSet<Resource> returnList = new LinkedHashSet<>();

		path = path.replace("\\", "/").replaceFirst("^/", "");

		Enumeration<URL> all = classLoader.getResources(path);
		try {
			while (all.hasMoreElements()) {
				URI uri = all.nextElement().toURI();
				if (uri.getScheme().equals("file")) {
					returnList.add(new PathResource(path, Paths.get(uri)));
				} else {
					returnList.add(new URIResource(path, uri));
				}
			}
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage(), e);
		}

		if (returnList.size() == 0) {
			return null;
		}
		return new ArrayList<>(returnList);
	}
}
