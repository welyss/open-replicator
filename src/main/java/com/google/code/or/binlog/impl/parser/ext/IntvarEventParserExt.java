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
package com.google.code.or.binlog.impl.parser.ext;

import java.io.IOException;

import com.google.code.or.binlog.BinlogEventV4Header;
import com.google.code.or.binlog.BinlogParserContext;
import com.google.code.or.binlog.ext.XChecksum;
import com.google.code.or.binlog.impl.event.IntvarEvent;
import com.google.code.or.common.glossary.UnsignedLong;
import com.google.code.or.io.XInputStream;

/**
 * 
 * @author Arbore
 */
public class IntvarEventParserExt extends AbstractBinlogEventParserExt {

  /**
	 * 
	 */
  public IntvarEventParserExt(XChecksum checksum) {
    super(IntvarEvent.EVENT_TYPE, checksum);
  }

  /**
	 * 
	 */
  public void parse(XInputStream is, BinlogEventV4Header header, BinlogParserContext context)
      throws IOException {
    final IntvarEvent event = new IntvarEvent(header);
    event.setType(is.readInt(1, checksum));
    event.setValue(UnsignedLong.valueOf(is.readLong(8, checksum)));
    checksum.validateAndReset(is.readInt(4));// CRC32
    context.getEventListener().onEvents(event);
  }
}
