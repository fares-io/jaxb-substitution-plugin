package io.fares.jaxb.xjc.plugins.substitution;

import io.fares.jaxb.xjc.plugins.substitution.validators.ExtensionListValidator;
import io.fares.jaxb.xjc.plugins.substitution.validators.TestValidator;

import java.io.File;

public class SubstitutionHeadElementListTest extends AbstractSubstitutionPluginTest {

  @Override
  public File getSchemaDirectory() {
    return new File(getBaseDir(), "src/test/resources/schemas/SubstitutionHeadElementList");
  }

  @Override
  protected TestValidator getValidator() {
    return new ExtensionListValidator();
  }

}
