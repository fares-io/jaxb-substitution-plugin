/*
 * Copyright 2019 Niels Bertram
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
package io.fares.bind.xjc.plugins.substitution;

import com.sun.tools.xjc.model.CCustomizable;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

import static java.util.Optional.ofNullable;

public class Utils {

  public static List<CPluginCustomization> findCustomizations(CCustomizable customizable, QName name) {
    return findCustomizations(customizable.getCustomizations(), name);
  }

  private static List<CPluginCustomization> findCustomizations(CCustomizations customizations, QName name) {

    final List<CPluginCustomization> foundCustomizations = new LinkedList<>();

    for (CPluginCustomization customization : customizations) {

      String elementNS = ofNullable(customization.element.getNamespaceURI()).orElse("");
      String elementName = ofNullable(customization.element.getLocalName()).orElse("");

      if (elementNS.equals(name.getNamespaceURI()) && elementName.equals(name.getLocalPart())) {
        customization.markAsAcknowledged();
        foundCustomizations.add(customization);
      }

    }

    return foundCustomizations;

  }

  public static boolean isEmpty(String s) {
    return s == null || s.length() == 0 || "".equals(s.trim());
  }

  public static boolean isNotEmpty(String s) {
    return !isEmpty(s);
  }

}
