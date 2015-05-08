/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LazySet;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.util.ResourceResolver;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * <p>ResponseTimeResourceType class.</p>
 */
public class ResponseTimeResourceType implements OnmsResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResponseTimeResourceType.class);

    private final ResourceResolver m_resourceResolver;
    private final NodeDao m_nodeDao;
    private final IpInterfaceDao m_ipInterfaceDao;
    
    /**
     * <p>Constructor for ResponseTimeResourceType.</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public ResponseTimeResourceType(final ResourceResolver resourceResolver, final NodeDao nodeDao, final IpInterfaceDao ipInterfaceDao) {
        m_resourceResolver = resourceResolver;
        m_nodeDao = nodeDao;
        m_ipInterfaceDao = ipInterfaceDao;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "Response Time";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "responseTime";
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForDomain(final String domain) {
        return Collections.emptyList();
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNode(final int nodeId) {
    	final LinkedList<OnmsResource> resources = new LinkedList<OnmsResource>();
        
    	final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, nodeId, "Could not find node for node Id " + nodeId, null);
        }
        
        for (final OnmsIpInterface i : node.getIpInterfaces()) {
            String ipAddr = InetAddressUtils.str(i.getIpAddress());

            if(m_resourceResolver.exists(ResourceTypeUtils.RESPONSE_DIRECTORY, ipAddr)) {
                resources.add(createResource(i));
            }
        }

        return resources;
    }

    @Override
    public OnmsResource getChildByName(OnmsResource parent, String ipAddress) {
        // Grab the node entity
        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);

        // Grab the interface
        final OnmsIpInterface matchingIf = m_ipInterfaceDao.get(node, ipAddress);
        if (matchingIf == null) {
            throw new ObjectRetrievalFailureException(OnmsIpInterface.class, "No interface with ipAddr "
                    + ipAddress + " could be found on node with id " + node.getId());
        }

        // Create the resource
        final OnmsResource resource = createResource(matchingIf);
        resource.setParent(parent);
        return resource;
    }

    private OnmsResource createResource(final OnmsIpInterface ipInterface) {
    	final String ipAddr = InetAddressUtils.str(ipInterface.getIpAddress());
    	final String label = ipAddr;
    	final String resource = ipAddr;

    	final Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(new AttributeLoader(m_resourceResolver, ipAddr));
    	final OnmsResource r = new OnmsResource(resource, label, this, set);
        r.setEntity(ipInterface);
        return r;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnDomain(final String domain) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnNode(final int nodeId) {
        return getResourcesForNode(nodeId).size() > 0;
    }

    public static class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private final ResourceResolver m_resourceResolver;
        private final String m_ipAddr;

        public AttributeLoader(final ResourceResolver resourceResolver, final String ipAddr) {
            m_resourceResolver = resourceResolver;
            m_ipAddr = ipAddr;
        }

        @Override
        public Set<OnmsAttribute> load() {
            LOG.debug("lazy-loading attributes for response time resource '{}'", m_ipAddr);
            return m_resourceResolver.getAttributes(ResourceTypeUtils.RESPONSE_DIRECTORY, m_ipAddr);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(final OnmsResource resource) {
        return "element/interface.jsp?node=" + resource.getParent().getName() + "&intf=" + resource.getName();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnNodeSource(String nodeSource, int nodeId) {
        return getResourcesForNodeSource(nodeSource, nodeId).size() > 0;
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNodeSource(String nodeSource, int nodeId) {
        return getResourcesForNode(nodeId);
    }
}
