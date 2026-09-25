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

package io.openk9.datasource.pipeline.service;

import io.openk9.datasource.util.SchedulerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SchedulingServiceTest {

	private static final String EXPECTED_ERROR_DESCRIPTION =
		"java.lang.RuntimeException\n" +
		"Resulted in: io.openk9.datasource.pipeline.service.SchedulingServiceTest" +
		"$TestException: Error message";

	@Test
	void should_return_errorDescription_from_exception() {

		try {
			throwsVeryLongStackTrace(0);
		}
		catch (TestException e) {
			var errorDescription = SchedulerUtil.getErrorDescription(e);

			Assertions.assertEquals(EXPECTED_ERROR_DESCRIPTION, errorDescription);
		}

	}

	@Test
	void should_append_the_cause_on_a_new_line() {
		Assertions.assertEquals(
			"refused\ndiscarded", SchedulerUtil.appendErrorDescription("refused", "discarded"));
		Assertions.assertEquals(
			"discarded", SchedulerUtil.appendErrorDescription(null, "discarded"));
		Assertions.assertEquals(
			"discarded", SchedulerUtil.appendErrorDescription(" ", "discarded"));
	}

	@Test
	void should_shorten_the_recorded_part_to_fit_the_column() {
		var recorded = "r".repeat(4000);
		var cause = "discarded after 3 delivery attempts";

		var description = SchedulerUtil.appendErrorDescription(recorded, cause);

		Assertions.assertEquals(4000, description.length());
		Assertions.assertTrue(description.endsWith("r\n" + cause), description);
	}

	@Test
	void should_truncate_a_cause_longer_than_the_column() {
		var description = SchedulerUtil.appendErrorDescription("refused", "c".repeat(5000));

		Assertions.assertEquals("c".repeat(4000), description);
	}

	private void throwsVeryLongStackTrace(int count) throws TestException {

		if (count < 4200) {
			throwsVeryLongStackTrace(count + 1);
		}
		else {
			throw new TestException("Error message", new RuntimeException());
		}

	}

	private static class TestException extends Exception {

		public TestException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
