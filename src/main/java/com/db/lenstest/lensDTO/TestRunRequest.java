package com.db.lenstest.lensDTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestRunRequest {
    private List<String> includeTags;
    private List<String> excludeTags;

    public String fetchTestExpression(){
        String includeExpression = String.join(" or ", includeTags);
        String excludeExpression = String.join(" or ", excludeTags);

        String finalTagExpression = "";

        if (!includeExpression.isEmpty() && !excludeExpression.isEmpty()) {
            finalTagExpression = includeExpression + " and not (" + excludeExpression + ")";
        } else if (!includeExpression.isEmpty()) {
            finalTagExpression = includeExpression;
        } else if (!excludeExpression.isEmpty()) {
            finalTagExpression = "not (" + excludeExpression + ")";
        }

        return finalTagExpression;
    }
}
