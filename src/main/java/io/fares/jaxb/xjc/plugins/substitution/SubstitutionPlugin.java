package io.fares.jaxb.xjc.plugins.substitution;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.namespace.QName;

import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JFieldVar;

import com.sun.tools.xjc.Options;

import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode;

import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;

import org.apache.commons.lang3.reflect.FieldUtils;

public class SubstitutionPlugin extends AbstractParameterizablePlugin {

  public static final String NS = "http://jaxb2-commons.dev.java.net/basic/substitution";

  public static final String SUBSTITUTION_HEAD = "head";

  public static final QName SUBSTITUTION_HEAD_NAME = new QName(NS, SUBSTITUTION_HEAD);

  public static final String SUBSTITUTION_HEAD_REF = "head-ref";

  public static final QName SUBSTITUTION_HEAD_REF_NAME = new QName(NS, SUBSTITUTION_HEAD_REF);

  @Override
  public String getOptionName() {
    return "Xsubstitution";
  }

  @Override
  public String getUsage() {
    return "  -Xsubstitution          : enable substitution group replacement";
  }

  @Override
  public Collection<QName> getCustomizationElementNames() {
    return Arrays.asList(SUBSTITUTION_HEAD_NAME, SUBSTITUTION_HEAD_REF_NAME);
  }

  @Override
  public void postProcessModel(Model model, ErrorHandler errorHandler) {

    for (final CClassInfo classInfo : model.beans().values()) {
      postProcessClassInfo(model, classInfo);
    }

  }


  private void postProcessClassInfo(final Model model, final CClassInfo classInfo) {

    classInfo.accept(new CClassInfoParent.Visitor<Void>() {

      @Override
      public Void onBean(CClassInfo bean) {
        return null;
      }

      @Override
      public Void onPackage(JPackage pkg) {
        return null;
      }

      @Override
      public Void onElement(CElementInfo element) {
        return null;
      }

    });


    for (CPropertyInfo property : classInfo.getProperties()) {

      property.accept(new CPropertyVisitor<Void>() {

        public Void onElement(CElementPropertyInfo element) {

          boolean isCandidate = Customisation.hasCustomizationsInProperty(element, SUBSTITUTION_HEAD_REF_NAME);

          for (CTypeInfo ref : element.ref()) {
            isCandidate = isCandidate || Customisation.hasCustomizationsInType(ref, SUBSTITUTION_HEAD_NAME);
          }

          if (isCandidate) {
            element.getAdapter();
          }

          return null;
        }

        public Void onAttribute(CAttributePropertyInfo attribute) {
          return null;
        }

        public Void onValue(CValuePropertyInfo property) {
          return null;
        }

        public Void onReference(final CReferencePropertyInfo property) {

          // TODO check if the property, accessor or referenced class has a substitution annotation

          boolean hasPropertyCustomizations = Customisation.hasCustomizationsInProperty(property, SUBSTITUTION_HEAD_REF_NAME);

          boolean hasTargetElementCustomizations = false;

          // REVIEW should check if there is only 1 reference
          for (CElement element : property.getElements()) {
            hasTargetElementCustomizations = hasTargetElementCustomizations || Customisation.hasCustomizationsInType(element, SUBSTITUTION_HEAD_NAME);
          }

          if (hasPropertyCustomizations || hasTargetElementCustomizations) {

            int index = classInfo.getProperties().indexOf(property);

            for (CElement element : property.getElements()) {

              final CElementPropertyInfo elementPropertyInfo;

              if (element instanceof CElementInfo) {
                // FIXME need to translate the element type reference head customizations into head-ref ones
                CCustomizations customizations = hasPropertyCustomizations ? property.getCustomizations() : element.getCustomizations();
                elementPropertyInfo = createElementPropertyInfo(property, (CElementInfo) element, customizations);
              } else if (element instanceof CClassInfo) {
                elementPropertyInfo = null;
              } else if (element instanceof CClassRef) {
                elementPropertyInfo = null;
              } else {
                elementPropertyInfo = null;
              }

              if (elementPropertyInfo != null) {
                // hah this is fun, setParent is package scoped on CPropertyInfo so the only way to get
                // the parent set on the property is to first add it using the CClassInfo.addProperty
                // method, then remove it from the collection and then add it back with the correct index
                classInfo.addProperty(elementPropertyInfo);
                classInfo.getProperties().remove(elementPropertyInfo);
                classInfo.getProperties().add(index++, elementPropertyInfo);
                classInfo.getProperties().remove(property);
              }
            }
          }

          return null;

        }

      });

    }
  }

