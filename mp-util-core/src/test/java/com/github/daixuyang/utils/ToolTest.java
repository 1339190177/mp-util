package com.github.daixuyang.utils;

import com.github.daixuyang.utils.Tool;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ToolTest {
    @Test
    void isSimpleType_ShouldHandleLegacyDateTypes() {
        assertAll(
                () -> assertTrue(Tool.isSimpleType(java.sql.Date.class)),
                () -> assertTrue(Tool.isSimpleType(java.sql.Timestamp.class)),
                () -> assertTrue(Tool.isSimpleType(java.util.Calendar.class))
        );
    }

    // 添加传统集合类型测试
    @Test
    void isSimpleType_ShouldHandleLegacyCollections() {
        assertAll(
                () -> assertFalse(Tool.isSimpleType(java.util.Vector.class)),
                () -> assertFalse(Tool.isSimpleType(java.util.Hashtable.class))
        );
    }

    @Test
    void isSimpleType_ShouldHandleDateTimeTypes() {
        assertAll(
                () -> assertTrue(Tool.isSimpleType(LocalDate.class)),
                () -> assertTrue(Tool.isSimpleType(LocalDateTime.class)),
                () -> assertTrue(Tool.isSimpleType(Date.class))
        );
    }

    @Test
    void humpToLine2_ShouldHandleComplexCases() {
        assertAll(
                () -> assertEquals("user_name", Tool.humpToLine2("UserName")),
                () -> assertEquals("http_response_code", Tool.humpToLine2("HTTPResponseCode")),
                () -> assertEquals("_id", Tool.humpToLine2("Id"))
        );
    }

    @Test
    void isSimpleType_ShouldHandleNullAndInvalidInput() {
        assertAll(
                () -> assertFalse(Tool.isSimpleType(null)),
                () -> assertFalse(Tool.isSimpleType(List.class))
        );
    }
}
