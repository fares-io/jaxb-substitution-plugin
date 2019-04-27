package io.fares.bind.xjc.plugins.substitution;

import com.sun.codemodel.JFieldVar;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSParticle;

import java.math.BigInteger;

import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.NOT_REPEATED;
import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT;

public class ChoicePropertyElement extends SubstitutionPropertyBase<CElementPropertyInfo, CClassInfo> {

  private String fieldName;

  private final XSParticle particle;

  public ChoicePropertyElement(CClassInfo classInfo, CElementPropertyInfo property, CClassInfo typeReference, XSParticle particle) {
    super(classInfo, property, typeReference);
    this.particle = particle;
  }

  @Override
  public String getFieldName() {
    return fieldName != null ? fieldName : super.getFieldName();
  }

  @Override
  public void modifyStage1() {

    logger.info(String.format("  [+] %-19s: consolidate to %s", property.getName(false), typeReference.getType()));

    final CElementPropertyInfo elementPropertyInfo = createProperty(property, typeReference);
    this.fieldName = elementPropertyInfo.getName(false);

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
  private CElementPropertyInfo createProperty(final @NotNull CElementPropertyInfo property,
                                              final @NotNull CClassInfo elementInfo) {


    // property has substitution members (may not be the case for abstract)
    String name = property.isCollection() ? elementInfo.shortName + "s" : elementInfo.shortName;

    boolean isRequired = !BigInteger.ZERO.equals(particle.getMinOccurs());

    final CElementPropertyInfo elementPropertyInfo = new CElementPropertyInfo(
      name,
      property.isCollection() ? REPEATED_ELEMENT : NOT_REPEATED,
      property.id(),
      property.getExpectedMimeType(),
      property.getSchemaComponent(),
      property.getCustomizations(),
      property.getLocator(),
      isRequired);

    // add adapters of the target element
    final CAdapter adapter = elementInfo.getAdapterUse();

    if (adapter != null) {
      elementPropertyInfo.setAdapter(adapter);
    }

    // add the type reference to the field
    CTypeRef ref = new CTypeRef(
      elementInfo,
      elementInfo.getTypeName(),
      elementInfo.getTypeName(),
      false,
      null);

    elementPropertyInfo.getTypes().add(ref);

    return elementPropertyInfo;

  }
}
