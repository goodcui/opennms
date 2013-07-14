/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;

import java.util.Calendar;
import java.util.List;

/**
 * This class represents a Alert Dashlet with minimum details.
 *
 * @author Christian Pape
 */
public class AlarmsDashlet extends VerticalLayout implements Dashlet {
    /**
     * the dashlet's name
     */
    private String m_name;

    /**
     * The {@link AlarmDao} used
     */
    private AlarmDao m_alarmDao;
    /**
     * The {@link NodeDao} used
     */
    private NodeDao m_nodeDao;
    /**
     * The {@link DashletSpec} for this instance
     */
    private DashletSpec m_dashletSpec;
    /**
     * boosted value
     */
    private boolean boosted = false;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     * @param alarmDao    the {@link AlarmDao} to be used
     * @param nodeDao     the {@link NodeDao} to be used
     */
    public AlarmsDashlet(String name, DashletSpec dashletSpec, AlarmDao alarmDao, NodeDao nodeDao) {
        /**
         * Setting the member fields
         */
        m_name = name;
        m_dashletSpec = dashletSpec;
        m_alarmDao = alarmDao;
        m_nodeDao = nodeDao;

        /**
         * Setting up the layout
         */
        setCaption(getName());
        setWidth("100%");
    }

    /**
     * Updates the alarm data using the associated {@link AlarmDao} and {@link NodeDao} instances.
     *
     * @return true, if boosted, false otherwise
     */
    @Override
    public void update() {
        final CriteriaBuilder alarmCb = new CriteriaBuilder(OnmsAlarm.class);

        int minimumSeverity = 4;
        int boostSeverity = 6;
        int alarmsPerPage = 12;

        try {
            alarmsPerPage = Math.max(1, Integer.parseInt(m_dashletSpec.getParameters().get("alarmsPerPage")));
            minimumSeverity = Math.min(7, Math.max(1, Integer.parseInt(m_dashletSpec.getParameters().get("minimumSeverity"))));
            boostSeverity = Math.min(7, Math.max(1, Integer.parseInt(m_dashletSpec.getParameters().get("boostSeverity"))));
        } catch (NumberFormatException numberFormatException) {
            /**
             * Just ignore
             */
        }

        alarmCb.fetch("firstEvent", Fetch.FetchType.EAGER);
        alarmCb.fetch("lastEvent", Fetch.FetchType.EAGER);

        alarmCb.isNull("alarmAckUser");
        alarmCb.ge("severity", OnmsSeverity.get(minimumSeverity));

        alarmCb.orderBy("lastEventTime").desc();
        alarmCb.limit(alarmsPerPage);

        alarmCb.distinct();

        List<OnmsAlarm> alarms = m_alarmDao.findMatching(alarmCb.toCriteria());

        removeAllComponents();

        injectStyles();

        boosted = false;

        if (alarms.size() == 0) {
            Label label = new Label("No alarms found!");
            label.addStyleName("alerts-noalarms-font");
            addComponent(label);
        } else {
            for (OnmsAlarm onmsAlarm : alarms) {
                OnmsNode onmsNode = null;

                if (onmsAlarm.getNodeId() != null) {
                    CriteriaBuilder nodeCb = new CriteriaBuilder(OnmsNode.class);
                    nodeCb.eq("id", onmsAlarm.getNodeId());

                    List<OnmsNode> nodes = m_nodeDao.findMatching(nodeCb.toCriteria());

                    if (nodes.size() == 1) {
                        onmsNode = nodes.get(0);
                    }
                }
                addComponent(createAlarmComponent(onmsAlarm, onmsNode));

                if (onmsAlarm.getSeverity().isGreaterThanOrEqual(OnmsSeverity.get(boostSeverity))) {
                    boosted = true;
                }
            }
        }
    }

    /**
     * Returns a human-readable {@link String} representation of a timestamp in the past.
     *
     * @param secondsAll the timestamp to be used
     * @return a human-readable representation
     */
    public String getHumanReadableFormat(long secondsAll) {
        long seconds = secondsAll;
        long minutes = 0;
        long hours = 0;

        if (seconds / 60 > 0) {
            long rest = seconds % 60;
            minutes = seconds / 60;
            seconds = rest;
        }

        if (minutes / 60 > 0) {
            long rest = minutes % 60;
            hours = minutes / 60;
            minutes = rest;
        }

        String output = "";

        if (hours > 0) {
            output = hours + "h, " + minutes + "m, " + seconds + "s";
        } else {
            if (minutes > 0) {
                output = minutes + "m, " + seconds + "s";
            } else {
                output = seconds + "s";
            }
        }

        return output + " ago";
    }

    /**
     * Injects CSS styles on current page for this dashlet
     */
    private void injectStyles() {
        Page.getCurrent().getStyles().add(".alerts.cleared { background: #000000; border-left: 15px solid #858585; }");
        Page.getCurrent().getStyles().add(".alerts.normal { background: #000000; border-left: 15px solid #336600; }");
        Page.getCurrent().getStyles().add(".alerts.indeterminate {  background: #000000; border-left: 15px solid #999; }");
        Page.getCurrent().getStyles().add(".alerts.warning { background: #000000; border-left: 15px solid #FFCC00; }");
        Page.getCurrent().getStyles().add(".alerts.minor { background: #000000;  border-left: 15px solid #FF9900; }");
        Page.getCurrent().getStyles().add(".alerts.major { background: #000000; border-left: 15px solid #FF3300; }");
        Page.getCurrent().getStyles().add(".alerts.critical { background: #000000; border-left: 15px solid #CC0000; }");
        Page.getCurrent().getStyles().add(".alerts-font {color: #3ba300; font-size: 18px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alerts-noalarms-font { font-size: 18px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alerts { padding: 5px 5px; margin: 1px; }");
    }

    /**
     * Returns the component for visualising the alarms data.
     *
     * @param onmsAlarm an {@link OnmsAlarm} instance
     * @param onmsNode  an {@link OnmsNode} instance
     * @return component for this alarm
     */
    public Component createAlarmComponent(OnmsAlarm onmsAlarm, OnmsNode onmsNode) {

        Calendar calendar = Calendar.getInstance();

        String ago = getHumanReadableFormat((calendar.getTimeInMillis() / 1000) - (onmsAlarm.getLastEventTime().getTime() / 1000));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.addStyleName("alerts");
        horizontalLayout.addStyleName(onmsAlarm.getSeverity().name().toLowerCase());

        Label labelAgo = new Label();
        labelAgo.setSizeUndefined();
        labelAgo.addStyleName("alerts-font");
        labelAgo.setValue(ago);

        Label labelId = new Label();
        labelId.setSizeUndefined();
        labelId.addStyleName("alerts-font");
        if (onmsNode != null) {
            labelId.setValue(onmsNode.getLabel() + " (" + onmsNode.getNodeId() + ")");
        } else {
            labelId.setValue("-");
        }

        Label labelUei = new Label();
        labelUei.setSizeUndefined();
        labelUei.addStyleName("alerts-font");
        labelUei.setValue(onmsAlarm.getUei());

        horizontalLayout.addComponent(labelAgo);
        horizontalLayout.addComponent(labelId);
        horizontalLayout.addComponent(labelUei);

        horizontalLayout.setExpandRatio(labelAgo, 1.0f);
        horizontalLayout.setExpandRatio(labelId, 2.0f);
        horizontalLayout.setExpandRatio(labelUei, 4.0f);

        return horizontalLayout;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }
}