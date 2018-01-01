package com.keyboard3.apt;

import java.util.List;

/**
 * Created by lizhaoxuan on 16/5/27.
 */
public class MobclickMethod {

    private int id;
    private String type;
    private String methodName;
    private List<String> methodParameters;

    MobclickMethod(int id, String type, String methodName, List<String> methodParameters) {
        this.id = id;
        this.type = type;
        this.methodName = methodName;
        this.methodParameters = methodParameters;
    }

    public int getMethodParametersSize() {
        return methodParameters == null ? 0 : methodParameters.size();
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getMethodParameters() {
        return methodParameters;
    }

}
