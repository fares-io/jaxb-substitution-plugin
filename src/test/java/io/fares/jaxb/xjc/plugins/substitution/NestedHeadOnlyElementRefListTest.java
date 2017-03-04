package io.fares.jaxb.xjc.plugins.substitution;

import io.fares.jaxb.xjc.plugins.substitution.validators.TestValidator;
import io.fares.jaxb.xjc.plugins.substitution.validators.WrappedExtensionListValidator;

import java.io.File;

public class NestedHeadOnlyElementRefListTest extends AbstractSubstitutionPluginTest {


  private boolean found = false;

  @Override
  public File getSchemaDirectory() {
    return new File(getBaseDir(), "src/test/resources/schemas/NestedHeadOnlyElementRefList");
  }

  @Override
  protected TestValidator getValidator() {
    return new WrappedExtensionListValidator();
  }

}
