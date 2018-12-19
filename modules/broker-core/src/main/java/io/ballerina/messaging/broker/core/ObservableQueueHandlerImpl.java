/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.EventSync;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.transaction.xa.Xid;

/**
 * Represents an Queue Handler which trigger events for the broker.
 */
public class ObservableQueueHandlerImpl extends QueueHandler {

    private final QueueHandlerImpl queueHandler;
    private final DefaultQueueHandlerEventPublisher defaultQueueHandlerEventPublisher;
    private final EventSync eventSync;

    private static final String CONSUMER_ADDED_EVENT = "consumer.added";
    private static final String CONSUMER_REMOVED_EVENT = "consumer.removed";
    private static final String BINDING_ADDED_EVENT = "binding.added";
    private static final String BINDING_REMOVED_EVENT = "binding.removed";


    ObservableQueueHandlerImpl(QueueHandlerImpl queueHandler, EventSync eventSync) {
        this.queueHandler = queueHandler;
        defaultQueueHandlerEventPublisher = new DefaultQueueHandlerEventPublisher(eventSync);
        this.eventSync = eventSync;
    }

    @Override
    public Queue getUnmodifiableQueue() {
        return queueHandler.getUnmodifiableQueue();
    }

    @Override
    public Collection<Consumer> getConsumers() {
        return queueHandler.getConsumers();
    }

    @Override
    boolean addConsumer(Consumer consumer) {
        boolean consumerAdded = queueHandler.addConsumer(new ObservableConsumer(consumer, eventSync));
        if (consumerAdded) {
            defaultQueueHandlerEventPublisher.publishConsumerEvent(CONSUMER_ADDED_EVENT, consumer);
        }
        return consumerAdded;
    }

    @Override
    boolean removeConsumer(Consumer consumer) {
        boolean consumerRemoved = queueHandler.removeConsumer(new ObservableConsumer(consumer, eventSync));
        if (consumerRemoved) {
            defaultQueueHandlerEventPublisher.publishConsumerEvent(CONSUMER_REMOVED_EVENT, consumer);
        }
        return consumerRemoved;
    }

    @Override
    void enqueue(Message message) throws BrokerException {
        queueHandler.enqueue(message);
    }

    @Override
    void prepareForEnqueue(Xid xid, Message message) throws BrokerException {
        queueHandler.prepareForEnqueue(xid, message);
    }

    @Override
    void prepareForDetach(Xid xid, DetachableMessage detachableMessage) throws BrokerException {
        queueHandler.prepareForDetach(xid, detachableMessage);
    }

    @Override
    public void commit(Xid xid) {
        queueHandler.commit(xid);
    }

    @Override
    public void rollback(Xid xid) {
        queueHandler.rollback(xid);
    }

    @Override
    Message takeForDelivery() {
        return queueHandler.takeForDelivery();
    }

    @Override
    void dequeue(DetachableMessage detachableMessage) throws BrokerException {
        queueHandler.dequeue(detachableMessage);
    }

    @Override
    public void requeue(Message message) throws BrokerException {
        queueHandler.requeue(message);
    }

    @Override
    CyclicConsumerIterator getCyclicConsumerIterator() {
        return queueHandler.getCyclicConsumerIterator();
    }

    @Override
    boolean isEmpty() {
        return queueHandler.isEmpty();
    }

    @Override
    public int size() {
        return queueHandler.size();
    }

    @Override
    boolean isUnused() {
        return queueHandler.isUnused();
    }

    @Override
    public int consumerCount() {
        return queueHandler.consumerCount();
    }

    @Override
    public void addBinding(Binding binding, ThrowingConsumer<Binding, BrokerException> bindingChangeListener) {
        queueHandler.addBinding(binding, bindingChangeListener);
        defaultQueueHandlerEventPublisher.publishBindingEvent(BINDING_ADDED_EVENT, binding);
    }

    @Override
    public int releaseResources() throws BrokerException {
        return queueHandler.releaseResources();
    }

    @Override
    public void removeBinding(Binding binding) {
        queueHandler.removeBinding(binding);
        defaultQueueHandlerEventPublisher.publishBindingEvent(BINDING_REMOVED_EVENT, binding);
    }

    @Override
    public int purgeQueue() throws ValidationException {
        return queueHandler.purgeQueue();
    }

    private static class DefaultQueueHandlerEventPublisher {
        EventSync eventSync;

        DefaultQueueHandlerEventPublisher(EventSync eventSync) {
            this.eventSync = eventSync;
        }

        private void publishConsumerEvent(String id, Consumer consumer) {
            Map<String, String> properties = new HashMap<>();
            properties.put("consumerID", String.valueOf(consumer.getId()));
            properties.put("queueName", consumer.getQueueName());
            properties.put("ready", String.valueOf(consumer.isReady()));
            properties.put("exclusive", String.valueOf(consumer.isExclusive()));
            eventSync.publish(id, properties);
        }

        private void publishBindingEvent(String id, Binding binding) {
            Map<String, String> properties = new HashMap<>();
            properties.put("bindingQueue", binding.getQueue().getName());
            properties.put("bindingPattern", binding.getBindingPattern());
            eventSync.publish(id, properties);
        }
    }
}