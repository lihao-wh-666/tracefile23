package com.exam.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionImportVO {

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private List<ImportError> errors;

    public QuestionImportVO() {
        this.totalCount = 0;
        this.successCount = 0;
        this.failCount = 0;
        this.errors = new ArrayList<>();
    }

    public void addError(Integer rowNum, String message) {
        this.errors.add(new ImportError(rowNum, message));
    }

    @Data
    public static class ImportError {
        private Integer rowNum;
        private String message;

        public ImportError(Integer rowNum, String message) {
            this.rowNum = rowNum;
            this.message = message;
        }
    }
}
