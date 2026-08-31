package com.first.arcana.spell;

/**
 * 시전 방식.
 * INSTANT     — 누르는 즉시 발동
 * CHARGE      — 꾹 눌러 모았다가 놓을 때 발동
 * CONTINUOUS  — 누르고 있는 동안 매 틱 발동 (마나도 매 틱 소모)
 */
public enum CastType {
    INSTANT,
    CHARGE,
    CONTINUOUS
}
