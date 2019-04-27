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
import io.fares.bind.xjc.plugins.extras.testing.JaxbMojoExension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;

class ElementExtendedTest implements AnnotationHelper {

  @RegisterExtension
  static JaxbMojoExension MOJO = JaxbMojoExension.builder()
    .verbose()
    .arg("-Xsubstitution")
    .build();

  @Test
  void optionalExtensionElementShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyOptionalField("test.OptionalContext",
      "extension",
      "Extension",
      "Extension",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void optionalExtensionElementListShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyOptionalField("test.OptionalListContext",
      "extension",
      "Extension",
      "List<Extension>",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void requiredExtensionElementShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyRequiredField("test.RequiredContext",
      "extension",
      "Extension",
      "Extension",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void requiredExtensionElementListShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyRequiredField("test.RequiredListContext",
      "extension",
      "Extension",
      "List<Extension>",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

  @Test
  void extendedExtensionElementShouldNotHaveAnyRefs() throws ParseException, IOException {

    verifyRequiredField("test.ExtendedContext",
      "extension",
      "Extension",
      "ExtendedExtendedExtension",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

}
