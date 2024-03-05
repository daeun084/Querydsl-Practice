package practice.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import practice.querydsl.entity.Member;
import practice.querydsl.entity.QMember;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {
    //jpa 접근을 위한 entity manager
    private final EntityManager em;
    //querydsl을 사용하기 위함
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAll_Querydsl(){
        return queryFactory
                .selectFrom(QMember.member)
                .fetch();
    }

    public List<Member> findByUserName(String username){
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUserName_Querydsl(String name){
        return queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq(name))
                .fetch();
    }
}
