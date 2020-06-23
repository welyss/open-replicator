/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.code.or.io.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.google.code.or.binlog.ext.XChecksum;
import com.google.code.or.common.glossary.UnsignedLong;
import com.google.code.or.common.glossary.column.BitColumn;
import com.google.code.or.common.glossary.column.StringColumn;
import com.google.code.or.common.util.CodecUtils;
import com.google.code.or.io.ExceedLimitException;
import com.google.code.or.io.XInputStream;
import com.google.code.or.io.util.XSerializer;

/**
 * 
 * @author Jingqi Xu
 */
public class XInputStreamImpl extends InputStream implements XInputStream {
  //
  private int head = 0;
  private int tail = 0;
  private int readCount = 0;
  private int readLimit = 0;
  private final byte[] buffer;
  private final InputStream is;


  /**
	 * 
	 */
  public XInputStreamImpl(InputStream is) {
    this(is, 512 * 1024);
  }

  public XInputStreamImpl(InputStream is, int size) {
    this.is = is;
    this.buffer = new byte[size];
  }
  

  @Override
  public void resetStream() {
	  this.head = 0;
	  this.tail = 0;
	  this.readCount = 0;
	  this.readLimit = 0;
  }


  /**
	 * 
	 */
  public int readInt(int length) throws IOException {
    return readInt(length, true);
  }

  /**
   * 
   * @add
   * */
  public int readInt(int length, XChecksum checksum) throws IOException {
    return readInt(length, true, checksum);
  }

  public long readLong(int length) throws IOException {
    return readLong(length, true);
  }

  /**
   * 
   * @add
   * */
  public long readLong(int length, XChecksum checksum) throws IOException {
    return readLong(length, true, checksum);
  }

  public byte[] readBytes(int length) throws IOException {
    final byte[] r = new byte[length];
    this.read(r, 0, length);
    return r;
  }

  /**
   * 
   * @add
   * */
  public byte[] readBytes(int length, XChecksum checksum) throws IOException {
    final byte[] r = new byte[length];
    this.read(r, 0, length, checksum);
    return r;
  }

  public byte[] readBytesForBlob(int length, XChecksum checksum) throws IOException {
	final byte[] r = new byte[length];
	if (this.readLimit > 0 && (this.readCount + length) > this.readLimit) {
		int offset = this.readLimit - this.readCount;
		doRead(r, 0, offset, checksum);
		int left = length - offset;
		int loopCnt = left / this.readLimit;
		int mod = left % this.readLimit;
		if (mod > 0) {
			loopCnt++;
		}
		long lastPackageSize = this.readCount;
		for (int i = 0; i < loopCnt; i++) {
			lastPackageSize = readInt(3);
			skip(1);
			if (i == loopCnt - 1) {
				doRead(r, offset, mod, checksum);
			} else {
				doRead(r, offset += (this.readLimit), this.readLimit - 4, checksum);
			}
		}
		this.readCount = this.readLimit - (int) (lastPackageSize - (long) mod);
	} else {
		this.readCount += this.read(r, 0, length, checksum);
	}
	return r;
  }

  public BitColumn readBit(int length) throws IOException {
    return readBit(length, true);
  }

  /**
   * 
   * @add
   * */
  public BitColumn readBit(int length, XChecksum checksum) throws IOException {
    return readBit(length, true, checksum);
  }

  public UnsignedLong readUnsignedLong() throws IOException {
    final int v = this.read();
    if (v < 251)
      return UnsignedLong.valueOf(v);
    else if (v == 251)
      return null;
    else if (v == 252)
      return UnsignedLong.valueOf(readInt(2));
    else if (v == 253)
      return UnsignedLong.valueOf(readInt(3));
    else if (v == 254)
      return UnsignedLong.valueOf(readLong(8));
    else
      throw new RuntimeException("assertion failed, should NOT reach here");
  }

  /**
   * 
   * @add
   * */
  public UnsignedLong readUnsignedLong(XChecksum checksum) throws IOException {
    final int v = this.read(checksum);
    if (v < 251)
      return UnsignedLong.valueOf(v);
    else if (v == 251)
      return null;
    else if (v == 252)
      return UnsignedLong.valueOf(readInt(2, checksum));
    else if (v == 253)
      return UnsignedLong.valueOf(readInt(3, checksum));
    else if (v == 254)
      return UnsignedLong.valueOf(readLong(8, checksum));
    else
      throw new RuntimeException("assertion failed, should NOT reach here");
  }

  public StringColumn readLengthCodedString() throws IOException {
    final UnsignedLong length = readUnsignedLong();
    return length == null ? null : readFixedLengthString(length.intValue());
  }

  /**
   * 
   * @add
   * */
  public StringColumn readLengthCodedString(XChecksum checksum) throws IOException {
    final UnsignedLong length = readUnsignedLong(checksum);
    return length == null ? null : readFixedLengthString(length.intValue(), checksum);
  }

