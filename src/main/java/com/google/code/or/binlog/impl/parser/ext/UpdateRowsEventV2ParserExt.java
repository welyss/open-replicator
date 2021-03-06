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
import java.util.LinkedList;
import java.util.List;

import com.google.code.or.binlog.BinlogEventV4Header;
import com.google.code.or.binlog.BinlogParserContext;
import com.google.code.or.binlog.ext.XChecksum;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEventV2;
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.io.XInputStream;

/**
 * 
 * @author Arbore
 */
public class UpdateRowsEventV2ParserExt extends AbstractRowEventParserExt {

  /**
	 * 
	 */
  public UpdateRowsEventV2ParserExt(XChecksum checksum) {
    super(UpdateRowsEventV2.EVENT_TYPE, checksum);
  }

  /**
	 * 
	 */
  public void parse(XInputStream is, BinlogEventV4Header header, BinlogParserContext context)
      throws IOException {
    //
    final long tableId = is.readLong(6, checksum);
    final TableMapEvent tme = context.getTableMapEvent(tableId);
    if (this.rowEventFilter != null && !this.rowEventFilter.accepts(header, context, tme)) {
      is.skip(is.available());// CRC32
      checksum.reset();
      return;
    }

    //
    final UpdateRowsEventV2 event = new UpdateRowsEventV2(header);
    
	event.setDatabaseName(tme.getDatabaseName().toString());
	event.setTableName(tme.getTableName().toString());

    event.setTableId(tableId);
    event.setReserved(is.readInt(2, checksum));
    event.setExtraInfoLength(is.readInt(2, checksum));
    if (event.getExtraInfoLength() > 2)
      event.setExtraInfo(is.readBytes(event.getExtraInfoLength() - 2, checksum));
    event.setColumnCount(is.readUnsignedLong(checksum));
    event.setUsedColumnsBefore(is.readBit(event.getColumnCount().intValue(), checksum));
    event.setUsedColumnsAfter(is.readBit(event.getColumnCount().intValue(), checksum));
    event.setRows(parseRows(is, tme, event));
    checksum.validateAndReset(is.readInt(4));// CRC32
    context.getEventListener().onEvents(event);
  }

  /**
	 * 
	 */
  protected List<Pair<Row>> parseRows(XInputStream is, TableMapEvent tme, UpdateRowsEventV2 ure)
      throws IOException {
    final List<Pair<Row>> r = new LinkedList<Pair<Row>>();
    while (is.available() > 4) {
      final Row before = parseRow(is, tme, ure.getUsedColumnsBefore());
      final Row after = parseRow(is, tme, ure.getUsedColumnsAfter());
      r.add(new Pair<Row>(before, after));
    }
    return r;
  }
}
