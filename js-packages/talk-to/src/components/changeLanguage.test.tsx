import { describe, expect, test, vi, beforeEach } from "vitest";
import { render, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ChangeLanguageMemo } from "./changeLanguage";

const mockChangeLanguage = vi.fn();
const mockSetLanguage = vi.fn();
const mockGetDefaultLanguage = vi.fn();

vi.mock("react-i18next", () => ({
	useTranslation: () => ({ i18n: { changeLanguage: mockChangeLanguage } }),
}));

vi.mock("./ChatInfoContext", () => ({
	useUser: () => ({ setLanguage: mockSetLanguage, language: "en" }),
}));

vi.mock("./client", () => ({
	OpenK9Client: () => ({
		getAvailableLanguages: async () => [{ name: "Italiano", value: "it" }],
		getDefaultLanguage: () => mockGetDefaultLanguage(),
	}),
}));

function renderWith(queryClient: QueryClient) {
	return render(
		<QueryClientProvider client={queryClient}>
			<ChangeLanguageMemo />
		</QueryClientProvider>,
	);
}

describe("ChangeLanguage", () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	test("applies the default language once, even when the component remounts", async () => {
		mockGetDefaultLanguage.mockResolvedValue({ value: "it" });
		const queryClient = new QueryClient();

		const first = renderWith(queryClient);
		await waitFor(() => expect(mockChangeLanguage).toHaveBeenCalledWith("it"));
		first.unmount();
		renderWith(queryClient);
		await waitFor(() => expect(queryClient.isFetching()).toBe(0));

		expect(mockChangeLanguage).toHaveBeenCalledTimes(1);
		expect(mockSetLanguage).toHaveBeenCalledTimes(1);
	});

	test("falls back to English when the default language cannot be fetched", async () => {
		vi.spyOn(console, "error").mockImplementation(() => {});
		mockGetDefaultLanguage.mockRejectedValue(new Error("Failed to fetch default language"));
		const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });

		renderWith(queryClient);

		await waitFor(() => expect(mockChangeLanguage).toHaveBeenCalledWith("en"));
	});
});
