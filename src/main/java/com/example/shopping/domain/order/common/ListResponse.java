package com.example.shopping.domain.order.common;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
// 페이지 형태로 구현하기 위한 공통 응답(추후 통합 될수도 있음)
public class ListResponse <T>{
    private List<T> items;
}
