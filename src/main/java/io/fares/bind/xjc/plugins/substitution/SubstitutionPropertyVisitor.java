package io.fares.bind.xjc.plugins.substitution;

import com.sun.tools.xjc.model.*;

public class SubstitutionPropertyVisitor implements CPropertyVisitor<Void> {

  final CClassInfo classInfo;

  protected SubstitutionPropertyVisitor(final CClassInfo classInfo) {
    this.classInfo = classInfo;
  }

  public Void onElement(CElementPropertyInfo element) {

    boolean isCandidate = Customisation.hasCustomizationsInProperty(element, SubstitutionPlugin.SUBSTITUTION_HEAD_REF_NAME);

    for (CTypeInfo ref : element.ref()) {
      isCandidate = isCandidate || Customisation.hasCustomizationsInType(ref, SubstitutionPlugin.SUBSTITUTION_HEAD_NAME);
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

    boolean hasPropertyCustomizations = Customisation.hasCustomizationsInProperty(property, SubstitutionPlugin.SUBSTITUTION_HEAD_REF_NAME);

    boolean hasTargetElementCustomizations = false;

    // REVIEW should check if there is only 1 reference
    for (CElement element : property.getElements()) {
      hasTargetElementCustomizations = hasTargetElementCustomizations || Customisation.hasCustomizationsInType(element, SubstitutionPlugin.SUBSTITUTION_HEAD_NAME);
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

  private CElementPropertyInfo createElementPropertyInfo(final CReferencePropertyInfo property,
                                                         final CElementInfo elementInfo,
                                                         final CCustomizations customizations) {

    final CElementPropertyInfo elementPropertyInfo = new CElementPropertyInfo(
      property.getName(false),
      property.isCollection() ? CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT : CElementPropertyInfo.CollectionMode.NOT_REPEATED,
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

}
