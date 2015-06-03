package com.yida.framework.lucene5.incrementindex;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

public class DBHelper {
	private static DataSource dataSource;
	
	public static QueryRunner getQueryRunner(){
        if(DBHelper.dataSource == null){
            BasicDataSource dbcpDataSource = new BasicDataSource();
            dbcpDataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=utf8");
            dbcpDataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dbcpDataSource.setUsername("root");
            dbcpDataSource.setPassword("123");
            dbcpDataSource.setDefaultAutoCommit(true);
            dbcpDataSource.setMaxActive(100);
            dbcpDataSource.setMaxIdle(30);
            dbcpDataSource.setMaxWait(500);
            DBHelper.dataSource = (DataSource)dbcpDataSource;
        }
        return new QueryRunner(DBHelper.dataSource);
    }
}
