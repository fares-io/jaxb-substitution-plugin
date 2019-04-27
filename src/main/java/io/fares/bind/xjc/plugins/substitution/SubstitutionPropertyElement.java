package io.fares.bind.xjc.plugins.substitution;

import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.outline.Outline;

public class SubstitutionPropertyElement extends SubstitutionPropertyBase<CElementPropertyInfo, CElement> {

  public SubstitutionPropertyElement(CClassInfo classInfo, CElementPropertyInfo property, CElement typeReference) {
    super(classInfo, property, typeReference);
  }

  @Override
  public void modifyStage1() {
    logger.info(String.format("  %-23s: not a candidate for stage 1 processing", property.getName(false)));
  }

  @Override
  public void modifyStage2(Outline outline, JFieldVar field) {
    logger.info(String.format("  %-23s: not a candidate for stage 2 processing", field.name()));
  }

}
