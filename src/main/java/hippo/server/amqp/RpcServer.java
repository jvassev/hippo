package hippo.server.amqp;

//The contents of this file are subject to the Mozilla Public License
//Version 1.1 (the "License"); you may not use this file except in
//compliance with the License. You may obtain a copy of the License
//at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS"
//basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
//the License for the specific language governing rights and
//limitations under the License.
//
//The Original Code is RabbitMQ.
//
//The Initial Developer of the Original Code is VMware, Inc.
//Copyright (c) 2007-2011 VMware, Inc.  All rights reserved.
//


import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * Class which manages a request queue for a simple RPC-style service. The class
 * is agnostic about the format of RPC arguments / return values.
 */
public class RpcServer {

    /** Channel we are communicating on */
    private final Channel _channel;

    /** Queue to receive requests from */
    private final String _queueName;

    /** Boolean controlling the exit from the mainloop. */
    private boolean _mainloopRunning = true;

    /** Consumer attached to our request queue */
    private DefaultConsumer _consumer;

    /**
     * Creates an RpcServer listening on a temporary exclusive autodelete queue.
     */
    public RpcServer(Channel channel) throws IOException {
        this(channel, null);
    }

    /**
     * If the passed-in queue name is null, creates a server-named temporary
     * exclusive autodelete queue to use; otherwise expects the queue to have
     * already been declared.
     */
    public RpcServer(Channel channel, String queueName) throws IOException {
        _channel = channel;
        if (queueName == null || queueName.equals("")) {
            _queueName = _channel.queueDeclare().getQueue();
        } else {
            _queueName = queueName;
        }
        _consumer = setupConsumer();
    }

    /**
     * Public API - cancels the consumer, thus deleting the queue, if it was a
     * temporary queue, and marks the RpcServer as closed.
     * 
     * @throws IOException
     *             if an error is encountered
     */
    public void close() throws IOException {
        if (_consumer != null) {
            _channel.basicCancel(_consumer.getConsumerTag());
            _consumer = null;
        }
        terminateMainloop();
    }

    /**
     * Registers a consumer on the reply queue.
     * 
     * @throws IOException
     *             if an error is encountered
     * @return the newly created and registered consumer
     */
    protected DefaultConsumer setupConsumer() throws IOException {
        DefaultConsumer consumer = new DefaultConsumer(_channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                    throws IOException {
                if (_mainloopRunning) {
                    Delivery request = new Delivery(envelope, properties, body);
                    processRequest(request);
                }
            }
        };
        _channel.basicConsume(_queueName, true, consumer);
        return consumer;
    }

    /**
     * Public API - main server loop. Call this to begin processing requests.
     * Request processing will continue until the Channel (or its underlying
     * Connection) is shut down, or until terminateMainloop() is called.
     * 
     * Note that if the mainloop is blocked waiting for a request, the
     * termination flag is not checked until a request is received, so a good
     * time to call terminateMainloop() is during a request handler.
     * 
     * @return the exception that signalled the Channel shutdown, or null for
     *         orderly shutdown
     */
    public void start() {
        _mainloopRunning = true;
    }

    /**
     * Call this method to terminate the mainloop.
     * 
     * Note that if the mainloop is blocked waiting for a request, the
     * termination flag is not checked until a request is received, so a good
     * time to call terminateMainloop() is during a request handler.
     */
    public void terminateMainloop() {
        _mainloopRunning = false;
    }

    /**
     * Private API - Process a single request. Called from mainloop().
     */
    public void processRequest(QueueingConsumer.Delivery request) throws IOException {
        AMQP.BasicProperties requestProperties = request.getProperties();
        String correlationId = requestProperties.getCorrelationId();
        String replyTo = requestProperties.getReplyTo();
        if (correlationId != null && replyTo != null) {
            AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.Builder().correlationId(correlationId)
                    .build();
            byte[] replyBody = handleCall(request, replyProperties);
            _channel.basicPublish("", replyTo, replyProperties, replyBody);
        } else {
            handleCast(request);
        }
    }

    /**
     * Lowest-level response method. Calls
     * handleCall(AMQP.BasicProperties,byte[],AMQP.BasicProperties).
     */
    public byte[] handleCall(QueueingConsumer.Delivery request, AMQP.BasicProperties replyProperties) {
        return handleCall(request.getProperties(), request.getBody(), replyProperties);
    }

    /**
     * Mid-level response method. Calls handleCall(byte[],AMQP.BasicProperties).
     */
    public byte[] handleCall(AMQP.BasicProperties requestProperties, byte[] requestBody,
            AMQP.BasicProperties replyProperties) {
        return handleCall(requestBody, replyProperties);
    }

    /**
     * High-level response method. Returns an empty response by default -
     * override this (or other handleCall and handleCast methods) in subclasses.
     */
    public byte[] handleCall(byte[] requestBody, AMQP.BasicProperties replyProperties) {
        return new byte[0];
    }

    /**
     * Lowest-level handler method. Calls
     * handleCast(AMQP.BasicProperties,byte[]).
     */
    public void handleCast(QueueingConsumer.Delivery request) {
        handleCast(request.getProperties(), request.getBody());
    }

    /**
     * Mid-level handler method. Calls handleCast(byte[]).
     */
    public void handleCast(AMQP.BasicProperties requestProperties, byte[] requestBody) {
        handleCast(requestBody);
    }

    /**
     * High-level handler method. Does nothing by default - override this (or
     * other handleCast and handleCast methods) in subclasses.
     */
    public void handleCast(byte[] requestBody) {
        // Does nothing.
    }

    /**
     * Retrieve the channel.
     * 
     * @return the channel to which this server is connected
     */
    public Channel getChannel() {
        return _channel;
    }

    /**
     * Retrieve the queue name.
     * 
     * @return the queue which this server is consuming from
     */
    public String getQueueName() {
        return _queueName;
    }
}
