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

package io.openk9.http.web.body;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface FileUpload {
	String getFilename();

	void setFilename(String filename);

	void setContentType(String contentType);

	String getContentType();

	void setContentTransferEncoding(String contentTransferEncoding);

	String getContentTransferEncoding();

	FileUpload copy();

	FileUpload duplicate();

	FileUpload retainedDuplicate();

	FileUpload replace(ByteBuffer content);

	FileUpload retain();

	FileUpload retain(int increment);

	FileUpload touch();

	FileUpload touch(Object hint);

	long getMaxSize();

	void setMaxSize(long maxSize);

	void checkSize(long newSize) throws IOException;

	void setContent(ByteBuffer buffer) throws IOException;

	void addContent(ByteBuffer buffer, boolean last) throws IOException;

	void setContent(File file) throws IOException;

	void setContent(InputStream inputStream) throws IOException;

	boolean isCompleted();

	long length();

	long definedLength();

	void delete();

	byte[] get() throws IOException;

	ByteBuffer getByteBuffer() throws IOException;

	ByteBuffer getChunk(int length) throws IOException;

	String getString() throws IOException;

	String getString(Charset encoding) throws IOException;

	void setCharset(Charset charset);

	Charset getCharset();

	boolean renameTo(File dest) throws IOException;

	boolean isInMemory();

	File getFile() throws IOException;

	ByteBuffer content();

	String getName();

	int refCnt();

	boolean release();

	boolean release(int decrement);
}
