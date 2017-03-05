/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.fares.bind.xjc.plugins.substitution.validators;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;

import javax.xml.bind.annotation.XmlElementRef;
import java.util.List;

import static org.junit.Assert.*;

public class ExtensionListValidator extends TestValidator {


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
      assertEquals("List", ((ClassOrInterfaceType) ((ReferenceType) n.getType()).getType()).getName());
      assertEquals(1, ((ClassOrInterfaceType) ((ReferenceType) n.getType()).getType()).getTypeArgs().size());
      assertEquals("Extension", ((ClassOrInterfaceType) ((ReferenceType) ((List) ((ClassOrInterfaceType) ((ReferenceType) n.getType()).getType()).getTypeArgs()).get(0)).getType()).getName());
      // check if annotated correctly
      assertEquals(1, n.getAnnotations().size());
      assertEquals(XmlElementRef.class.getSimpleName(), n.getAnnotations().get(0).getName().getName());

    }

  }

}
