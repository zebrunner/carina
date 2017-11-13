package com.qaprosoft.carina.core.foundation.color;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class ColorName {
    @XmlElement
    public int r;
    @XmlElement
    public int g;
    @XmlElement
    public int b;
    @XmlElement
    public String name;

    public ColorName() {

    }

    public ColorName(String name, int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.name = name;
    }

    /**
     * Compute MSE - Mean squared error
     * 
     * @param pixR
     * @param pixG
     * @param pixB
     * @return
     */
    public int computeMSE(int pixR, int pixG, int pixB) {
        return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b) * (pixB - b)) / 3);
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setR(int r) {
        this.r = r;
    }

    public void setG(int g) {
        this.g = g;
    }

    public void setB(int b) {
        this.b = b;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final ColorName other = (ColorName) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.r != other.r || this.g != other.g || this.b != other.b) {
            return false;
        }
        return true;
    }

}
