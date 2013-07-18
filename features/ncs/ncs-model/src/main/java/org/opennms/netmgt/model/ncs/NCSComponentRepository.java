/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.ncs;

import java.util.List;

import org.opennms.netmgt.model.OnmsCriteria;

public interface NCSComponentRepository {

    /**
     * This is used to lock the table in order to implement upsert type operations
     */
    void lock();

    
    /**
     * <p>initialize</p>
     *
     * @param obj a {@link java.lang.Object} object.
     */
    void initialize(Object obj);

    /**
     * <p>flush</p>
     */
    void flush();

    /**
     * <p>clear</p>
     */
    void clear();

    /**
     * <p>countAll</p>
     *
     * @return a int.
     */
    int countAll();

    /**
     * <p>delete</p>
     */
    void delete(NCSComponent component);

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<NCSComponent> findAll();
    
    /**
     * <p>findMatching</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link java.util.List} object.
     */
    List<NCSComponent> findMatching(OnmsCriteria criteria);

    /**
     * <p>countMatching</p>
     *
     * @param onmsCrit a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a int.
     */
    int countMatching(final OnmsCriteria onmsCrit);
    
    /**
     * <p>get</p>
     */
    NCSComponent get(Long id);

    /**
     * <p>load</p>
     */
    NCSComponent load(Long id);

    /**
     * <p>save</p>
     */
    void save(NCSComponent component);

    /**
     * <p>saveOrUpdate</p>
     */
    void saveOrUpdate(NCSComponent component);

    /**
     * <p>update</p>
     */
    void update(NCSComponent component);

	/**
	 * @param type
	 */
	List<NCSComponent> findByType(String type);

	/**
	 * @param type
	 * @param foreignSource
	 * @param foreignId
	 */
	NCSComponent findByTypeAndForeignIdentity(String type, String foreignSource, String foreignId);

	/**
	 * @param component
	 */
	List<NCSComponent> findComponentsThatDependOn(NCSComponent component);

	/**
	 * @param attrKey
	 * @param attrValue
	 */
	List<NCSComponent> findComponentsWithAttribute(String attrKey, String attrValue);

	/**
	 * @param nodeid
	 */
	List<NCSComponent> findComponentsByNodeId(int nodeid);
}