package dev.rm20.anglersalmanac.MinigameManager;


import com.hypixel.hytale.codec.builder.BuilderCodec;


public class RodStats{

    float control;
    float difficulty;
    float forgiveness;
    float stamina;
    float sizeMul;
    float rarityMul;


    public RodStats(){
    }
    public RodStats(float control, float difficulty, float forgiveness, float stamina, float sizeMul, float rarityMul){
        this.control = control;
        this.difficulty = difficulty;
        this.forgiveness = forgiveness;
        this.stamina = stamina;
        this.sizeMul = sizeMul;
        this.rarityMul = rarityMul;
    }


    public static final BuilderCodec<RodStats> CODEC = BuilderCodec.builder(RodStats.class, RodStats::new

    ).build();

}
