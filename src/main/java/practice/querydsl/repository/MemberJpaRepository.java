package practice.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import practice.querydsl.dto.MemberSearchCondition;
import practice.querydsl.dto.MemberTeamDto;
import practice.querydsl.dto.QMemberDto;
import practice.querydsl.dto.QMemberTeamDto;
import practice.querydsl.entity.Member;
import practice.querydsl.entity.QMember;

import java.util.List;
import java.util.Optional;

import static practice.querydsl.entity.QMember.member;
import static practice.querydsl.entity.QTeam.team;

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
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUserName(String username){
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUserName_Querydsl(String name){
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(name))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        //springframework method 사용 -> null check
        if(StringUtils.hasText(condition.getUsername())) {
            booleanBuilder.and(member.username.eq(condition.getUsername()));
        }

        if(StringUtils.hasText(condition.getTeamName())){
            booleanBuilder.and(team.name.eq(condition.getTeamName()));
        }

        if(condition.getAgeGoe() != null){
            booleanBuilder.and(member.age.goe(condition.getAgeGoe()));
        }

        if(condition.getAgeLoe() != null){
            booleanBuilder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"), //id mapping
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(booleanBuilder) //builder으로 조건 필터링
                .fetch();
    }
}
