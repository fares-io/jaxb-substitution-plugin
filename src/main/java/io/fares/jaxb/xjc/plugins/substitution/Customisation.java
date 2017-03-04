package io.fares.jaxb.xjc.plugins.substitution;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.CPropertyInfo;
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

  public static List<CPluginCustomization> findCustomizationsInElement(CElement element, QName name) {

    final List<CPluginCustomization> foundCustomizations = new LinkedList<CPluginCustomization>();

    for (CPluginCustomization customization : element.getCustomizations()) {
      if (fixNull(customization.element.getNamespaceURI()).equals(name.getNamespaceURI())
        && fixNull(customization.element.getLocalName()).equals(name.getLocalPart())) {
        customization.markAsAcknowledged();
        foundCustomizations.add(customization);
      }
    }

    return foundCustomizations;

  }

  public static boolean hasCustomizationsInElement(CElement element, QName name) {
    return !findCustomizationsInElement(element, name).isEmpty();
  }


  private static String fixNull(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }
}
