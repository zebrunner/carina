#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013-2021 QAPROSOFT (http://qaprosoft.com/).
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
 */
package ${package}.carina.demo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.apache.ibatis.session.SqlSession;
import org.testng.annotations.Test;
import com.qaprosoft.carina.core.foundation.AbstractTest;
import ${package}.carina.demo.db.mappers.UserMapper;
import ${package}.carina.demo.db.mappers.UserPreferenceMapper;
import ${package}.carina.demo.db.models.User;
import ${package}.carina.demo.db.models.User.Status;
import ${package}.carina.demo.utils.ConnectionFactory;
import ${package}.carina.demo.db.models.UserPreference;

/**
 * This sample shows how create DB test.
 * 
 * @author qpsdemo
 */
public class DBSampleTest extends AbstractTest {

	private static User USER = new User() {
		{
			setUsername("bmarley");
			setFirstName("Bob");
			setLastName("Marley");
			setStatus(Status.ACTIVE);
		}
	};

	private static UserPreference USER_PREFERENCE = new UserPreference() {
		{
			setName(Name.DEFAULT_DASHBOARD);
			setValue("Default");
		}
	};

	@Test
	public void createUser() {
		try (SqlSession session = ConnectionFactory.getSqlSessionFactory().openSession(true)) {
			UserMapper userMapper = session.getMapper(UserMapper.class);
			userMapper.create(USER);
			checkUser(userMapper.findById(USER.getId()));
		}
	}

	@Test(dependsOnMethods = "createUser")
	public void createUserPreference() {
		try (SqlSession session = ConnectionFactory.getSqlSessionFactory().openSession(true)) {
			UserMapper userMapper = session.getMapper(UserMapper.class);
			UserPreferenceMapper userPreferenceMapper = session.getMapper(UserPreferenceMapper.class);
			USER_PREFERENCE.setUserId(USER.getId());
			userPreferenceMapper.create(USER_PREFERENCE);
			checkUserPreference(userMapper.findById(USER.getId()).getPreferences().get(0));
		}
	}

	@Test(dependsOnMethods = "createUserPreference")
	public void updateUser() {
		try (SqlSession session = ConnectionFactory.getSqlSessionFactory().openSession(true)) {
			UserMapper userMapper = session.getMapper(UserMapper.class);
			USER.setUsername("rjohns");
			USER.setFirstName("Roy");
			USER.setLastName("Johns");
			USER.setStatus(Status.INACTIVE);
			userMapper.update(USER);
			checkUser(userMapper.findById(USER.getId()));
		}
	}

	@Test(dependsOnMethods = "updateUser")
	public void deleteUser() {
		try (SqlSession session = ConnectionFactory.getSqlSessionFactory().openSession(true)) {
			UserMapper userMapper = session.getMapper(UserMapper.class);
			userMapper.delete(USER);
			assertNull(userMapper.findById(USER.getId()));
		}
	}

	private void checkUser(User user) {
		assertEquals(user.getUsername(), USER.getUsername(), "User name must match");
		assertEquals(user.getFirstName(), USER.getFirstName(), "First name must match");
		assertEquals(user.getLastName(), USER.getLastName(), "Last name must match");
		assertEquals(user.getEmail(), USER.getEmail(), "Email must match");
	}

	private void checkUserPreference(UserPreference userPreference) {
		assertEquals(userPreference.getName(), USER_PREFERENCE.getName(), "Preference name must match");
		assertEquals(userPreference.getValue(), USER_PREFERENCE.getValue(), "Preference value must match");
		assertEquals(userPreference.getUserId(), USER_PREFERENCE.getUserId(), "Preference user id must match");
	}
}