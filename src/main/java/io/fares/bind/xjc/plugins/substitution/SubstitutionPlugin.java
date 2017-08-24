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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    // need to do this as there is another call to classInfo.getProperties() inside the visitor that
    // otherwise causes a ConcurrentModificationException
    List<CPropertyInfo> cpy = new ArrayList<>(50);
    for (CPropertyInfo property : classInfo.getProperties()) {
      cpy.add(property);
    }

    for (CPropertyInfo property : cpy) {
      property.accept(new SubstitutionPropertyVisitor(classInfo));
    }

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
