package io.fares.jaxb.xjc.plugins.substitution;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.Ignore;

import java.io.File;

@Ignore
public class SubstitutionHeadElementListTest extends AbstractSubstitutionPluginTest {

  @Override
  public File getSchemaDirectory() {
    return new File(getBaseDir(), "src/test/resources/schemas/SubstitutionHeadElementList");
  }

  @Override
  protected void validateGeneratedContextClass(CompilationUnit unit) {
    new ExtensionListValidator().visit(unit, null);
  }

}
