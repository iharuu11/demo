package com.example.demo.service;

import com.example.demo.domain.dto.member.CreateMemberRequest;
import com.example.demo.domain.dto.member.MemberResponse;
import com.example.demo.domain.entity.Member;
import com.example.demo.mapper.MemberMapper;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
    private final MemberMapper memberMapper;

    public MemberService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Transactional
    public MemberResponse create(CreateMemberRequest request) {
        // 创建会员（写数据库，所以加事务）：
        // - 手机号是会员的唯一标识之一（不允许重复）
        // - level/points/balance/status 这类字段在创建时给默认值，避免出现 null
        Member existed = memberMapper.findByPhone(request.phone());
        if (existed != null) {
            throw new IllegalArgumentException("member phone already exists");
        }
        Member member = new Member();
        member.setPhone(request.phone());
        member.setName(request.name());
        // gender: 0/1/2...（这里用 0 表示未知/默认）
        member.setGender(request.gender() == null ? 0 : request.gender());
        member.setLevel(1);
        member.setPoints(0);
        member.setBalance(BigDecimal.ZERO);
        // status: 1 启用，0/其它值可视为禁用（具体规则由业务决定）
        member.setStatus(1);
        memberMapper.insert(member);
        // insert 后再查一次，拿到数据库生成的字段（例如 id、createdAt）
        return toResponse(memberMapper.findByPhone(request.phone()));
    }

    public List<MemberResponse> list(int pageNum, int pageSize) {
        // 分页参数做“兜底修正”：
        // - pageNum 至少为 1
        // - pageSize 限制在 1~100，防止一次查询过大
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        //查询会员列表，stream().map(this::toResponse)将会员列表转换为MemberResponse列表
        //将每个查出来的member对象调用toResponse方法
        return memberMapper.listMembers(safePageSize, offset).stream().map(this::toResponse).toList();
    }

    private MemberResponse toResponse(Member member) {
        // Entity -> DTO：
        // 对外接口不要直接暴露数据库实体对象，避免字段随意外泄/随意变更导致兼容问题
        return new MemberResponse(
                member.getId(),
                member.getPhone(),
                member.getName(),
                member.getGender(),
                member.getLevel(),
                member.getPoints(),
                member.getBalance(),
                member.getStatus(),
                member.getCreatedAt());
    }
}
