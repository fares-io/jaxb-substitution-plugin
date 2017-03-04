package io.fares.jaxb.xjc.plugins.substitution;

import io.fares.jaxb.xjc.plugins.substitution.validators.ExtensionValidator;
import io.fares.jaxb.xjc.plugins.substitution.validators.TestValidator;

import java.io.File;

public class HeadOnlyElementRefTest extends AbstractSubstitutionPluginTest {


  private boolean found = false;

  @Override
  public File getSchemaDirectory() {
    return new File(getBaseDir(), "src/test/resources/schemas/HeadOnlyElementRef");
  }

  @Override
  protected TestValidator getValidator() {
    return new ExtensionValidator();
  }

}
