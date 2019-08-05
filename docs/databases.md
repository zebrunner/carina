# Database usage

## Dependencies
For integration with DB we recommend to use [MyBatis](http://www.mybatis.org/mybatis-3) ORM framework. MyBatis is a first class persistence framework with support for custom SQL, stored procedures and advanced mappings. To start with, let's add required dependencies into Maven pom.xml:
```xml
<dependency>
   <groupId>org.mybatis</groupId>
   <artifactId>mybatis</artifactId>
   <version>3.5.2</version>
</dependency>
<!-- Postgres driver -->
<dependency> 
   <groupId>org.postgresql</groupId>
   <artifactId>postgresql</artifactId>
   <version>42.2.6</version>
</dependency>
<!-- MySQL driver -->
<dependency>
   <groupId>mysql</groupId>
   <artifactId>mysql-connector-java</artifactId>
   <version>8.0.17</version>
</dependency>
```

## Mappers
Next step is MyBatis mappers implementation, read [official documentation](http://www.mybatis.org/mybatis-3/sqlmap-xml.html) to understand all the details. Let's place all the mappers into **src/main/resources/mappers**. Here is [UserMapper.xml](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/mappers/UserMapper.xml) sample:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.qaprosoft.carina.demo.db.mappers.UserMapper">

	<insert id="create" useGeneratedKeys="true" keyProperty="id">
		<![CDATA[
			INSERT INTO carina.USERS (USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, STATUS)
			VALUES (
				#{username},
				#{password},
			    #{firstName},
				#{lastName},
				#{email},
				#{status}
			)
		]]>
	</insert>

	<sql id="getUser">
		<![CDATA[
			SELECT
				U.ID AS USER_ID,
				U.USERNAME AS USER_USERNAME,
				U.PASSWORD AS USER_PASSWORD,
				U.FIRST_NAME AS USER_FIRST_NAME,
				U.LAST_NAME AS USER_LAST_NAME,
				U.EMAIL AS USER_EMAIL,
				U.STATUS AS USER_STATUS,
				UP.ID AS USER_PREFERENCE_ID,
				UP.NAME AS USER_PREFERENCE_NAME,
				UP.VALUE AS USER_PREFERENCE_VALUE,
				UP.USER_ID AS USER_PREFERENCE_USER_ID
			FROM
				carina.USERS U
			LEFT JOIN
				carina.USER_PREFERENCES UP
			ON
				UP.USER_ID = U.ID
		]]>
	</sql>

	<select id="findById" resultMap="UserResultMap">
		<include refid="getUser" />
		<![CDATA[
			WHERE U.ID = #{id};
		]]>
	</select>

	<select id="findByUserName" resultMap="UserResultMap">
		<include refid="getUser" />
		<![CDATA[
			WHERE U.USERNAME = #{username};
		]]>
	</select>

	<update id="update">
		<![CDATA[
			UPDATE
			    carina.USERS
		]]>
		<set>
			<if test="null != firstName">
		            <![CDATA[
		               FIRST_NAME = #{firstName},
		            ]]>
			</if>
			<if test="null != lastName">
		            <![CDATA[
		               LAST_NAME = #{lastName},
		            ]]>
			</if>
			<if test="null != email">
		            <![CDATA[
		               EMAIL = #{email},
		            ]]>
			</if>
			<if test="null != username">
		            <![CDATA[
		               USERNAME = #{username},	
		            ]]>
			</if>
			<if test="null != password">
		            <![CDATA[
		               PASSWORD = #{password},	
		            ]]>
			</if>
		</set>
		<![CDATA[
			WHERE
			    ID = #{id}
		]]>
	</update>

	<delete id="delete">
		<![CDATA[
			DELETE FROM carina.USERS
			WHERE ID = #{id}
		]]>
	</delete>

	<resultMap type="com.qaprosoft.carina.demo.db.models.User"
		id="UserResultMap" autoMapping="false">
		<id column="USER_ID" property="id" />
		<result column="USER_USERNAME" property="username" />
		<result column="USER_PASSWORD" property="password" />
		<result column="USER_FIRST_NAME" property="firstName" />
		<result column="USER_LAST_NAME" property="lastName" />
		<result column="USER_EMAIL" property="email" />
		<result column="USER_STATUS" property="status" />
		<collection property="preferences"
			ofType="com.qaprosoft.carina.demo.db.models.UserPreference"
			resultMap="com.qaprosoft.carina.demo.db.mappers.UserPreferenceMapper.UserPreferenceResultMap" />
	</resultMap>

</mapper>
```

## Configuration
First of all we need to place DB credentials into **src/main/resources/_database.properties**:
```
#===============================================================#
#================== Database configuration ====================#
#===============================================================#
db.url=jdbc:postgresql://localhost:5432/postgres
db.driver=org.postgresql.Driver
db.user=postgres
db.pass=postgres

#db.url=jdbc:mysql://localhost:3306/mysql
#db.driver=com.mysql.jdbc.Driver
#db.user=mysql
#db.pass=mysql
```
