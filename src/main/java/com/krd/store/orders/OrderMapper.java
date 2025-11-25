package com.krd.store.orders;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(order.getStatus() != null ? order.getStatus().name() : null)")
    OrderDto toDto(Order order);

}
