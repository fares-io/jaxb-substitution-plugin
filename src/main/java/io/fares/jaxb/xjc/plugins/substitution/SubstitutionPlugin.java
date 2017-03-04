package io.fares.jaxb.xjc.plugins.substitution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.namespace.QName;

import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFieldVar;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;

import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode;

import com.sun.tools.xjc.generator.bean.field.SingleField;
import com.sun.tools.xjc.generator.bean.field.UntypedListField;

import org.apache.commons.lang3.reflect.FieldUtils;

public class SubstitutionPlugin extends Plugin {

  public static final String NS = "http://jaxb2-commons.dev.java.net/basic/substitution";

  public static final String SUBSTITUTION_HEAD = "head";

  public static final QName SUBSTITUTION_HEAD_NAME = new QName(NS, SUBSTITUTION_HEAD);

  public static final String SUBSTITUTION_HEAD_REF = "head-ref";

  public static final QName SUBSTITUTION_HEAD_REF_NAME = new QName(NS, SUBSTITUTION_HEAD_REF);


  @Override
  public List<String> getCustomizationURIs() {
    return Arrays.asList(NS);
  }

  @Override
  public boolean isCustomizationTagName(String nsUri, String localName) {
    return NS.equals(nsUri) && SUBSTITUTION_HEAD_REF.equals(localName);
  }

  @Override
  public String getOptionName() {
    return "Xsubstitution";
  }

  @Override
  public String getUsage() {
    return "  -Xsubstitution          : enable substitution group replacement";
  }

  @Override
  public void postProcessModel(Model model, ErrorHandler errorHandler) {

    for (final CClassInfo classInfo : model.beans().values()) {
      postProcessClassInfo(model, classInfo);
    }

  }


  private void postProcessClassInfo(final Model model, final CClassInfo classInfo) {

    final List<CPropertyInfo> properties = new ArrayList<>(classInfo.getProperties());

    for (CPropertyInfo property : properties) {

      List<CPluginCustomization> elementCustomizations = Customisation.findPropertyCustomizationsInPropertyAndClass(property, SUBSTITUTION_HEAD_REF_NAME, SUBSTITUTION_HEAD_REF_NAME);

      if (!elementCustomizations.isEmpty()) {

        property.accept(new CPropertyVisitor<Void>() {

          public Void onElement(CElementPropertyInfo property) {
            return null;
          }

          public Void onAttribute(CAttributePropertyInfo property) {
            return null;
          }

          public Void onValue(CValuePropertyInfo property) {
            return null;
          }

          public Void onReference(final CReferencePropertyInfo property) {

            int index = classInfo.getProperties().indexOf(property);

            for (CElement element : property.getElements()) {

              final CElementPropertyInfo elementPropertyInfo;

              if (element instanceof CElementInfo) {
                elementPropertyInfo = createElementPropertyInfo(property, (CElementInfo) element);
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

            return null;

          }

        });
      }
    }
  }

  private CElementPropertyInfo createElementPropertyInfo(final CReferencePropertyInfo property,
                                                         final CElementInfo elementInfo) {

    final CElementPropertyInfo elementPropertyInfo = new CElementPropertyInfo(
      property.getName(false),
      property.isCollection() ? CollectionMode.REPEATED_ELEMENT : CollectionMode.NOT_REPEATED,
      property.id(),
      property.getExpectedMimeType(),
      property.getSchemaComponent(),
      property.getCustomizations(),
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
        List<CPluginCustomization> customizations = Customisation.findPropertyCustomizationsInProperty(propertyInfo, SUBSTITUTION_HEAD_REF_NAME);
        // TODO check reference type customization as we may have a substitution:head on the global type
        if (!customizations.isEmpty()) {

          try {
            Object rawFieldVar = FieldUtils.readField(fieldOutline, "field", true);
            if (rawFieldVar instanceof JFieldVar) {
              JFieldVar jFieldVar = (JFieldVar) rawFieldVar;
              for (JAnnotationUse annotation : jFieldVar.annotations()) {
                JClass acl = annotation.getAnnotationClass();
                if (XmlElement.class.getName().equals(acl.fullName())) {
                  // swap XmlElement for XmlElementRef
                  FieldUtils.writeField(annotation, "clazz", outline.getCodeModel().ref(XmlElementRef.class), true);
                  // TODO inspect params to make sure we don't transfer [nillable|defaultValue]
                }
              }
            } else {
              errorHandler.error(new SAXParseException("The substitution plugin is unable to process substitution field.", propertyInfo.getLocator()));
              return false;
            }
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

    return true;
  }

}
