package org.opennms.netmgt.poller.remote.support;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>PollerBackEndEventProcessor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@EventListener(name="pollerBackEnd")
public class PollerBackEndEventProcessor {

    private PollerBackEnd m_pollerBackEnd;
    
    /**
     * <p>setPollerBackEnd</p>
     *
     * @param pollerBackEnd a {@link org.opennms.netmgt.poller.remote.PollerBackEnd} object.
     */
    public void setPollerBackEnd(PollerBackEnd pollerBackEnd) {
        m_pollerBackEnd = pollerBackEnd;
    }

    /**
     * <p>handleSnmpPollerConfigChanged</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.SNMPPOLLERCONFIG_CHANGED_EVENT_UEI)
    public void handleSnmpPollerConfigChanged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleDaemonConfigChanged</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleDaemonConfigChanged(final Event event) {
        String daemon = EventUtils.getParm(event, EventConstants.PARM_DAEMON_NAME);
        if ("PollerBackEnd".equalsIgnoreCase(daemon)) {
            m_pollerBackEnd.configurationUpdated();
        }
    }

    /**
     * <p>handleNodeAdded</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAdded(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleNodeGainedInterface</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)
    public void handleNodeGainedInterface(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleNodeGainedService</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleNodeConfigChanged</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.NODE_CONFIG_CHANGE_UEI)
    public void handleNodeConfigChanged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleNodeInfoChanged</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.NODE_INFO_CHANGED_EVENT_UEI)
    public void handleNodeInfoChanged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleServiceDeleted</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.SERVICE_DELETED_EVENT_UEI)
    public void handleServiceDeleted(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleServiceUnmanaged</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.SERVICE_UNMANAGED_EVENT_UEI)
    public void handleServiceUnmanaged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleInterfaceDeleted</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.INTERFACE_DELETED_EVENT_UEI)
    public void handleInterfaceDeleted(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    /**
     * <p>handleNodeDeleted</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeleted(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

}
