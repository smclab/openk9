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

package io.openk9.internal.http.body;

import io.openk9.http.web.body.FileUpload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class FileUploadWrapper implements FileUpload {

	public FileUploadWrapper(
		io.netty.handler.codec.http.multipart.FileUpload _delegate) {
		this._delegate = _delegate;
	}

	public String getFilename() {
		return this._delegate.getFilename();
	}

	public void setFilename(String filename) {
		this._delegate.setFilename(filename);
	}

	public void setContentType(String contentType) {
		this._delegate.setContentType(contentType);
	}

	public String getContentType() {
		return this._delegate.getContentType();
	}

	public void setContentTransferEncoding(String contentTransferEncoding) {
		this._delegate.setContentTransferEncoding(contentTransferEncoding);
	}

	public String getContentTransferEncoding() {
		return this._delegate.getContentTransferEncoding();
	}

	public FileUpload copy() {
		return new FileUploadWrapper(this._delegate.copy());
	}

	public FileUpload duplicate() {
		return new FileUploadWrapper(this._delegate.duplicate());
	}

	public FileUpload retainedDuplicate() {
		return new FileUploadWrapper(this._delegate.retainedDuplicate());
	}

	@Override
	public FileUpload replace(ByteBuffer content) {
		return new FileUploadWrapper(
			_delegate.replace(Unpooled.wrappedBuffer(content)));
	}

	public FileUpload replace(
		ByteBuf content) {
		return new FileUploadWrapper(this._delegate.replace(content));
	}

	public FileUpload retain() {
		this._delegate.retain();
		return this;
	}

	public FileUpload retain(int increment) {
		this._delegate.retain(increment);
		return this;
	}

	public FileUpload touch() {
		this._delegate.touch();
		return this;
	}

	public FileUpload touch(Object hint) {
		this._delegate.touch(hint);
		return this;
	}

	public long getMaxSize() {
		return this._delegate.getMaxSize();
	}

	public void setMaxSize(long maxSize) {
		this._delegate.setMaxSize(maxSize);
	}

	public void checkSize(long newSize) throws java.io.IOException {
		this._delegate.checkSize(newSize);
	}

	@Override
	public void setContent(ByteBuffer buffer) throws IOException {
		_delegate.setContent(Unpooled.wrappedBuffer(buffer));
	}

	@Override
	public void addContent(ByteBuffer buffer, boolean last) throws IOException {
		_delegate.addContent(Unpooled.wrappedBuffer(buffer), last);
	}

	public void setContent(ByteBuf buffer) throws java.io.IOException {
		this._delegate.setContent(buffer);
	}

	public void addContent(ByteBuf buffer, boolean last)
		throws java.io.IOException {
		this._delegate.addContent(buffer, last);
	}

	public void setContent(File file) throws java.io.IOException {
		this._delegate.setContent(file);
	}

	public void setContent(InputStream inputStream) throws java.io.IOException {
		this._delegate.setContent(inputStream);
	}

	public boolean isCompleted() {
		return this._delegate.isCompleted();
	}

	public long length() {
		return this._delegate.length();
	}

	public long definedLength() {
		return this._delegate.definedLength();
	}

	public void delete() {
		this._delegate.delete();
	}

	public byte[] get() throws java.io.IOException {
		return this._delegate.get();
	}

	@Override
	public ByteBuffer getByteBuffer() throws IOException {
		return _delegate.getByteBuf().nioBuffer();
	}

	public ByteBuffer getByteBuf() throws java.io.IOException {
		return this._delegate.getByteBuf().nioBuffer();
	}

	public ByteBuffer getChunk(int length) throws java.io.IOException {
		return this._delegate.getChunk(length).nioBuffer();
	}

	public String getString() throws java.io.IOException {
		return this._delegate.getString();
	}

	public String getString(Charset encoding) throws java.io.IOException {
		return this._delegate.getString(encoding);
	}

	public void setCharset(Charset charset) {
		this._delegate.setCharset(charset);
	}

	public Charset getCharset() {
		return this._delegate.getCharset();
	}

	public boolean renameTo(File dest) throws java.io.IOException {
		return this._delegate.renameTo(dest);
	}

	public boolean isInMemory() {
		return this._delegate.isInMemory();
	}

	public File getFile() throws java.io.IOException {
		return this._delegate.getFile();
	}

	public String getName() {
		return this._delegate.getName();
	}

	public InterfaceHttpData.HttpDataType getHttpDataType() {
		return this._delegate.getHttpDataType();
	}

	public int compareTo(InterfaceHttpData o) {
		return this._delegate.compareTo(o);
	}

	public int refCnt() {
		return this._delegate.refCnt();
	}

	public boolean release() {
		return this._delegate.release();
	}

	public boolean release(int decrement) {
		return this._delegate.release(decrement);
	}

	public ByteBuffer content() {
		return this._delegate.content().nioBuffer();
	}

	public io.netty.handler.codec.http.multipart.FileUpload getDelegate() {
		return _delegate;
	}

	private final io.netty.handler.codec.http.multipart.FileUpload _delegate;

}
