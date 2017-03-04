# JAXB Substitution Group Plugin

XJC does some pretty strange stuff based on the assumption that people will do weird things within the bounds of the XML schema specification. Unfortunately some of those assumptions result in generation of unusable JAXB binding classes. This plugin attempts to correct one of these subtile anoyances.


## Problem 1 - Using Element Ref with a Substitution Group Head

Depending on whether the substitution group head is referenced in the same compilation multiple times or not, XJC compiler will create a `JAXBElement<T>` typed field. There was an [article](https://community.oracle.com/blogs/kohsuke/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always) on the java.net community that goes into the details but my interpretation of the design decision is that unless one can guarantee that all types are known at design time, XJC will uglify the model to ensure no one can simply wedge a Java object into the field. XSD spec here or there, mixing runtime considerations with compile time generation strategies is poor practice. There are at least 2 ways to get around the problem at runtime that the JAXB team tried to solve at compile time.

Lets say we have a schema where the `Extension` element is defined as a substitution head in at least 1 other XML element, XJC will create a ugly and unusable class with `JaxbElement` wrapped fields.

Schema:

```xml
<xsd:complexType name="Context">
  <xsd:sequence>
    <xsd:element ref="tns:Extension" minOccurs="0" maxOccurs="10"/>
  </xsd:sequence>
</xsd:complexType>

<xsd:element name="Extension" type="tns:Extension" abstract="true"/>

<xsd:complexType name="Extension" abstract="true">
  <xsd:attribute name="type" type="xsd:string" use="required"/>
</xsd:complexType>

<xsd:element name="SomeExtension" type="tns:SomeExtension" substitutionGroup="tns:Extension"/>

<xsd:complexType name="SomeExtension">
  <xsd:complexContent>
    <xsd:extension base="tns:Extension"/>
  </xsd:complexContent>
</xsd:complexType>
```

Generated Class:

```java
public class Context {

    @XmlElementRef(name = "Extension", namespace = "urn:test", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends Extension>> extension;

}
```

## Problem 2 - Using Inline Element based on a Substitution Group Head type

This variant will generate a properly formatted Java bean but when the JAXB marshaller tries to serialise the bean it makes an utter mess of the XML that is hard to process in xml processing engines such as xquery.

Schema:

```xml
<xsd:complexType name="Context">
  <xsd:sequence>
    <xsd:element name="Extension" type="tns:Extension" minOccurs="0" maxOccurs="10"/>
  </xsd:sequence>
</xsd:complexType>
```

Generated Class:

```java
public class Context {

  @XmlElement(name = "Extension")
  protected List<Extension> extension;

}
```

JAXB Output:

```xml
<t:Context xmlns:t="urn:test" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <t:Extension xsi:type="t:SomeExtension"/>    
  <t:Extension xsi:type="t:OneExtension"/>
</t:Context>
```

## Target State

The ideal modelling state would be to just reference a substitution head element (substitution group heads dont work at the type level anyhow).

1. ensure the `JaxbElement` wrapper is not generated
2. ensure any `@XmlElement` annotations at field or accessor level are replaced with `@XmlElementRef`

Schema: 

```xml
<xsd:complexType name="Context">
  <xsd:sequence>
    <xsd:element ref="tns:Extension" minOccurs="0" maxOccurs="10"/>
  </xsd:sequence>
</xsd:complexType>
```

Generated Class:

```java
public class Context {

  @XmlElementRef
  protected List<Extension> extension;

}
```

JAXB Output:

```xml
<t:Context xmlns:t="urn:test">
  <t:SomeExtension/>    
  <t:OneExtension/>
</t:Context>
```

## Solution

The substitution plugin can fix a limited number of use cases. Simple add the `substitution:head-ref` on a referenced substitution head element or use the `substitution:head` annotation on the substitution head element directly. Applied at the substitution head element level, the substitution plugin will be applied to all element references in the compilation scope. 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema targetNamespace="urn:test"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:tns="urn:test"
            xmlns:substitution="http://jaxb2-commons.dev.java.net/basic/substitution"
            xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
            elementFormDefault="qualified"
            attributeFormDefault="unqualified"
            jaxb:version="2.1"
            jaxb:extensionBindingPrefixes="substitution">

  <xsd:element name="Context" type="tns:Context"/>

  <xsd:complexType name="Context">
    <xsd:sequence>
      <xsd:element ref="tns:Extension">
        <xsd:annotation>
          <xsd:appinfo>
            <substitution:head-ref/>
          </xsd:appinfo>
        </xsd:annotation>
      </xsd:element>
    </xsd:sequence>
  </xsd:complexType>

  <xsd:element name="Extension" type="tns:Extension" abstract="true"/>

  <xsd:complexType name="Extension" abstract="true">
    <xsd:attribute name="type" type="xsd:string" use="required"/>
  </xsd:complexType>

  <xsd:element name="SomeExtension" type="tns:SomeExtension" substitutionGroup="tns:Extension"/>

  <xsd:complexType name="SomeExtension">
    <xsd:complexContent>
      <xsd:extension base="tns:Extension"/>
    </xsd:complexContent>
  </xsd:complexType>

</xsd:schema>
```




