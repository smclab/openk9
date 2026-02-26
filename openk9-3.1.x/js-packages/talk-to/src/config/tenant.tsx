export function resolveTenantUrl(route: string) {
	const t = typeof window !== "undefined" ? window.tenant : undefined;
	if (!t) return route;

	if (/^https?:\/\//i.test(route)) return route;

	const normalizedRoute = route.startsWith("/") ? route : `/${route}`;

	if (/^https?:\/\//i.test(t)) {
		const base = t.replace(/\/+$/g, "");
		return `${base}${normalizedRoute}`;
	}

	const tenantPath = t.replace(/^\/+|\/+$/g, "");
	if (normalizedRoute.startsWith(`/${tenantPath}/`)) return normalizedRoute;
	return `/${tenantPath}${normalizedRoute}`;
}
