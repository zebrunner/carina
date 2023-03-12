package com.zebrunner.carina.webdriver.locator.converter.caseinsensitive;

public class ParamsToConvert {

    private boolean id = false;
    private boolean name = false;
    private boolean text = false;
    private boolean classAttr = false;

    public ParamsToConvert(boolean id, boolean name, boolean text, boolean classAttr) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.classAttr = classAttr;
    }

    public boolean isId() {
        return id;
    }

    public boolean isName() {
        return name;
    }

    public boolean isText() {
        return text;
    }

    public boolean isClassAttr() {
        return classAttr;
    }
}
