package solidstack.query.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.testng.annotations.Test;

public class JPATests
{
	@Test(groups="new")
	public void test1()
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("manager1");
		EntityManager em = emf.createEntityManager();
		em.close();
		emf.close();
	}
}
