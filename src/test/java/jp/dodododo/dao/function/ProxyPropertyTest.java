package jp.dodododo.dao.function;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import jp.dodododo.dao.annotation.Bean;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.id.Sequence;
import jp.dodododo.dao.impl.Dept;
import jp.dodododo.dao.impl.RdbDao;
import jp.dodododo.dao.lazyloading.AutoLazyLoadingProxy;
import jp.dodododo.dao.log.SqlLogRegistry;
import jp.dodododo.dao.unit.DbTestRule;

import org.junit.Rule;
import org.junit.Test;

public class ProxyPropertyTest {

	@Rule
	public DbTestRule dbTestRule = new DbTestRule();

	private RdbDao dao;

	private SqlLogRegistry logRegistry = new SqlLogRegistry();

	@Test
	public void testInsertAndSelect() {
		dao = new RdbDao(dbTestRule.getDataSource());
		DeptProxy.dao = dao;

		dao.setSqlLogRegistry(logRegistry);
		Emp emp = new Emp();
		emp.dept = new Dept();
		emp.dept.setDEPTNO("10");
		emp.dept.setDNAME("dept__name");
		emp.COMM = "2";
		// emp.EMPNO = "1";
		emp.TSTAMP = null;
		emp.TSTAMP = new Date();
		emp.NAME = "ename";
		dao.insert("emp", emp);
		// dao.insert(emp.dept);
		String empNo = emp.EMPNO;

		List<Emp> select = dao.select("select * from emp, dept where emp.deptno = dept.deptno and empno = " + empNo, Emp.class);
		System.out.println(select);
		assertEquals(empNo, select.get(0).EMPNO);
		assertEquals("2.00", select.get(0).COMM);
		assertEquals("ename", select.get(0).NAME);
		assertNotNull(select.get(0).TSTAMP);

		assertEquals("select * from emp, dept where emp.deptno = dept.deptno and empno = " + empNo, logRegistry.getLast()
				.getCompleteSql());
		assertEquals("10", select.get(0).dept.getDEPTNO());
		assertEquals("select * from dept where deptno =10", logRegistry.getLast().getCompleteSql());
	}

	public static class Emp {
		@Id(value = @IdDefSet(type = Sequence.class, name = "sequence"), targetTables = { "emp" })
		public String EMPNO;

		@Column("ename")
		public String NAME;

		@Column(table = "emp", value = "Tstamp")
		public Date TSTAMP;

		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		public String COMM;

		@Bean(DeptProxy.class)
		public Dept dept;

		public Emp() {
		}

	}

	public static class DeptProxy extends Dept implements AutoLazyLoadingProxy<Dept> {
		private static RdbDao dao;

		@Column("deptNO")
		public String DEPTNO;

		private Dept real;

		protected DeptProxy() {
		}

		public DeptProxy(@Column("deptNO") String DEPTNO) {
			this.DEPTNO = DEPTNO;
		}

		@Override
		public Dept lazyLoad() {
			System.out.println("load");
			return DeptProxy.dao.selectOne("select * from dept where deptno =" + DEPTNO, Dept.class).get();
		}

		@Override
		public Dept real() {
			System.out.println("real");
			return real;
		}

		@Override
		public void setReal(Dept real) {
			System.out.println("setreal");
			this.real = real;
		}

	}
}
