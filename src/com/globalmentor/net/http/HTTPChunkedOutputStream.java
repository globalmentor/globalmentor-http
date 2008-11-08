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

import static com.globalmentor.java.Integers.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.http.HTTP.*;

/**An output stream that writes HTTP chunked content to an existing stream, but doesn't close the underlying stream when closed.
<p>This decorator provides convenience methods {@link #beforeClose()} and {@link #afterClose()} called before and after the stream is closed, respectively.</p>
<p>This implementation only writes complete chunks unless {@link #flush()} is called, in which case all data in the current chunk will be written.</p> 
<p>This stream should always be closed when access is finished; otherwise the underlying stream could be corrupted.</p>
<p>This class is not thread safe.</p>
@author Garret Wilson
*/
public class HTTPChunkedOutputStream extends OutputStream
{

	/**The default size of chunks if none is specified.*/
	public final static int DEFAULT_CHUNK_SIZE=64*1024;

	/**The output stream being decorated.*/
	private OutputStream outputStream;

	/**The buffer used to gather bytes to write in a chunk.*/
	private final byte[] chunk;

	/**The current number of bytes in the chunk.*/
	private int length;

	/**Whether the decorated stream should be closed when this stream is closed.*/
	private final boolean closeDecoratedStream;

	/**Decorates the given output stream using a default chunk size.
	The underlying stream will be closed when this stream is closed.
	@param outputStream The output stream to decorate.
	@throws NullPointerException if the given stream is <code>null</code>.
	*/
	public HTTPChunkedOutputStream(final OutputStream outputStream)
	{
		this(outputStream, true);
	}

	/**Decorates the given output stream using a default chunk size of {@value #DEFAULT_CHUNK_SIZE}.
	@param outputStream The output stream to decorate.
	@param closeDecoratedStream Whether the decorated stream should be closed when this stream is closed.
	@throws NullPointerException if the given stream is <code>null</code>.
	*/
	public HTTPChunkedOutputStream(final OutputStream outputStream, final boolean closeDecoratedStream)
	{
		this(outputStream, DEFAULT_CHUNK_SIZE, closeDecoratedStream);
	}

	/**Decorates the given output stream and provides a chunk size.
	The underlying stream will be closed when this stream is closed.
	@param outputStream The output stream to decorate.
	@param chunkSize The maximum number of bytes to use in each chunk.
	@throws NullPointerException if the given stream is <code>null</code>.
	@throws IllegalArgumentException if the given chunk size is less than or equal to zero.
	*/
	public HTTPChunkedOutputStream(final OutputStream outputStream, final int chunkSize)
	{
		this(outputStream, chunkSize, true);
	}

	/**Decorates the given output stream and provides a chunk size.
	@param outputStream The output stream to decorate.
	@param chunkSize The maximum number of bytes to use in each chunk.
	@param closeDecoratedStream Whether the decorated stream should be closed when this stream is closed.
	@throws NullPointerException if the given stream is <code>null</code>.
	@throws IllegalArgumentException if the given chunk size is less than or equal to zero.
	*/
	public HTTPChunkedOutputStream(final OutputStream outputStream, final int chunkSize, final boolean closeDecoratedStream)
	{
		this.outputStream=checkInstance(outputStream, "Output stream cannot be null.");	//save the decorated output stream
		this.closeDecoratedStream=closeDecoratedStream;
		chunk=new byte[checkMinimum(chunkSize, 1)];
		length=0;
	}


  /**
   * Writes the specified byte to this output stream. The general 
   * contract for <code>write</code> is that one byte is written 
   * to the output stream. The byte to be written is the eight 
   * low-order bits of the argument <code>b</code>. The 24 
   * high-order bits of <code>b</code> are ignored.
   * <p>
   * Subclasses of <code>OutputStream</code> must provide an 
   * implementation for this method. 
   *
   * @param      b   the <code>byte</code>.
   * @exception  IOException  if an I/O error occurs. In particular, 
   *             an <code>IOException</code> may be thrown if the 
   *             output stream has been closed.
   */
	public void write(int b) throws IOException
	{
  	if(outputStream==null)	//if this stream is closed
  	{
  		throw new IOException("Stream already closed.");
  	}
  	chunk[length++]=(byte)b;	//store the byte in the buffer and increment our length
  	if(length==chunk.length)	//if the chunk is full
  	{
  		flush();	//flush the chunk
  	}
	}

