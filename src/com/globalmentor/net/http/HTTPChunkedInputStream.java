/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import com.globalmentor.util.Debug;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.http.HTTPParser.*;

/**An input stream that reads HTTP chunked content from an existing stream, signalling the end of the stream when the chunks are finished.
<p>This decorator provides convenience methods {@link #beforeClose()} and {@link #afterClose()} called before and after the stream is closed, respectively.</p>
<p>This stream should always be closed when access is finished; otherwise the underlying stream could be corrupted.</p>
<p>This implementation ignores chunked trailers.</p>
<p>This implementation does not support mark and reset.</p>
<p>This class is not thread safe.</p>
@author Garret Wilson
*/
public class HTTPChunkedInputStream extends InputStream
{

	/**The input stream being decorated.*/
	private InputStream inputStream;

	/**The current chunk being read, or <code>null</code> if the end of the chunks has been reached.*/
	private byte[] chunk=Bytes.NO_BYTES;

	/**The current index within the chunk, or the length of the chunk if this chunk has been drained.
	This value is not specified if there is no current chunk.
	*/
	private int index=0;

	/**Whether the decorated stream should be closed when this stream is closed.*/
	private final boolean closeDecoratedStream;

	/**Decorates the given input stream.
	The underlying stream will be closed when this stream is closed.
	@param inputStream The input stream to decorate.
	@exception NullPointerException if the given stream is <code>null</code>.
	*/
	public HTTPChunkedInputStream (final InputStream inputStream)
	{
		this(inputStream, true);
	}

	/**Decorates the given input stream.
	@param inputStream The input stream to decorate.
	@param closeDecoratedStream Whether the decorated stream should be closed when this stream is closed.
	@exception NullPointerException if the given stream is <code>null</code>.
	*/
	public HTTPChunkedInputStream (final InputStream inputStream, final boolean closeDecoratedStream)
	{
		this.inputStream=checkInstance(inputStream, "Input stream cannot be null.");	//save the decorated input stream
		this.closeDecoratedStream=closeDecoratedStream;
	}

  /**
   * Reads the next byte of data from the input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the stream
   * has been reached, the value <code>-1</code> is returned. This method
   * blocks until input data is available, the end of the stream is detected,
   * or an exception is thrown.
   *
   * <p> A subclass must provide an implementation of this method.
   *
   * @return     the next byte of data, or <code>-1</code> if the end of the
   *             stream is reached.
   * @exception  IOException  if an I/O error occurs.
   */
  public int read() throws IOException
	{
  	if(inputStream==null || chunk==null)	//if this stream is closed or we're out of chunks
  	{
  		return -1;
  	}
  	if(index==chunk.length)	//if we have drained this chunk
  	{
  		chunk=parseChunk(inputStream);	//parse another chunk from the input stream
  		index=0;	//reset our position to the start of the chunk
  		if(chunk==null)	//if we were unable to get another chunk
  		{
  			return -1;
  		}
  	}
  	return chunk[index++];	//return the current data and advance the index
	}

