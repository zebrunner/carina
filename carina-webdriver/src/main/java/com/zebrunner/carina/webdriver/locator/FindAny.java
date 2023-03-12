package com.zebrunner.carina.webdriver.locator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.openqa.selenium.By;
import org.openqa.selenium.support.AbstractFindByBuilder;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactoryFinder;

/**
 * Used to mark a field on a Page Object to indicate that lookup should use a series of @FindBy tags.
 * It will then search for elements that match any of the FindBy criteria
 * (If one of the FindBy elements was found, the rest of the FindBy are not taken into account). Note that elements
 * are not guaranteed to be in document order.
 *
 * It can be used on a types as well, but will not be processed by default.
 *
 * Eg:
 *
 * <pre class="code">
 * &#64;FindAny({&#64;FindBy(how = How.ID, using = "foo"),
 *           &#64;FindBy(className = "bar")})
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
@PageFactoryFinder(FindAny.FindAnyBuilder.class)
public @interface FindAny {
    FindBy[] value();

    class FindAnyBuilder extends AbstractFindByBuilder {
        @Override
        public By buildIt(Object annotation, Field field) {
            FindAny findBys = (FindAny) annotation;
            assertValidFindAny(findBys);

            FindBy[] findByArray = findBys.value();
            By[] byArray = new By[findByArray.length];
            for (int i = 0; i < findByArray.length; i++) {
                byArray[i] = buildByFromFindBy(findByArray[i]);
            }

            return new ByAny(byArray);
        }

        void assertValidFindAny(FindAny findBys) {
            for (FindBy findBy : findBys.value()) {
                assertValidFindBy(findBy);
            }
        }
    }
}
