package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/7/17 20:40
 */

import javafx.beans.property.SimpleStringProperty;

/**
 * @program: LTPC2020-3-10_version
 *
 * @description:配置参数项
 *
 * @author: WuYe
 *
 * @create: 2020-07-17 20:40
 **/
public class Configuration {
    private final SimpleStringProperty key = new SimpleStringProperty();
    private final SimpleStringProperty value = new SimpleStringProperty();

    public Configuration(String key,String value) {
        setKey(key);
        setValue(value);
    }

    public String getKey() {
        return key.get();
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public String getValue() {
        return value.get();
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}
