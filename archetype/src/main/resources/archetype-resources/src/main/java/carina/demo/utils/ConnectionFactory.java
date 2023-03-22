#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.utils;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import ${package}.carina.demo.db.mappers.UserMapper;
import ${package}.carina.demo.db.mappers.UserPreferenceMapper;

public class ConnectionFactory {

	private static SqlSessionFactory factory;

	static {
		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader("mybatis-config.xml");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		factory = new SqlSessionFactoryBuilder().build(reader);
	}

	public static SqlSessionFactory getSqlSessionFactory() {
		return factory;
	}

	public static UserMapper getUserMapper() {
		try (SqlSession sqlSession = ConnectionFactory.getSqlSessionFactory().openSession(true)) {
			return sqlSession.getMapper(UserMapper.class);
		}
	}

	public static UserPreferenceMapper getUserPreferenceMapperMapper() {
		try (SqlSession sqlSession = ConnectionFactory.getSqlSessionFactory().openSession(true)) {
			return sqlSession.getMapper(UserPreferenceMapper.class);
		}
	}

}
