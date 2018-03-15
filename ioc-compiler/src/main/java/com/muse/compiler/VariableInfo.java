package com.muse.compiler;

import javax.lang.model.element.VariableElement;

/**
 * Created by GuoWee on 2018/3/14.
 */

public class VariableInfo {
    // the Id value of the annotated View
    int viewId;
    // information that is annotated View: variable name, type
    VariableElement variableElement;


    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public VariableElement getVariableElement() {
        return variableElement;
    }

    public void setVariableElement(VariableElement variableElement) {
        this.variableElement = variableElement;
    }
}
