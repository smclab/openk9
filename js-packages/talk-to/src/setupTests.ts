// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// jsdom lacks URL.createObjectURL/revokeObjectURL. Plain functions, not jest.fn(): CRA sets
// resetMocks: true, which would wipe a mock installed here before every test.
if (typeof URL.createObjectURL !== 'function') {
	URL.createObjectURL = () => 'blob:jsdom/object-url';
}
if (typeof URL.revokeObjectURL !== 'function') {
	URL.revokeObjectURL = () => undefined;
}
