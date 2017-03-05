/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.fares.bind.xjc.plugins.substitution;

import com.sun.tools.xjc.model.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

public class Customisation {

  public static List<CPluginCustomization> findCustomizationsInProperty(CPropertyInfo propertyInfo, QName name) {

    final List<CPluginCustomization> foundCustomizations = new LinkedList<CPluginCustomization>();

    for (CPluginCustomization customization : propertyInfo.getCustomizations()) {
      if (fixNull(customization.element.getNamespaceURI()).equals(name.getNamespaceURI())
        && fixNull(customization.element.getLocalName()).equals(name.getLocalPart())) {
        customization.markAsAcknowledged();
        foundCustomizations.add(customization);
      }
    }

    return foundCustomizations;

  }

  public static boolean hasCustomizationsInProperty(CPropertyInfo propertyInfo, QName name) {
    return !findCustomizationsInProperty(propertyInfo, name).isEmpty();
  }

  public static List<CPluginCustomization> findCustomizationsInType(CTypeInfo type, QName name) {

    final List<CPluginCustomization> foundCustomizations = new LinkedList<CPluginCustomization>();

    for (CPluginCustomization customization : type.getCustomizations()) {
      if (fixNull(customization.element.getNamespaceURI()).equals(name.getNamespaceURI())
        && fixNull(customization.element.getLocalName()).equals(name.getLocalPart())) {
        customization.markAsAcknowledged();
        foundCustomizations.add(customization);
      }
    }

    return foundCustomizations;

  }

  public static boolean hasCustomizationsInType(CTypeInfo type, QName name) {
    return !findCustomizationsInType(type, name).isEmpty();
  }


  private static String fixNull(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }

}
