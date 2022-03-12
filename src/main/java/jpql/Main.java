package jpql;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class Main {
    
    public static void main(String[] args) {

        // Persistence가 데이터베이스 설정 정보를 조회해서 entityManagerFactory를 만든다. DB당 하나씩 묶여서 사용된다.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        // entityManager가 트랜잭션 단위의 쿼리를 처리해준다.
        EntityManager em = emf.createEntityManager();

        // JPA는 트랜잭션 안에서 수행을 해줘야한다.
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            for (int i = 0; i < 100; i++) {
                Member member = new Member();
                member.setUsername("member" + i);
                member.setAge(i);
                em.persist(member);
            }
        
            // 반환 타입이 명확할 때 TypedQuery, 명확하지 않을 때 Query
            TypedQuery<Member> query1 =  em.createQuery("select m from Member m", Member.class);
            Query query2 =  em.createQuery("select m.username, m.age from Member m");

            // 결과가 컬렉션일 때는 getResultList(), 결과가 하나일 때는 getSingleResult()
            List<Member> resultList = query1.getResultList(); // 결과가 없어도 빈 리스트를 반환하기 때문에 널포인트익셉션을 걱정할 필요 없다.
            // Member singleResult = query1.getSingleResult(); // 결과가 무조건 하나여야 한다.

            // 동적 쿼리 파라미터 바인딩
            TypedQuery<Member> parameterBinding =  em.createQuery("select m from Member m where m.username = :username", Member.class);
            parameterBinding.setParameter("username", "memberA");
            // Member singleResult2 = parameterBinding.getSingleResult(); // 무조건 하나만 가져와야함.
            // System.out.println("=============" + singleResult2.getUsername());

            em.flush();
            em.clear();

            // 프로젝션
            List<Team> result = em.createQuery("select m.team from Member m", Team.class).getResultList();
            List<Address> embeddedProjection = em.createQuery("select o.address from Order o", Address.class).getResultList();
            List<MemberDTO> scalaTypeProjection = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class).getResultList();

            MemberDTO memberDTO = scalaTypeProjection.get(0);
            System.out.println("memberDTO = " + memberDTO.getUsername());
            System.out.println("memberDTO = " + memberDTO.getAge());

            List<Member> resultList2 = em.createQuery("select m from Member m order by m.age desc", Member.class)
            .setFirstResult(0).setMaxResults(10).getResultList();

            System.out.println("result size = " + resultList2.size());
            for (Member member : resultList2) {
                System.out.println("member = " + member.toString());
            }
                
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
