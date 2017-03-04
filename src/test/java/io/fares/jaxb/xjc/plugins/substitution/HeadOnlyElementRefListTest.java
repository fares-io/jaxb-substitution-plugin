package io.fares.jaxb.xjc.plugins.substitution;

import io.fares.jaxb.xjc.plugins.substitution.validators.ExtensionListValidator;
import io.fares.jaxb.xjc.plugins.substitution.validators.TestValidator;

import java.io.File;

public class HeadOnlyElementRefListTest extends AbstractSubstitutionPluginTest {


  private boolean found = false;

  @Override
  public File getSchemaDirectory() {
    return new File(getBaseDir(), "src/test/resources/schemas/HeadOnlyElementRefList");
  }

  @Override
  protected TestValidator getValidator() {
    return new ExtensionListValidator();
  }

}
