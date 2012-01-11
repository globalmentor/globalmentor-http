/*
 * Copyright © 1996-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http;

import java.io.*;

import static java.lang.System.*;

import com.globalmentor.java.Bytes;

import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.http.HTTPParser.*;

/**
 * An input stream that reads HTTP chunked content from an existing stream, signaling the end of the stream when the chunks are finished.
 * <p>
 * This decorator provides convenience methods {@link #beforeClose()} and {@link #afterClose()} called before and after the stream is closed, respectively.
 * </p>
 * <p>
 * This stream should always be closed when access is finished; otherwise the underlying stream could be corrupted.
 * </p>
 * <p>
 * This implementation ignores chunked trailers.
 * </p>
 * <p>
 * This implementation does not support mark and reset.
 * </p>
 * <p>
 * This class is not thread safe.
 * </p>
 * @author Garret Wilson
 */
public class HTTPChunkedInputStream extends InputStream
{

	/** The input stream being decorated. */
	private InputStream inputStream;

	/** The current chunk being read, or <code>null</code> if the end of the chunks has been reached. */
	private byte[] chunk = Bytes.NO_BYTES;

	/**
	 * The current index within the chunk, or the length of the chunk if this chunk has been drained. This value is not specified if there is no current chunk.
	 */
	private int index = 0;

	/** The number of chunks read so far. */
	private long chunkCount = 0;

	/** @return The number of chunks read so far. */
	public long getChunkCount()
	{
		return chunkCount;
	}

	/**
	 * Loads another chunk from the underlying input stream and resets the index to the start of the chunk.
	 * <p>
	 * This methods guarantees that a non-empty chunk or <code>null</code> will be returned.
	 * </p>
	 * <p>
	 * This method must not be called again after it returns <code>null</code>.
	 * </p>
	 * @return The next chunk read, or <code>null</code> if the ending, empty chunk was reached and there are no further chunks.
	 * @throws IOException If there is an error reading the chunk.
	 * @throws IllegalStateException if the stream has already been closed.
	 */
	protected byte[] loadChunk() throws IOException
	{
		checkState(inputStream != null, "Input stream already closed.");
		byte[] chunk;
		do
		{
			chunk = parseChunk(inputStream); //parse another chunk from the input stream
			if(chunk != null)
			{
				++chunkCount;
			}
		}
		while(chunk != null && chunk.length == 0); //technically HTTP chunks can't be zero length, but this makes the algorithm more rigorous
		index = 0; //reset our position to the start of the chunk
		return chunk;
	}

	/** Whether the decorated stream should be closed when this stream is closed. */
	private final boolean closeDecoratedStream;

	/**
	 * Decorates the given input stream. The underlying stream will be closed when this stream is closed.
	 * @param inputStream The input stream to decorate.
	 * @throws NullPointerException if the given stream is <code>null</code>.
	 */
	public HTTPChunkedInputStream(final InputStream inputStream)
	{
		this(inputStream, true);
	}

	/**
	 * Decorates the given input stream.
	 * @param inputStream The input stream to decorate.
	 * @param closeDecoratedStream Whether the decorated stream should be closed when this stream is closed.
	 * @throws NullPointerException if the given stream is <code>null</code>.
	 */
	public HTTPChunkedInputStream(final InputStream inputStream, final boolean closeDecoratedStream)
	{
		this.inputStream = checkInstance(inputStream, "Input stream cannot be null."); //save the decorated input stream
		this.closeDecoratedStream = closeDecoratedStream;
	}

	/** {@inheritDoc} */
	@Override
	public int read() throws IOException
	{
		if(inputStream == null || chunk == null) //if this stream is closed or we're out of chunks
		{
			return -1;
		}
		//technically the HTTP protocol says the chunk shouldn't be empty,
		//but having a loop makes this logically rigorous for empty chunks
		while(index == chunk.length) //if we have drained this chunk
		{
			chunk = loadChunk(); //parse another chunk from the input stream
			if(chunk == null) //if we were unable to get another chunk
			{
				return -1;
			}
		}
		return chunk[index++]; //return the current data and advance the index
	}

