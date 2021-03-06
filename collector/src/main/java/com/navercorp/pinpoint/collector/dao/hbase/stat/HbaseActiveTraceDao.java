/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase.stat;

import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ActiveTraceSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseActiveTraceDao implements AgentStatDaoV2<ActiveTraceBo> {

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    @Autowired
    private ActiveTraceSerializer activeTraceSerializer;

    @Override
    public void insert(String agentId, List<ActiveTraceBo> agentStatDataPoints) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (agentStatDataPoints == null || agentStatDataPoints.isEmpty()) {
            return;
        }
        List<Put> activeTracePuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.ACTIVE_TRACE, agentStatDataPoints, this.activeTraceSerializer);
        if (!activeTracePuts.isEmpty()) {
            List<Put> rejectedPuts = this.hbaseTemplate.asyncPut(HBaseTables.AGENT_STAT_VER2, activeTracePuts);
            if (!rejectedPuts.isEmpty()) {
                this.hbaseTemplate.put(HBaseTables.AGENT_STAT_VER2, rejectedPuts);
            }
        }
    }
}
