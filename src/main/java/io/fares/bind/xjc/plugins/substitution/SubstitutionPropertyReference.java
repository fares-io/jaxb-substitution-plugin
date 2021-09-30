package io.fares.bind.xjc.plugins.substitution;

import com.sun.codemodel.JFieldVar;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.Outline;

import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.NOT_REPEATED;
import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT;

public class SubstitutionPropertyReference extends SubstitutionPropertyBase<CReferencePropertyInfo, CElementInfo> {

  public SubstitutionPropertyReference(CClassInfo classInfo, CReferencePropertyInfo property, CElementInfo typeReference) {
    super(classInfo, property, typeReference);
  }

  @Override
  public void modifyStage1() {

    logger.info(String.format("  [+] %-19s: unwrap JAXBElement to %s", property.getName(false),
      typeReference.getContentType().toString()));

    final CElementPropertyInfo elementPropertyInfo = createProperty(property, typeReference);

    swapProperty(elementPropertyInfo);

  }

  @Override
  public void modifyStage2(Outline outline, JFieldVar field) {
    // FIXME only swap annotation if list
    if (property.isCollection()) {
      replaceXmlElementWithRef(outline, field);
    } else {
      logger.info(String.format("  %-23s: not a candidate for stage 2 processing", field.name()));
    }
  }

  @NotNull
  private CElementPropertyInfo createProperty(final @NotNull CReferencePropertyInfo property,
                                              final @NotNull CElementInfo elementInfo) {


    // property has substitution members (may not be the case for abstract)

    final CElementPropertyInfo elementPropertyInfo = new CElementPropertyInfo(
      property.getName(true),
      property.isCollection() ? REPEATED_ELEMENT : NOT_REPEATED,
      property.id(),
      property.getExpectedMimeType(),
      property.getSchemaComponent(),
      property.getCustomizations(),
      property.getLocator(),
      property.isRequired());

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
