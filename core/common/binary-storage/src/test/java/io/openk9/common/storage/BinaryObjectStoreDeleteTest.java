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

package io.openk9.common.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.Contents;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BinaryObjectStoreDeleteTest {

	private static BinaryObjectStore service(MinioClient client) {
		return new BinaryObjectStore(
			client, BinaryKeys.DEFAULT_BUCKET_TEMPLATE, 120);
	}

	private static Result<Item> item(String objectName) {
		return new Result<>(new Contents(objectName));
	}

	@Test
	void deletesEveryStagedObjectUnderTheDatasourcePrefix() throws Exception {
		// a tenant bucket holding two staged binaries of datasource 42
		MinioClient client = mock(MinioClient.class);
		when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
		when(client.listObjects(any(ListObjectsArgs.class)))
			.thenReturn(List.of(item("42/c1/f1"), item("42/c2/f2")));
		when(client.removeObjects(any(RemoveObjectsArgs.class)))
			.thenReturn(List.of());

		// delete the datasource working copy
		service(client).deleteByDatasource("acme", 42L);

		// it lists the tenant bucket under the datasource prefix, recursively
		ArgumentCaptor<ListObjectsArgs> listArgs =
			ArgumentCaptor.forClass(ListObjectsArgs.class);
		verify(client).listObjects(listArgs.capture());
		assertEquals("openk9-acme", listArgs.getValue().bucket());
		assertEquals("42/", listArgs.getValue().prefix());

		// and removes exactly the listed objects from the same bucket
		ArgumentCaptor<RemoveObjectsArgs> removeArgs =
			ArgumentCaptor.forClass(RemoveObjectsArgs.class);
		verify(client).removeObjects(removeArgs.capture());
		assertEquals("openk9-acme", removeArgs.getValue().bucket());

		List<DeleteObject> queued = new ArrayList<>();
		removeArgs.getValue().objects().forEach(queued::add);
		assertEquals(2, queued.size());
	}

	@Test
	void doesNothingWhenTheBucketDoesNotExist() throws Exception {
		// a tenant that never staged any binary has no bucket
		MinioClient client = mock(MinioClient.class);
		when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

		// delete is a no-op: no listing, no removal
		service(client).deleteByDatasource("acme", 42L);

		verify(client, never()).listObjects(any());
		verify(client, never()).removeObjects(any());
	}

	@Test
	void doesNotRemoveWhenNoObjectMatchesThePrefix() throws Exception {
		// the bucket exists but holds nothing under the datasource prefix
		MinioClient client = mock(MinioClient.class);
		when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
		when(client.listObjects(any(ListObjectsArgs.class)))
			.thenReturn(List.of());

		// nothing to remove
		service(client).deleteByDatasource("acme", 42L);

		verify(client, never()).removeObjects(any());
	}

}