  /**
   * Reads some number of bytes from the input stream and stores them into
   * the buffer array <code>b</code>. The number of bytes actually read is
   * returned as an integer.  This method blocks until input data is
   * available, end of file is detected, or an exception is thrown.
   *
   * <p> If <code>b</code> is <code>null</code>, a
   * <code>NullPointerException</code> is thrown.  If the length of
   * <code>b</code> is zero, then no bytes are read and <code>0</code> is
   * returned; otherwise, there is an attempt to read at least one byte. If
   * no byte is available because the stream is at end of file, the value
   * <code>-1</code> is returned; otherwise, at least one byte is read and
   * stored into <code>b</code>.
   *
   * <p> The first byte read is stored into element <code>b[0]</code>, the
   * next one into <code>b[1]</code>, and so on. The number of bytes read is,
   * at most, equal to the length of <code>b</code>. Let <i>k</i> be the
   * number of bytes actually read; these bytes will be stored in elements
   * <code>b[0]</code> through <code>b[</code><i>k</i><code>-1]</code>,
   * leaving elements <code>b[</code><i>k</i><code>]</code> through
   * <code>b[b.length-1]</code> unaffected.
   *
   * <p> If the first byte cannot be read for any reason other than end of
   * file, then an <code>IOException</code> is thrown. In particular, an
   * <code>IOException</code> is thrown if the input stream has been closed.
   *
   * <p> The <code>read(b)</code> method for class <code>InputStream</code>
   * has the same effect as: <pre><code> read(b, 0, b.length) </code></pre>
   *
   * @param      b   the buffer into which the data is read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> is there is no more data because the end of
   *             the stream has been reached.
   * @exception  IOException  if an I/O error occurs.
   * @exception  NullPointerException  if <code>b</code> is <code>null</code>.
   * @see        java.io.InputStream#read(byte[], int, int)
   */
  public final int read(byte b[]) throws IOException
	{
  	return read(b, 0, b.length);	//let the other method take care of the fixed length
	}

  /**
   * Reads up to <code>len</code> bytes of data from the input stream into
   * an array of bytes.  An attempt is made to read as many as
   * <code>len</code> bytes, but a smaller number may be read.
   * The number of bytes actually read is returned as an integer.
   *
   * <p> This method blocks until input data is available, end of file is
   * detected, or an exception is thrown.
   *
   * <p> If <code>b</code> is <code>null</code>, a
   * <code>NullPointerException</code> is thrown.
   *
   * <p> If <code>off</code> is negative, or <code>len</code> is negative, or
   * <code>off+len</code> is greater than the length of the array
   * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is
   * thrown.
   *
   * <p> If <code>len</code> is zero, then no bytes are read and
   * <code>0</code> is returned; otherwise, there is an attempt to read at
   * least one byte. If no byte is available because the stream is at end of
   * file, the value <code>-1</code> is returned; otherwise, at least one
   * byte is read and stored into <code>b</code>.
   *
   * <p> The first byte read is stored into element <code>b[off]</code>, the
   * next one into <code>b[off+1]</code>, and so on. The number of bytes read
   * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
   * bytes actually read; these bytes will be stored in elements
   * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
   * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
   * <code>b[off+len-1]</code> unaffected.
   *
   * <p> In every case, elements <code>b[0]</code> through
   * <code>b[off]</code> and elements <code>b[off+len]</code> through
   * <code>b[b.length-1]</code> are unaffected.
   *
   * <p> If the first byte cannot be read for any reason other than end of
   * file, then an <code>IOException</code> is thrown. In particular, an
   * <code>IOException</code> is thrown if the input stream has been closed.
   *
   * <p> The <code>read(b,</code> <code>off,</code> <code>len)</code> method
   * for class <code>InputStream</code> simply calls the method
   * <code>read()</code> repeatedly. If the first such call results in an
   * <code>IOException</code>, that exception is returned from the call to
   * the <code>read(b,</code> <code>off,</code> <code>len)</code> method.  If
   * any subsequent call to <code>read()</code> results in a
   * <code>IOException</code>, the exception is caught and treated as if it
   * were end of file; the bytes read up to that point are stored into
   * <code>b</code> and the number of bytes read before the exception
   * occurred is returned.  Subclasses are encouraged to provide a more
   * efficient implementation of this method.
   *
   * @param      b     the buffer into which the data is read.
   * @param      off   the start offset in array <code>b</code>
   *                   at which the data is written.
   * @param      len   the maximum number of bytes to read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> if there is no more data because the end of
   *             the stream has been reached.
   * @exception  IOException  if an I/O error occurs.
   * @exception  NullPointerException  if <code>b</code> is <code>null</code>.
   * @see        java.io.InputStream#read()
   */
  public int read(byte b[], int off, int len) throws IOException
	{
  	if(inputStream==null || chunk==null)	//if this stream is closed or we're out of chunks
  	{
  		return -1;
  	}
  	int total=0;
  	while(chunk!=null && len>0)	//keep reading until we run out of chunks or we don't need to read any more
  	{
  		final int count=Math.min(len, chunk.length-index);	//don't read more from this chunk that there is left in the chunk
  		arraycopy(chunk, index, b, off, count);	//copy from the chunk
  		total+=count;	//increase the total number of bytes read
  		index+=count;	//advance our position in the chunk
  		off+=count;	//advance the destination position in the buffer
  		len-=count;	//decrease the number of bytes we need to copy
  		if(len>0 && index==chunk.length)	//if we need to read more bytes but we've drained the chunk
  		{
	  		chunk=parseChunk(inputStream);	//parse another chunk from the input stream
	  		index=0;	//reset our position to the start of the chunk
  		}
  	}
  	return total;	//return the total bytes read
	}

