package io.fares.jaxb.xjc.plugins.substitution.validators;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class TestValidator extends VoidVisitorAdapter<Void> {

  protected boolean found = false;

  public boolean isFound() {
    return found;
  }

}
