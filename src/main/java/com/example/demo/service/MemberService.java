package com.example.demo.service;

import com.example.demo.domain.dto.member.CreateMemberRequest;
import com.example.demo.domain.dto.member.MemberBalanceLogPageResponse;
import com.example.demo.domain.dto.member.MemberBalanceLogResponse;
import com.example.demo.domain.dto.member.MemberLoginRequest;
import com.example.demo.domain.dto.member.MemberResponse;
import com.example.demo.domain.dto.member.UpdateMemberRequest;
import com.example.demo.domain.entity.Member;
import com.example.demo.mapper.MemberMapper;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
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
        member.setPassword(passwordEncoder.encode(request.password()));
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

    public MemberResponse login(MemberLoginRequest request) {
        Member member = memberMapper.findByPhone(request.phone());
        if (member == null || !passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new IllegalArgumentException("member phone or password is invalid");
        }
        if (member.getStatus() == null || member.getStatus() != 1) {
            throw new IllegalArgumentException("member is disabled");
        }
        return toResponse(member);
    }

    public List<MemberResponse> list(String keyword, int pageNum, int pageSize) {
        // 分页参数做“兜底修正”：
        // - pageNum 至少为 1
        // - pageSize 限制在 1~100，防止一次查询过大
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        //查询会员列表，stream().map(this::toResponse)将会员列表转换为MemberResponse列表
        //将每个查出来的member对象调用toResponse方法
        return memberMapper.listMembers(safeKeyword, safePageSize, offset).stream().map(this::toResponse).toList();
    }

    public MemberBalanceLogPageResponse listBalanceLogs(String keyword, String bizType, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String safeBizType = (bizType == null || bizType.isBlank()) ? null : bizType.trim();
        List<MemberBalanceLogResponse> records = memberMapper.listBalanceLogs(safeKeyword, safeBizType, safePageSize, offset);
        long total = memberMapper.countBalanceLogs(safeKeyword, safeBizType);
        return new MemberBalanceLogPageResponse(records, total);
    }

    public MemberResponse getById(Long id) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new IllegalArgumentException("member not found");
        }
        return toResponse(member);
    }

    @Transactional
    public void update(Long id, UpdateMemberRequest request, String operatorName) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new IllegalArgumentException("member not found");
        }
        BigDecimal oldBalance = member.getBalance();
        member.setName(request.name());
        member.setGender(request.gender() == null ? 0 : request.gender());
        member.setLevel(request.level() == null ? member.getLevel() : request.level());
        member.setPoints(request.points() == null ? member.getPoints() : request.points());
        member.setBalance(request.balance() == null ? member.getBalance() : request.balance());
        memberMapper.update(member);
        BigDecimal delta = member.getBalance().subtract(oldBalance);
        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            memberMapper.insertBalanceLog(
                    member.getId(),
                    delta,
                    member.getBalance(),
                    delta.compareTo(BigDecimal.ZERO) > 0 ? "MANUAL_RECHARGE" : "MANUAL_DEDUCT",
                    "manual balance update",
                    operatorName);
        }
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new IllegalArgumentException("member not found");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("status must be 0 or 1");
        }
        memberMapper.updateStatus(id, status);
    }

    @Transactional
    public void applySalesBenefits(Long id, BigDecimal paidAmount, BigDecimal pointsBaseAmount) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new IllegalArgumentException("member not found");
        }
        if (member.getStatus() == null || member.getStatus() != 1) {
            throw new IllegalArgumentException("member is disabled");
        }
        if (member.getBalance().compareTo(paidAmount) < 0) {
            throw new IllegalArgumentException("member balance is not enough");
        }
        int addPoints = pointsBaseAmount.intValue();
        int levelUp = pointsBaseAmount.divideToIntegralValue(BigDecimal.valueOf(1000)).intValue();
        member.setBalance(member.getBalance().subtract(paidAmount));
        member.setPoints(member.getPoints() + addPoints);
        member.setLevel(member.getLevel() + levelUp);
        memberMapper.updateBenefits(member);
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
