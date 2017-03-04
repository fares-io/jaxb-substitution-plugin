package io.fares.jaxb.xjc.plugins.substitution;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.jvnet.jaxb2.maven2.AbstractXJC2Mojo;
import org.jvnet.jaxb2.maven2.test.RunXJC2Mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubstitutionPluginTest extends RunXJC2Mojo {

  AbstractXJC2Mojo mojoUnderTest;

  @Override
  public void testExecute() throws Exception {

    super.testExecute();
    // need to load test.Context from sources and see if we have a extension list prop
    File genDir = mojoUnderTest.getGenerateDirectory();

    File clazzFile = new File(genDir, "test/Context.java");

    assertNotNull(clazzFile);
    assertTrue(clazzFile.exists());
    assertTrue(clazzFile.isFile());

    try {
      CompilationUnit unit = JavaParser.parse(clazzFile);
      validateGeneratedContextClass(unit);

    } catch (FileNotFoundException e) {
      assertTrue("not expecting parse failures", false);
    }

  }

  @Override
  protected void configureMojo(AbstractXJC2Mojo mojo) {
    super.configureMojo(mojo);
    mojo.setForceRegenerate(true);
    this.mojoUnderTest = mojo;
  }

  @Override
  public List<String> getArgs() {
    final List<String> args = new ArrayList<>(super.getArgs());
    args.add("-extension");
    args.add("-Xsubstitution");
    return args;
  }

  protected abstract void validateGeneratedContextClass(CompilationUnit unit);

}
