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

class ElementRefNestedHeadOnlyListTest implements AnnotationHelper {

  @RegisterExtension
  static JaxbMojoExension MOJO = JaxbMojoExension.builder()
    .verbose()
    .arg("-Xsubstitution")
    .arg("-Xxew")
    .build();


  @Test
  void optionalExtensionListShouldHaveRef() throws ParseException, IOException {

    verifyOptionalField("test.OptionalContext",
      "extensions",
      "Extension",
      "List<Extension>",
      XmlElement.class,
      MOJO::getGeneratedPath);

  }

}