  public StringColumn readNullTerminatedString() throws IOException {
    final XSerializer s = new XSerializer(128); // 128 should be OK for most schema names
    while (true) {
      final int v = this.read();
      if (v == 0) break;
      s.writeInt(v, 1);
    }
    return StringColumn.valueOf(s.toByteArray());
  }

  /**
   * 
   * @add
   * */
  public StringColumn readNullTerminatedString(XChecksum checksum) throws IOException {
    final XSerializer s = new XSerializer(128); // 128 should be OK for most schema names
    while (true) {
      final int v = this.read(checksum);
      if (v == 0) break;
      s.writeInt(v, 1);
    }
    return StringColumn.valueOf(s.toByteArray());
  }

  public StringColumn readFixedLengthString(final int length) throws IOException {
    return StringColumn.valueOf(readBytes(length));
  }

  /**
   * 
   * @add
   * */
  public StringColumn readFixedLengthString(final int length, XChecksum checksum)
      throws IOException {
    return StringColumn.valueOf(readBytes(length, checksum));
  }

  /**
	 * 
	 */
  public int readSignedInt(int length) throws IOException {
    int r = 0;
    for (int i = 0; i < length; ++i) {
      final int v = this.read();
      r |= (v << (i << 3));
      if ((i == length - 1) && ((v & 0x80) == 0x80)) {
        for (int j = length; j < 4; j++) {
          r |= (255 << (j << 3));
        }
      }
    }
    return r;
  }

  /**
   * 
   * @add
   * */
  public int readSignedInt(int length, XChecksum checksum) throws IOException {
    int r = 0;
    for (int i = 0; i < length; ++i) {
      final int v = this.read(checksum);
      r |= (v << (i << 3));
      if ((i == length - 1) && ((v & 0x80) == 0x80)) {
        for (int j = length; j < 4; j++) {
          r |= (255 << (j << 3));
        }
      }
    }
    return r;
  }

  public long readSignedLong(int length) throws IOException {
    long r = 0;
    for (int i = 0; i < length; ++i) {
      final long v = this.read();
      r |= (v << (i << 3));
      if ((i == length - 1) && ((v & 0x80) == 0x80)) {
        for (int j = length; j < 8; j++) {
          r |= (255 << (j << 3));
        }
      }
    }
    return r;
  }

  /**
   * 
   * @add
   * */
  public long readSignedLong(int length, XChecksum checksum) throws IOException {
    long r = 0;
    for (int i = 0; i < length; ++i) {
      final long v = this.read(checksum);
      r |= (v << (i << 3));
      if ((i == length - 1) && ((v & 0x80) == 0x80)) {
        for (int j = length; j < 8; j++) {
          r |= (255 << (j << 3));
        }
      }
    }
    return r;
  }

  public int readInt(int length, boolean littleEndian) throws IOException {
    int r = 0;
    for (int i = 0; i < length; ++i) {
      final int v = this.read();
      if (littleEndian) {
        r |= (v << (i << 3));
      } else {
        r = (r << 8) | v;
      }
    }
    return r;
  }

  /**
   * 
   * @add
   * */
  public int readInt(int length, boolean littleEndian, XChecksum checksum) throws IOException {
    int r = 0;
    for (int i = 0; i < length; ++i) {
      final int v = this.read(checksum);
      if (littleEndian) {
        r |= (v << (i << 3));
      } else {
        r = (r << 8) | v;
      }
    }
    return r;
  }

  public long readLong(int length, boolean littleEndian) throws IOException {
    long r = 0;
    for (int i = 0; i < length; ++i) {
      final long v = this.read();
      if (littleEndian) {
        r |= (v << (i << 3));
      } else {
        r = (r << 8) | v;
      }
    }
    return r;
  }

  /**
   * 
   * @add
   * */
  public long readLong(int length, boolean littleEndian, XChecksum checksum) throws IOException {
    long r = 0;
    for (int i = 0; i < length; ++i) {
      final long v = this.read(checksum);
      if (littleEndian) {
        r |= (v << (i << 3));
      } else {
        r = (r << 8) | v;
      }
    }
    return r;
  }

  public BitColumn readBit(int length, boolean littleEndian) throws IOException {
    byte[] bytes = readBytes((int) ((length + 7) >> 3));
    if (!littleEndian) bytes = CodecUtils.toBigEndian(bytes);
    return BitColumn.valueOf(length, bytes);
  }

  /**
   * 
   * @add
   * */
  public BitColumn readBit(int length, boolean littleEndian, XChecksum checksum) throws IOException {
    byte[] bytes = readBytes((int) ((length + 7) >> 3), checksum);
    if (!littleEndian) bytes = CodecUtils.toBigEndian(bytes);
    return BitColumn.valueOf(length, bytes);
  }

  /**
	 * 
	 */
  @Override
  public void close() throws IOException {
    this.is.close();
  }

  public void setReadLimit(final int limit) throws IOException {
    this.readCount = 0;
    this.readLimit = limit;
  }

