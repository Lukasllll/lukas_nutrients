package net.lukasllll.lukas_nutrients.config;

public interface ConfigValidator<T> {
    boolean validate(T config);
}
