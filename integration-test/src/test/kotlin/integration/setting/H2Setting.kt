package integration.setting

import org.komapper.core.DatabaseConfig
import org.komapper.jdbc.h2.H2DatabaseConfig

class H2Setting(url: String, user: String, password: String) : Setting {
    override val config: DatabaseConfig =
        object : H2DatabaseConfig(url, user, password) {
            override val jdbcOption = super.jdbcOption.copy(batchSize = 2)
        }
    override val dbms: Dbms = Dbms.H2
    override val createSql: String = """
        CREATE SEQUENCE IF NOT EXISTS SEQUENCE_STRATEGY_ID START WITH 1 INCREMENT BY 100;
        CREATE SEQUENCE IF NOT EXISTS MY_SEQUENCE_STRATEGY_ID START WITH 1 INCREMENT BY 100;
        CREATE SEQUENCE IF NOT EXISTS PERSON_ID_SEQUENCE START WITH 1 INCREMENT BY 100;

        CREATE TABLE IF NOT EXISTS DEPARTMENT(DEPARTMENT_ID INTEGER NOT NULL PRIMARY KEY, DEPARTMENT_NO INTEGER NOT NULL UNIQUE,DEPARTMENT_NAME VARCHAR(20),LOCATION VARCHAR(20) DEFAULT 'TOKYO', VERSION INTEGER);
        CREATE TABLE IF NOT EXISTS ADDRESS(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
        CREATE TABLE IF NOT EXISTS ADDRESS_ARCHIVE(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
        CREATE TABLE IF NOT EXISTS EMPLOYEE(EMPLOYEE_ID INTEGER NOT NULL PRIMARY KEY, EMPLOYEE_NO INTEGER NOT NULL ,EMPLOYEE_NAME VARCHAR(20),MANAGER_ID INTEGER,HIREDATE DATE,SALARY NUMERIC(7,2),DEPARTMENT_ID INTEGER,ADDRESS_ID INTEGER,VERSION INTEGER, CONSTRAINT FK_DEPARTMENT_ID FOREIGN KEY(DEPARTMENT_ID) REFERENCES DEPARTMENT(DEPARTMENT_ID), CONSTRAINT FK_ADDRESS_ID FOREIGN KEY(ADDRESS_ID) REFERENCES ADDRESS(ADDRESS_ID));
        CREATE TABLE IF NOT EXISTS PERSON(PERSON_ID INTEGER NOT NULL PRIMARY KEY, NAME VARCHAR(20), CREATED_AT TIMESTAMP, UPDATED_AT TIMESTAMP, VERSION INTEGER);
        CREATE TABLE IF NOT EXISTS "ORDER"("ORDER_ID" INTEGER NOT NULL PRIMARY KEY, "VALUE" VARCHAR(20));

        CREATE TABLE IF NOT EXISTS COMP_KEY_DEPARTMENT(DEPARTMENT_ID1 INTEGER NOT NULL, DEPARTMENT_ID2 INTEGER NOT NULL, DEPARTMENT_NO INTEGER NOT NULL UNIQUE,DEPARTMENT_NAME VARCHAR(20),LOCATION VARCHAR(20) DEFAULT 'TOKYO', VERSION INTEGER, CONSTRAINT PK_COMP_KEY_DEPARTMENT PRIMARY KEY(DEPARTMENT_ID1, DEPARTMENT_ID2));
        CREATE TABLE IF NOT EXISTS COMP_KEY_ADDRESS(ADDRESS_ID1 INTEGER NOT NULL, ADDRESS_ID2 INTEGER NOT NULL, STREET VARCHAR(20), VERSION INTEGER, CONSTRAINT PK_COMP_KEY_ADDRESS PRIMARY KEY(ADDRESS_ID1, ADDRESS_ID2));
        CREATE TABLE IF NOT EXISTS COMP_KEY_EMPLOYEE(EMPLOYEE_ID1 INTEGER NOT NULL, EMPLOYEE_ID2 INTEGER NOT NULL, EMPLOYEE_NO INTEGER NOT NULL ,EMPLOYEE_NAME VARCHAR(20),MANAGER_ID1 INTEGER,MANAGER_ID2 INTEGER,HIREDATE DATE,SALARY NUMERIC(7,2),DEPARTMENT_ID1 INTEGER,DEPARTMENT_ID2 INTEGER,ADDRESS_ID1 INTEGER,ADDRESS_ID2 INTEGER,VERSION INTEGER, CONSTRAINT PK_COMP_KEY_EMPLOYEE PRIMARY KEY(EMPLOYEE_ID1, EMPLOYEE_ID2), CONSTRAINT FK_COMP_KEY_DEPARTMENT_ID FOREIGN KEY(DEPARTMENT_ID1, DEPARTMENT_ID2) REFERENCES COMP_KEY_DEPARTMENT(DEPARTMENT_ID1, DEPARTMENT_ID2), CONSTRAINT FK_COMP_KEY_ADDRESS_ID FOREIGN KEY(ADDRESS_ID1, ADDRESS_ID2) REFERENCES COMP_KEY_ADDRESS(ADDRESS_ID1, ADDRESS_ID2));

        CREATE TABLE IF NOT EXISTS LARGE_OBJECT(ID NUMERIC(8) NOT NULL PRIMARY KEY, NAME VARCHAR(20), LARGE_NAME CLOB, BYTES BINARY, LARGE_BYTES BLOB, DTO BINARY, LARGE_DTO BLOB);
        CREATE TABLE IF NOT EXISTS TENSE (ID INTEGER PRIMARY KEY,DATE_DATE DATE, DATE_TIME TIME, DATE_TIMESTAMP TIMESTAMP, CAL_DATE DATE, CAL_TIME TIME, CAL_TIMESTAMP TIMESTAMP, SQL_DATE DATE, SQL_TIME TIME, SQL_TIMESTAMP TIMESTAMP);
        CREATE TABLE IF NOT EXISTS JOB (ID INTEGER NOT NULL PRIMARY KEY, JOB_TYPE VARCHAR(20));
        CREATE TABLE IF NOT EXISTS AUTHORITY (ID INTEGER NOT NULL PRIMARY KEY, AUTHORITY_TYPE INTEGER);
        CREATE TABLE IF NOT EXISTS NO_ID (VALUE1 INTEGER, VALUE2 INTEGER);
        CREATE TABLE IF NOT EXISTS OWNER_OF_NO_ID (ID INTEGER NOT NULL PRIMARY KEY, NO_ID_VALUE1 INTEGER);
        CREATE TABLE IF NOT EXISTS CONSTRAINT_CHECKING (PRIMARY_KEY INTEGER PRIMARY KEY, UNIQUE_KEY INTEGER UNIQUE, FOREIGN_KEY INTEGER, CHECK_CONSTRAINT INTEGER, NOT_NULL INTEGER NOT NULL, CONSTRAINT CK_CONSTRAINT_CHECKING_1 CHECK (CHECK_CONSTRAINT > 0), CONSTRAINT FK_JOB_ID FOREIGN KEY (FOREIGN_KEY) REFERENCES JOB (ID));
        CREATE TABLE IF NOT EXISTS PATTERN (VALUE VARCHAR(10));

        CREATE TABLE IF NOT EXISTS ID_GENERATOR(PK VARCHAR(20) NOT NULL PRIMARY KEY, VALUE INTEGER NOT NULL);
        CREATE TABLE IF NOT EXISTS MY_ID_GENERATOR(MY_PK VARCHAR(20) NOT NULL PRIMARY KEY, MY_VALUE INTEGER NOT NULL);
        CREATE TABLE IF NOT EXISTS AUTO_STRATEGY(ID INTEGER NOT NULL IDENTITY PRIMARY KEY, VALUE VARCHAR(10));
        CREATE TABLE IF NOT EXISTS IDENTITY_STRATEGY(ID INTEGER NOT NULL IDENTITY PRIMARY KEY, VALUE VARCHAR(10));
        CREATE TABLE IF NOT EXISTS SEQUENCE_STRATEGY(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
        CREATE TABLE IF NOT EXISTS SEQUENCE_STRATEGY2(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
        CREATE TABLE IF NOT EXISTS TABLE_STRATEGY(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
        CREATE TABLE IF NOT EXISTS TABLE_STRATEGY2(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));

        CREATE TABLE IF NOT EXISTS ANY_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE OTHER);
        CREATE TABLE IF NOT EXISTS BIG_DECIMAL_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BIGINT);
        CREATE TABLE IF NOT EXISTS BIG_INTEGER_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BIGINT);
        CREATE TABLE IF NOT EXISTS BOOLEAN_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BOOL);
        CREATE TABLE IF NOT EXISTS BYTE_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TINYINT);
        CREATE TABLE IF NOT EXISTS BYTE_ARRAY_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BINARY);
        CREATE TABLE IF NOT EXISTS DOUBLE_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE DOUBLE);
        CREATE TABLE IF NOT EXISTS ENUM_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(20));
        CREATE TABLE IF NOT EXISTS FLOAT_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE FLOAT);
        CREATE TABLE IF NOT EXISTS INT_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE INTEGER);
        CREATE TABLE IF NOT EXISTS LOCAL_DATE_TIME_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TIMESTAMP);
        CREATE TABLE IF NOT EXISTS LOCAL_DATE_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE DATE);
        CREATE TABLE IF NOT EXISTS LOCAL_TIME_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TIME);
        CREATE TABLE IF NOT EXISTS LONG_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BIGINT);
        CREATE TABLE IF NOT EXISTS OFFSET_DATE_TIME_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TIMESTAMP WITH TIME ZONE);
        CREATE TABLE IF NOT EXISTS SHORT_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE SMALLINT);
        CREATE TABLE IF NOT EXISTS STRING_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(20));

        INSERT INTO DEPARTMENT VALUES(1,10,'ACCOUNTING','NEW YORK',1);
        INSERT INTO DEPARTMENT VALUES(2,20,'RESEARCH','DALLAS',1);
        INSERT INTO DEPARTMENT VALUES(3,30,'SALES','CHICAGO',1);
        INSERT INTO DEPARTMENT VALUES(4,40,'OPERATIONS','BOSTON',1);
        INSERT INTO ADDRESS VALUES(1,'STREET 1',1);
        INSERT INTO ADDRESS VALUES(2,'STREET 2',1);
        INSERT INTO ADDRESS VALUES(3,'STREET 3',1);
        INSERT INTO ADDRESS VALUES(4,'STREET 4',1);
        INSERT INTO ADDRESS VALUES(5,'STREET 5',1);
        INSERT INTO ADDRESS VALUES(6,'STREET 6',1);
        INSERT INTO ADDRESS VALUES(7,'STREET 7',1);
        INSERT INTO ADDRESS VALUES(8,'STREET 8',1);
        INSERT INTO ADDRESS VALUES(9,'STREET 9',1);
        INSERT INTO ADDRESS VALUES(10,'STREET 10',1);
        INSERT INTO ADDRESS VALUES(11,'STREET 11',1);
        INSERT INTO ADDRESS VALUES(12,'STREET 12',1);
        INSERT INTO ADDRESS VALUES(13,'STREET 13',1);
        INSERT INTO ADDRESS VALUES(14,'STREET 14',1);
        INSERT INTO ADDRESS VALUES(15,'STREET 15',1);
        INSERT INTO EMPLOYEE VALUES(1,7369,'SMITH',13,'1980-12-17',800,2,1,1);
        INSERT INTO EMPLOYEE VALUES(2,7499,'ALLEN',6,'1981-02-20',1600,3,2,1);
        INSERT INTO EMPLOYEE VALUES(3,7521,'WARD',6,'1981-02-22',1250,3,3,1);
        INSERT INTO EMPLOYEE VALUES(4,7566,'JONES',9,'1981-04-02',2975,2,4,1);
        INSERT INTO EMPLOYEE VALUES(5,7654,'MARTIN',6,'1981-09-28',1250,3,5,1);
        INSERT INTO EMPLOYEE VALUES(6,7698,'BLAKE',9,'1981-05-01',2850,3,6,1);
        INSERT INTO EMPLOYEE VALUES(7,7782,'CLARK',9,'1981-06-09',2450,1,7,1);
        INSERT INTO EMPLOYEE VALUES(8,7788,'SCOTT',4,'1982-12-09',3000.0,2,8,1);
        INSERT INTO EMPLOYEE VALUES(9,7839,'KING',NULL,'1981-11-17',5000,1,9,1);
        INSERT INTO EMPLOYEE VALUES(10,7844,'TURNER',6,'1981-09-08',1500,3,10,1);
        INSERT INTO EMPLOYEE VALUES(11,7876,'ADAMS',8,'1983-01-12',1100,2,11,1);
        INSERT INTO EMPLOYEE VALUES(12,7900,'JAMES',6,'1981-12-03',950,3,12,1);
        INSERT INTO EMPLOYEE VALUES(13,7902,'FORD',4,'1981-12-03',3000,2,13,1);
        INSERT INTO EMPLOYEE VALUES(14,7934,'MILLER',7,'1982-01-23',1300,1,14,1);

        INSERT INTO COMP_KEY_DEPARTMENT VALUES(1,1,10,'ACCOUNTING','NEW YORK',1);
        INSERT INTO COMP_KEY_DEPARTMENT VALUES(2,2,20,'RESEARCH','DALLAS',1);
        INSERT INTO COMP_KEY_DEPARTMENT VALUES(3,3,30,'SALES','CHICAGO',1);
        INSERT INTO COMP_KEY_DEPARTMENT VALUES(4,4,40,'OPERATIONS','BOSTON',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(1,1,'STREET 1',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(2,2,'STREET 2',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(3,3,'STREET 3',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(4,4,'STREET 4',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(5,5,'STREET 5',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(6,6,'STREET 6',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(7,7,'STREET 7',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(8,8,'STREET 8',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(9,9,'STREET 9',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(10,10,'STREET 10',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(11,11,'STREET 11',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(12,12,'STREET 12',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(13,13,'STREET 13',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(14,14,'STREET 14',1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(1,1,7369,'SMITH',13,13,'1980-12-17',800,2,2,1,1,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(2,2,7499,'ALLEN',6,6,'1981-02-20',1600,3,3,2,2,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(3,3,7521,'WARD',6,6,'1981-02-22',1250,3,3,3,3,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(4,4,7566,'JONES',9,9,'1981-04-02',2975,2,2,4,4,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(5,5,7654,'MARTIN',6,6,'1981-09-28',1250,3,3,5,5,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(6,6,7698,'BLAKE',9,9,'1981-05-01',2850,3,3,6,6,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(7,7,7782,'CLARK',9,9,'1981-06-09',2450,1,1,7,7,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(8,8,7788,'SCOTT',4,4,'1982-12-09',3000.0,2,2,8,8,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(9,9,7839,'KING',NULL,NULL,'1981-11-17',5000,1,1,9,9,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(10,10,7844,'TURNER',6,6,'1981-09-08',1500,3,3,10,10,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(11,11,7876,'ADAMS',8,8,'1983-01-12',1100,2,2,11,11,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(12,12,7900,'JAMES',6,6,'1981-12-03',950,3,3,12,12,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(13,13,7902,'FORD',4,4,'1981-12-03',3000,2,2,13,13,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(14,14,7934,'MILLER',7,7,'1982-01-23',1300,1,1,14,14,1);

        INSERT INTO TENSE VALUES (1, '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10');
        INSERT INTO JOB VALUES (1, 'SALESMAN');
        INSERT INTO JOB VALUES (2, 'MANAGER');
        INSERT INTO JOB VALUES (3, 'PRESIDENT');
        INSERT INTO AUTHORITY VALUES (1, 10);
        INSERT INTO AUTHORITY VALUES (2, 20);
        INSERT INTO AUTHORITY VALUES (3, 30);
        INSERT INTO NO_ID VALUES (1, 1);
        INSERT INTO NO_ID VALUES (1, 1);

        INSERT INTO ID_GENERATOR VALUES('TABLE_STRATEGY_ID', 1);
    """.trimIndent()
    override val dropSql: String = "DROP ALL OBJECTS"
    override val resetSql: String = """
        ALTER TABLE IDENTITY_STRATEGY ALTER COLUMN ID RESTART WITH 1;
    """.trimIndent()
}
