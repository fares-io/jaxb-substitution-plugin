package io.fares.jaxb.xjc.plugins.substitution.validators;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import javax.xml.bind.annotation.XmlElementRef;
import java.util.List;

import static org.junit.Assert.*;

public class ExtensionValidator extends TestValidator {

  @Override
  public void visit(FieldDeclaration n, Void arg) {
    super.visit(n, arg);

    Node parent = n.getParentNode();

    if (parent instanceof TypeDeclaration &&
      "Context".equals(((TypeDeclaration) parent).getName()) &&
      n.getVariables().size() == 1 && "extension".equals(n.getVariables().get(0).getId().getName())) {

      this.found = true;

      // check if type is List<Extension> and not JaxbElement
      assertNotNull(n.getType());
      assertTrue(n.getType() instanceof ReferenceType);
      assertTrue(((ReferenceType) n.getType()).getType() instanceof ClassOrInterfaceType);
      assertEquals("Extension", ((ClassOrInterfaceType) ((ReferenceType) n.getType()).getType()).getName());
      // check if annotated correctly
      assertEquals(1, n.getAnnotations().size());
      assertEquals(XmlElementRef.class.getSimpleName(), n.getAnnotations().get(0).getName().getName());

    }

  }

}
