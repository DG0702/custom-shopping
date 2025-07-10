package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.shopping.domain.order.entity.QOrder.order;
import static com.example.shopping.domain.user.entity.QUser.user;


@Repository
@RequiredArgsConstructor
// QueryDSL 사용할 Repository
public class QueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<OrderResponseDto> getOrders(AuthUser authUser, Pageable pageable) {
        // 1. 전제 개수 조회
        Long total = queryFactory.select(order.count())
                .from(order)
                .leftJoin(order.user, user)
                .where(
                        order.user.id.eq(authUser.getId())
                )
                .fetchOne();

        // null 체크
        long totalCount = total != null ? total : 0L;

        // 2. 데이터 조회
        List<OrderResponseDto> purchaseList = queryFactory.select(Projections.constructor(
                        OrderResponseDto.class,
                        order.id,
                        order.status,
                        order.totalPrice
                ))
                .from(order)
                .leftJoin(order.user, user)
                .where(
                        order.user.id.eq(authUser.getId())
                )
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3. PageImpl 생성 및 반환
        return new PageImpl<>(purchaseList,pageable,totalCount);
    }
}
