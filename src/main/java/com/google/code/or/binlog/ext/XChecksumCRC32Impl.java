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
package com.google.code.or.binlog.ext;

import java.util.zip.CRC32;

/**
 * 
 * @author Arbore
 * */
public class XChecksumCRC32Impl implements XChecksum {

  private CRC32 cksum = new CRC32();

  @Override
  public void update(int b) {
    this.cksum.update(b);
  }

  @Override
  public void update(byte[] b, int off, int len) {
    this.cksum.update(b, off, len);
  }

  @Override
  public long getValue() {
    return this.cksum.getValue();
  }

  @Override
  public void reset() {
    this.cksum.reset();
  }

  @Override
  public ChecksumType getType() {
    return ChecksumType.CRC32;
  }

  @Override
  public void validateAndReset(int expected) throws IllegalStateException {

//    if (expected != (int) this.getValue())
//      throw new IllegalStateException(
//          "Cyclic Redundancy Check (CRC32) Illegal State, the expected crc value is [" + expected
//              + "], but calculated value is [" + (int) this.getValue() + "]");
//
//    this.cksum.reset();
  }

}
