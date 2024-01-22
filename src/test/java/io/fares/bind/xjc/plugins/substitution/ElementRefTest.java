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
package io.fares.bind.xjc.plugins.substitution;

import com.github.javaparser.ParseException;
import io.fares.bind.xjc.plugins.extras.testing.JaxbMojoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import java.io.IOException;

class ElementRefTest implements AnnotationHelper {

  @RegisterExtension
  static JaxbMojoExtension MOJO = JaxbMojoExtension.builder()
    .verbose()
    .arg("-Xsubstitution")
    .build();

  @Test
  void optionalExtensionRefShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyOptionalField("test.OptionalContext",
      "extension",
      "Extension",
      "Extension",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void optionalExtensionRefListShouldHaveRef() throws ParseException, IOException {

    verifyOptionalField("test.OptionalListContext",
      "extension",
      "Extension",
      "List<Extension>",
      XmlElementRef.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void requiredExtensionRefShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyRequiredField("test.RequiredContext",
      "extension",
      "Extension",
      "Extension",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void requiredExtensionRefListShouldHaveRef() throws ParseException, IOException {

    verifyRequiredField("test.RequiredListContext",
      "extension",
      "Extension",
      "List<Extension>",
      XmlElementRef.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void extendedExtensionRefShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyRequiredField("test.ExtendedContext",
      "extendedExtendedExtension",
      "ExtendedExtendedExtension",
      "ExtendedExtendedExtension",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

}