  @Override
  public int available() throws IOException {
    if (this.readLimit > 0) {
      return this.readLimit - this.readCount;
    } else {
      return this.tail - this.head + this.is.available();
    }
  }

  public boolean hasMore() throws IOException {
    if (this.head < this.tail) return true;
    return this.available() > 0;
  }

  @Override
  public long skip(final long n) throws IOException {
    if (this.readLimit > 0 && (this.readCount + n) > this.readLimit) {
      this.readCount += doSkip(this.readLimit - this.readCount);
      throw new ExceedLimitException();
    } else {
      this.readCount += doSkip(n);
      return n; // always skip the number of bytes specified by parameter "n"
    }
  }

  /**
   * skip some bytes, but checksum still work.
   * 
   * @add
   * */
  @Override
  public long skip(final long n, XChecksum checksum) throws IOException {
    if (this.readLimit > 0 && (this.readCount + n) > this.readLimit) {
      this.readCount += doSkip(this.readLimit - this.readCount, checksum);
      throw new ExceedLimitException();
    } else {
      this.readCount += doSkip(n, checksum);
      return n; // always skip the number of bytes specified by parameter "n"
    }
  }

  @Override
  public int read() throws IOException {
    if (this.readLimit > 0 && (this.readCount + 1) > this.readLimit) {
      throw new ExceedLimitException();
    } else {
      if (this.head >= this.tail) doFill();
      final int r = this.buffer[this.head++] & 0xFF;
      ++this.readCount;
      return r;
    }
  }

  /**
   * 
   * @add
   * */
  @Override
  public int read(XChecksum checksum) throws IOException {
    if (this.readLimit > 0 && (this.readCount + 1) > this.readLimit) {
      throw new ExceedLimitException();
    } else {
      if (this.head >= this.tail) doFill();
      final int r = this.buffer[this.head++] & 0xFF;
      checksum.update(r);// add
      ++this.readCount;
      return r;
    }
  }

  @Override
  public int read(final byte b[], final int off, final int len) throws IOException {
    if (this.readLimit > 0 && (this.readCount + len) > this.readLimit) {
      this.readCount += doRead(b, off, this.readLimit - this.readCount);
      throw new ExceedLimitException();
    } else {
      this.readCount += doRead(b, off, len);
      return len; // always read the number of bytes specified by parameter "len"
    }
  }

  /**
   * 
   * @add
   * */
  @Override
  public int read(final byte b[], final int off, final int len, XChecksum checksum)
      throws IOException {
    if (this.readLimit > 0 && (this.readCount + len) > this.readLimit) {
      this.readCount += doRead(b, off, this.readLimit - this.readCount, checksum);
      throw new ExceedLimitException();
    } else {
      this.readCount += doRead(b, off, len, checksum);
      return len; // always read the number of bytes specified by parameter "len"
    }
  }

  /**
	 * 
	 */
  private void doFill() throws IOException {// with no need for checksum at this step (in read
                                            // step!)
    this.head = 0;
    this.tail = this.is.read(this.buffer, 0, this.buffer.length);
    if (this.tail <= 0) 
    	throw new EOFException();
  }

  private long doSkip(final long n) throws IOException {
    long total = n;
    while (total > 0) {
      final int availabale = this.tail - this.head;
      if (availabale >= total) {
        this.head += total;
        break;
      } else {
        total -= availabale;
        doFill();
      }
    }
    return n;
  }

  /**
   * 
   * @add
   * */
  private long doSkip(final long n, XChecksum checksum) throws IOException {
    long total = n;
    while (total > 0) {
      final int availabale = this.tail - this.head;
      if (availabale >= total) {
        checksum.update(this.buffer, this.head, (int) total);// XXX not verified!
        this.head += total;
        break;
      } else {
        checksum.update(this.buffer, this.head, (int) total);// XXX not verified!
        total -= availabale;
        doFill();
      }
    }
    return n;
  }

  private int doRead(final byte[] b, final int off, final int len) throws IOException {
    int total = len;
    int index = off;
    while (total > 0) {
      final int available = this.tail - this.head;
      if (available >= total) {
        System.arraycopy(this.buffer, this.head, b, index, total);
        this.head += total;
        break;
      } else {
        System.arraycopy(this.buffer, this.head, b, index, available);
        index += available;
        total -= available;
        doFill();
      }
    }
    return len;
  }

  /**
   * 
   * @add
   * */
  private int doRead(final byte[] b, final int off, final int len, XChecksum checksum)
      throws IOException {
    int total = len;
    int index = off;
    while (total > 0) {
      final int available = this.tail - this.head;
      if (available >= total) {
        System.arraycopy(this.buffer, this.head, b, index, total);
        checksum.update(this.buffer, this.head, total);
        this.head += total;
        break;
      } else {
        System.arraycopy(this.buffer, this.head, b, index, available);
        checksum.update(this.buffer, this.head, available);
        index += available;
        total -= available;
        doFill();
      }
    }
    return len;
  }

}
