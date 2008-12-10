package imi.serialization.xml.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkinnedMeshAdditionParams", propOrder = {
    "skinnedMeshName",
    "subGroupName"
})
public class xmlSkinnedMeshParams
{
    @XmlElement(name = "SkinnedMeshName", required = true)
    protected String skinnedMeshName;
    @XmlElement(name = "SubGroupName")
    protected String subGroupName;

    public String getSkinnedMeshName() {
        return skinnedMeshName;
    }


    public void setSkinnedMeshName(String value) {
        this.skinnedMeshName = value;
    }

    public String getSubGroupName() {
        return subGroupName;
    }


    public void setSubGroupName(String value) {
        this.subGroupName = value;
    }

}
