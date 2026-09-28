// jest-dom adds custom matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import * as matchers from '@testing-library/jest-dom/matchers';
import { cleanup } from '@testing-library/react';
import { afterEach, expect } from 'vitest';

expect.extend(matchers);

// not via jest-dom/vitest: it types Vitest 4 and may augment another vitest copy
declare module 'vitest' {
  interface Assertion<R extends void | Promise<void> = void, T = unknown> extends matchers.TestingLibraryMatchers<any, R> {}
  interface AsymmetricMatchersContaining extends matchers.TestingLibraryMatchers<any, any> {}
}

// without Vitest globals Testing Library does not clean up on its own
afterEach(cleanup);
