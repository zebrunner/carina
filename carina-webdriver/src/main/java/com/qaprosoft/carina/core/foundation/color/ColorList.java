package com.qaprosoft.carina.core.foundation.color;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "colorList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ColorList {

    @XmlElement(name = "colorName")
    ArrayList<ColorName> listOfColors;

    public ArrayList<ColorName> getListOfColors() {
        return listOfColors;
    }

    public void setListOfColors(ArrayList<ColorName> listOfColors) {
        this.listOfColors = listOfColors;
    }

    public ColorList() {

    }

}
