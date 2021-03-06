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
package com.google.code.or.binlog.impl.event;

import java.io.Serializable;

import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.BinlogEventV4Header;
import com.google.code.or.common.util.ToStringBuilder;

/**
 * 
 * @author Jingqi Xu
 */
public abstract class AbstractBinlogEventV4 implements BinlogEventV4, Serializable {
	private static final long serialVersionUID = -4013250615180782057L;
	//
	protected BinlogEventV4Header header;

	/**
	 * 
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("header", header).toString();
	}

	/**
	 * 
	 */
	public BinlogEventV4Header getHeader() {
		return header;
	}

	public void setHeader(BinlogEventV4Header header) {
		this.header = header;
	}
}
