package io.fares.bind.xjc.plugins.substitution;

import com.sun.tools.xjc.model.*;
import com.sun.xml.bind.v2.model.core.MaybeElement;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSParticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.*;

import static java.lang.String.format;

public class SubstitutionPropertyVisitor implements CPropertyVisitor<Void> {

  private static final Logger logger = LoggerFactory.getLogger(SubstitutionPropertyVisitor.class);

  private static final String PROP_FORMAT_INFO = "  %-23s: %-70s | %s";
  private static final String PROP_FORMAT_MSG = "  %-23s: %s";
  private static final String PROP_FORMAT_MSG_TYPE = "  %-23s: not substitutable as type %s";

  private final CClassInfo classInfo;

  private final List<SubstitutionProperty> properties = new LinkedList<>();

  boolean hasSubstitutedProperties() {
    return !properties.isEmpty();
  }

  List<SubstitutionProperty> getProperties() {
    return Collections.unmodifiableList(properties);
  }

  public SubstitutionPropertyVisitor(CClassInfo classInfo) {
    this.classInfo = classInfo;
  }


  private void handleElementChoice(CElementPropertyInfo property, XSParticle particle) {

    logger.warn(format(PROP_FORMAT_MSG, property.getName(false), "try to collapse multi-typed choice"));
    logger.info(format("    %d choice members", property.ref().size()));

    Map<CClassInfo, CClassInfo> baseTypes = new HashMap<>(property.ref().size());

    int m = 1;

    for (CTypeRef typeRef : property.getTypes()) {
      if (typeRef.getTarget() instanceof CClassInfo) {
        CClassInfo targetType = (CClassInfo) typeRef.getTarget();
        logger.info(format("      %d -> %s", m++, targetType.fullName()));
        if (!baseTypes.containsKey(targetType.getBaseClass())) {
          baseTypes.put(targetType.getBaseClass(), targetType);
        }
      } else {
        logger.info(format("      %d -> %s", m++, typeRef.getTypeName()));
      }

    }

    if (baseTypes.size() != 1) {
      // can't do much with that, better to use simplify plugin to separate this thing
    } else {
      properties.add(new ChoicePropertyElement(classInfo, property, baseTypes.keySet().stream().findFirst().get(), particle));
    }

  }


  public Void onElement(CElementPropertyInfo property) {

    if (property.ref().size() == 0) {
      logger.info(format(PROP_FORMAT_MSG, property.getName(false), "may be wildcard"));
      return null;
    }

    if (property.ref().size() > 1) {

      XSComponent xsc = property.getSchemaComponent();
      if (xsc instanceof XSParticle) {
        XSParticle xsp = (XSParticle) xsc;
        if (xsp.getTerm().isModelGroup() && "choice".equals(xsp.getTerm().asModelGroup().getCompositor().toString())) {
          handleElementChoice(property, xsp);
        }
      }

      return null;

    }

    // its a single type

    CNonElement elementDef = property.ref().get(0);

    if (elementDef instanceof CClassInfo) {

      CClassInfo targetClassInfo = (CClassInfo) elementDef;

      // if type is a class and has subclasses and is abstract, definitely a candidate for replacement
      if (targetClassInfo.isAbstract() && targetClassInfo.hasSubClasses()) {

        properties.add(new SubstitutionPropertyElement(classInfo, property, targetClassInfo));

        logger.info(format(PROP_FORMAT_INFO,
          property.getName(false),
          targetClassInfo.getType().fullName(),
          toFixedString(targetClassInfo.getTypeName())));

      }

    } else if (elementDef instanceof MaybeElement) {
      MaybeElement e = (MaybeElement) elementDef;
      logger.info(format(PROP_FORMAT_MSG_TYPE, property.getName(false), toFixedString(e.getTypeName())));
    } else {
      logger.error(format(PROP_FORMAT_MSG_TYPE, property.getName(false), elementDef.getType().fullName()));
      assert false;
    }


    return null;

  }

  public Void onReference(final CReferencePropertyInfo property) {

    // region normalise input

    if (property.getElements().size() > 1) {
      logger.warn(format(PROP_FORMAT_MSG, property.getName(false), "is not single typed"));
      return null;
    } else if (property.getElements().size() == 0) {
      if (property.getWildcard() != null) {
        logger.warn(format(PROP_FORMAT_MSG, property.getName(false), "is wildcard"));
      } else {
        logger.warn(format(PROP_FORMAT_MSG, property.getName(false), "is non-typed, please raise a defect for this scenario"));
      }
      return null;
    }

    // the ref should be to a type that is base of the substitution list

    CElement elementDef = property.getElements().stream().findFirst().orElse(null);

    if (!(elementDef instanceof CElementInfo)) {
      logger.warn(format("  %-23s: element ref definition is not CElementInfo but %s", property.getName(false), elementDef.getClass().getSimpleName()));
      return null;
    }

    // endregion

    logger.info(format(PROP_FORMAT_INFO, property.getName(false), elementDef.getType().fullName(), elementDef.getElementName()));

    // 1. straight ref to an abstract substitutable type
    // 2. ref to a complex extension of a substitutable type

    CElementInfo elementInfo = (CElementInfo) elementDef;

    // auto detect refs that have substitution members
    if (elementInfo.getSubstitutionMembers().size() > 0) {

      logger.info(format("    %d substitution members", elementInfo.getSubstitutionMembers().size()));
      int m = 1;
      for (CElementInfo sub : elementInfo.getSubstitutionMembers()) {
        logger.info(format("      %d -> %s", m++, sub.fullName()));
      }

    }

    // only rewrite if the type name is
    if (elementDef.getType().fullName().startsWith("javax.xml.bind.JAXBElement")) {
      properties.add(new SubstitutionPropertyReference(classInfo, property, elementInfo));
    }

    return null;

  }

  private String toFixedString(QName name) {
    if (name == null) {
      return null;
    } else if ("\u0000".equals(name.getLocalPart())) {
      return new QName(name.getNamespaceURI(), "gregorianCalendar").toString();
    } else {
      return name.toString();
    }
  }

  // region not implemented

  public Void onAttribute(CAttributePropertyInfo property) {
    logger.info(format(PROP_FORMAT_MSG, property.getName(false), "not substitutable as attribute"));
    return null;
  }

  public Void onValue(CValuePropertyInfo property) {
    logger.info(format(PROP_FORMAT_MSG, property.getName(false), "not substitutable as value"));
    return null;
  }

  // endregion

}
