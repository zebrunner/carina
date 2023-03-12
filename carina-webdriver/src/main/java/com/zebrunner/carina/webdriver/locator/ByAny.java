package com.zebrunner.carina.webdriver.locator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ByAny extends By implements Serializable {

    private static final long serialVersionUID = 4573668832699497306L;

    private By[] bys;

    public ByAny(By... bys) {
        this.bys = bys;
    }

    @Override
    public WebElement findElement(SearchContext context) {
        for (By by : bys) {
            List<WebElement> elements = context.findElements(by);
            if (!elements.isEmpty()) {
                return elements.get(0);
            }
        }
        throw new NoSuchElementException("Cannot locate an element using " + toString());
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        List<WebElement> elems = new ArrayList<>();
        for (By by : bys) {
            List<WebElement> elements = context.findElements(by);
            if (!elements.isEmpty()) {
                elems = elements;
                break;
            }
        }
        return elems;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("By.any(");
        stringBuilder.append("{");

        boolean first = true;
        for (By by : bys) {
            stringBuilder.append((first ? "" : ",")).append(by);
            first = false;
        }
        stringBuilder.append("})");
        return stringBuilder.toString();
    }

}
