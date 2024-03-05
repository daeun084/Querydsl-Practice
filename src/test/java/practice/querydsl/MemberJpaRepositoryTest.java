package practice.querydsl;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import practice.querydsl.entity.Member;
import practice.querydsl.repository.MemberJpaRepository;

import java.util.List;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest(){
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        //find Member
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUserName("member1");
        Assertions.assertThat(result1).containsExactly(member);
    }

    @Test
    public void basicQuerydslTest(){
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        //find Member
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUserName_Querydsl("member1");
        Assertions.assertThat(result1).containsExactly(member);
    }
}
