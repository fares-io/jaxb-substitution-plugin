package io.fares.jaxb.xjc.plugins.substitution;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

import static org.junit.Assert.*;

public class ExtensionListValidator extends VoidVisitorAdapter<Void> {

  @Override
  public void visit(MethodDeclaration n, Void arg) {

    if ("getExtension".equals(n.getName())) {
      assertNotNull(n.getType());
      assertTrue(n.getType() instanceof ReferenceType);
      assertTrue(((ReferenceType) n.getType()).getType() instanceof ClassOrInterfaceType);
      assertEquals("List", ((ClassOrInterfaceType) ((ReferenceType) n.getType()).getType()).getName());
      assertEquals(1, ((ClassOrInterfaceType) ((ReferenceType) n.getType()).getType()).getTypeArgs().size());
      assertEquals("Extension", ((ClassOrInterfaceType) ((ReferenceType) ((List) ((ClassOrInterfaceType) ((ReferenceType) n.getType()).getType()).getTypeArgs()).get(0)).getType()).getName());
    }

  }

}
