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

package io.openk9.filemanager.service;

import com.google.protobuf.Empty;
import io.openk9.filemanager.grpc.FileManager;
import io.openk9.filemanager.grpc.FileResourceRequest;
import io.openk9.filemanager.grpc.FileResourceResponse;
import io.openk9.filemanager.grpc.FindFileResourceByDatasourceIdFileIdRequest;
import io.openk9.filemanager.grpc.FindFileResourceByResourceIdRequest;
import io.quarkus.grpc.GrpcService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class UploadServiceTest {

	private static final byte[] binary = new byte[]{
		0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7,
		0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF,
		0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7,
		0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF,
		0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7,
		0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF,
		0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7,
		0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF,
	};
	private static final String expectedResourceId = UUID.randomUUID().toString();
	@Inject
	UploadService uploadService;

	@Test
	void should_return_an_expected_resourceId() {

		var resourceId = uploadService.uploadObject(
			new ByteArrayInputStream(binary), "100", "101", "mew");

		assertEquals(expectedResourceId, resourceId);

	}

	@GrpcService
	static class MockFileManager implements FileManager {

		@Override
		public Uni<FileResourceResponse> findFileResourceByResourceId(
			FindFileResourceByResourceIdRequest request) {
			return Uni.createFrom().item(FileResourceResponse.newBuilder()
				.setResourceId(expectedResourceId)
				.build());
		}

		@Override
		public Uni<FileResourceResponse> findFileResourceByDatasourceIdAndFileId(
			FindFileResourceByDatasourceIdFileIdRequest request) {
			return Uni.createFrom().item(FileResourceResponse.newBuilder()
				.setResourceId(expectedResourceId)
				.build());
		}

		@Override
		public Uni<FileResourceResponse> createFileResource(FileResourceRequest request) {
			return Uni.createFrom().nullItem();
		}

		@Override
		public Uni<Empty> deleteFileResource(FindFileResourceByResourceIdRequest request) {
			return Uni.createFrom().nullItem();
		}

	}

}