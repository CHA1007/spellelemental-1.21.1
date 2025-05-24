package com.chadate.spellelemental.event.element;

public class ReactionInjuryFormula {
    public static float ElectroReactiveteDamage(float originalDamage, float reactivityMultiplier, float astralBlessing) {
        return originalDamage * reactivityMultiplier * (1 + (5 * astralBlessing) / (astralBlessing + 1200));
    }

    public static float CalculateOverloadDamage(float attackDamage, float reactionMultiplier, float astralBlessing) {
        return attackDamage * reactionMultiplier * (1 + (16 * astralBlessing) / (astralBlessing + 2000));
    }

    public static float CalculateBlessingBonus(float astralBlessing) {
        return (float) (1 + ((2.78 * astralBlessing) / (astralBlessing + 1400)));
    }
}
