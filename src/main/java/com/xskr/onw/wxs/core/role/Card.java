package com.xskr.onw.wxs.core.role;

import java.util.Objects;

public abstract class Card {

    public static final int ROLE_COUNT = 16;

    protected int id = 0;

    public abstract String getDisplayName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card role = (Card) o;
        return id == role.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
