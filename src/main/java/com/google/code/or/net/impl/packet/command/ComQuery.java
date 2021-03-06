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
package com.google.code.or.net.impl.packet.command;

import java.io.IOException;

import com.google.code.or.common.glossary.column.StringColumn;
import com.google.code.or.common.util.MySQLConstants;
import com.google.code.or.common.util.ToStringBuilder;
import com.google.code.or.io.util.XSerializer;

/**
 * 
 * @author Jingqi Xu
 */
public class ComQuery extends AbstractCommandPacket {
  //
  private static final long serialVersionUID = 1580858690926781520L;

  //
  private StringColumn sql;

  /**
	 * 
	 */
  public ComQuery() {
    super(MySQLConstants.COM_QUERY);
  }

  /**
	 * 
	 */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("sql", sql).toString();
  }

  /**
	 * 
	 */
  public byte[] getPacketBody() throws IOException {
    final XSerializer ps = new XSerializer();
    ps.writeInt(this.command, 1);
    ps.writeFixedLengthString(this.sql);
    return ps.toByteArray();
  }

  /**
	 * 
	 */
  public StringColumn getSql() {
    return sql;
  }

  public void setSql(StringColumn sql) {
    this.sql = sql;
  }
}
