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

package io.openk9.ingestion.web;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.openk9.ingestion.storage.BinaryStorageService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@QuarkusTest
class BinaryStorageEmitterTest {

	@Inject
	BinaryStorageEmitter binaryStorageEmitter;

	@InjectMock
	BinaryStorageService binaryStorageService;

	@InjectMock
	IngestionEmitter ingestionEmitter;

	@Test
	void should_split_each_binary_into_its_own_message() {

		// storage accepts every binary
		Mockito
			.when(binaryStorageService.store(any(), any(), any(), any()))
			.thenReturn(Uni.createFrom().voidItem());

		// an ingestion request carrying two binaries to split
		IngestionDTO ingestionDTO = IngestionDTO.builder()
			.datasourceId(100L)
			.tenantId("mew")
			.contentId("doc-1")
			.datasourcePayload(Map.of("title", "Lorem ipsum"))
			.resources(ResourcesDTO.builder()
				.splitBinaries(true)
				.binaries(List.of(
					binary("f1", "text/plain"),
					binary("f2", "application/pdf")))
				.build())
			.build();

		// run the upload pipeline to completion
		binaryStorageEmitter.emit(ingestionDTO).await().indefinitely();

		// each binary is stored once
		then(binaryStorageService)
			.should(times(2))
			.store(any(), any(), any(), any());

		// one message per binary plus the original request are emitted
		ArgumentCaptor<IngestionDTO> emitted =
			ArgumentCaptor.forClass(IngestionDTO.class);
		then(ingestionEmitter).should(times(3)).emit(emitted.capture());

		// the per-binary messages are the objects that reach the datasource:
		// contentId == fileId, no raw content, a single stripped binary
		List<IngestionDTO> perBinary = emitted.getAllValues()
			.stream()
			.filter(dto -> dto != ingestionDTO)
			.toList();

		assertEquals(2, perBinary.size());

		for (IngestionDTO dto : perBinary) {
			List<BinaryDTO> references = dto.getResources().getBinaries();
			assertEquals(1, references.size());

			BinaryDTO reference = references.get(0);
			assertEquals(dto.getContentId(), reference.getId());
			assertNull(reference.getData());
			assertEquals("", dto.getRawContent());
			assertTrue(dto.getDatasourcePayload().containsKey("file"));
		}
	}

	@Test
	void should_emit_a_single_message_when_binaries_are_not_split() {

		// storage accepts every binary
		Mockito
			.when(binaryStorageService.store(any(), any(), any(), any()))
			.thenReturn(Uni.createFrom().voidItem());

		// an ingestion request carrying two binaries kept together
		IngestionDTO ingestionDTO = IngestionDTO.builder()
			.datasourceId(100L)
			.tenantId("mew")
			.contentId("doc-1")
			.resources(ResourcesDTO.builder()
				.splitBinaries(false)
				.binaries(List.of(
					binary("f1", "text/plain"),
					binary("f2", "application/pdf")))
				.build())
			.build();

		// run the upload pipeline to completion
		binaryStorageEmitter.emit(ingestionDTO).await().indefinitely();

		// both binaries are stored
		then(binaryStorageService)
			.should(times(2))
			.store(any(), any(), any(), any());

		// only the original request is emitted, now carrying the references
		ArgumentCaptor<IngestionDTO> emitted =
			ArgumentCaptor.forClass(IngestionDTO.class);
		then(ingestionEmitter).should(times(1)).emit(emitted.capture());

		// the references reaching the datasource are stripped of their bytes
		List<BinaryDTO> references =
			emitted.getValue().getResources().getBinaries();
		assertEquals(2, references.size());
		references.forEach(reference -> assertNull(reference.getData()));
	}

	private static BinaryDTO binary(String id, String contentType) {
		return BinaryDTO.builder()
			.id(id)
			.name(id)
			.contentType(contentType)
			.data("some-bytes".getBytes())
			.build();
	}

}
