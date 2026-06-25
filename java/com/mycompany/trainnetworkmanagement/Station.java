package com.mycompany.trainnetworkmanagement;

import java.util.Objects;

public class Station {
    private String code;
    private String name;

    public Station(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
public boolean equals(Object obj) {
    if (this == obj)
        return true;

    if (obj == null || getClass() != obj.getClass())
        return false;

    Station other = (Station) obj;
    return Objects.equals(this.code, other.code) || Objects.equals(this.name, other.name);
}

@Override
public int hashCode() {
    
    return 1; 
}
    @Override
    public String toString() {
        return code + " - " + name;
    }
}