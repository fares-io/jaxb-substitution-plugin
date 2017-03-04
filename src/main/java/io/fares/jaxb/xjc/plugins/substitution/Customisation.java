package io.fares.jaxb.xjc.plugins.substitution;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.CPropertyInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

public class Customisation {

  public static List<CPluginCustomization> findPropertyCustomizationsInPropertyAndClass(CPropertyInfo propertyInfo,
                                                                                        QName propertyCustomizationName,
                                                                                        QName customizationName) {

    final List<CPluginCustomization> foundPropertyCustomizations = new LinkedList<CPluginCustomization>();
    foundPropertyCustomizations.addAll(findPropertyCustomizationsInProperty(propertyInfo, customizationName));
    if (propertyInfo.parent() instanceof CClassInfo) {
      foundPropertyCustomizations.addAll(findPropertyCustomizationsInClass((CClassInfo) propertyInfo.parent(),
        propertyInfo, propertyCustomizationName, customizationName));
    }
    return foundPropertyCustomizations;
  }

  public static List<CPluginCustomization> findPropertyCustomizationsInProperty(CPropertyInfo propertyInfo,
                                                                                QName name) {

    final List<CPluginCustomization> foundPropertyCustomizations = new LinkedList<CPluginCustomization>();

    final List<CPluginCustomization> propertyCustomizations = propertyInfo.getCustomizations();

    for (CPluginCustomization propertyCustomization : propertyCustomizations) {
      if (fixNull(propertyCustomization.element.getNamespaceURI()).equals(name.getNamespaceURI())
        && fixNull(propertyCustomization.element.getLocalName()).equals(name.getLocalPart())) {
        propertyCustomization.markAsAcknowledged();
        foundPropertyCustomizations.add(propertyCustomization);
      }
    }
    return foundPropertyCustomizations;
  }

  public static List<CPluginCustomization> findPropertyCustomizationsInClass(CClassInfo classInfo,
                                                                             CPropertyInfo propertyInfo,
                                                                             QName propertyCustomizationName,
                                                                             QName customizationName) {

    final List<CPluginCustomization> foundPropertyCustomizations = new LinkedList<CPluginCustomization>();

    final List<CPluginCustomization> classCustomizations = classInfo.getCustomizations();

    for (CPluginCustomization classCustomization : classCustomizations) {
      if (fixNull(classCustomization.element.getNamespaceURI())
        .equals(propertyCustomizationName.getNamespaceURI())
        && fixNull(classCustomization.element.getLocalName())
        .equals(propertyCustomizationName.getLocalPart())
        && propertyInfo.getName(false).equals(classCustomization.element.getAttribute("name"))) {

        final Element classCustomizationElement = classCustomization.element;

        final NodeList nodes = classCustomizationElement.getChildNodes();
        final int length = nodes.getLength();
        for (int index = 0; index < length; index++) {
          final Node node = nodes.item(index);
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            final Element element = (Element) node;
            if (fixNull(element.getNamespaceURI()).equals(customizationName.getNamespaceURI())
              && fixNull(element.getLocalName()).equals(customizationName.getLocalPart())) {

              classCustomization.markAsAcknowledged();
              final CPluginCustomization propertyCustomization = new CPluginCustomization(element,
                classCustomization.locator);
              propertyCustomization.markAsAcknowledged();
              foundPropertyCustomizations.add(propertyCustomization);
            }
          }
        }
      }
    }
    return foundPropertyCustomizations;
  }

  private static String fixNull(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }
}
