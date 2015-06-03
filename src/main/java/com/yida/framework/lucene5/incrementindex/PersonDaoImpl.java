package com.yida.framework.lucene5.incrementindex;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

public class PersonDaoImpl implements PersonDao {
	private QueryRunner queryRunner = DBHelper.getQueryRunner();
	/**
	 * 新增
	 * @return
	 */
	public boolean save(Person person) {
		int result = 0;
		try {
			result = queryRunner.update("insert into person(personName,sex,birth,nativePlace,job,salary,hobby,deleteFlag,updatedTime) " + 
					"values(?,?,?,?,?,?,?,?,?)" , new Object[] {
					person.getPersonName(),
					person.getSex(),
					person.getBirth(),
					person.getNativePlace(),
					person.getJob(),
					person.getSalary(),
					person.getHobby(),
					person.isDeleteFlag(),
					new Date()
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result == 1;
	}
	
	/**
	 * 根据ID更新
	 * @param person
	 * @return
	 */
	public boolean update(Person person) {
		int result = 0;
		try {
			result = queryRunner.update(
					"update person set personName = ?, sex = ?, birth = ?, " + 
					"nativePlace = ?, job = ?, salary = ?, hobby = ?,deleteFlag = ?, " +
					"updatedTime = ? where id = ?" 
					, new Object[] {
					person.getPersonName(),
					person.getSex(),
					person.getBirth(),
					person.getNativePlace(),
					person.getJob(),
					person.getSalary(),
					person.getHobby(),
					person.isDeleteFlag(),
					new Date(),
					person.getId()
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result == 1;
	}
	
	/**
	 * 根据ID删除
	 * @param id
	 * @return
	 */
	public boolean delete(Long id) {
		int result = 0;
		try {
			result = queryRunner.update("delete from person where id = ?", id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result == 1;
	}
	
	/**
	 * 根据ID查询
	 * @param id
	 * @return
	 */
	public Person findById(Long id) {
		Person person = null;
		try {
			person = queryRunner.query("select * from person where id = ?", new BeanHandler<Person>(Person.class),id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return person;
	}
	
	/**
	 * 查询所有
	 * @return
	 */
	public List<Person> findAll() {
		List<Person> persons = null;
		try {
			persons = queryRunner.query("select * from person", new BeanListHandler<Person>(Person.class));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return persons;
	}
	
	/**
	 * 查询3秒之前的数据，用于测试
	 * @return
	 */
	public List<Person> findPersonBefore3S() {
		List<Person> persons = null;
		try {
			persons = queryRunner.query("select * from person where updatedTime >= DATE_SUB(NOW(),INTERVAL 3 SECOND)", new BeanListHandler<Person>(Person.class));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return persons;
	}
}
