package io.fares.jaxb.xjc.plugins.substitution;

import com.github.javaparser.ast.CompilationUnit;

import java.io.File;

public class ElementRefTest extends AbstractSubstitutionPluginTest {

  @Override
  public File getSchemaDirectory() {
    return new File(getBaseDir(), "src/test/resources/schemas/ElementRef");
  }

  @Override
  protected void validateGeneratedContextClass(CompilationUnit unit) {
    new ExtensionValidator().visit(unit, null);
  }
}
