package io.fares.bind.xjc.plugins.substitution;

import com.sun.tools.xjc.model.*;
import com.sun.xml.bind.v2.model.core.MaybeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static io.fares.bind.xjc.plugins.substitution.SubstitutionPlugin.SUBSTITUTION_HEAD_NAME;
import static io.fares.bind.xjc.plugins.substitution.SubstitutionPlugin.SUBSTITUTION_HEAD_REF_NAME;
import static io.fares.bind.xjc.plugins.substitution.Utils.findCustomizations;
import static java.lang.String.format;

public class ManualCollector implements CPropertyVisitor<Void> {

  @Override
  public Void onElement(CElementPropertyInfo p) {
    return null;
  }

  @Override
  public Void onAttribute(CAttributePropertyInfo p) {
    return null;
  }

  @Override
  public Void onValue(CValuePropertyInfo p) {
    return null;
  }

  @Override
  public Void onReference(CReferencePropertyInfo p) {
    return null;
  }


  //
//  private static final Logger logger = LoggerFactory.getLogger(ManualCollector.class);
//
//  private static final String PROP_FORMAT_INFO = "  %-23s: %-70s | %s";
//  private static final String PROP_FORMAT_MSG = "  %-23s: %s";
//  private static final String PROP_FORMAT_MSG_TYPE = "  %-23s: not substitutable as type %s";
//
//  private final boolean debug;
//
//  private final List<SubstitutionProperty> properties = new LinkedList<>();
//
//  boolean hasSubstitutedProperties() {
//    return !properties.isEmpty();
//  }
//
//  List<SubstitutionProperty> getProperties() {
//    return Collections.unmodifiableList(properties);
//  }
//
//  ManualCollector(boolean debug) {
//    this.debug = debug;
//  }
//
//  public Void onElement(CElementPropertyInfo property) {
//
//    // region normalise input
//
//    if (property.ref().size() > 1) {
//      logger.warn(format("property %s is not single typed, please raise a defect for this condition", property.displayName()));
//      return null;
//    } else if (property.ref().size() == 0) {
//      if (debug) {
//        logger.info(format(PROP_FORMAT_MSG, property.getName(false), "may be wildcard"));
//      }
//      return null;
//    }
//
//    CNonElement elementDef = property.ref().get(0);
//
//    SubstitutionPropertyElement candidate;
//
//    if (elementDef instanceof CClassInfo) {
//      CClassInfo targetClassInfo = (CClassInfo) elementDef;
//      candidate = new SubstitutionPropertyElement(property, (CClassInfo) elementDef);
//      if (targetClassInfo.isAbstract() && targetClassInfo.hasSubClasses()) {
//        // if type is a class and has subclasses and is abstract, definitely a candidate for replacement
//        properties.add(candidate.auto());
//        if (debug) {
//          logger.info(format(PROP_FORMAT_INFO, property.getName(false), targetClassInfo.getType().fullName(), toFixedString(targetClassInfo.getTypeName())));
//        }
//      }
//    } else if (elementDef instanceof CElement) {
//      candidate = new SubstitutionPropertyElement(property, (CElement) elementDef);
//    } else if (elementDef instanceof MaybeElement) {
//      MaybeElement e = (MaybeElement) elementDef;
//      if (debug) {
//        logger.info(format(PROP_FORMAT_MSG_TYPE, property.getName(false), toFixedString(e.getTypeName())));
//      }
//      return null;
//    } else {
//      logger.error(format(PROP_FORMAT_MSG_TYPE, property.getName(false), elementDef.getType().fullName()));
//      assert false;
//      return null;
//    }
//
//    // endregion
//
//    // REVIEW there seems to be no scenario where the element has a substitution head attached,
//    // odd but meeh
//    // Element head = getSubstitutionHead(elementDef);
//
//    // region manual customisation
//
//    List<CPluginCustomization> propertyCustomizations = findCustomizations(property, SUBSTITUTION_HEAD_REF_NAME);
//    List<CPluginCustomization> targetElementCustomizations = findCustomizations(elementDef, SUBSTITUTION_HEAD_NAME);
//
//    if (!propertyCustomizations.isEmpty() || !targetElementCustomizations.isEmpty()) {
//      properties.add(candidate);
//    }
//
//    // endregion
//
//    return null;
//
//  }
//
//  public Void onReference(final CReferencePropertyInfo property) {
//
//    // region normalise input
//
//    if (property.getElements().size() > 1) {
//      logger.warn(format(PROP_FORMAT_MSG, property.getName(false), "is not single typed"));
//      return null;
//    } else if (property.getElements().size() == 0) {
//      if (property.getWildcard() != null) {
//        logger.warn(format(PROP_FORMAT_MSG, property.getName(false), "is wildcard"));
//      } else {
//        logger.warn(format(PROP_FORMAT_MSG, property.getName(false), "is non-typed, please raise a defect for this scenario"));
//      }
//      return null;
//    }
//
//    CElement elementDef = property.getElements().stream().findFirst().orElse(null);
//
//    if (!(elementDef instanceof CElementInfo)) {
//      logger.warn(format("  %-23s: element ref definition is not CElementInfo but %s", property.getName(false), elementDef.getClass().getSimpleName()));
//      return null;
//    }
//
//    // endregion
//
//    if (debug) {
//      logger.info(format(PROP_FORMAT_INFO, property.getName(false), elementDef.getType().fullName(), elementDef.getElementName()));
//    }
//
//    CElementInfo elementInfo = (CElementInfo) elementDef;
//
//    SubstitutionPropertyReference candidate = new SubstitutionPropertyReference(property, elementInfo);
//
//    List<CPluginCustomization> propertyCustomizations = findCustomizations(property, SUBSTITUTION_HEAD_REF_NAME);
//    List<CPluginCustomization> targetElementCustomizations = findCustomizations(elementDef, SUBSTITUTION_HEAD_NAME);
//
//    if (!propertyCustomizations.isEmpty() || !targetElementCustomizations.isEmpty()) {
//      properties.add(candidate);
//    }
//
//    return null;
//
//  }
//
//  private String toFixedString(QName name) {
//    if (name == null) {
//      return null;
//    } else if ("\u0000".equals(name.getLocalPart())) {
//      return new QName(name.getNamespaceURI(), "gregorianCalendar").toString();
//    } else {
//      return name.toString();
//    }
//  }
//
//  // region not implemented
//
//  public Void onAttribute(CAttributePropertyInfo property) {
//    if (debug) {
//      logger.info(format(PROP_FORMAT_MSG, property.getName(false), "not substitutable as attribute"));
//    }
//    return null;
//  }
//
//  public Void onValue(CValuePropertyInfo property) {
//    if (debug) {
//      logger.info(format(PROP_FORMAT_MSG, property.getName(false), "not substitutable as value"));
//    }
//    return null;
//  }
//
//  // endregion

}