	/** {@inheritDoc} This version delegates to {@link #read(byte[], int, int)}. */
	@Override
	public final int read(byte b[]) throws IOException
	{
		return read(b, 0, b.length); //let the other method take care of the fixed length
	}

	/** {@inheritDoc} */
	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		if(inputStream == null) //if this stream is closed
		{
			return -1;

		}
		//make sure we have a chunk with data or no chunk at all---otherwise we would wind up sending back zero bytes
		//the API contract prohibits sending back zero bytes, and can confuse the Sun XML parser, making it think there is content after the body
		if(chunk != null && index == chunk.length)
		{
			chunk = loadChunk(); //parse another chunk from the input stream
		}
		if(chunk == null) //if there is no data to begin with
		{
			return -1; //EOT
		}
		int total = 0;
		//they shouldn't ask for zero bytes, and we shouldn't return zero bytes, but if they ask for zero
		//we'll needlessly go through the loop and then give it to them like they asked
		do
		{
			final int count = Math.min(len, chunk.length - index); //don't read more from this chunk that there is left in the chunk
			arraycopy(chunk, index, b, off, count); //copy from the chunk
			total += count; //increase the total number of bytes read
			index += count; //advance our position in the chunk
			off += count; //advance the destination position in the buffer
			len -= count; //decrease the number of bytes we need to copy
			if(len > 0 && index == chunk.length) //if they want more data but we've reached the end of a chunk
			{
				chunk = loadChunk(); //load another chunk from the input stream
			}
		}
		while(chunk != null && len > 0); //keep reading until we run out of chunks or we don't need to read any more
		return total; //return the total bytes read
	}

	/** {@inheritDoc} */
	@Override
	public long skip(long n) throws IOException
	{
		if(inputStream == null) //if this stream is closed
		{
			return 0;
		}
		int total = 0;
		while(chunk != null && n > 0) //keep reading until we run out of chunks or we don't need to read any more
		{
			final long count = Math.min(n, chunk.length - index); //don't skip more from this chunk that there is left in the chunk
			total += count; //increase the total number of bytes read
			index += count; //advance our position in the chunk
			n -= count; //decrease the number of bytes we need to skip
			if(n > 0 && index == chunk.length) //if we need to read more bytes but we've drained the chunk
			{
				chunk = loadChunk(); //parse another chunk from the input stream
			}
		}
		return total; //return the total bytes skipped
	}

	/** {@inheritDoc} */
	@Override
	public int available() throws IOException
	{
		if(inputStream == null || chunk == null) //if this stream is closed or we're out of chunks
		{
			return 0;
		}
		return chunk.length - index; //show how many bytes are left in this chunk
	}

	/** {@inheritDoc} This implementation does nothing. */
	@Override
	public synchronized void mark(int readlimit)
	{
	}

	/** {@inheritDoc} This method throws an {@link IOException}, as mark/reset is not supported. */
	@Override
	public synchronized void reset() throws IOException
	{
		throw new IOException("Mark/reset not supported.");
	}

	/** {@inheritDoc} This method returns <code>false</code>. */
	@Override
	public boolean markSupported()
	{
		return false;
	}

	/**
	 * Called before the stream is closed.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void beforeClose() throws IOException
	{
	}

	/**
	 * Called after the stream is successfully closed.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void afterClose() throws IOException
	{
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		if(inputStream != null) //if we still have an input stream to decorate
		{
			while(chunk != null) //while there are more chunks, drain the input stream
			{
				chunk = loadChunk(); //parse another chunk from the input stream
			}
			parseHeaders(inputStream); //parse any trailers
			beforeClose(); //perform actions before closing
			if(closeDecoratedStream) //if we should close the underlying stream
			{
				inputStream.close();
			}
			inputStream = null; //release the underlying input stream, but don't close it
			afterClose(); //perform actions after closing
		}
	}
}