  /**
   * Skips over and discards <code>n</code> bytes of data from this input
   * stream. The <code>skip</code> method may, for a variety of reasons, end
   * up skipping over some smaller number of bytes, possibly <code>0</code>.
   * This may result from any of a number of conditions; reaching end of file
   * before <code>n</code> bytes have been skipped is only one possibility.
   * The actual number of bytes skipped is returned.  If <code>n</code> is
   * negative, no bytes are skipped.
   *
   * <p> The <code>skip</code> method of <code>InputStream</code> creates a
   * byte array and then repeatedly reads into it until <code>n</code> bytes
   * have been read or the end of the stream has been reached. Subclasses are
   * encouraged to provide a more efficient implementation of this method.
   *
   * @param      n   the number of bytes to be skipped.
   * @return     the actual number of bytes skipped.
   * @exception  IOException  if an I/O error occurs.
   */
  public long skip(long n) throws IOException
	{
  	if(inputStream==null)	//if this stream is closed
  	{
  		return 0;
  	}
  	int total=0;
  	while(chunk!=null && n>0)	//keep reading until we run out of chunks or we don't need to read any more
  	{
  		final long count=Math.min(n, chunk.length-index);	//don't skip more from this chunk that there is left in the chunk
  		total+=count;	//increase the total number of bytes read
  		index+=count;	//advance our position in the chunk
  		n-=count;	//decrease the number of bytes we need to skip
  		if(n>0 && index==chunk.length)	//if we need to read more bytes but we've drained the chunk
  		{
	  		chunk=parseChunk(inputStream);	//parse another chunk from the input stream
	  		index=0;	//reset our position to the start of the chunk
  		}
  	}
  	return total;	//return the total bytes skipped
	}

  /**
   * Returns the number of bytes that can be read (or skipped over) from
   * this input stream without blocking by the next caller of a method for
   * this input stream.  The next caller might be the same thread or
   * another thread.
   *
   * <p> The <code>available</code> method for class <code>InputStream</code>
   * always returns <code>0</code>.
   *
   * <p> This method should be overridden by subclasses.
   *
   * @return     the number of bytes that can be read from this input stream
   *             without blocking.
   * @exception  IOException  if an I/O error occurs.
   */
  public int available() throws IOException
	{
  	if(inputStream==null || chunk==null)	//if this stream is closed or we're out of chunks
  	{
  		return 0;
  	}
  	return chunk.length-index;	//show how many bytes are left in this chunk
	}

  /**
   * Marks the current position in this input stream. A subsequent call to
   * the <code>reset</code> method repositions this stream at the last marked
   * position so that subsequent reads re-read the same bytes.
   *
   * <p> The <code>readlimit</code> arguments tells this input stream to
   * allow that many bytes to be read before the mark position gets
   * invalidated.
   *
   * <p> The general contract of <code>mark</code> is that, if the method
   * <code>markSupported</code> returns <code>true</code>, the stream somehow
   * remembers all the bytes read after the call to <code>mark</code> and
   * stands ready to supply those same bytes again if and whenever the method
   * <code>reset</code> is called.  However, the stream is not required to
   * remember any data at all if more than <code>readlimit</code> bytes are
   * read from the stream before <code>reset</code> is called.
   *
   * <p> The <code>mark</code> method of <code>InputStream</code> does
   * nothing.
   *
   * @param   readlimit   the maximum limit of bytes that can be read before
   *                      the mark position becomes invalid.
   * @see     java.io.InputStream#reset()
   */
  public synchronized void mark(int readlimit)
	{
	}

