package com.qaprosoft.carina.core.foundation.webdriver.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yauhenipatotski on 4/12/17.
 */
public class ElementsInfo {


    List<ElementInfo> elements = new ArrayList<>();

    public List<ElementInfo> getElements() {
        return elements;
    }

    public void addElement(ElementInfo element) {
        elements.add(element);
    }
}
