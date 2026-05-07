package com.example.gamifikasi.entity;

/**
 * Enum yang menentukan level/rank siswa berdasarkan total bintang yang dikumpulkan.
 *
 * Threshold bintang:
 *  BEGINNER  :  0 – 9   bintang
 *  BRONZE    : 10 – 24  bintang
 *  SILVER    : 25 – 49  bintang
 *  GOLD      : 50 – 79  bintang
 *  PLATINUM  : 80 – 119 bintang
 *  DIAMOND   : 120+     bintang
 */
public enum RankLevel {

    BEGINNER(0, 9),
    BRONZE(10, 24),
    SILVER(25, 49),
    GOLD(50, 79),
    PLATINUM(80, 119),
    DIAMOND(120, Integer.MAX_VALUE);

    private final int minStars;
    private final int maxStars;

    RankLevel(int minStars, int maxStars) {
        this.minStars = minStars;
        this.maxStars = maxStars;
    }

    public int getMinStars() {
        return minStars;
    }

    public int getMaxStars() {
        return maxStars;
    }

    /**
     * Menentukan RankLevel berdasarkan jumlah total bintang.
     *
     * @param totalStars total bintang siswa
     * @return RankLevel yang sesuai
     */
    public static RankLevel fromTotalStars(int totalStars) {
        for (RankLevel level : values()) {
            if (totalStars >= level.minStars && totalStars <= level.maxStars) {
                return level;
            }
        }
        return BEGINNER;
    }
}
