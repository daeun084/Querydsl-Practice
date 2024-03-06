package practice.querydsl.repository;

import practice.querydsl.dto.MemberSearchCondition;
import practice.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
