package practice.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.ast.Projection;
import practice.querydsl.dto.MemberDto;
import practice.querydsl.dto.QMemberDto;
import practice.querydsl.dto.UserDto;
import practice.querydsl.entity.Member;
import practice.querydsl.entity.QMember;
import practice.querydsl.entity.Team;

import java.util.List;

import static practice.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslAdvancedTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    //테스트 실행 전 실행되는 함수
    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    /**
     * projection - Dto
     */
    @Test
    public void simpleProjection(){
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for(String s : result){
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection(){
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username); //get으로 꺼내서 사용
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username + ", age = " + age);
        }
    }

    @Test
    public void findDtoByJPQL(){
        List<MemberDto> result = em.createQuery("select new practice.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        for(MemberDto m : result){
            System.out.println("result = "+ m);
        }
    }

    //프로퍼티 접근 (setter 사용)
    @Test
    public void findDtoBySetter(){
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for(MemberDto m : result){
            System.out.println("result = "+ m);
        }
    }

    //필드 접근
    @Test
    public void findDtoByField(){
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for(MemberDto m : result){
            System.out.println("result = "+ m);
        }
    }

    //생성자 사용
    @Test
    public void findDtoByConstructor(){
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age)) //생성자의 매개변수 타입, 위치가 일치해야 함
                .from(member)
                .fetch();

        for(MemberDto m : result){
            System.out.println("result = "+ m);
        }
    }

    @Test
    public void findUserDto(){
        QMember subMember = new QMember("subMember");

        //필드에 별칭 제공
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), //username을 name으로 mapping
                        ExpressionUtils.as(JPAExpressions //subquery
                                        .select(subMember.age.max())
                                        .from(subMember), "age"))
                )
                .from(member)
                .fetch();

        for(UserDto m : result){
            System.out.println("result = "+ m);
        }
    }

    /**
     * projection - QueryProjection
     */
    @Test
    public void findByQueryProjection(){
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age)) //생성자에 명시된 매개변수 사용
                .from(member)
                .fetch();

        for(MemberDto m : result){
            System.out.println("result = "+ m);
        }
    }

    /**
     * 동적쿼리
     */
    @Test
    public void dynamicQuery_booleanBuilder(){
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond){
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(usernameCond != null){
            booleanBuilder.and(member.username.eq(usernameCond)); //and로 조건 추가
        }
        if(ageCond != null){
            booleanBuilder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(booleanBuilder) //builder를 필터링 조건으로 사용
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam(){
        String usernameParam = "member2";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond)) //메소드 생성
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        //usernameCond가 Null이 아니면 username = usernameCond
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    //상태 확인 등 동적으로 쿼리 짜기에 용이함
    private BooleanExpression allEq(String usernameCond, Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


}