  private CElementPropertyInfo createElementPropertyInfo(final CReferencePropertyInfo property,
                                                         final CElementInfo elementInfo,
                                                         final CCustomizations customizations) {

    final CElementPropertyInfo elementPropertyInfo = new CElementPropertyInfo(
      property.getName(false),
      property.isCollection() ? CollectionMode.REPEATED_ELEMENT : CollectionMode.NOT_REPEATED,
      property.id(),
      property.getExpectedMimeType(),
      property.getSchemaComponent(),
      customizations,
      property.getLocator(),
      property.isRequired());

    // also need to set the public name
    elementPropertyInfo.setName(true, property.getName(true));

    // add adapters of the ref type to this element as it is not going to be referenced anymore
    final CAdapter adapter = elementInfo.getProperty().getAdapter();

    if (adapter != null) {
      elementPropertyInfo.setAdapter(adapter);
    }

    // add the type reference to the field
    CTypeRef ref = new CTypeRef(
      elementInfo.getContentType(),
      elementInfo.getElementName(),
      elementInfo.getContentType().getTypeName(),
      false,
      null);

    elementPropertyInfo.getTypes().add(ref);

    return elementPropertyInfo;
  }

  @Override
  public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws SAXException {

    for (ClassOutline classOutline : outline.getClasses()) {
      // now in part 2 of the transformation we need to traverse the generated field declarations
      // find our plugin customization and swap @XmlElement for @XmlElementRef
      for (FieldOutline fieldOutline : classOutline.getDeclaredFields()) {

        CPropertyInfo propertyInfo = fieldOutline.getPropertyInfo();

        // check field property customization
        boolean hasHeadRef = Customisation.hasCustomizationsInProperty(propertyInfo, SUBSTITUTION_HEAD_REF_NAME);
        // FIXME should not need to do this here but customization gets mixed up when we replace the Ref with
        //       Element field in the model transformation.
        hasHeadRef = Customisation.hasCustomizationsInProperty(propertyInfo, SUBSTITUTION_HEAD_NAME) || hasHeadRef;

        // check if the referenced type is the subsctitution head
        // FIXME currently only able to add the customization on the complexType and not the element
        boolean hasHead = false;

        for (CTypeInfo typeInfo : propertyInfo.ref()) {
          hasHead = hasHead || Customisation.hasCustomizationsInType(typeInfo, SUBSTITUTION_HEAD_NAME);
        }

        if (hasHeadRef || hasHead) {

          // can be changed to containsKey and getKey
          for (JFieldVar field : classOutline.ref.fields().values()) {

            if (propertyInfo.getName(false).equals(field.name())) {

              JFieldVar jFieldVar = classOutline.ref.fields().get(propertyInfo.getName(false));

              for (JAnnotationUse annotation : jFieldVar.annotations()) {
                JClass acl = annotation.getAnnotationClass();
                if (XmlElement.class.getName().equals(acl.fullName())) {
                  try {
                    // swap XmlElement for XmlElementRef
                    FieldUtils.writeField(annotation, "clazz", outline.getCodeModel().ref(XmlElementRef.class), true);
                    // TODO inspect params to make sure we don't transfer [nillable|defaultValue]
                  } catch (IllegalAccessException e) {
                    errorHandler.error(new SAXParseException("The substitution plugin is prevented from modifying an inaccessible field in the XJC model (generation time only). Please ensure your security manager is configured correctly.", propertyInfo.getLocator(), e));
                    return false;
                  } catch (IllegalArgumentException e) {
                    errorHandler.error(new SAXParseException("The substitution plugin encountered an internal error extracting the generated field details.", propertyInfo.getLocator(), e));
                    return false;
                  }
                }
              }
            }
          }
        }
      }
    }

    return true;
  }

}
