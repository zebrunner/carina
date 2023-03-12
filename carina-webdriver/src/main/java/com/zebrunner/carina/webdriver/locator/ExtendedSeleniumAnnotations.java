package com.zebrunner.carina.webdriver.locator;

import java.lang.reflect.Field;

import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.Annotations;

public class ExtendedSeleniumAnnotations extends Annotations {

    /**
     * @param field expected to be an element in a Page Object
     */
    public ExtendedSeleniumAnnotations(Field field) {
        super(field);
    }

    @Override
    protected void assertValidAnnotations() {
        FindBys findBys = getField().getAnnotation(FindBys.class);
        FindAll findAll = getField().getAnnotation(FindAll.class);
        FindBy findBy = getField().getAnnotation(FindBy.class);
        FindAny findAny = getField().getAnnotation(FindAny.class);
        if (findBys != null && findBy != null) {
            throw new IllegalArgumentException("If you use a '@FindBys' annotation, " +
                    "you must not also use a '@FindBy' annotation");
        }
        if (findAll != null && findBy != null) {
            throw new IllegalArgumentException("If you use a '@FindAll' annotation, " +
                    "you must not also use a '@FindBy' annotation");
        }
        if (findAll != null && findBys != null) {
            throw new IllegalArgumentException("If you use a '@FindAll' annotation, " +
                    "you must not also use a '@FindBys' annotation");
        }

        if (findAny != null && findBy != null) {
            throw new IllegalArgumentException("If you use a '@FindAny' annotation, " +
                    "you must not also use a '@FindBy' annotation");
        }

        if (findAny != null && findBys != null) {
            throw new IllegalArgumentException("If you use a '@FindAny' annotation, " +
                    "you must not also use a '@FindBys' annotation");
        }

        if (findAny != null && findAll != null) {
            throw new IllegalArgumentException("If you use a '@FindAny' annotation, " +
                    "you must not also use a '@FindAll' annotation");
        }
    }
}
