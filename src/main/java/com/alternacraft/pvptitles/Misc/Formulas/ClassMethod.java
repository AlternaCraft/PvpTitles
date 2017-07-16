/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alternacraft.pvptitles.Misc.Formulas;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
/**
 * Class for using methods from another class by name.
 * <i>It works by using java reflection</i>
 * 
 * @author AlternaCraft
 * @param <T> Expression type
 */
public class ClassMethod<T> {

    private final Class clazz;    
    private final String name;
    private final Class<T> type;
    
    private Expression[] values;

    /**
     * Constructor.
     * 
     * @param clazz Class which contains the method.
     * @param args_type Argument/s type
     * @param method_name Method's name
     */
    public ClassMethod(Class clazz, Class<T> args_type, String method_name) {
        this.clazz = clazz;
        this.type = args_type;
        this.name = method_name;
        this.values = new Expression[0];
    }
    
    public boolean validMethod(int... q) {        
        for (int qq : q) {
            if (validMethod(Collections.nCopies(qq, this.type).toArray(new Class[qq]))) {
                return true;
            }
        }
        return false;
    }

    public boolean validMethod(Class<T>[] args) {
        try {
            this.clazz.getMethod(this.name, args);
            return true;
        } catch (NoSuchMethodException | SecurityException ex) {
            return false;
        }
    }    

    public void addArgs(Expression[] vals) {
        values = vals;
    }

    public Object applyAsObject() {
        try {
            Method function = clazz.getMethod(name.toLowerCase(), getArguments());
            return function.invoke(null, parseValues());
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex.getCause());
        }
    }

    private Class<T>[] getArguments() {
        Class[] result = new Class[this.values.length];
        Arrays.fill(result, this.type);
        return result;
    }

    private Object[] parseValues() {
        Object[] result = new Object[this.values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.values[i].eval();
        }
        return result;
    }   
}