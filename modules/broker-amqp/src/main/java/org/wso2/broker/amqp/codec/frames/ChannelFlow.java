/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpConnectionHandler;

/**
 * AMQP frame for channel.flow
 * Parameter Summary:
 *     1.active (bit) - current flow setting
 */
public class ChannelFlow extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFlow.class);

    private final boolean active;

    public ChannelFlow(int channel, boolean active) {
        super(channel, (short) 20, (short) 20);
        this.active = active;
    }

    @Override
    protected long getMethodBodySize() {
        return 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeBoolean(active);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // TODO handle channel flow
        ctx.writeAndFlush(new ChannelFlowOk(getChannel(), active));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            boolean active = buf.readBoolean();
            return new ChannelFlow(channel, active);
        };
    }
}
