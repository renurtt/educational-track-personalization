package com.nurtdinov.educationaltrackpersonalization.service;

import java.util.HashMap;
import java.util.Map;

public enum SkillLevelEnum {

    BEGINNER(1.0),
    INTERMEDIATE(2.0),
    EXPERT(3.0);

    private final double level;

    SkillLevelEnum(double level) {
        this.level = level;
    }

    private static final Map<Double, SkillLevelEnum> map;

    static {
        map = new HashMap<Double, SkillLevelEnum>();
        for (SkillLevelEnum v : SkillLevelEnum.values()) {
            map.put(v.level, v);
        }
    }

    public static SkillLevelEnum findByLevel(double i) {
        return map.get(i);
    }
}
