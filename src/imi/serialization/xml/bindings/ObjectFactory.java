//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.17 at 12:30:48 PM EST 
//


package imi.serialization.xml.bindings;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the imi.serialization.xml.bindings package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Character_QNAME = new QName("http://xml.netbeans.org/schema/CharacterXMLSchema", "Character");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: imi.serialization.xml.bindings
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FloatRow }
     * 
     */
    public xmlFloatRow createFloatRow() {
        return new xmlFloatRow();
    }

    /**
     * Create an instance of {@link SkinnedMeshAdditionParams }
     * 
     */
    public xmlSkinnedMeshParams createSkinnedMeshAdditionParams() {
        return new xmlSkinnedMeshParams();
    }

    /**
     * Create an instance of {@link xmlShaderProperty }
     * 
     */
    public xmlShaderProperty createShaderProperty() {
        return new xmlShaderProperty();
    }

    /**
     * Create an instance of {@link CharacterAttachmentParameters }
     * 
     */
    public xmlCharacterAttachmentParameters createCharacterAttachmentParameters() {
        return new xmlCharacterAttachmentParameters();
    }

    /**
     * Create an instance of {@link JointModification }
     * 
     */
    public xmlJointModification createJointModification() {
        return new xmlJointModification();
    }

    /**
     * Create an instance of {@link xmlShaderProgram }
     * 
     */
    public xmlShaderProgram createShaderProgram() {
        return new xmlShaderProgram();
    }

    /**
     * Create an instance of {@link xmlColorRGBA }
     * 
     */
    public xmlColorRGBA createColorRGBA() {
        return new xmlColorRGBA();
    }

    /**
     * Create an instance of {@link Character }
     * 
     */
    public xmlCharacter createCharacter() {
        return new xmlCharacter();
    }

    /**
     * Create an instance of {@link xmlShader }
     * 
     */
    public xmlShader createShader() {
        return new xmlShader();
    }

    /**
     * Create an instance of {@link CharacterAttributes }
     * 
     */
    public xmlCharacterAttributes createCharacterAttributes() {
        return new xmlCharacterAttributes();
    }

    /**
     * Create an instance of {@link xmlMaterial }
     * 
     */
    public xmlMaterial createMaterial() {
        return new xmlMaterial();
    }

    /**
     * Create an instance of {@link xmlTextureAttributes }
     * 
     */
    public xmlTextureAttributes createTextureAttributes() {
        return new xmlTextureAttributes();
    }

    /**
     * Create an instance of {@link Matrix }
     * 
     */
    public xmlMatrix createMatrix() {
        return new xmlMatrix();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Character }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.netbeans.org/schema/CharacterXMLSchema", name = "Character")
    public JAXBElement<Character> createCharacter(Character value) {
        return new JAXBElement<Character>(_Character_QNAME, Character.class, null, value);
    }

}
