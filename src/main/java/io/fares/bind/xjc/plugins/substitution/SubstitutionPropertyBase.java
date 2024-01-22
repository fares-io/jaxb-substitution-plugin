package io.fares.bind.xjc.plugins.substitution;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.Outline;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;

public abstract class SubstitutionPropertyBase<P extends CPropertyInfo, T extends CElement> implements SubstitutionProperty<P, T> {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected final CClassInfo classInfo;

  protected final P property;

  protected final T typeReference;

  public SubstitutionPropertyBase(CClassInfo classInfo, P property, T typeReference) {
    this.classInfo = classInfo;
    this.property = property;
    this.typeReference = typeReference;
  }

  public P getProperty() {
    return property;
  }

  public T getTypeReference() {
    return typeReference;
  }

  /**
   * Java generics are retarded
   *
   * @return the type of the candidate
   */
  @SuppressWarnings("unchecked")
  public Class<P> getType() {
    return (Class<P>) property.getClass();
  }

  @Override
  public String getFieldName() {
    return property.getName(false);
  }

  @Override
  public void ignoreStage2(String reason) {
    logger.info(String.format("  %-23s: %s", getFieldName(), reason));
  }

  void swapProperty(CPropertyInfo elementPropertyInfo) {
    // hah this is fun, setParent is package scoped on CPropertyInfo so the only way to get
    // the parent set on the property is to first add it using the CClassInfo.addProperty
    // method, then remove it from the collection and then add it back with the correct index

    int index = classInfo.getProperties().indexOf(property);

    // set parent
    classInfo.addProperty(elementPropertyInfo);
    classInfo.getProperties().remove(elementPropertyInfo);

    // add at original position
    classInfo.getProperties().add(index, elementPropertyInfo);
    classInfo.getProperties().remove(property);
  }


  /**
   * This hack replaces the @{@link XmlElement} type of the annotation use with @{@link XmlElementRef}.
   * All other attributes on the annotation itself stay intact.
   *
   * @param outline the outline to use for type loading
   * @param field   the field on which to replace the annotation type
   */
  void replaceXmlElementWithRef(Outline outline, JFieldVar field) {

    JAnnotationUse xmlElementAnnotation = null;

    for (JAnnotationUse annotation : field.annotations()) {
      if (XmlElement.class.getName().equals(annotation.getAnnotationClass().fullName())) {
        xmlElementAnnotation = annotation;
        break;
      }
    }

    if (xmlElementAnnotation == null) {
      logger.warn("no XmlElement annotation found");
      return;
    }

    JClass refAnnotation = outline.getCodeModel().ref(XmlElementRef.class);

    // region we cannot transfer as everything is private or package visible only, see https://github.com/eclipse-ee4j/jaxb-ri/issues/1314

//    field.removeAnnotation(xmlElementAnnotation);
//    JAnnotationUse xmlElementRefAnnotation = field.annotate(refAnnotation);
//    JAnnotationValue name = xmlElementAnnotation.getAnnotationMembers().get("name");
//    xmlElementRefAnnotation.addValue("name", name);
//    // transfer required
//    // add type to point to the head of the substitution

    // endregion

    // so we brute force the annotation class type

    try {
      logger.info(String.format("  %-23s: swap XmlElement for XmlElementRef", field.name()));
      // swap XmlElement for XmlElementRef
      FieldUtils.writeField(xmlElementAnnotation, "clazz", refAnnotation, true);
      // TODO inspect params to make sure we don't transfer [nillable|defaultValue]
    } catch (IllegalAccessException e) {
      throw new PostProcessingException("The substitution plugin is prevented from modifying an inaccessible field in the XJC model (generation time only). Please ensure your security manager is configured correctly.", e);
    } catch (IllegalArgumentException e) {
      throw new PostProcessingException("The substitution plugin encountered an internal error extracting the generated field details.", e);
    }

  }

}
