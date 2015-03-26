/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.downstream;

import com.cyanspring.common.message.ErrorMessage;

public class DownStreamException extends Exception {
	private static final long serialVersionUID = -8391613503025566757L;
	private ErrorMessage clientMessage;

	public DownStreamException(String message) {
		super(message);
	}
	public DownStreamException(String localMessage,ErrorMessage clientMessage) {
		
		super(localMessage);
		this.clientMessage = clientMessage;
		
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
}
