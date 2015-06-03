package com.yida.framework.lucene5.incrementindex;

import java.util.Date;

public class Person {
	private Long id;
	private String personName;
	/**性别：1(男)/0(女)*/
	private String sex;
	private Date birth;
	/**籍贯*/
	private String nativePlace;
	private String job;
	private Integer salary;
	/**兴趣爱好*/
	private String hobby;
	
	/**删除标记： true已删除/false未删除*/
	private boolean deleteFlag;
	/**最后一次更新时间*/
	private Date updatedTime;
	
	public Person() {}
	
	
	
	public Person(String personName, String sex, Date birth,
			String nativePlace, String job, Integer salary, String hobby) {
		super();
		this.personName = personName;
		this.sex = sex;
		this.birth = birth;
		this.nativePlace = nativePlace;
		this.job = job;
		this.salary = salary;
		this.hobby = hobby;
	}
	
	public Person(String personName, String sex, Date birth,
			String nativePlace, String job, Integer salary, String hobby,boolean deleteFlag) {
		super();
		this.personName = personName;
		this.sex = sex;
		this.birth = birth;
		this.nativePlace = nativePlace;
		this.job = job;
		this.salary = salary;
		this.hobby = hobby;
		this.deleteFlag = deleteFlag;
	}

	public Person(String personName, String sex, Date birth,
			String nativePlace, String job, Integer salary, String hobby,
			boolean deleteFlag, Date updatedTime) {
		super();
		this.personName = personName;
		this.sex = sex;
		this.birth = birth;
		this.nativePlace = nativePlace;
		this.job = job;
		this.salary = salary;
		this.hobby = hobby;
		this.deleteFlag = deleteFlag;
		this.updatedTime = updatedTime;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPersonName() {
		return personName;
	}
	public void setPersonName(String personName) {
		this.personName = personName;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Date getBirth() {
		return birth;
	}
	public void setBirth(Date birth) {
		this.birth = birth;
	}
	public String getNativePlace() {
		return nativePlace;
	}
	public void setNativePlace(String nativePlace) {
		this.nativePlace = nativePlace;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public Integer getSalary() {
		return salary;
	}
	public void setSalary(Integer salary) {
		this.salary = salary;
	}
	public String getHobby() {
		return hobby;
	}
	public void setHobby(String hobby) {
		this.hobby = hobby;
	}

	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
