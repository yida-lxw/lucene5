package com.yida.framework.lucene5.incrementindex;

import java.util.List;

public interface PersonDao {
	/**
	 * 新增
	 * @return
	 */
	public boolean save(Person person);
	
	/**
	 * 更新
	 * @param person
	 * @return
	 */
	public boolean update(Person person);
	
	/**
	 * 根据ID删除
	 * @param id
	 * @return
	 */
	public boolean delete(Long id);
	
	/**
	 * 根据ID查询
	 * @param id
	 * @return
	 */
	public Person findById(Long id);
	
	/**
	 * 查询所有
	 * @return
	 */
	public List<Person> findAll();
	
	/**
	 * 查询3秒之前的数据，用于测试
	 * @return
	 */
	public List<Person> findPersonBefore3S();
}
