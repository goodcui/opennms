//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.virtual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.config.PassiveStatusConfig;
import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.pollables.PollStatus;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;

public class PassiveStatusKeeper extends ServiceDaemon implements EventListener {
    
    private static PassiveStatusKeeper s_instance = new PassiveStatusKeeper();

    private Map m_statusTable = null;
    private EventIpcManager m_eventMgr;
    private PassiveStatusConfig m_config;
    private boolean m_initialized = false;

    
    public PassiveStatusKeeper() {
    }
    
    public PassiveStatusKeeper(EventIpcManager eventMgr) {
        setEventManager(eventMgr);
    }
    
    public synchronized static void setInstance(PassiveStatusKeeper psk) {
        s_instance = psk;
    }
    
    public synchronized static PassiveStatusKeeper getInstance() {
        return s_instance;
    }

    
    public void init() {
        if (!m_initialized) {
            checkPreRequisites();
            createMessageSelectorAndSubscribe();
            m_statusTable = new HashMap();
            m_initialized = true;
            setStatus(START_PENDING);
        }
    }

    private void checkPreRequisites() {
        if (m_config == null)
            throw new IllegalStateException("config has not been set");
        if (m_eventMgr == null)
            throw new IllegalStateException("eventManager has not been set");
    }

    public void start() {
        setStatus(RUNNING);
    }

    public void stop() {
        setStatus(STOP_PENDING);
        setStatus(STOPPED);
    }
    
    public void destroy() {
        setStatus(STOPPED);
        m_initialized = false;
        m_eventMgr = null;
        m_config = null;
        m_statusTable = null;
    }

    public String getName() {
        return "OpenNMS.PassiveStatusKeeper";    }

    public void pause() {
        setStatus(PAUSED);
    }

    public void resume() {
        setStatus(RESUME_PENDING);
    }

    public void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        checkInit();
        m_statusTable.put(nodeLabel+":"+ipAddr+":"+svcName, pollStatus);
    }

    private void checkInit() {
        if (!m_initialized)
            throw new IllegalStateException("the service has not been intialized");
    }

    public PollStatus getStatus(String nodeLabel, String ipAddr, String svcName) {
        //FIXME: Throw a log or exception here if this method is called and the this class hasn't been initialized
        PollStatus status = (PollStatus) (m_statusTable == null ? PollStatus.STATUS_UNKNOWN : m_statusTable.get(nodeLabel+":"+ipAddr+":"+svcName));
        return (status == null ? PollStatus.STATUS_UNKNOWN : status);
    }

    private void createMessageSelectorAndSubscribe() {
        // Subscribe to eventd
        getEventManager().addEventListener(this, m_config.getUEIList());
    }

    public void onEvent(Event e) {
        if (isPassiveStatusEvent(e)) {
            log().debug("onEvent: received valid registered passive status event: "+e);
            setStatus(EventUtils.getParm(e, EventConstants.PARM_PASSIVE_NODE_LABEL), EventUtils.getParm(e, EventConstants.PARM_PASSIVE_IPADDR), EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_NAME), determinePollStatus(e));
        } else {
            log().debug("onEvent: received Invalid registered passive status event: "+e);
        }
    }

    private PollStatus determinePollStatus(Event e) {
        String status = EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_STATUS);
        
        if (status.equalsIgnoreCase("Up")) {
            return PollStatus.getPollStatus(ServiceMonitor.SERVICE_AVAILABLE, e.getLogmsg().getContent());
        } else if(status.equalsIgnoreCase("Down")) {
            return PollStatus.getPollStatus(ServiceMonitor.SERVICE_UNAVAILABLE, e.getLogmsg().getContent());
        } else if(status.equalsIgnoreCase("Unknown")) {
            return PollStatus.getPollStatus(ServiceMonitor.SERVICE_UNKNOWN, e.getLogmsg().getContent());
        } else if(status.equalsIgnoreCase("Unresponsive")) {
            return PollStatus.getPollStatus(ServiceMonitor.SERVICE_UNRESPONSIVE, e.getLogmsg().getContent());
        } else {
            return PollStatus.getPollStatus(ServiceMonitor.SERVICE_UNKNOWN, e.getLogmsg().getContent());
        }
    }

    private boolean isPassiveStatusEvent(Event e) {
        String labels[] = { EventConstants.PARM_PASSIVE_NODE_LABEL, EventConstants.PARM_PASSIVE_IPADDR, EventConstants.PARM_PASSIVE_SERVICE_NAME, EventConstants.PARM_PASSIVE_SERVICE_STATUS };
        Parms parms = e.getParms();
        if (parms != null) {
            List labelList = getParmsLabels(parms);
            if (labelList.containsAll(Arrays.asList(labels)))
                return true;
        }
        return false;
    }

    private List getParmsLabels(Parms parms) {
        List labels = new ArrayList();
        Collection parmColl = parms.getParmCollection();
        for (Iterator it = parmColl.iterator(); it.hasNext();) {
            labels.add(((Parm) it.next()).getParmName());
        }
        return labels;
    }

    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    public PassiveStatusConfig getConfig() {
        return m_config;
    }
    
    public void setConfig(PassiveStatusConfig config) {
        m_config = config;
    }

    private Category log() {
        return ThreadCategory.getInstance(PassiveStatusKeeper.class);
    }

}
