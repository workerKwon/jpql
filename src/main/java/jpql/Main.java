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

            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            String query = "select m from Member m";
            List<Member> resultList = em.createQuery(query, Member.class).getResultList();

            for (Member member : resultList) {
                System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
                // 회원1, 팀A(SQL)
                // 회원2, 팀A(1차 캐시)
                // 회원3, 팀B(SQL)
            }

            String query1 = "select m from Member m join fetch m.team"; // fetch join 사용 즉시로딩처럼 한방에 다 가져와놓고 실제 엔티티를 사용(프록시 사용 안함.)
            List<Member> resultList1 = em.createQuery(query1, Member.class).getResultList();

            for (Member member : resultList1) {
                System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
            }

            String query2 = "select t from Team t join fetch t.members"; // fetch join 사용 즉시로딩처럼 한방에 다 가져와놓고 실제 엔티티를 사용(프록시 사용 안함.)
            List<Team> resultList2 = em.createQuery(query2, Team.class).getResultList();

            for (Team team : resultList2) {
                System.out.println("team = " + team.getName() + ", members = " + team.getMembers().size());
            }

            String query3 = "select distinct t from Team t join fetch t.members"; // distinct : db 데이터 중복 제거 + 같은 식벽자를 가진 엔티티 중복 제거
            List<Team> resultList3 = em.createQuery(query3, Team.class).getResultList();

            for (Team team : resultList3) {
                System.out.println("team = " + team.getName() + ", members = " + team.getMembers().size());
            }

            /**
             * 컬렉션에 페치 조인을 할 수 없다. 그래서 페치 조인을 사용하지 않는 대신 BatchSize를 사용한다. 
             * BatchSize를 100으로 하면 team id 를 100개 넣고 members를 조회한다. 지금은 2개 밖에 없어서 쿼리를 보면 2개가 들어감.
             * 만약 100개가 넘어가면 그 다음 쿼리를 또 날림
             * */ 
            String query4 = "select t from Team t";
            List<Team> resultList4 = em.createQuery(query4, Team.class)
                                    .setFirstResult(0)
                                    .setMaxResults(2)
                                    .getResultList();

            for (Team team : resultList4) {
                System.out.println("team = " + team.getName() + ", members = " + team.getMembers().size());
            }



            String query5 = "select m from Member m where m = :member"; // 엔티티를 직접 사용해도 쿼리는 식별자를 사용한 쿼리가 나간다.
            List<Member> resultList5 = em.createQuery(query5, Member.class)
                                        .setParameter("member", member1)
                                        .getResultList();

            for (Member member : resultList5) {
                System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
            }

            String query6 = "select m from Member m where m.team = :team"; // 엔티티를 직접 사용해도 쿼리는 식별자를 사용한 쿼리가 나간다.
            List<Member> resultList6 = em.createQuery(query6, Member.class)
                                        .setParameter("team", teamA)
                                        .getResultList();

            for (Member member : resultList6) {
                System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
            }

            List<Member> resultList7 = em.createNamedQuery("Member.findByUsername", Member.class).setParameter("username", "회원1").getResultList();

            for (Member member : resultList7) {
                System.out.println("NamedQuery member = " + member);
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
