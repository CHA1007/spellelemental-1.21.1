package com.chadate.spellelemental.event.element;

public class ReactionInjuryFormula {
    public static float ElectroReactiveteDamage(float originalDamage, float reactivityMultiplier, float astralBlessing) {
        return originalDamage * reactivityMultiplier * (1 + (5 * astralBlessing) / (astralBlessing + 1200));
    }

    public static float CalculateOverloadDamage(float elementsAmount, float reactionMultiplier, float astralBlessing) {
        return elementsAmount * 0.1f * reactionMultiplier * (1 + (16 * astralBlessing) / (astralBlessing + 2000));
    }

    public static float AmplifiedReactionBonus(float astralBlessing) {
        return (float) (1 + ((2.78 * astralBlessing) / (astralBlessing + 1400)));
    }
}
