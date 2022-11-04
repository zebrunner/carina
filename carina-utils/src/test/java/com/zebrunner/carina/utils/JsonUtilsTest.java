/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JsonUtilsTest {

    private static final String JSON_PATH = "src/test/resources/json/testJson.json";

    private static final Member MEMBER1 = new Member("Molecule Man", 29);
    private static final Member MEMBER2 = new Member("Madame Uppercut", 39);
    private static final Member MEMBER3 = new Member("Eternal Flame", 25);

    private static final City CITY = new City("Metro City", 2016, true, Arrays.asList(MEMBER1, MEMBER2, MEMBER3));

    private static final String WRONG_JSON = "{\"name\": \"Metro City\", " + "  \"formed\": 2016, " + "  \"active\": true";

    @Test
    public void testReadJsonStr() {
        String json = readJson(JSON_PATH);

        City actualCity = JsonUtils.fromJson(json, City.class);

        Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testReadJsonStrThrowRuntimeException() {
        String json = readJson(JSON_PATH);

        JsonUtils.fromJson(json, Member.class);
    }

    @Test
    public void testReadJsonFile() {
        City actualCity = JsonUtils.fromJson(new File(JSON_PATH), City.class);

        Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testReadJsonFileThrowRuntimeException() {
        JsonUtils.fromJson(new File(JSON_PATH), Member.class);
    }

    @Test
    public void testWriteJsonStr() {
        String expectedStrJson = JsonUtils.toJson(CITY);
        String actualStrJson = readJson(JSON_PATH);

        City expectedCity = JsonUtils.fromJson(expectedStrJson, City.class);
        City actualCity = JsonUtils.fromJson(actualStrJson, City.class);

        Assert.assertEquals(actualCity, expectedCity, actualCity.getName() + " is different than " + expectedCity.name);
    }

    @Test
    public void testReadJsonStrWithType() {
        String json = readJson(JSON_PATH);

        City actualCity = JsonUtils.fromJson(json, (Type) City.class);

        Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testReadJsonStrWithTypeThrowRuntimeException() {
        String json = readJson(JSON_PATH);

        JsonUtils.fromJson(json, (Type) Member.class);
    }

    @Test
    public void testReadJsonFileWithType() {
        City actualCity = JsonUtils.fromJson(new File(JSON_PATH), (Type) City.class);

        Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testReadJsonFileWithTypeThrowRuntimeException() {
        JsonUtils.fromJson(new File(JSON_PATH), (Type) Member.class);
    }

    @Test
    public void testReadTree() {
        String expectedStrJson = JsonUtils.toJson(CITY);
        String actualStrJson = readJson(JSON_PATH);

        JsonNode expectedJsonNode = JsonUtils.readTree(expectedStrJson);
        JsonNode actualJsonNode = JsonUtils.readTree(actualStrJson);

        Assert.assertEquals(actualJsonNode, expectedJsonNode, "JsonNode wasn't generated correctly");
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testReadTreeThrowRuntimeException() {
        JsonUtils.readTree(WRONG_JSON);
    }

    @Test
    public void testWriteTreeToValue() {
        String expectedStrJson = JsonUtils.toJson(CITY);
        String actualStrJson = readJson(JSON_PATH);

        JsonNode expectedJsonNode = JsonUtils.readTree(expectedStrJson);
        JsonNode actualJsonNode = JsonUtils.readTree(actualStrJson);

        City expectedCity = JsonUtils.treeToValue(expectedJsonNode, City.class);
        City actualCity = JsonUtils.treeToValue(actualJsonNode, City.class);

        Assert.assertEquals(actualCity, expectedCity, actualCity.getName() + " is different than " + expectedCity.getName());
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testWriteTreeToValueThrowRuntimeException() {
        String strJson = JsonUtils.toJson(CITY);

        JsonNode jsonNode = JsonUtils.readTree(strJson);

        JsonUtils.treeToValue(jsonNode, Member.class);
    }


    private String readJson(String pathStr) {
        Path path = Paths.get(pathStr);

        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException ex) {
            // Handle exception
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }



    private static class City {
        private String name;
        private int formed;
        private boolean active;
        private List<Member> members;

        public City(String name, int formed, boolean active, List<Member> members) {
            this.name = name;
            this.formed = formed;
            this.active = active;
            this.members = members;
        }

        public City() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getFormed() {
            return formed;
        }

        public void setFormed(int formed) {
            this.formed = formed;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public List<Member> getMembers() {
            return members;
        }

        public void setMembers(List<Member> members) {
            this.members = members;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof City))
                return false;
            City city = (City) o;
            return formed == city.formed && active == city.active && Objects.equals(name, city.name) && Objects.equals(members, city.members);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, formed, active, members);
        }
    }

    private static class Member {
        private String name;
        private int age;

        public Member(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public Member() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Member member = (Member) o;
            return age == member.age && Objects.equals(name, member.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