  /**
   * Writes <code>b.length</code> bytes from the specified byte array 
   * to this output stream. The general contract for <code>write(b)</code> 
   * is that it should have exactly the same effect as the call 
   * <code>write(b, 0, b.length)</code>.
   *
   * @param      b   the data.
   * @exception  IOException  if an I/O error occurs.
   * @see        java.io.OutputStream#write(byte[], int, int)
   */
  public final void write(byte b[]) throws IOException
	{
  	if(outputStream==null)	//if this stream is closed
  	{
  		throw new IOException("Stream already closed.");
  	}
  	write(b, 0, b.length);	//delegate to the other array-writing function
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this output stream. 
   * The general contract for <code>write(b, off, len)</code> is that 
   * some of the bytes in the array <code>b</code> are written to the 
   * output stream in order; element <code>b[off]</code> is the first 
   * byte written and <code>b[off+len-1]</code> is the last byte written 
   * by this operation.
   * <p>
   * The <code>write</code> method of <code>OutputStream</code> calls 
   * the write method of one argument on each of the bytes to be 
   * written out. Subclasses are encouraged to override this method and 
   * provide a more efficient implementation. 
   * <p>
   * If <code>b</code> is <code>null</code>, a 
   * <code>NullPointerException</code> is thrown.
   * <p>
   * If <code>off</code> is negative, or <code>len</code> is negative, or 
   * <code>off+len</code> is greater than the length of the array 
   * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs. In particular, 
   *             an <code>IOException</code> is thrown if the output 
   *             stream is closed.
   */
  public void write(byte b[], int off, int len) throws IOException
	{
  	if(outputStream==null)	//if this stream is closed
  	{
  		throw new IOException("Stream already closed.");
  	}
  	while(len>0)	//keep writing chunks until we run out of data
  	{
  		final int count=Math.min(len, chunk.length-length);	//for this go-around, don't write more information than our chunk has room for
  		arraycopy(b, 0, chunk, 0, count);	//write the bytes to the chunk
  		length+=count;	//keep track of how many bytes are in the chunk
    	if(length==chunk.length)	//if the chunk is full
    	{
    		flush();	//flush the chunk
    	}
  		len-=count;	//indicate that we have fewer bytes to write, now
  	}
  }

  /**
   * Flushes this output stream and forces any buffered output bytes 
   * to be written out. The general contract of <code>flush</code> is 
   * that calling it is an indication that, if any bytes previously 
   * written have been buffered by the implementation of the output 
   * stream, such bytes should immediately be written to their 
   * intended destination.
   * <p>
   * If the intended destination of this stream is an abstraction provided by
   * the underlying operating system, for example a file, then flushing the
   * stream guarantees only that bytes previously written to the stream are
   * passed to the operating system for writing; it does not guarantee that
   * they are actually written to a physical device such as a disk drive.
   * <p>
   * The <code>flush</code> method of <code>OutputStream</code> does nothing.
   *
   * @exception  IOException  if an I/O error occurs.
   */
  public void flush() throws IOException
	{
  	if(outputStream==null)	//if this stream is closed
  	{
  		throw new IOException("Stream already closed.");
  	}
  	if(length>0)	//if there is chunk data to write
  	{
  		outputStream.write((Integer.toHexString(length)+CRLF).getBytes(HTTP_CHARSET));	//write the size of the chunk, followed by CRLF
  		outputStream.write(chunk, 0, length);	//write whatever we have in the chunk
  		outputStream.write(CR);	//CRLF
  		outputStream.write(LF);
  		length=0;	//reset the length of our chunk
  	}
 		outputStream.flush();	//flush the underlying stream
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

	/**Closes this output stream and releases any system resources associated with this stream. 
	A closed stream cannot perform output operations and cannot be reopened.
	@exception IOException if an I/O error occurs.
	@see #beforeClose()
	@see #afterClose()
	@see #close(boolean)
	*/
  public void close() throws IOException 
	{
  	if(outputStream!=null)	//if the stream is still open
  	{
  		flush();	//flush the current chunk, if any
  		outputStream.write(("0"+CRLF+CRLF).getBytes(HTTP_CHARSET));	//write an empty chunk, followed by CRLF, followed by a blank line
  		beforeClose();	//perform actions before closing
  		if(closeDecoratedStream)	//if we should close the underlying stream
  		{
  			outputStream.close();
  		}
  		else	//if we shouldn't close the underlying stream
  		{
    		outputStream.flush();	//at least flush the data we sent to the output stream before we release it
  		}
  		outputStream=null;	//release the underlying output stream, but don't close it
  		afterClose();	//perform actions after closing
  	}
	}
}
