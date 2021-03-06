/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import java.io.Serializable;
import java.util.Hashtable;

public class KeyMapper implements Serializable {

    private static final long serialVersionUID = 1997604505886247127L;

    private int lastKey = 0;

    private final Hashtable<Object, String> objectKeyMap = new Hashtable<Object, String>();

    private final Hashtable<String, Object> keyObjectMap = new Hashtable<String, Object>();

    private String m_prefix;
    
    public KeyMapper(String prefix) {
        m_prefix = prefix;
    }
    
    /**
     * Gets key for an object.
     * 
     * @param o
     *            the object.
     */
    public String key(Object o) {

        if (o == null) {
            return "null";
        }

        // If the object is already mapped, use existing key
        String key = objectKeyMap.get(o);
        if (key != null) {
            return key;
        }

        // If the object is not yet mapped, map it
        key = getNextKey();
        objectKeyMap.put(o, key);
        keyObjectMap.put(key, o);

        return key;
    }

    private String getNextKey() {
        return m_prefix + String.valueOf(++lastKey);
    }

    /**
     * Retrieves object with the key.
     * 
     * @param key
     *            the name with the desired value.
     * @return the object with the key.
     */
    public Object get(String key) {

        return keyObjectMap.get(key);
    }

    /**
     * Removes object from the mapper.
     * 
     * @param removeobj
     *            the object to be removed.
     */
    public void remove(Object removeobj) {
        final String key = objectKeyMap.get(removeobj);

        if (key != null) {
            objectKeyMap.remove(removeobj);
            keyObjectMap.remove(key);
        }
    }

    /**
     * Removes all objects from the mapper.
     */
    public void removeAll() {
        objectKeyMap.clear();
        keyObjectMap.clear();
    }
}

