package com.washer.Things.global.util;
import com.washer.Things.domain.user.entity.enums.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringListConverter implements AttributeConverter<List<Role>, String> {

    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<Role> attribute) {
        return attribute != null ? attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(SPLIT_CHAR)) : "";
    }

    @Override
    public List<Role> convertToEntityAttribute(String dbData) {
        return dbData != null && !dbData.isEmpty() ? Arrays.stream(dbData.split(SPLIT_CHAR))
                .map(Role::valueOf)
                .collect(Collectors.toList()) : List.of();
    }
}