  /**
   * Repositions this stream to the position at the time the
   * <code>mark</code> method was last called on this input stream.
   *
   * <p> The general contract of <code>reset</code> is:
   *
   * <p><ul>
   *
   * <li> If the method <code>markSupported</code> returns
   * <code>true</code>, then:
   *
   *     <ul><li> If the method <code>mark</code> has not been called since
   *     the stream was created, or the number of bytes read from the stream
   *     since <code>mark</code> was last called is larger than the argument
   *     to <code>mark</code> at that last call, then an
   *     <code>IOException</code> might be thrown.
   *
   *     <li> If such an <code>IOException</code> is not thrown, then the
   *     stream is reset to a state such that all the bytes read since the
   *     most recent call to <code>mark</code> (or since the start of the
   *     file, if <code>mark</code> has not been called) will be resupplied
   *     to subsequent callers of the <code>read</code> method, followed by
   *     any bytes that otherwise would have been the next input data as of
   *     the time of the call to <code>reset</code>. </ul>
   *
   * <li> If the method <code>markSupported</code> returns
   * <code>false</code>, then:
   *
   *     <ul><li> The call to <code>reset</code> may throw an
   *     <code>IOException</code>.
   *
   *     <li> If an <code>IOException</code> is not thrown, then the stream
   *     is reset to a fixed state that depends on the particular type of the
   *     input stream and how it was created. The bytes that will be supplied
   *     to subsequent callers of the <code>read</code> method depend on the
   *     particular type of the input stream. </ul></ul>
   *
   * <p>The method <code>reset</code> for class <code>InputStream</code>
   * does nothing except throw an <code>IOException</code>.
   *
   * @exception  IOException  if this stream has not been marked or if the
   *               mark has been invalidated.
   * @see     java.io.InputStream#mark(int)
   * @see     java.io.IOException
   */
  public synchronized void reset() throws IOException
	{
  	throw new IOException("Mark/reset not supported.");
	}

  /**
   * Tests if this input stream supports the <code>mark</code> and
   * <code>reset</code> methods. Whether or not <code>mark</code> and
   * <code>reset</code> are supported is an invariant property of a
   * particular input stream instance. The <code>markSupported</code> method
   * of <code>InputStream</code> returns <code>false</code>.
   *
   * @return  <code>true</code> if this stream instance supports the mark
   *          and reset methods; <code>false</code> otherwise.
   * @see     java.io.InputStream#mark(int)
   * @see     java.io.InputStream#reset()
   */
  public boolean markSupported()
	{
  	return false;
	}

  /**Called before the stream is closed.
	@exception IOException if an I/O error occurs.
	*/
  protected void beforeClose() throws IOException 
  {
  }

  /**Called after the stream is successfully closed.
	@exception IOException if an I/O error occurs.
	*/
  protected void afterClose() throws IOException
  {
  }

	/**Closes this input stream and releases any system resources associated with the stream.
	A closed stream cannot perform output operations and cannot be reopened.
	@exception IOException if an I/O error occurs.
	*/
	public void close() throws IOException
	{
  	if(inputStream!=null)	//if we still have an input stream to decorate
  	{
  		while(chunk!=null)	//while there are more chunks, drain the input stream
  		{
    		chunk=parseChunk(inputStream);	//parse another chunk from the input stream
  		}
  		parseHeaders(inputStream);	//parse any trailers
  		beforeClose();	//perform actions before closing
  		if(closeDecoratedStream)	//if we should close the underlying stream
  		{
  			inputStream.close();
  		}
  		inputStream=null;	//release the underlying input stream, but don't close it
  		afterClose();	//perform actions after closing
  	}
	}
}
