/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.bootstrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapSupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TestCluster.
 */
public class TestCluster {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(TestCluster.class);

    /** The Constant BOOTSTRAP_NODE_HOST. */
    private static final String BOOTSTRAP_NODE_HOST = "192.168.0.202";

    /** The Constant ENDPOINT_NODE_HOST. */
    private static final String ENDPOINT_NODE_HOST = "192.168.0.101";

    /** The zookeeper cluster static instance. */
    private static TestingCluster zkCluster;

    /** The endpoint node static instance. */
    private static OperationsNode endpointNode;

    /** The bootstrap node static instance. */
    private static BootstrapNode bootstrapNode;
    /**
     * Start.
     *
     * @throws Exception the exception
     */
    public static void checkStarted() throws Exception {
        zkCluster = new TestingCluster(new InstanceSpec(null, BootstrapStartIT.ZK_PORT, -1, -1, true, -1, -1, -1));
        zkCluster.start();
        logger.info("ZK Cluster started");
        OperationsNodeInfo endpointNodeInfo = buildEndpointNodeInfo();

        endpointNode = new OperationsNode(endpointNodeInfo,
                zkCluster.getConnectString(), buildDefaultRetryPolicy());
        endpointNode.start();

        BootstrapNodeInfo bootstrapNodeInfo = buildBootstrapNodeInfo();

        bootstrapNode = new BootstrapNode(bootstrapNodeInfo,
                zkCluster.getConnectString(), buildDefaultRetryPolicy());
        bootstrapNode.start();
    }

    /**
     * Stop.
     *
     * @throws Exception the exception
     */
    public static void stop() throws Exception {
        endpointNode.close();
        zkCluster.close();
    }

    public static void addBootstrapListener(BootstrapNodeListener listener) {
        bootstrapNode.addListener(listener);
    }

    public static void removeBootstrapListener(BootstrapNodeListener listener) {
        bootstrapNode.removeListener(listener);
    }
    /**
     * Builds the default retry policy.
     *
     * @return the retry policy
     */
    private static RetryPolicy buildDefaultRetryPolicy() {
        return new ExponentialBackoffRetry(100, 1);
    }

    /**
     * Builds the endpoint node info.
     *
     * @return the endpoint node info
     */
    private static OperationsNodeInfo buildEndpointNodeInfo() {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[]{10,11,12,45,34,23,67,89,66,12});
        nodeInfo.setConnectionInfo(new ConnectionInfo(ENDPOINT_NODE_HOST, 1000, testKeyData));
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        List<SupportedChannel> supportedChannels = new ArrayList<>();
        ZkHttpComunicationParameters httpCommunicationParameters = new ZkHttpComunicationParameters(new IpComunicationParameters(ENDPOINT_NODE_HOST, 1000));
        BaseStatistics statistics = new BaseStatistics(2, 3, 1, System.currentTimeMillis());
        ZkHttpStatistics httpChannelStatistics = new ZkHttpStatistics(statistics );
        SupportedChannel channelHttp = new SupportedChannel(new ZkSupportedChannel(ZkChannelType.HTTP, true, httpCommunicationParameters, httpChannelStatistics));
        supportedChannels.add(channelHttp);
        nodeInfo.setSupportedChannelsArray(supportedChannels );
        return nodeInfo;
    }

    /**
     * Builds the bootstrap node info.
     *
     * @return the bootstrap node info
     */
    private static BootstrapNodeInfo buildBootstrapNodeInfo() {
        BootstrapNodeInfo nodeInfo = new BootstrapNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[]{10,11,12,45,34,23,67,89,66,12});
        nodeInfo.setConnectionInfo(new ConnectionInfo(BOOTSTRAP_NODE_HOST, 1000, testKeyData));
        List<BootstrapSupportedChannel> supportedChannels = new ArrayList<>();

        ZkHttpComunicationParameters httpCommunicationParameters = new ZkHttpComunicationParameters(new IpComunicationParameters(BOOTSTRAP_NODE_HOST, 1000));
        BaseStatistics httpStatistics = new BaseStatistics(2, 3, 1, System.currentTimeMillis());
        ZkHttpStatistics httpChannelStatistics = new ZkHttpStatistics(httpStatistics);
        BootstrapSupportedChannel channelHttp = new BootstrapSupportedChannel(new ZkSupportedChannel(ZkChannelType.HTTP, true, httpCommunicationParameters, httpChannelStatistics));
        supportedChannels.add(channelHttp);

        ZkKaaTcpComunicationParameters tcpCommunicationParameters = new ZkKaaTcpComunicationParameters(new IpComunicationParameters(BOOTSTRAP_NODE_HOST, 1001));
        BaseStatistics tcpStatistics = new BaseStatistics(2, 3, 1, System.currentTimeMillis());
        ZkKaaTcpStatistics tcpChannelStatistics = new ZkKaaTcpStatistics(tcpStatistics);
        BootstrapSupportedChannel channelTcp = new BootstrapSupportedChannel(new ZkSupportedChannel(ZkChannelType.KAATCP, true, tcpCommunicationParameters, tcpChannelStatistics));
        supportedChannels.add(channelTcp);

        nodeInfo.setSupportedChannelsArray(supportedChannels);
        return nodeInfo;
    }
}