package com.ceramic.compiler.compiler;

public enum DynamicCompilerOption {
    ADD_EXPORTS("--add-exports"),
    ADD_OPENS("--add-opens");

    private String option;

    DynamicCompilerOption(String option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return option;
    }
}