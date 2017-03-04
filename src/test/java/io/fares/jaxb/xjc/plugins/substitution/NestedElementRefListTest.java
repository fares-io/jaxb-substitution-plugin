package io.fares.jaxb.xjc.plugins.substitution;

import io.fares.jaxb.xjc.plugins.substitution.validators.TestValidator;
import io.fares.jaxb.xjc.plugins.substitution.validators.WrappedExtensionListValidator;

import java.io.File;

public class NestedElementRefListTest extends AbstractSubstitutionPluginTest {

  @Override
  public File getSchemaDirectory() {
    return new File(getBaseDir(), "src/test/resources/schemas/NestedElementRefList");
  }

  @Override
  protected TestValidator getValidator() {
    return new WrappedExtensionListValidator();
  }


}
