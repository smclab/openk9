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

package io.openk9.datasource.pipeline.resource;

import java.util.concurrent.CompletionException;

import org.apache.pekko.pattern.AskTimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The callback answers 503 when the pipeline entity does not answer in time:
 * the ask fails with a timeout, bare or wrapped by the completion stage.
 */
class PipelineResourceTimeoutTest {

	@Test
	void should_recognize_the_ask_timeout() {
		var timeout = new AskTimeoutException("Ask timed out");

		// bare, and wrapped as the completion stage delivers it
		Assertions.assertTrue(PipelineResource.isTimeout(timeout));
		Assertions.assertTrue(
			PipelineResource.isTimeout(new CompletionException(timeout)));
	}

	@Test
	void should_not_mistake_other_failures_for_a_timeout() {
		Assertions.assertFalse(
			PipelineResource.isTimeout(new IllegalStateException("boom")));
		Assertions.assertFalse(
			PipelineResource.isTimeout(
				new CompletionException(new IllegalStateException("boom"))));
	}

}
